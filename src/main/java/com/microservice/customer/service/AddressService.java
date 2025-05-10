package com.microservice.customer.service;

import com.microservice.customer.dto.AddressDto;

import java.util.List;

public interface AddressService {
    
    AddressDto createAddress(String customerId, AddressDto addressDto);
    
    AddressDto getAddressById(String id);
    
    List<AddressDto> getAddressesByCustomerId(String customerId);
    
    AddressDto updateAddress(String id, AddressDto addressDto);
    
    void deleteAddress(String id);
    
    AddressDto setPrimaryAddress(String customerId, String addressId);
    
    List<AddressDto> searchAddressesByCity(String city);
    
    List<AddressDto> searchAddressesByState(String state);
    
    List<AddressDto> searchAddressesByZipCode(String zipCode);
}