package com.microservice.customer.mapper;

import com.microservice.customer.dto.AddressDto;
import com.microservice.customer.dto.CustomerDto;
import com.microservice.customer.model.Address;
import com.microservice.customer.model.Customer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerDto dto) {
        Customer customer = Customer.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .active(dto.isActive())
                .build();
        
        if (dto.getId() != null) {
            customer.setId(dto.getId());
            customer.setUpdatedAt(LocalDateTime.now());
        }
        
        return customer;
    }
    
    public CustomerDto toDto(Customer customer, List<Address> addresses) {
        CustomerDto dto = CustomerDto.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .active(customer.isActive())
                .build();
        
        if (addresses != null && !addresses.isEmpty()) {
            dto.setAddresses(addresses.stream()
                    .map(this::toAddressDto)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    public Address toAddressEntity(AddressDto dto, String customerId) {
        Address address = Address.builder()
                .street(dto.getStreet())
                .number(dto.getNumber())
                .complement(dto.getComplement())
                .neighborhood(dto.getNeighborhood())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .zipCode(dto.getZipCode())
                .primary(dto.isPrimary())
                .customerId(customerId)
                .build();
        
        if (dto.getId() != null) {
            address.setId(dto.getId());
        }
        
        return address;
    }
    
    public AddressDto toAddressDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .street(address.getStreet())
                .number(address.getNumber())
                .complement(address.getComplement())
                .neighborhood(address.getNeighborhood())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .primary(address.isPrimary())
                .build();
    }
}