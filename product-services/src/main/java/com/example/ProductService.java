package com.example;

import com.example.request.ProductRequest;
import com.example.request.PurchaseRequest;
import com.example.request.StockRequest;
import com.example.response.BaseResponse;
import com.example.response.ProductQuantityCheckResponse;
import com.example.response.ProductResponse;
import com.example.response.Report;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProductService {

    ResponseEntity<BaseResponse<String>> addOrUpdateProduct(ProductRequest product) throws IOException;

    ResponseEntity<BaseResponse<ProductResponse>> getProduct(String productName);

    ResponseEntity<BaseResponse<Map<String, List<Report>>>> generateReport();

    ResponseEntity<BaseResponse<String>> deleteProduct(String productName);
    ResponseEntity<BaseResponse<String>> deleteAllProduct();
    BaseResponse<List<ProductResponse>> getAllProductsFromInventory();
    ProductQuantityCheckResponse[] checkStock(List<PurchaseRequest> productRequest);
}
