package com.microservice.customer.service.impl;

import com.microservice.customer.client.AddressApiClient;
import com.microservice.customer.dto.AddressDto;
import com.microservice.customer.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressApiClient addressApiClient;

    @Override
    public AddressDto createAddress(String customerId, AddressDto addressDto) {
        return addressApiClient.createAddress(customerId, addressDto);
    }

    @Override
    public AddressDto getAddressById(String id) {
        return addressApiClient.getAddressById(id);
    }

    @Override
    public List<AddressDto> getAddressesByCustomerId(String customerId) {
        return addressApiClient.getAddressesByCustomerId(customerId);
    }

    @Override
    public AddressDto updateAddress(String id, AddressDto addressDto) {
        return addressApiClient.updateAddress(id, addressDto);
    }

    @Override
    public void deleteAddress(String id) {
        addressApiClient.deleteAddress(id);
    }

    @Override
    public AddressDto setPrimaryAddress(String customerId, String addressId) {
        return addressApiClient.setPrimaryAddress(customerId, addressId);
    }

    @Override
    public List<AddressDto> searchAddressesByCity(String city) {
        return addressApiClient.searchAddressesByCity(city);
    }

    @Override
    public List<AddressDto> searchAddressesByState(String state) {
        return addressApiClient.searchAddressesByState(state);
    }

    @Override
    public List<AddressDto> searchAddressesByZipCode(String zipCode) {
        return addressApiClient.searchAddressesByZipCode(zipCode);
    }
}