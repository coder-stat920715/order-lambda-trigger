# order-lambda-trigger

Spring Boot 3 (Java 17) service that fires an AWS Lambda asynchronously (InvocationType.EVENT) when an order is created.

## Run locally (no AWS needed)
    mvn spring-boot:run          # mock-mode defaults to true

    curl -i -X POST http://localhost:8080/api/v1/orders \
      -H "Content-Type: application/json" \
      -d '{"customerEmail":"jane@example.com","items":[{"sku":"SKU-1","quantity":2,"unitPrice":19.99}],"totalAmount":39.98}'

Validation error test: send `{"customerEmail":"bad","items":[]}` -> 400 with field errors.

## Run against real AWS
    cd lambda && mvn package           # target/invoice-generator-lambda-1.0.0.jar
    # Create Lambda: runtime java17, handler com.example.invoice.InvoiceGeneratorHandler::handleRequest
    export AWS_REGION=us-east-1 LAMBDA_FUNCTION_NAME=invoice-generator LAMBDA_MOCK_MODE=false
    # credentials via env vars / ~/.aws / IAM role; needs lambda:InvokeFunction on the function
    mvn spring-boot:run

Response field `invoiceDispatch`: DISPATCHED (Lambda returned 202), MOCKED, or FALLBACK (invoke failed; order still succeeds).
