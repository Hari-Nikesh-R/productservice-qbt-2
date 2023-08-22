package com.example.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerDetailRequest {
    @Email(message = "Invalid email")
    @NotNull(message = "email cannot be null")
    private String email;
    @NotNull(message = "Name cannot be null")
    private String name;
    private String phoneNumber;

}
