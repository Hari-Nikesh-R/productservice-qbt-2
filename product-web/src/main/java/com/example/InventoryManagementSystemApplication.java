package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication(scanBasePackages = {"com.example"})
public class InventoryManagementSystemApplication {
    public static void main(String[] args) throws IOException {
        SpringApplication.run(InventoryManagementSystemApplication.class, args);
    }
}