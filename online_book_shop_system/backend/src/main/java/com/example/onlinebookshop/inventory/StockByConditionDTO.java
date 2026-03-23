package com.example.onlinebookshop.inventory;

import lombok.Data;

@Data
public class StockByConditionDTO {
    private String condition;
    private Integer totalAvailable;
}

