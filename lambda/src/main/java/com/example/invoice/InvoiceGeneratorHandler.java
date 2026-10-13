package com.example.invoice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Consumes the OrderCreatedEvent JSON sent by the Spring Boot service.
 * Handler string: com.example.invoice.InvoiceGeneratorHandler::handleRequest
 * Runtime: java17 (or java21).
 */
public class InvoiceGeneratorHandler implements RequestHandler<Map<String, Object>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderCreatedEvent(String eventId, String eventType, String occurredAt, String orderId,
                                    String transactionId, String customerEmail, List<Item> items,
                                    BigDecimal totalAmount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(String sku, int quantity, BigDecimal unitPrice) {}

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        LambdaLogger log = context.getLogger();
        OrderCreatedEvent event = MAPPER.convertValue(input, OrderCreatedEvent.class);
        log.log("Received eventId=" + event.eventId() + " orderId=" + event.orderId());

        // Idempotency: async invokes are at-least-once, so dedupe on eventId (e.g. DynamoDB conditional put).
        BigDecimal computed = event.items().stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (computed.compareTo(event.totalAmount()) != 0) {
            log.log("WARN total mismatch computed=" + computed + " declared=" + event.totalAmount());
        }

        String invoiceNumber = "INV-" + event.orderId().substring(0, 8).toUpperCase();
        // TODO: render PDF, store in S3, send email via SES to event.customerEmail().
        log.log("Generated invoice " + invoiceNumber + " for " + event.customerEmail());
        return invoiceNumber;
    }
}
