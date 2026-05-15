package com.jacent.storefront.service;

import com.jacent.storefront.dto.helper.ItemWithStoreIds;

import java.io.IOException;
import java.util.List;

public interface OpenSearchService {

    boolean isOpenSearchHealthy();

    void bulkIndexProducts(List<ItemWithStoreIds> items) throws IOException;

    List<ItemWithStoreIds> searchItems(String searchString) throws IOException;
}
