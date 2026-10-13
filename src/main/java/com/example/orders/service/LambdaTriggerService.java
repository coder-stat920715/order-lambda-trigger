package com.example.orders.service;

import com.example.orders.config.AwsProperties;
import com.example.orders.event.OrderCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

/**
 * Fire-and-forget Lambda invocation. Uses InvocationType.EVENT, so Lambda queues the event and
 * returns HTTP 202 immediately without waiting for the function to run.
 *
 * Failure policy: a failed side-effect (invoice generation) must never fail the customer's order.
 * Errors are logged and reported as FALLBACK so callers can retry / route to a DLQ or outbox.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LambdaTriggerService {

    public enum DispatchResult { DISPATCHED, MOCKED, FALLBACK }

    private final ObjectProvider<LambdaClient> lambdaClientProvider;
    private final AwsProperties props;
    private final ObjectMapper objectMapper;

    public DispatchResult trigger(OrderCreatedEvent event) {
        final String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event eventId={} orderId={}", event.getEventId(), event.getOrderId(), e);
            return DispatchResult.FALLBACK;
        }

        String functionName = props.getLambda().getFunctionName();

        if (props.getLambda().isMockMode()) {
            log.info("[MOCK] Would invoke Lambda '{}' asynchronously eventId={} orderId={}",
                    functionName, event.getEventId(), event.getOrderId());
            log.debug("[MOCK] payload={}", payload);
            return DispatchResult.MOCKED;
        }

        LambdaClient client = lambdaClientProvider.getIfAvailable();
        if (client == null) {
            log.warn("LambdaClient bean unavailable, falling back. eventId={} orderId={}",
                    event.getEventId(), event.getOrderId());
            return DispatchResult.FALLBACK;
        }

        InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .invocationType(InvocationType.EVENT)
                .payload(SdkBytes.fromUtf8String(payload))
                .build();

        try {
            InvokeResponse response = client.invoke(request);
            if (response.statusCode() == 202) {
                log.info("Lambda '{}' accepted event eventId={} orderId={} status={}",
                        functionName, event.getEventId(), event.getOrderId(), response.statusCode());
                return DispatchResult.DISPATCHED;
            }
            log.warn("Unexpected Lambda status={} functionError={} eventId={}",
                    response.statusCode(), response.functionError(), event.getEventId());
            return DispatchResult.FALLBACK;
        } catch (SdkException e) {
            // Covers missing credentials, throttling, timeouts, unknown function, etc.
            log.error("Lambda invocation failed function='{}' eventId={} orderId={}: {}",
                    functionName, event.getEventId(), event.getOrderId(), e.getMessage(), e);
            // Production: persist to outbox table / send to SQS DLQ here for later replay.
            return DispatchResult.FALLBACK;
        }
    }
}
