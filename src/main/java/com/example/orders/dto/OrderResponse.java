package com.example.orders.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class OrderResponse {
    String orderId;
    String transactionId;
    /** Business status of the order, e.g. ACCEPTED. */
    String status;
    /** Outcome of the async invoice trigger: DISPATCHED, MOCKED or FALLBACK. */
    String invoiceDispatch;
    Instant createdAt;
}
