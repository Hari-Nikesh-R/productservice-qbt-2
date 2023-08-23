package com.example;

import com.example.request.PurchaseRequest;
import com.example.response.BaseResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PurchaseService {
    BaseResponse<?> deductStock(List<PurchaseRequest> productRequest);

}
