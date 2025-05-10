# Customer Service Microservice

A Spring Boot microservice for managing customers and their addresses via external APIs. This service provides a RESTful API that acts as a facade, interacting with downstream customer and address services.

## Technologies Used

- Java 21
- Spring Boot 3.2.3
- Spring WebFlux (for `WebClient`)
- Project Lombok
- JUnit 5
- Mockito
- Cucumber for BDD Testing
- RestAssured for API Testing
- SpringDoc OpenAPI (Swagger UI)

## Architecture

The application follows a classic layered architecture:

1. **Controller Layer**: Handles HTTP requests/responses and input validation
2. **Service Layer**: Contains business logic and rules, orchestrates operations
3. **Client Layer**: Manages external API interactions via WebClient

This separation of concerns ensures that:
- Business logic is isolated from API communication details
- External dependencies can be easily mocked for testing
- Code is more maintainable and testable

## Features

Acts as a gateway to manage customer and address data by calling external APIs.

### Customer Management
- Proxies requests to create, retrieve, update, and delete customers via an external API.
- Proxies requests to search customers by first name, last name, or email via an external API.
- Proxies requests to activate or deactivate customers via an external API.

### Address Management
- Proxies requests to add, update, and delete addresses for customers via an external API.
- Proxies requests to set a primary address for a customer via an external API.
- Proxies requests to search addresses by city, state, or ZIP code via an external API.

## API Documentation

The API documentation is available via Swagger UI at `/swagger-ui` when the application is running.

## Running the Application

### Prerequisites
- Java 21
- Maven
- Access to the external Customer and Address APIs

### Steps
1. Clone the repository
2. Configure the external API base URL and timeouts in `application.properties`.
   ```properties
   api.base-url=https://xyz.net/api/v1/enterprise
   api.connect-timeout=5000
   api.read-timeout=5000
   ```
3. Build the application: `mvn clean package`
4. Run the application: `java -jar target/customer-service-0.0.1-SNAPSHOT.jar`

## Testing

The application includes three levels of testing that follow proper architectural testing practices:

1. **Unit Tests**: Testing service classes in isolation using Mockito to mock API clients. This verifies the business logic without calling external dependencies.
   ```bash
   mvn test
   ```

2. **Integration Tests**: Testing the controllers and application context using `TestRestTemplate` and `@MockBean` to mock the API clients. This verifies the controller layer and request/response handling, ensuring that controllers properly interact with the service layer.
   ```bash
   mvn test -Dtest=*IntegrationTest
   ```

3. **BDD Tests**: Feature-based tests using Cucumber with RestAssured to test HTTP endpoints and service layer interactions. These tests validate the API behavior from an external perspective while using `@MockBean` to mock the API clients, ensuring complete test coverage without external dependencies.
   ```bash
   mvn test -Dtest=CucumberTestRunner
   ```

## API Endpoints

### Customer Endpoints
- `POST /api/v1/customers` - Create a new customer (proxied)
- `GET /api/v1/customers/{id}` - Get customer by ID (proxied)
- `GET /api/v1/customers/email/{email}` - Get customer by email (proxied)
- `GET /api/v1/customers` - Get all customers or search by name (proxied)
- `PUT /api/v1/customers/{id}` - Update customer (proxied)
- `DELETE /api/v1/customers/{id}` - Delete customer (proxied)
- `PATCH /api/v1/customers/{id}/status` - Update customer status (proxied)

### Address Endpoints
- `POST /api/v1/customers/{customerId}/addresses` - Add address to customer (proxied)
- `GET /api/v1/addresses/{id}` - Get address by ID (proxied)
- `GET /api/v1/customers/{customerId}/addresses` - Get all addresses for a customer (proxied)
- `PUT /api/v1/addresses/{id}` - Update address (proxied)
- `DELETE /api/v1/addresses/{id}` - Delete address (proxied)
- `PATCH /api/v1/customers/{customerId}/addresses/{addressId}/primary` - Set primary address (proxied)
- `GET /api/v1/addresses/search/city/{city}` - Search addresses by city (proxied)
- `GET /api/v1/addresses/search/state/{state}` - Search addresses by state (proxied)
- `GET /api/v1/addresses/search/zipcode/{zipCode}` - Search addresses by ZIP code (proxied)