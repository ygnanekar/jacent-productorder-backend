package com.jacent.storefront.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private Integer itemId;
    private String itemExtId;
    private String itemName;
    private String itemDesc;
    private String commodity;
    private String division;
    private String divisionId;
    private String divName;
    private String upcCode;
    private String upc;
    private String saleUnit;
    private BigDecimal price;
    private BigDecimal retailPrice;
    private String mvid;
    private String rank;
    private String orderRank;
    private Integer storeId;
}
