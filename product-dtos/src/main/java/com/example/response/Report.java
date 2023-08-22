package com.example.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Report {
    private String productName;
    private Double totalPrice;
    private Integer quantity;
}
