package com.example.orders.service;

import com.example.orders.dto.OrderRequest;
import com.example.orders.dto.OrderResponse;
import com.example.orders.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final LambdaTriggerService lambdaTriggerService;

    public OrderResponse createOrder(OrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        String transactionId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        // TODO: persist the order (JPA / DynamoDB) before publishing the event.
        log.info("Order created orderId={} transactionId={} items={} total={}",
                orderId, transactionId, request.getItems().size(), request.getTotalAmount());

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(now)
                .orderId(orderId)
                .transactionId(transactionId)
                .customerEmail(request.getCustomerEmail())
                .totalAmount(request.getTotalAmount())
                .items(request.getItems().stream()
                        .map(i -> OrderCreatedEvent.Item.builder()
                                .sku(i.getSku())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .build())
                        .toList())
                .build();

        LambdaTriggerService.DispatchResult result = lambdaTriggerService.trigger(event);

        return OrderResponse.builder()
                .orderId(orderId)
                .transactionId(transactionId)
                .status("ACCEPTED")
                .invoiceDispatch(result.name())
                .createdAt(now)
                .build();
    }
}
