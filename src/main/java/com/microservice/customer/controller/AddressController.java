package com.microservice.customer.controller;

import com.microservice.customer.dto.AddressDto;
import com.microservice.customer.dto.ApiResponse;
import com.microservice.customer.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Address", description = "Address management APIs")
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/customers/{customerId}/addresses")
    @Operation(summary = "Add address to customer", description = "Creates a new address for a specific customer")
    public ResponseEntity<ApiResponse<AddressDto>> createAddress(
            @Parameter(description = "Customer ID", required = true) @PathVariable String customerId,
            @Valid @RequestBody AddressDto addressDto) {
        AddressDto createdAddress = addressService.createAddress(customerId, addressDto);
        return new ResponseEntity<>(ApiResponse.success("Address created successfully", createdAddress), HttpStatus.CREATED);
    }

    @GetMapping("/addresses/{id}")
    @Operation(summary = "Get address by ID", description = "Retrieves an address by its ID")
    public ResponseEntity<ApiResponse<AddressDto>> getAddressById(
            @Parameter(description = "Address ID", required = true) @PathVariable String id) {
        AddressDto address = addressService.getAddressById(id);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @GetMapping("/customers/{customerId}/addresses")
    @Operation(summary = "Get all addresses for a customer", description = "Retrieves all addresses associated with a specific customer")
    public ResponseEntity<ApiResponse<List<AddressDto>>> getAddressesByCustomerId(
            @Parameter(description = "Customer ID", required = true) @PathVariable String customerId) {
        List<AddressDto> addresses = addressService.getAddressesByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @PutMapping("/addresses/{id}")
    @Operation(summary = "Update address", description = "Updates an existing address by ID")
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(
            @Parameter(description = "Address ID", required = true) @PathVariable String id,
            @Valid @RequestBody AddressDto addressDto) {
        AddressDto updatedAddress = addressService.updateAddress(id, addressDto);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", updatedAddress));
    }

    @DeleteMapping("/addresses/{id}")
    @Operation(summary = "Delete address", description = "Deletes an address by ID")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @Parameter(description = "Address ID", required = true) @PathVariable String id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }

    @PatchMapping("/customers/{customerId}/addresses/{addressId}/primary")
    @Operation(summary = "Set primary address", description = "Sets an address as the primary address for a customer")
    public ResponseEntity<ApiResponse<AddressDto>> setPrimaryAddress(
            @Parameter(description = "Customer ID", required = true) @PathVariable String customerId,
            @Parameter(description = "Address ID", required = true) @PathVariable String addressId) {
        AddressDto updatedAddress = addressService.setPrimaryAddress(customerId, addressId);
        return ResponseEntity.ok(ApiResponse.success("Address set as primary successfully", updatedAddress));
    }

    @GetMapping("/addresses/search/city/{city}")
    @Operation(summary = "Search addresses by city", description = "Retrieves all addresses for a specific city")
    public ResponseEntity<ApiResponse<List<AddressDto>>> searchAddressesByCity(
            @Parameter(description = "City name", required = true) @PathVariable String city) {
        List<AddressDto> addresses = addressService.searchAddressesByCity(city);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/addresses/search/state/{state}")
    @Operation(summary = "Search addresses by state", description = "Retrieves all addresses for a specific state")
    public ResponseEntity<ApiResponse<List<AddressDto>>> searchAddressesByState(
            @Parameter(description = "State name", required = true) @PathVariable String state) {
        List<AddressDto> addresses = addressService.searchAddressesByState(state);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/addresses/search/zipcode/{zipCode}")
    @Operation(summary = "Search addresses by ZIP code", description = "Retrieves all addresses for a specific ZIP code")
    public ResponseEntity<ApiResponse<List<AddressDto>>> searchAddressesByZipCode(
            @Parameter(description = "ZIP code", required = true) @PathVariable String zipCode) {
        List<AddressDto> addresses = addressService.searchAddressesByZipCode(zipCode);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }
}