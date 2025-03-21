package com.microservice.customer.controller;

import com.microservice.customer.dto.ApiResponse;
import com.microservice.customer.dto.CustomerDto;
import com.microservice.customer.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer management APIs")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create a new customer", description = "Creates a new customer with optional addresses")
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        CustomerDto createdCustomer = customerService.createCustomer(customerDto);
        return new ResponseEntity<>(ApiResponse.success("Customer created successfully", createdCustomer), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieves a customer by their ID")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerById(
            @Parameter(description = "Customer ID", required = true) @PathVariable String id) {
        CustomerDto customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get customer by email", description = "Retrieves a customer by their email address")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerByEmail(
            @Parameter(description = "Customer email", required = true) @PathVariable String email) {
        CustomerDto customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping
    @Operation(summary = "Get all customers or search by name", description = "Retrieves all customers or searches by first name and/or last name")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> getCustomers(
            @Parameter(description = "First name to search (optional)") @RequestParam(required = false) String firstName,
            @Parameter(description = "Last name to search (optional)") @RequestParam(required = false) String lastName) {
        
        List<CustomerDto> customers;
        if (firstName != null || lastName != null) {
            customers = customerService.searchCustomers(firstName, lastName);
        } else {
            customers = customerService.getAllCustomers();
        }
        
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Updates an existing customer by ID")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(
            @Parameter(description = "Customer ID", required = true) @PathVariable String id,
            @Valid @RequestBody CustomerDto customerDto) {
        CustomerDto updatedCustomer = customerService.updateCustomer(id, customerDto);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updatedCustomer));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Deletes a customer by ID")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @Parameter(description = "Customer ID", required = true) @PathVariable String id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update customer status", description = "Activates or deactivates a customer")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomerStatus(
            @Parameter(description = "Customer ID", required = true) @PathVariable String id,
            @Parameter(description = "Customer active status", required = true) @RequestParam boolean active) {
        CustomerDto updatedCustomer = customerService.setCustomerStatus(id, active);
        String status = active ? "activated" : "deactivated";
        return ResponseEntity.ok(ApiResponse.success("Customer " + status + " successfully", updatedCustomer));
    }
}