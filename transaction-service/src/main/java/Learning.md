What you built?
1. Built an endpoint for health check
2. Created entities
3. Implemented controller and service for transaction
4. Validations for request fields
5. Custom exceptions 

What you learned?
1. How API endpoints work
2. Usage of @Annotations
3. Lombok's @RequiredArgsConstructor only creates a constructor for final or @NonNull fields
4. Why shouldn't the client be allowed to send the transaction status?
The client should not be allowed to set the transaction status because the client is 
untrusted. If a malicious user sends "status": "APPROVED" in the request, they could bypass 
the application's business rules and fraud detection process. This could result in 
unauthorized or fraudulent transactions being marked as approved, leading to incorrect 
records in the database and potentially financial loss. The backend must be the single 
source of truth for fields like status, transactionId, and timestamp, since they are 
determined by business logic, not by the client.
5. Controller - should do Structural validation. Eg: amount = -5000, is invalid. Checked by controller
   Service - should do Business validation. Eg: Insufficient balance, is account blocked?, same source and destination
6. Why this difference? To separate concerns and keep controller thin
   Service should have all business logic - so that it can be used from various endpoints (Eg: Kafka)
7. @ExtendWith(MockitoExtension.class) is the annotation used to integrate the Mockito framework with the JUnit 
Jupiter test lifecycle
8. Controller testing is diff from usual service tests (unit tests) - bcoz controller handles HTTP requests
Mockmvc - acts like fake postman inside my test 
9. PATCH vs PUT: apply partial modifications to existing resource, non-idempotent vs Replaces entire resource, idempotent
10. Idempotency: making multiple identical requests to the server leaves the resource in the same state as the very first request
