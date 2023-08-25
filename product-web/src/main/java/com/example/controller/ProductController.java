package com.example.controller;


import com.example.ProductService;
import com.example.helper.Urls;
import com.example.request.ProductRequest;
import com.example.request.PurchaseRequest;
import com.example.request.StockRequest;
import com.example.response.BaseResponse;
import com.example.response.ProductQuantityCheckResponse;
import com.example.response.ProductResponse;
import com.example.response.Report;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = Urls.PRODUCT)
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping(value = Urls.GENERATE_REPORT)
    public ResponseEntity<BaseResponse<Map<String, List<Report>>>> generateReport() {
        return productService.generateReport();
    }

    @PostMapping
    public ResponseEntity<BaseResponse<String>> addProduct(@RequestBody @Valid ProductRequest product) throws IOException {
        return productService.addOrUpdateProduct(product);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<ProductResponse>> getProduct(@RequestParam("productName") String productName) {
        return productService.getProduct(productName);
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<String>> deleteProduct(@RequestParam("productName") String productName) {
        return productService.deleteProduct(productName);
    }

    @DeleteMapping(value = Urls.ALL_PRODUCT)
    public ResponseEntity<BaseResponse<String>> deleteAllProduct() {
        return productService.deleteAllProduct();
    }

    @GetMapping(value = Urls.ALL_PRODUCT)
    public BaseResponse<List<ProductResponse>> getAllProducts() {
        return productService.getAllProductsFromInventory();
    }

    @PostMapping(value = Urls.QUANTITY)
    public ProductQuantityCheckResponse[] quantityCheck(@RequestBody List<PurchaseRequest> productRequest) {
        return productService.checkStock(productRequest);
    }

}
