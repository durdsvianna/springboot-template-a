# Customer Service Microservice

A Spring Boot microservice for managing customers and their addresses. This service provides a RESTful API for creating, updating, deleting, and searching for customers and their associated addresses.

## Technologies Used

- Java 21
- Spring Boot 3.2.3
- Spring Data MongoDB
- MongoDB
- Project Lombok
- JUnit 5
- Cucumber for BDD Testing
- RestAssured for API Testing
- TestContainers for MongoDB Integration Testing
- SpringDoc OpenAPI (Swagger UI)

## Features

### Customer Management
- Create, retrieve, update, and delete customers
- Search customers by first name, last name, or email
- Activate or deactivate customers

### Address Management
- Add, update, and delete addresses for customers
- Set a primary address for a customer
- Search addresses by city, state, or ZIP code

## API Documentation

The API documentation is available via Swagger UI at `/swagger-ui` when the application is running.

## Running the Application

### Prerequisites
- Java 21
- MongoDB
- Maven

### Steps
1. Clone the repository
2. Configure MongoDB settings in `application.properties` if necessary
3. Build the application: `mvn clean package`
4. Run the application: `java -jar target/customer-service-0.0.1-SNAPSHOT.jar`

## Testing

The application includes three levels of testing:

1. **Unit Tests**: Testing individual components in isolation
   ```
   mvn test
   ```

2. **Integration Tests**: Testing components working together with TestContainers
   ```
   mvn test -Dtest=*IntegrationTest
   ```

3. **BDD Tests**: Feature-based tests using Cucumber
   ```
   mvn test -Dtest=CucumberTestRunner
   ```

## API Endpoints

### Customer Endpoints
- `POST /api/v1/customers` - Create a new customer
- `GET /api/v1/customers/{id}` - Get customer by ID
- `GET /api/v1/customers/email/{email}` - Get customer by email
- `GET /api/v1/customers` - Get all customers or search by name
- `PUT /api/v1/customers/{id}` - Update customer
- `DELETE /api/v1/customers/{id}` - Delete customer
- `PATCH /api/v1/customers/{id}/status` - Update customer status

### Address Endpoints
- `POST /api/v1/customers/{customerId}/addresses` - Add address to customer
- `GET /api/v1/addresses/{id}` - Get address by ID
- `GET /api/v1/customers/{customerId}/addresses` - Get all addresses for a customer
- `PUT /api/v1/addresses/{id}` - Update address
- `DELETE /api/v1/addresses/{id}` - Delete address
- `PATCH /api/v1/customers/{customerId}/addresses/{addressId}/primary` - Set primary address
- `GET /api/v1/addresses/search/city/{city}` - Search addresses by city
- `GET /api/v1/addresses/search/state/{state}` - Search addresses by state
- `GET /api/v1/addresses/search/zipcode/{zipCode}` - Search addresses by ZIP code