package com.example;

import com.example.document.Product;
import com.example.request.ProductRequest;
import com.example.request.PurchaseRequest;
import com.example.response.BaseResponse;
import com.example.response.ProductQuantityCheckResponse;
import com.example.response.ProductResponse;
import com.example.response.Report;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;


@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private ProductRepository productRepository;

    @Override
    public ResponseEntity<BaseResponse<String>> addOrUpdateProduct(ProductRequest productRequest) throws IOException {
        try {
            Optional<Product> optionalProduct = getProductByName(productRequest.getProductName());
            return optionalProduct.map(product -> updateProduct(productRequest)).orElseGet(() -> {
                Product product = new Product();
                BeanUtils.copyProperties(productRequest, product);
                productRepository.save(product);
                log.info("Product saved in database");
                return ResponseEntity.ok(new BaseResponse<>("Product added successfully", null, true, HttpStatus.OK.value()));
            });
        } catch (Exception exception) {
            log.error("thrown an exception: " + exception.getMessage());
            return ResponseEntity.ok(new BaseResponse<>(null, exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    private ResponseEntity<BaseResponse<String>> updateProduct(ProductRequest productRequest) {
        try {
            Optional<Product> optionalProduct = getProductByName(productRequest.getProductName());
            return optionalProduct.map((product -> {
                product.setProductQuantity(product.getProductQuantity() + productRequest.getProductQuantity());
                if (Objects.nonNull(productRequest.getProductPrice())) {
                    product.setProductPrice(productRequest.getProductPrice());
                }
                product.setProductName(productRequest.getProductName());
                productRepository.save(product);
                return ResponseEntity.ok(new BaseResponse<>("Product Updated", null, true, HttpStatus.OK.value()));
            })).orElseGet(() -> ResponseEntity.ok(new BaseResponse<>(null, "Product not updated", false, HttpStatus.NO_CONTENT.value())));
        } catch (Exception exception) {
            return ResponseEntity.ok(new BaseResponse<>(null, exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Override
    public ResponseEntity<BaseResponse<ProductResponse>> getProduct(String productName) {
        try {
            Optional<Product> optionalProduct = getProductByName(productName);
            return optionalProduct.map(product -> {
                ProductResponse productResponse = new ProductResponse();
                BeanUtils.copyProperties(optionalProduct.get(), productResponse);
                return ResponseEntity.ok(new BaseResponse<>(productResponse, null, true, HttpStatus.OK.value()));
            }).orElseGet(() -> ResponseEntity.ok(new BaseResponse<>(null, "Product Not found", true, HttpStatus.NO_CONTENT.value())));
        } catch (Exception exception) {
            return ResponseEntity.ok(new BaseResponse<>(null, exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Override
    public ResponseEntity<BaseResponse<Map<String, List<Report>>>> generateReport() {
        try {
            double totalValue = 0;
            Map<String, List<Report>> doubleListMap = new HashMap<>();
            List<Report> reports = new Vector<>();
            List<Product> productList = productRepository.findAll();
            for (Product product : productList) {
                totalValue += product.getProductPrice() * product.getProductQuantity();
                reports.add(new Report(product.getProductName(), product.getProductPrice(), product.getProductQuantity()));
            }
            doubleListMap.put("Total value of inventory: " + totalValue, reports);
            return ResponseEntity.ok(new BaseResponse<>(doubleListMap, null, true, HttpStatus.OK.value()));
        } catch (Exception exception) {
            return ResponseEntity.ok(new BaseResponse<>(new HashMap<>(), exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Override
    public ResponseEntity<BaseResponse<String>> deleteProduct(String productName) {
        try {
            Optional<Product> optionalProduct = getProductByName(productName);
            if (optionalProduct.isPresent()) {
                productRepository.delete(optionalProduct.get());
                return ResponseEntity.ok(new BaseResponse<>("Deleted successfully", null, true, HttpStatus.OK.value()));
            } else {
                return ResponseEntity.ok(new BaseResponse<>(null, "No Product found", false, HttpStatus.NO_CONTENT.value()));
            }
        } catch (Exception exception) {
            return ResponseEntity.ok(new BaseResponse<>(null, exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Override
    public ResponseEntity<BaseResponse<String>> deleteAllProduct() {
        try {
            productRepository.deleteAll();
            return ResponseEntity.ok(new BaseResponse<>("Deleted successfully", null, true, HttpStatus.OK.value()));
        } catch (Exception exception) {
            return ResponseEntity.ok(new BaseResponse<>(null, exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Override
    public BaseResponse<List<ProductResponse>> getAllProductsFromInventory() {
        try {
            List<ProductResponse> productResponses = mapper.convertValue(productRepository.findAll(), new TypeReference<>() {
            });
            return new BaseResponse<>(productResponses, null, true, HttpStatus.OK.value());
        } catch (Exception exception) {
            return new BaseResponse<>(null, exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public synchronized ProductQuantityCheckResponse[] checkStock(List<PurchaseRequest> productRequest) {
        List<ProductQuantityCheckResponse> productQuantityCheckResponses = new ArrayList<>();
        try {
            List<Product> productList = productRepository.findAll();
            productRequest.forEach(product -> {
                productQuantityCheckResponses.add(getProductByQuantity(productList, product));
            });
        } catch (Exception exception) {
            exception.printStackTrace();
            productQuantityCheckResponses.add(new ProductQuantityCheckResponse(false, exception.getMessage(), null));
        }
        return productQuantityCheckResponses.toArray(ProductQuantityCheckResponse[]::new);
    }

    private ProductQuantityCheckResponse getProductByQuantity(List<Product> productList, PurchaseRequest purchaseRequest) {
        Optional<Product> product = productList.stream().filter(prod -> prod.getProductName(
        ).equalsIgnoreCase(purchaseRequest.getProductName())).findFirst();
        if (product.isPresent()) {
            if (!(product.get().getProductQuantity() >= purchaseRequest.getQuantity())) {
                return new ProductQuantityCheckResponse(false, product.get().getProductName() + " out of stock", null);
            } else {
                ProductResponse productResponse = new ProductResponse();
                BeanUtils.copyProperties(product.get(), productResponse);
                return new ProductQuantityCheckResponse(true, "Product available", productResponse);
            }
        } else {
            return new ProductQuantityCheckResponse(false, "No product found", null);
        }
    }


    private synchronized Optional<Product> getProductByName(String productName) {
        return productRepository.findByProductNameIgnoreCase(productName);
    }
}
