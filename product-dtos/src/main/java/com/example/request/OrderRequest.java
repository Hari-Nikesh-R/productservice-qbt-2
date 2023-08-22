package com.example.request;

import com.example.response.ProductResponse;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private CustomerDetailRequest orderedCustomerDetail;
    private List<ProductResponse> availableProduct;
    private double totalOrder;
}
