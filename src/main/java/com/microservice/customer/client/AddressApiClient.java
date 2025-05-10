package com.microservice.customer.client;

import com.microservice.customer.dto.AddressDto;
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
public class AddressApiClient {

    private final WebClient webClient;
    private static final String ADDRESSES_PATH = "/addresses";
    private static final String CUSTOMERS_PATH = "/customers";

    public AddressDto createAddress(String customerId, AddressDto addressDto) {
        return webClient.post()
                .uri(CUSTOMERS_PATH + "/{customerId}/addresses", customerId)
                .bodyValue(addressDto)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResourceNotFoundException("Customer", "id", customerId))
                )
                .bodyToMono(AddressDto.class)
                .onErrorMap(
                        throwable -> handleApiError(throwable, "create address for customer " + customerId)
                )
                .block();
    }

    public AddressDto getAddressById(String id) {
        return webClient.get()
                .uri(ADDRESSES_PATH + "/{id}", id)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResourceNotFoundException("Address", "id", id))
                )
                .bodyToMono(AddressDto.class)
                .onErrorMap(
                        throwable -> handleApiError(throwable, "get address by id: " + id)
                )
                .block();
    }

    public List<AddressDto> getAddressesByCustomerId(String customerId) {
        return webClient.get()
                .uri(CUSTOMERS_PATH + "/{customerId}/addresses", customerId)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResourceNotFoundException("Customer", "id", customerId))
                )
                .bodyToFlux(AddressDto.class)
                .collectList()
                .onErrorMap(
                        throwable -> handleApiError(throwable, "get addresses for customer " + customerId)
                )
                .block();
    }

    public AddressDto updateAddress(String id, AddressDto addressDto) {
        return webClient.put()
                .uri(ADDRESSES_PATH + "/{id}", id)
                .bodyValue(addressDto)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResourceNotFoundException("Address", "id", id))
                )
                .bodyToMono(AddressDto.class)
                .onErrorMap(
                        throwable -> handleApiError(throwable, "update address " + id)
                )
                .block();
    }

    public void deleteAddress(String id) {
        webClient.delete()
                .uri(ADDRESSES_PATH + "/{id}", id)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResourceNotFoundException("Address", "id", id))
                )
                .bodyToMono(Void.class)
                .onErrorMap(
                        throwable -> handleApiError(throwable, "delete address " + id)
                )
                .block();
    }

    public AddressDto setPrimaryAddress(String customerId, String addressId) {
        return webClient.patch()
                .uri(CUSTOMERS_PATH + "/{customerId}/addresses/{addressId}/primary", customerId, addressId)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(new ResourceNotFoundException("Address", "id", addressId))
                )
                .bodyToMono(AddressDto.class)
                .onErrorMap(
                        throwable -> handleApiError(throwable, "set primary address " + addressId)
                )
                .block();
    }

    public List<AddressDto> searchAddressesByCity(String city) {
        return webClient.get()
                .uri(ADDRESSES_PATH + "/search/city/{city}", city)
                .retrieve()
                .bodyToFlux(AddressDto.class)
                .collectList()
                .onErrorMap(
                        throwable -> handleApiError(throwable, "search addresses by city " + city)
                )
                .block();
    }

    public List<AddressDto> searchAddressesByState(String state) {
        return webClient.get()
                .uri(ADDRESSES_PATH + "/search/state/{state}", state)
                .retrieve()
                .bodyToFlux(AddressDto.class)
                .collectList()
                .onErrorMap(
                        throwable -> handleApiError(throwable, "search addresses by state " + state)
                )
                .block();
    }

    public List<AddressDto> searchAddressesByZipCode(String zipCode) {
        return webClient.get()
                .uri(ADDRESSES_PATH + "/search/zipcode/{zipCode}", zipCode)
                .retrieve()
                .bodyToFlux(AddressDto.class)
                .collectList()
                .onErrorMap(
                        throwable -> handleApiError(throwable, "search addresses by zipCode " + zipCode)
                )
                .block();
    }

    private Throwable handleApiError(Throwable ex, String operation) {
        if (ex instanceof WebClientResponseException) {
            WebClientResponseException wcre = (WebClientResponseException) ex;
            HttpStatus status = HttpStatus.valueOf(wcre.getStatusCode().value());
            
            if (status.equals(HttpStatus.NOT_FOUND)) {
                return new ResourceNotFoundException("Resource", "operation", operation);
            }
            
            return new RuntimeException("Error during API call: " + operation + ". Status: " + status + ", Message: " + wcre.getMessage());
        }
        
        return new RuntimeException("Unexpected error during API call: " + operation + ". Error: " + ex.getMessage());
    }
} 