package com.microservice.customer.client;

import com.microservice.customer.dto.CustomerDto;
import com.microservice.customer.exception.DuplicateResourceException;
import com.microservice.customer.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomerApiClient {

    private final WebClient webClient;
    private static final String CUSTOMERS_PATH = "/customers";

    public CustomerDto createCustomer(CustomerDto customerDto) {
        return webClient.post()
                .uri(CUSTOMERS_PATH)
                .bodyValue(customerDto)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.CONFLICT),
                        response -> Mono.error(new DuplicateResourceException("Customer", "email", customerDto.getEmail()))
                )
                .bodyToMono(CustomerDto.class)
                .onErrorMap(
                        throwable -> handleApiError(throwable, "create customer")
                )
                .block();
    }

    public CustomerDto getCustomerById(String id) {
        return webClient.get()
                .uri(CUSTOMERS_PATH + "/{id}", id)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResourceNotFoundException("Customer", "id", id))
                )
                .bodyToMono(CustomerDto.class)
                .onErrorMap(
                        throwable -> handleApiError(throwable, "get customer by id: " + id)
                )
                .block();
    }

    public CustomerDto getCustomerByEmail(String email) {
        return webClient.get()
                .uri(CUSTOMERS_PATH + "/email/{email}", email)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResourceNotFoundException("Customer", "email", email))
                )
                .bodyToMono(CustomerDto.class)
                .onErrorMap(
                        throwable -> handleApiError(throwable, "get customer by email: " + email)
                )
                .block();
    }

    public List<CustomerDto> getAllCustomers() {
        return webClient.get()
                .uri(CUSTOMERS_PATH)
                .retrieve()
                .bodyToFlux(CustomerDto.class)
                .collectList()
                .onErrorMap(
                        throwable -> handleApiError(throwable, "get all customers")
                )
                .block();
    }

    public List<CustomerDto> searchCustomers(String firstName, String lastName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(CUSTOMERS_PATH)
                        .queryParam("firstName", firstName == null ? "" : firstName)
                        .queryParam("lastName", lastName == null ? "" : lastName)
                        .build())
                .retrieve()
                .bodyToFlux(CustomerDto.class)
                .collectList()
                .onErrorMap(
                        throwable -> handleApiError(throwable, "search customers")
                )
                .block();
    }

    public CustomerDto updateCustomer(String id, CustomerDto customerDto) {
        return webClient.put()
                .uri(CUSTOMERS_PATH + "/{id}", id)
                .bodyValue(customerDto)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResourceNotFoundException("Customer", "id", id))
                )
                .onStatus(
                        status -> status.equals(HttpStatus.CONFLICT),
                        response -> Mono.error(new DuplicateResourceException("Customer", "email", customerDto.getEmail()))
                )
                .bodyToMono(CustomerDto.class)
                .onErrorMap(
                        throwable -> handleApiError(throwable, "update customer")
                )
                .block();
    }

    public void deleteCustomer(String id) {
        webClient.delete()
                .uri(CUSTOMERS_PATH + "/{id}", id)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResourceNotFoundException("Customer", "id", id))
                )
                .bodyToMono(Void.class)
                .onErrorMap(
                        throwable -> handleApiError(throwable, "delete customer")
                )
                .block();
    }

    public CustomerDto setCustomerStatus(String id, boolean active) {
        return webClient.patch()
                .uri(CUSTOMERS_PATH + "/{id}/status?active={active}", id, active)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResourceNotFoundException("Customer", "id", id))
                )
                .bodyToMono(CustomerDto.class)
                .onErrorMap(
                        throwable -> handleApiError(throwable, "set customer status")
                )
                .block();
    }

    private Throwable handleApiError(Throwable ex, String operation) {
        if (ex instanceof WebClientResponseException) {
            WebClientResponseException wcre = (WebClientResponseException) ex;
            HttpStatus status = HttpStatus.valueOf(wcre.getStatusCode().value());
            
            if (status.equals(HttpStatus.NOT_FOUND)) {
                return new ResourceNotFoundException("Resource", "operation", operation);
            } else if (status.equals(HttpStatus.CONFLICT)) {
                return new DuplicateResourceException("Resource", "operation", operation);
            }
            
            return new RuntimeException("Error during API call: " + operation + ". Status: " + status + ", Message: " + wcre.getMessage());
        }
        
        return new RuntimeException("Unexpected error during API call: " + operation + ". Error: " + ex.getMessage());
    }
} 