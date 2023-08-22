package com.example;

import com.example.request.PurchaseRequest;
import com.example.response.BaseResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface PurchaseService {
    ResponseEntity<BaseResponse<String>> makePurchase(PurchaseRequest purchaseRequest);

    BaseResponse<?> deductStock(List<PurchaseRequest> productRequest);

}
