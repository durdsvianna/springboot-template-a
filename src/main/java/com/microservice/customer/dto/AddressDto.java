package com.microservice.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    
    private String id;
    
    @NotBlank(message = "Street is required")
    private String street;
    
    @NotBlank(message = "Number is required")
    private String number;
    
    private String complement;
    
    @NotBlank(message = "Neighborhood is required")
    private String neighborhood;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    @NotBlank(message = "Zipcode is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid zip code format")
    private String zipCode;
    
    private boolean primary;
}