# User & Account API – Take Home Exercise

This service implements a simple banking-style API for managing users, bank accounts, and transactions, built according to the
provided OpenAPI specification.

## Features

* User management (create, update, delete with domain constraints)
* Bank accounts (CRUD with ownership enforcement)
* Transactions (deposit, withdrawal, listing)
* JWT-based authentication
* PostgreSQL persistence with Flyway migrations
* Comprehensive integration tests and focused unit tests

## Tech Stack

* Java 21
* Spring Boot 3.x
* Spring Security (JWT)
* PostgreSQL
* Flyway
* JPA / Hibernate
* Testcontainers (integration tests)

# Running the application

## Prerequisites

* Java 21
* Docker (for PostgreSQL)
* Gradle

## Environment variables

The application expects a JWT secret to be supplied via environment variable:

````shell
    export SECURITY_JWT_SECRET="a-secure-secret-at-least-32-characters-long"
````

A test-specific secret is configured in application-test.properties.

## Database

The application uses PostgreSQL via Docker Compose. Flyway migrations run automatically on startup to create tables.

## Start PostgreSQL DB

````shell
    docker compose up -d
````

## Run the application

````shell
    export SECURITY_JWT_SECRET="a-secure-secret-at-least-32-characters-long"
    ./gradlew bootRun
````

### 3) Database configuration

In `docker-compose.yml` you should have something like:

````yaml
    services:
      postgres:
        image: postgres:16
        container_name: eaglebank-postgres
        environment:
          POSTGRES_DB: eaglebank
          POSTGRES_USER: eagle
          POSTGRES_PASSWORD: eagle
        ports:
          - "5432:5432"
````

And your application.properties:

````shell
    spring.datasource.url=jdbc:postgresql://localhost:5432/eaglebank
    spring.datasource.username=eagle
    spring.datasource.password=eagle
    spring.flyway.enabled=true
````

## Run test

````shell
    ./gradlew test
````

The API will start on:

````shell
    http://localhost:8080
````

### Authentication flow

1. Create a user
2. Register credentials
3. Login to obtain JWT
4. Use Authorization: Bearer <token> for protected endpoints

Example:

````shell
curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{ "email": "user@example.com", "password": "Password123!" }'
````

## Domain constraints

* Users cannot be deleted if they have active bank accounts (returns 409 Conflict)
* Only account owners may access or modify their accounts (403 Forbidden)
* Withdrawals exceeding available balance return 422 Unprocessable Entity
* Currency is restricted to GBP as defined in the OpenAPI contract

## Validation errors

Validation failures return a BadRequestErrorResponse:

````shell
    {
    "message": "Validation failed",
    "details": [
        {
        "field": "amount",
        "message": "must be greater than or equal to 0.00",
        "type": "DecimalMin"
        }
      ]
    }
````

## Testing

Run all tests with:

````shell
    ./gradlew test
````

Integration tests use Testcontainers and do not require a running database.

## Notes

* GBP-only currency contract (spec enums currency = GBP)
* User deletion constraint returning 409 when user has accounts (this is implied by the domain rules in the specification and
  enforced here)
* TransactionId regex looks wrong (^tan-[A-Za-z0-9]$ only allows 1 char after hyphen).
* Balance updates are performed transactionally with row-level locking to prevent race conditions.
* A simple /health endpoint is provided via HealthController instead of using Spring Actuator to keep dependencies minimal for
  this exercise.
* Global security enabled in OpenAPI header, public endpoints explicitly set "security: []".
* Transaction creation is not idempotent, retries may create duplicate transactions.
* Idempotency support, POST /v1/accounts/{accountNumber}/transactions accepts an optional Idempotency-Key header to allow safe
  retries without creating duplicate transactions.

## OpenAPI Update

#### OpenAPI specification included at: /main/resources/openapi.yaml

* Auth Endpoints

````shell
  /auth/register:
   post:
     summary: Register user credentials
     operationId: register
     security: []   
     requestBody:
       required: true
       content:
         application/json:
           schema:
             $ref: "#/components/schemas/UserRegistrationRequest"
     responses:
       "204":
         description: Credentials registered
       "400":
         $ref: "#/components/responses/BadRequest"
       "401":
         description: Invalid credentials

  /auth/login:
   post:
     summary: Authenticate user and return JWT
     operationId: login
     security: []   
     requestBody:
       required: true
       content:
         application/json:
           schema:
             $ref: "#/components/schemas/LoginRequest"
     responses:
       "200":
         description: Authentication successful
         content:
           application/json:
             schema:
               $ref: "#/components/schemas/LoginResponse"
       "400":
         $ref: "#/components/responses/BadRequest"
       "401":
         description: Invalid credentials
````

* BadRequestErrorResponse schema added to components

````shell
    BadRequestErrorResponse:
      type: object
      required:
        - message
        - details
      properties:
        message:
          type: string
        details:
          type: array
          items:
            type: object
            required:
              - field
              - message
              - type
            properties:
              field:
                type: string
              message:
                type: string
              type:
                type: string
````