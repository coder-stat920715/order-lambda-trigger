package com.example.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotBlank(message = "customerEmail is required")
    @Email(message = "customerEmail must be a valid email address")
    private String customerEmail;

    @NotEmpty(message = "items must contain at least one item")
    @Valid
    private List<OrderItem> items;

    @NotNull(message = "totalAmount is required")
    @DecimalMin(value = "0.01", message = "totalAmount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "totalAmount must have at most 2 decimal places")
    private BigDecimal totalAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {

        @NotBlank(message = "item sku is required")
        private String sku;

        @Min(value = 1, message = "item quantity must be at least 1")
        private int quantity;

        @NotNull(message = "item unitPrice is required")
        @DecimalMin(value = "0.01", message = "item unitPrice must be greater than 0")
        private BigDecimal unitPrice;
    }
}
