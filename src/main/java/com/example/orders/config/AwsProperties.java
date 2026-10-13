package com.example.orders.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/** Type-safe binding for the {@code aws.*} section of application.yml. */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {

    @NotBlank
    private String region = "us-east-1";

    @Valid
    @NotNull
    private Lambda lambda = new Lambda();

    @Getter
    @Setter
    public static class Lambda {
        @NotBlank
        private String functionName;
        private boolean mockMode = false;
        @NotNull
        private Duration apiCallTimeout = Duration.ofSeconds(10);
    }
}
