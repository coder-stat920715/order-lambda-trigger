package com.example.orders.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

/**
 * Creates the {@link LambdaClient} only when {@code aws.lambda.mock-mode} is false (or absent).
 * In mock mode no bean exists, so no credential lookup or network access ever happens.
 */
@Slf4j
@Configuration
public class AwsLambdaConfig {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "aws.lambda", name = "mock-mode", havingValue = "false", matchIfMissing = true)
    public LambdaClient lambdaClient(AwsProperties props) {
        log.info("Creating LambdaClient region={} function={}", props.getRegion(), props.getLambda().getFunctionName());
        return LambdaClient.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(props.getLambda().getApiCallTimeout())
                        .retryPolicy(RetryMode.STANDARD)
                        .build())
                .build();
    }
}
