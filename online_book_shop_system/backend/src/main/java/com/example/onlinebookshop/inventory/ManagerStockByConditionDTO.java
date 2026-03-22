package com.example.onlinebookshop.inventory;

import lombok.Data;

@Data
public class ManagerStockByConditionDTO {
    private String condition;
    private Integer totalAvailable;
}

