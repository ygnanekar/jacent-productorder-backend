package com.jacent.storefront.service.impl;

import com.jacent.storefront.dto.response.ItemsResponse;
import com.jacent.storefront.entity.*;
import com.jacent.storefront.repository.CommodityRepository;
import com.jacent.storefront.repository.DivisionRepository;
import com.jacent.storefront.repository.ItemRepository;
import com.jacent.storefront.service.ConfigurationService;
import com.jacent.storefront.service.ItemService;
import com.jacent.storefront.service.OpenSearchService;
import com.jacent.storefront.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final ConfigurationService configurationService;
    private final ItemRepository itemRepository;
    private final DivisionRepository divisionRepository;
    private final CommodityRepository commodityRepository;
    private final OpenSearchService openSearchService;

    ItemServiceImpl(ItemRepository itemRepository, ConfigurationService configurationService, DivisionRepository divisionRepository, CommodityRepository commodityRepository , OpenSearchService openSearchService) {
        this.itemRepository = itemRepository;
        this.configurationService = configurationService;
        this.divisionRepository = divisionRepository;
        this.commodityRepository = commodityRepository;
        this.openSearchService = openSearchService;
    }

    @Override
    public List<Division> getAllDivisions() {
        return divisionRepository.findAll();
    }

    @Override
    public List<Commodity> getAllCommodities() {
        return commodityRepository.findAll();
    }

    @Override
    public ItemsResponse getItems(Integer pageNo, Integer pageSize) {
        if(pageSize == null){
            pageSize = configurationService.getValueAsInteger(Configuration.PAGINATION_SIZE, 25);
        }
        User user = SecurityUtils.getCurrentUser();
        long total = itemRepository.getTotalItemsCount(user.getStoreId());

        List<Item> itemList = itemRepository.getAllItemsPagination(pageNo, pageSize, user.getStoreId());
        return ItemsResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .content(itemList)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / pageSize))
                .build();
    }

    @Override
    public List<Item> searchItems(String searchString) throws IOException {
        boolean enableFullTextOpenSearch = configurationService.getValueAsBoolean(Configuration.ENABLE_FULL_TEXT_OPEN_SEARCH, false);
        if(enableFullTextOpenSearch){
            return openSearchService.searchItems(searchString);
        } else {
            // TODO: Search from DB
            return new ArrayList<>();
        }
    }

    @Override
    public void rebuildOpenSearchIndexForItems() {
        try {
            // TODO: add in bulk (maybe create separate index for store)
            User user = SecurityUtils.getCurrentUser();
            List<Item> itemList = itemRepository.getAllItemsPagination(0, 1000, user.getStoreId());
            openSearchService.bulkIndexProducts(itemList);
        } catch (Exception e){
            log.error("Error occured while bulkIndexProducts");
        }
    }
}
