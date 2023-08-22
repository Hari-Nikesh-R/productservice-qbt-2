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
    public synchronized ResponseEntity<BaseResponse<String>> makePurchase(PurchaseRequest purchaseRequest) {
        try {
            Optional<Product> optionalProduct = productRepository.findByProductName(purchaseRequest.getProductName());
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                if (product.getProductQuantity() >= purchaseRequest.getQuantity()) {
                    try {
                        updateStockByPurchase(purchaseRequest.getProductName(), purchaseRequest.getQuantity());
                        logPurchase(product, purchaseRequest.getQuantity());
                        log.info("Done purchasing");
                        return ResponseEntity.ok(new BaseResponse<>("Product Purchased", null, true, HttpStatus.OK.value()));
                    } catch (IOException e) {
                        log.error("Something went wrong with exception: " + e.getMessage());
                        throw new RuntimeException(e);
                    }
                } else {
                    log.warn("Product out of stock");
                    return ResponseEntity.ok(new BaseResponse<>("Product out of stock", null, false, HttpStatus.NO_CONTENT.value()));
                }
            } else {
                log.warn("No product found");
                return ResponseEntity.ok(new BaseResponse<>("No Product Found to Purchase", null, false, HttpStatus.I_AM_A_TEAPOT.value()));
            }
        } catch (NullPointerException exception) {
            return ResponseEntity.ok(new BaseResponse<>("No Product Found to Purchase", null, false, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        } catch (Exception exception) {
            return ResponseEntity.ok(new BaseResponse<>(null, exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Override
    public BaseResponse<?> deductStock(List<PurchaseRequest> productRequest) {
        try {
            for (PurchaseRequest product : productRequest) {
                Optional<Product> optionalProduct = productRepository.findByProductName(product.getProductName());
                if (optionalProduct.isPresent()) {
                    Product prod = optionalProduct.get();
                    if (prod.getProductQuantity() >= product.getQuantity()) {
                        prod.setProductQuantity(prod.getProductQuantity() - product.getQuantity());
                        productRepository.save(prod);
                    }
                    else {
                        log.warn("Out of stock");
                    }
                }
            }
            return new BaseResponse<>("Quantity deducted", null, true, HttpStatus.OK.value());
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return new BaseResponse<>(null, exception.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private synchronized void updateStockByPurchase(String productName, Integer quantity) {
        try {
            Optional<Product> optionalProduct = productRepository.findByProductName(productName);
            if (optionalProduct.isPresent()) {
                Product product = optionalProduct.get();
                product.setProductQuantity(product.getProductQuantity() - quantity);
                log.info("Updated quantity of the stock");
                productRepository.save(product);
            }
        } catch (Exception exception) {
            log.error("thrown an exception: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private synchronized void logPurchase(Product product, Integer quantity) throws IOException {
        try {
            File file = new File("purchases.log");
            if (file.exists()) {
                writePurchasesLog(product, quantity);
            } else {
                if (file.createNewFile()) {
                    writePurchasesLog(product, quantity);
                } else {
                    System.out.println("Unable to write Logs");
                }
            }

        } catch (Exception exception) {
            System.out.println("Unable to write Logs");
            exception.printStackTrace();
        }
    }

    private void writePurchasesLog(Product product, Integer quantity) throws IOException {
        FileWriter fileWriter = new FileWriter("purchases.log", true);
        fileWriter.append("ID: ").append(product.getProductId().toString()).append(" - Purchased: ").append(product.getProductName()).append(" of quantity ").append(String.valueOf(quantity));
        fileWriter.append("\n");
        fileWriter.close();
    }


}
