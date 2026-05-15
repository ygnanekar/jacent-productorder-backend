package com.jacent.storefront.service.impl;

import com.jacent.storefront.dto.helper.ItemWithStoreIds;
import com.jacent.storefront.entity.Item;
import com.jacent.storefront.service.OpenSearchService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.HealthStatus;
import org.opensearch.client.opensearch._types.query_dsl.TextQueryType;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OpenSearchServiceImpl implements OpenSearchService {

    private final OpenSearchClient openSearchClient;

    private static final String INDEX_NAME = "items";

    OpenSearchServiceImpl(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    @Override
    public boolean isOpenSearchHealthy() {
        try {
            HealthStatus status = openSearchClient.cluster().health().status();
            return status == HealthStatus.Green || status == HealthStatus.Yellow;
        } catch (Exception e) {
            log.error("OpenSearch health check failed", e);
            return false;
        }
    }

    @PostConstruct
    public void load() {
        try {
            createIndexIfNotExists();
        } catch (IOException e) {
            log.error("Error occured while create opensearch index. Exception: {}", e);
        }

    }

    // Create index if not exists
    public void createIndexIfNotExists() throws IOException {
        boolean exists = openSearchClient.indices()
                .exists(e -> e.index(INDEX_NAME))
                .value();

        if (!exists) {
            createIndex();
        }
    }

    private void createIndex() throws IOException {
        openSearchClient.indices().create(c -> c.index(INDEX_NAME));
        System.out.println("Index created: " + INDEX_NAME);
    }

    // Delete index
    private void deleteIndex() throws IOException {
        boolean exists = openSearchClient.indices()
                .exists(e -> e.index(INDEX_NAME)).value();
        if (exists) {
            openSearchClient.indices().delete(d -> d.index(INDEX_NAME));
            log.info("Index deleted: {}", INDEX_NAME);
        }
    }

    // Delete and recreate (wipes all data and starts fresh)
    public void reCreateIndex() throws IOException {
        deleteIndex();
        createIndex();
        log.info("Index recreated: {}", INDEX_NAME);
    }

    // Get document count
    public long getDocumentCount() throws IOException {
        return openSearchClient.count(c -> c.index(INDEX_NAME)).count();
    }

    // Index a single item
    public void indexProduct(Item item) throws IOException {
        IndexResponse response = openSearchClient.index(i -> i
                .index(INDEX_NAME)
                .id(String.valueOf(item.getItemId()))
                .document(item)
        );
        System.out.println("Indexed item: " + item.getItemName()
                + " | Result: " + response.result());
    }

    // Bulk load multiple items (efficient for large datasets)
    @Override
    public void bulkIndexProducts(List<ItemWithStoreIds> items) throws IOException {
        BulkRequest.Builder bulkRequest = new BulkRequest.Builder();

        for (Item item : items) {
            bulkRequest.operations(op -> op
                    .index(i -> i
                            .index(INDEX_NAME)
                            .id(String.valueOf(item.getItemId()))
                            .document(item)
                    )
            );
        }

        BulkResponse response = openSearchClient.bulk(bulkRequest.build());

        if (response.errors()) {
            response.items().stream()
                    .filter(item -> item.error() != null)
                    .forEach(item -> System.err.println(
                            "Failed to index: " + item.id() + " | " + item.error().reason()
                    ));
        } else {
            System.out.println("Bulk indexed " + items.size() + " items successfully.");
        }
    }

    @Override
    public List<ItemWithStoreIds> searchItems(String searchString) throws IOException {

        SearchResponse<ItemWithStoreIds> response = openSearchClient.search(s -> s
                        .index(INDEX_NAME)
                        .query(q -> q
                                .bool(b -> b

                                        // 1. MULTI-MATCH — best for full word matches across fields
                                        // "Cookware" matches "Cookware"
                                        .should(s1 -> s1.multiMatch(m -> m
                                                .fields(List.of(
                                                        "itemName^4",    // boost name highest
                                                        "itemDesc^2",    // desc medium
                                                        "upcCode^3",     // upc high
                                                        "commodity^1",
                                                        "division^1"
                                                ))
                                                .query(searchString)
                                                .type(TextQueryType.BestFields)
                                                .fuzziness("AUTO")       // handles typos
                                        ))

                                        // 2. MATCH PHRASE PREFIX — best for partial/prefix matches
                                        // "Cook" matches "Cookware", "Cooking"
                                        .should(s2 -> s2.multiMatch(m -> m
                                                .fields(List.of(
                                                        "itemName^4",
                                                        "itemDesc^2",
                                                        "upcCode^3",
                                                        "commodity^1",
                                                        "division^1"
                                                ))
                                                .query(searchString)
                                                .type(TextQueryType.PhrasePrefix)  // prefix matching
                                        ))

                                        // 3. WILDCARD on keyword — catches remaining edge cases
                                        // "*cook*" matches anything containing "cook"
                                        .should(s3 -> s3.wildcard(w -> w
                                                .field("itemName.keyword")
                                                .value("*" + searchString.toLowerCase() + "*")
                                                .caseInsensitive(true)
                                        ))
                                        .should(s4 -> s4.wildcard(w -> w
                                                .field("upcCode.keyword")
                                                .value("*" + searchString.toLowerCase() + "*")
                                                .caseInsensitive(true)
                                        ))
                                )
                        )
                        .size(50),   // limit results
                ItemWithStoreIds.class
        );

        return response.hits().hits()
                .stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }
}
