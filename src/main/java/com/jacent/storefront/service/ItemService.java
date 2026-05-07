package com.jacent.storefront.service;

import com.jacent.storefront.dto.response.ItemsResponse;
import com.jacent.storefront.entity.Commodity;
import com.jacent.storefront.entity.Division;
import com.jacent.storefront.entity.Item;

import java.io.IOException;
import java.util.List;


public interface ItemService {
    List<Division> getAllDivisions();

    List<Commodity> getAllCommodities();

    ItemsResponse getItems(Integer pageNo, Integer pageSize);

    List<Item> searchItems(String searchString) throws IOException;

    void rebuildOpenSearchIndexForItems();
}
