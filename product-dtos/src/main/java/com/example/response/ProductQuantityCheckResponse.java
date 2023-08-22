package com.example.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductQuantityCheckResponse {
    private boolean isAvailable;
    private String message;
    private ProductResponse productResponse;
}
