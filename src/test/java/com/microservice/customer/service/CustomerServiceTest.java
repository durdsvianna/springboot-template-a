package com.microservice.customer.service;

import com.microservice.customer.dto.CustomerDto;
import com.microservice.customer.exception.DuplicateResourceException;
import com.microservice.customer.exception.ResourceNotFoundException;
import com.microservice.customer.mapper.CustomerMapper;
import com.microservice.customer.model.Customer;
import com.microservice.customer.repository.AddressRepository;
import com.microservice.customer.repository.CustomerRepository;
import com.microservice.customer.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerDto customerDto;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customerDto = CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .addresses(new ArrayList<>())
                .active(true)
                .build();

        customer = Customer.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .active(true)
                .build();
    }

    @Test
    void createCustomer_Success() {
        // Arrange
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerMapper.toEntity(any(CustomerDto.class))).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.toDto(any(Customer.class), anyList())).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.createCustomer(customerDto);

        // Assert
        assertNotNull(result);
        assertEquals(customerDto.getFirstName(), result.getFirstName());
        assertEquals(customerDto.getEmail(), result.getEmail());

        // Verify
        verify(customerRepository, times(1)).findByEmail(customerDto.getEmail());
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(customerMapper, times(1)).toEntity(customerDto);
        verify(customerMapper, times(1)).toDto(any(Customer.class), anyList());
    }

    @Test
    void createCustomer_DuplicateEmail_ThrowsException() {
        // Arrange
        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> customerService.createCustomer(customerDto));

        // Verify
        verify(customerRepository, times(1)).findByEmail(customerDto.getEmail());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerById_Success() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.of(customer));
        when(addressRepository.findByCustomerId(anyString())).thenReturn(Collections.emptyList());
        when(customerMapper.toDto(any(Customer.class), anyList())).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.getCustomerById("1");

        // Assert
        assertNotNull(result);
        assertEquals(customerDto.getFirstName(), result.getFirstName());
        assertEquals(customerDto.getEmail(), result.getEmail());

        // Verify
        verify(customerRepository, times(1)).findById("1");
        verify(addressRepository, times(1)).findByCustomerId("1");
        verify(customerMapper, times(1)).toDto(customer, Collections.emptyList());
    }

    @Test
    void getCustomerById_NotFound_ThrowsException() {
        // Arrange
        when(customerRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> customerService.getCustomerById("1"));

        // Verify
        verify(customerRepository, times(1)).findById("1");
        verify(addressRepository, never()).findByCustomerId(anyString());
    }

    @Test
    void deleteCustomer_Success() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(true);
        doNothing().when(addressRepository).deleteByCustomerId(anyString());
        doNothing().when(customerRepository).deleteById(anyString());

        // Act
        customerService.deleteCustomer("1");

        // Verify
        verify(customerRepository, times(1)).existsById("1");
        verify(addressRepository, times(1)).deleteByCustomerId("1");
        verify(customerRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteCustomer_NotFound_ThrowsException() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> customerService.deleteCustomer("1"));

        // Verify
        verify(customerRepository, times(1)).existsById("1");
        verify(addressRepository, never()).deleteByCustomerId(anyString());
        verify(customerRepository, never()).deleteById(anyString());
    }
}