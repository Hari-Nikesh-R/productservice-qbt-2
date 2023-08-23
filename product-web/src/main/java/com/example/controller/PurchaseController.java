package com.example.controller;

import com.example.PurchaseService;
import com.example.helper.Urls;
import com.example.request.PurchaseRequest;
import com.example.response.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = Urls.PURCHASE)
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @PutMapping(value = Urls.DEDUCT_STOCK)
    public BaseResponse<?> deductStockAfterPurchase(@RequestBody List<PurchaseRequest> productRequest) {
        return purchaseService.deductStock(productRequest);
    }
}
