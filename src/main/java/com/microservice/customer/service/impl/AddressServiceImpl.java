package com.microservice.customer.service.impl;

import com.microservice.customer.dto.AddressDto;
import com.microservice.customer.exception.ResourceNotFoundException;
import com.microservice.customer.mapper.CustomerMapper;
import com.microservice.customer.model.Address;
import com.microservice.customer.repository.AddressRepository;
import com.microservice.customer.repository.CustomerRepository;
import com.microservice.customer.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public AddressDto createAddress(String customerId, AddressDto addressDto) {
        // Check if customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }
        
        // Check if this address is set as primary
        if (addressDto.isPrimary()) {
            // Reset any existing primary address
            addressRepository.findByCustomerIdAndPrimaryTrue(customerId)
                    .ifPresent(existingPrimary -> {
                        existingPrimary.setPrimary(false);
                        addressRepository.save(existingPrimary);
                    });
        }
        
        // Create new address
        Address address = customerMapper.toAddressEntity(addressDto, customerId);
        Address savedAddress = addressRepository.save(address);
        
        return customerMapper.toAddressDto(savedAddress);
    }

    @Override
    public AddressDto getAddressById(String id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));
        
        return customerMapper.toAddressDto(address);
    }

    @Override
    public List<AddressDto> getAddressesByCustomerId(String customerId) {
        // Check if customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }
        
        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        
        return addresses.stream()
                .map(customerMapper::toAddressDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressDto updateAddress(String id, AddressDto addressDto) {
        Address existingAddress = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));
        
        String customerId = existingAddress.getCustomerId();
        
        // Check if this address is being set as primary
        if (addressDto.isPrimary() && !existingAddress.isPrimary()) {
            // Reset any existing primary address
            addressRepository.findByCustomerIdAndPrimaryTrue(customerId)
                    .ifPresent(existingPrimary -> {
                        existingPrimary.setPrimary(false);
                        addressRepository.save(existingPrimary);
                    });
        }
        
        // Update the address
        Address addressToUpdate = customerMapper.toAddressEntity(addressDto, customerId);
        addressToUpdate.setId(id);
        Address updatedAddress = addressRepository.save(addressToUpdate);
        
        return customerMapper.toAddressDto(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(String id) {
        if (!addressRepository.existsById(id)) {
            throw new ResourceNotFoundException("Address", "id", id);
        }
        
        addressRepository.deleteById(id);
    }

    @Override
    @Transactional
    public AddressDto setPrimaryAddress(String customerId, String addressId) {
        // Check if customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }
        
        // Check if address exists and belongs to the customer
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        
        if (!address.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Address", "id", addressId + " for customer " + customerId);
        }
        
        // Reset any existing primary address
        addressRepository.findByCustomerIdAndPrimaryTrue(customerId)
                .ifPresent(existingPrimary -> {
                    existingPrimary.setPrimary(false);
                    addressRepository.save(existingPrimary);
                });
        
        // Set the new primary address
        address.setPrimary(true);
        Address updatedAddress = addressRepository.save(address);
        
        return customerMapper.toAddressDto(updatedAddress);
    }

    @Override
    public List<AddressDto> searchAddressesByCity(String city) {
        List<Address> addresses = addressRepository.findByCity(city);
        
        return addresses.stream()
                .map(customerMapper::toAddressDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AddressDto> searchAddressesByState(String state) {
        List<Address> addresses = addressRepository.findByState(state);
        
        return addresses.stream()
                .map(customerMapper::toAddressDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AddressDto> searchAddressesByZipCode(String zipCode) {
        List<Address> addresses = addressRepository.findByZipCode(zipCode);
        
        return addresses.stream()
                .map(customerMapper::toAddressDto)
                .collect(Collectors.toList());
    }
}