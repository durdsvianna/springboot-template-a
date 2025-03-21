package com.microservice.customer.service.impl;

import com.microservice.customer.dto.CustomerDto;
import com.microservice.customer.exception.DuplicateResourceException;
import com.microservice.customer.exception.ResourceNotFoundException;
import com.microservice.customer.mapper.CustomerMapper;
import com.microservice.customer.model.Address;
import com.microservice.customer.model.Customer;
import com.microservice.customer.repository.AddressRepository;
import com.microservice.customer.repository.CustomerRepository;
import com.microservice.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final CustomerMapper customerMapper;
    
    @Override
    @Transactional
    public CustomerDto createCustomer(CustomerDto customerDto) {
        // Check if customer with the same email already exists
        if (customerRepository.findByEmail(customerDto.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Customer", "email", customerDto.getEmail());
        }
        
        // Convert DTO to entity and save
        Customer customer = customerMapper.toEntity(customerDto);
        Customer savedCustomer = customerRepository.save(customer);
        
        // Process addresses if any
        List<Address> addresses = Collections.emptyList();
        if (customerDto.getAddresses() != null && !customerDto.getAddresses().isEmpty()) {
            addresses = customerDto.getAddresses().stream()
                    .map(addressDto -> customerMapper.toAddressEntity(addressDto, savedCustomer.getId()))
                    .collect(Collectors.toList());
            
            // Ensure only one address is primary
            boolean hasPrimary = addresses.stream().anyMatch(Address::isPrimary);
            if (!hasPrimary && !addresses.isEmpty()) {
                addresses.get(0).setPrimary(true);
            }
            
            addresses = addressRepository.saveAll(addresses);
        }
        
        return customerMapper.toDto(savedCustomer, addresses);
    }
    
    @Override
    public CustomerDto getCustomerById(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        
        List<Address> addresses = addressRepository.findByCustomerId(id);
        return customerMapper.toDto(customer, addresses);
    }
    
    @Override
    public CustomerDto getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        
        List<Address> addresses = addressRepository.findByCustomerId(customer.getId());
        return customerMapper.toDto(customer, addresses);
    }
    
    @Override
    public List<CustomerDto> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        
        return customers.stream()
                .map(customer -> {
                    List<Address> addresses = addressRepository.findByCustomerId(customer.getId());
                    return customerMapper.toDto(customer, addresses);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CustomerDto> searchCustomers(String firstName, String lastName) {
        List<Customer> customers;
        
        if (firstName != null && lastName != null) {
            customers = customerRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName);
        } else if (firstName != null) {
            customers = customerRepository.findByFirstNameContainingIgnoreCase(firstName);
        } else if (lastName != null) {
            customers = customerRepository.findByLastNameContainingIgnoreCase(lastName);
        } else {
            customers = customerRepository.findAll();
        }
        
        return customers.stream()
                .map(customer -> {
                    List<Address> addresses = addressRepository.findByCustomerId(customer.getId());
                    return customerMapper.toDto(customer, addresses);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public CustomerDto updateCustomer(String id, CustomerDto customerDto) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        
        // Check if email is being changed and if it's already in use
        if (!existingCustomer.getEmail().equals(customerDto.getEmail())) {
            Optional<Customer> customerWithEmail = customerRepository.findByEmail(customerDto.getEmail());
            if (customerWithEmail.isPresent() && !customerWithEmail.get().getId().equals(id)) {
                throw new DuplicateResourceException("Customer", "email", customerDto.getEmail());
            }
        }
        
        // Update customer fields
        Customer customerToUpdate = customerMapper.toEntity(customerDto);
        customerToUpdate.setId(id);
        customerToUpdate.setCreatedAt(existingCustomer.getCreatedAt());
        customerToUpdate.setUpdatedAt(LocalDateTime.now());
        
        Customer updatedCustomer = customerRepository.save(customerToUpdate);
        List<Address> addresses = addressRepository.findByCustomerId(id);
        
        return customerMapper.toDto(updatedCustomer, addresses);
    }
    
    @Override
    @Transactional
    public void deleteCustomer(String id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", "id", id);
        }
        
        // First delete all associated addresses
        addressRepository.deleteByCustomerId(id);
        
        // Then delete the customer
        customerRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public CustomerDto setCustomerStatus(String id, boolean active) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        
        customer.setActive(active);
        customer.setUpdatedAt(LocalDateTime.now());
        Customer updatedCustomer = customerRepository.save(customer);
        
        List<Address> addresses = addressRepository.findByCustomerId(id);
        return customerMapper.toDto(updatedCustomer, addresses);
    }
}