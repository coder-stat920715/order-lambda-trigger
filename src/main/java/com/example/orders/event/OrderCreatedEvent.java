package com.example.orders.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Payload sent to the Lambda function (serialized as JSON). */
@Value
@Builder
@Jacksonized
public class OrderCreatedEvent {

    String eventId;
    @Builder.Default
    String eventType = "ORDER_CREATED";
    Instant occurredAt;
    String orderId;
    String transactionId;
    String customerEmail;
    List<Item> items;
    BigDecimal totalAmount;

    @Value
    @Builder
    @Jacksonized
    public static class Item {
        String sku;
        int quantity;
        BigDecimal unitPrice;
    }
}
