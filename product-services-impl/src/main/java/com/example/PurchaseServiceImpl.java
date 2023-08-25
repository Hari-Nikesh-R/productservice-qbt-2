package com.example;

import com.example.document.Product;
import com.example.request.PurchaseRequest;
import com.example.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private RestTemplate restTemplate;


    @Override
    public BaseResponse<?> deductStock(List<PurchaseRequest> productRequest) {
        try {
            List<String> quantityDeductionMessage = new ArrayList<>();
            for (PurchaseRequest product : productRequest) {
                Optional<Product> optionalProduct = productRepository.findByProductNameIgnoreCase(product.getProductName());
                if (optionalProduct.isPresent()) {
                    Product prod = optionalProduct.get();
                    if (prod.getProductQuantity() >= product.getQuantity()) {
                        prod.setProductQuantity(prod.getProductQuantity() - product.getQuantity());
                        productRepository.save(prod);
                        quantityDeductionMessage.add(product.getProductName() + " - Quantity deducted");
                    } else {
                        quantityDeductionMessage.add(product.getProductName() + " cannot be deducted, Trying to purchase more than stock");
                        log.warn("Out of stock");
                    }
                }
            }
            return new BaseResponse<>(quantityDeductionMessage, null, true, HttpStatus.OK.value());
        } catch (Exception exception) {
            exception.printStackTrace();
            return new BaseResponse<>(null, exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
