package com.example;

import com.example.document.Product;
import com.example.request.ProductRequest;
import com.example.request.PurchaseRequest;
import com.example.request.StockRequest;
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
    public ResponseEntity<BaseResponse<String>> addProduct(ProductRequest productRequest) throws IOException {
        try {
            Optional<Product> optionalProduct = productRepository.findByProductName(productRequest.getProductName());
            return optionalProduct.map(product -> ResponseEntity.ok(new BaseResponse<>("Product already present", null, false, HttpStatus.ALREADY_REPORTED.value()))).orElseGet(() -> {
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

    @Override
    public ResponseEntity<BaseResponse<String>> updateProduct(ProductRequest productRequest) {
        try {
            Optional<Product> optionalProduct = productRepository.findByProductName(productRequest.getProductName());
            return optionalProduct.map((product -> {
                Integer id = product.getProductId();
                BeanUtils.copyProperties(productRequest, product);
                product.setProductId(id);
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
            Optional<Product> optionalProduct = productRepository.findByProductName(productName);
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
            System.out.println("Id\t\t\tProduct Name\tPrice\t\tQuantity");
            for (Product product : productList) {
                totalValue += product.getProductPrice() * product.getProductQuantity();
                System.out.printf(
                        "%s\t\t%s\t\t%.4f\t\t%d%n",
                        product.getProductId(), product.getProductName(),
                        product.getProductPrice(), product.getProductQuantity());
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
            Optional<Product> optionalProduct = productRepository.findByProductName(productName);
            if (optionalProduct.isPresent()) {
                productRepository.delete(optionalProduct.get());
                return ResponseEntity.ok(new BaseResponse<>("Deleted successfully", null, true, HttpStatus.OK.value()));
            } else {
                return ResponseEntity.ok(new BaseResponse<>(null, "Product not deleted", false, HttpStatus.NO_CONTENT.value()));
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
    public BaseResponse<String> updateStock(StockRequest stockRequest) {
        try {
            Optional<Product> optionalProduct = productRepository.findByProductName(stockRequest.getProductName());
            return optionalProduct.map((product -> {
                product.setProductQuantity(stockRequest.getQuantity());
                productRepository.save(product);
                return new BaseResponse<>("Product Updated", null, true, HttpStatus.OK.value());
            })).orElseGet(() -> new BaseResponse<>(null, "No product found to update", false, HttpStatus.NO_CONTENT.value()));
        } catch (Exception exception) {
            return new BaseResponse<>(null, exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value());
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
    public synchronized ProductQuantityCheckResponse checkStock(PurchaseRequest productRequest) {
        try {
            Optional<Product> optionalProduct = productRepository.findByProductName(productRequest.getProductName());
            return optionalProduct.map(product -> {
                        boolean available = productRequest.getQuantity() <= product.getProductQuantity();
                        ProductResponse productResponse = new ProductResponse();
                        BeanUtils.copyProperties(product, productResponse);
                        return new ProductQuantityCheckResponse(available, available ? "Quantity available" : "Product out of stock", productResponse);
                    })
                    .orElseGet(() -> new ProductQuantityCheckResponse(false, "No Product found", null));
        } catch (Exception exception) {
            exception.printStackTrace();
            return new ProductQuantityCheckResponse(false, exception.getMessage(), null);
        }
    }
}
