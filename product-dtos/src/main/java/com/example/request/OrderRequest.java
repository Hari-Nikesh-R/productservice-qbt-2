package com.example.request;

import com.example.response.ProductResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private CustomerDetailRequest orderedCustomerDetail;
    private List<ProductResponse> availableProduct;
    private double totalOrder;
    @Email(message = "Invalid email")
    @NotNull(message = "Email cannot be null")
    private String email;
}
