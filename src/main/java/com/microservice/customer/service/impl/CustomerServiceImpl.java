package com.microservice.customer.service.impl;

import com.microservice.customer.client.AddressApiClient;
import com.microservice.customer.client.CustomerApiClient;
import com.microservice.customer.dto.CustomerDto;
import com.microservice.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    
    private final CustomerApiClient customerApiClient;
    private final AddressApiClient addressApiClient;
    
    @Override
    public CustomerDto createCustomer(CustomerDto customerDto) {
        return customerApiClient.createCustomer(customerDto);
    }
    
    @Override
    public CustomerDto getCustomerById(String id) {
        return customerApiClient.getCustomerById(id);
    }
    
    @Override
    public CustomerDto getCustomerByEmail(String email) {
        return customerApiClient.getCustomerByEmail(email);
    }
    
    @Override
    public List<CustomerDto> getAllCustomers() {
        return customerApiClient.getAllCustomers();
    }
    
    @Override
    public List<CustomerDto> searchCustomers(String firstName, String lastName) {
        return customerApiClient.searchCustomers(firstName, lastName);
    }
    
    @Override
    public CustomerDto updateCustomer(String id, CustomerDto customerDto) {
        return customerApiClient.updateCustomer(id, customerDto);
    }
    
    @Override
    public void deleteCustomer(String id) {
        customerApiClient.deleteCustomer(id);
    }
    
    @Override
    public CustomerDto setCustomerStatus(String id, boolean active) {
        return customerApiClient.setCustomerStatus(id, active);
    }
}