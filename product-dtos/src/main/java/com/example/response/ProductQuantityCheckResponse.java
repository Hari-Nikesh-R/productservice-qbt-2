package com.example.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductQuantityCheckResponse {
    private boolean isAvailable;
    private String message;
    private ProductResponse productResponse;
}
