package com.jacent.storefront.dto.helper;

import com.jacent.storefront.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemWithStoreIds extends Item {
    private List<Integer> storeIds;
}
