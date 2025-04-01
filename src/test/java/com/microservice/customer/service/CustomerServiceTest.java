package com.microservice.customer.service;

import com.microservice.customer.client.AddressApiClient;
import com.microservice.customer.client.CustomerApiClient;
import com.microservice.customer.dto.CustomerDto;
import com.microservice.customer.exception.DuplicateResourceException;
import com.microservice.customer.exception.ResourceNotFoundException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerApiClient customerApiClient;

    @Mock
    private AddressApiClient addressApiClient;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerDto customerDto;

    @BeforeEach
    void setUp() {
        customerDto = CustomerDto.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .addresses(new ArrayList<>())
                .active(true)
                .build();
    }

    @Test
    void createCustomer_Success() {
        // Arrange
        when(customerApiClient.createCustomer(any(CustomerDto.class))).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.createCustomer(customerDto);

        // Assert
        assertNotNull(result);
        assertEquals(customerDto.getFirstName(), result.getFirstName());
        assertEquals(customerDto.getEmail(), result.getEmail());

        // Verify
        verify(customerApiClient, times(1)).createCustomer(customerDto);
    }

    @Test
    void getCustomerById_Success() {
        // Arrange
        when(customerApiClient.getCustomerById(anyString())).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.getCustomerById("1");

        // Assert
        assertNotNull(result);
        assertEquals(customerDto.getFirstName(), result.getFirstName());
        assertEquals(customerDto.getEmail(), result.getEmail());

        // Verify
        verify(customerApiClient, times(1)).getCustomerById("1");
    }

    @Test
    void getCustomerByEmail_Success() {
        // Arrange
        when(customerApiClient.getCustomerByEmail(anyString())).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.getCustomerByEmail("john.doe@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(customerDto.getFirstName(), result.getFirstName());
        assertEquals(customerDto.getId(), result.getId());

        // Verify
        verify(customerApiClient, times(1)).getCustomerByEmail("john.doe@example.com");
    }

    @Test
    void getAllCustomers_Success() {
        // Arrange
        List<CustomerDto> customers = Collections.singletonList(customerDto);
        when(customerApiClient.getAllCustomers()).thenReturn(customers);

        // Act
        List<CustomerDto> result = customerService.getAllCustomers();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(customerDto.getId(), result.get(0).getId());

        // Verify
        verify(customerApiClient, times(1)).getAllCustomers();
    }

    @Test
    void searchCustomers_Success() {
        // Arrange
        List<CustomerDto> customers = Collections.singletonList(customerDto);
        when(customerApiClient.searchCustomers(anyString(), anyString())).thenReturn(customers);

        // Act
        List<CustomerDto> result = customerService.searchCustomers("John", "Doe");

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(customerDto.getFirstName(), result.get(0).getFirstName());

        // Verify
        verify(customerApiClient, times(1)).searchCustomers("John", "Doe");
    }

    @Test
    void updateCustomer_Success() {
        // Arrange
        when(customerApiClient.updateCustomer(anyString(), any(CustomerDto.class))).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.updateCustomer("1", customerDto);

        // Assert
        assertNotNull(result);
        assertEquals(customerDto.getId(), result.getId());
        assertEquals(customerDto.getEmail(), result.getEmail());

        // Verify
        verify(customerApiClient, times(1)).updateCustomer("1", customerDto);
    }

    @Test
    void deleteCustomer_Success() {
        // Arrange
        doNothing().when(customerApiClient).deleteCustomer(anyString());

        // Act
        customerService.deleteCustomer("1");

        // Verify
        verify(customerApiClient, times(1)).deleteCustomer("1");
    }

    @Test
    void setCustomerStatus_Success() {
        // Arrange
        when(customerApiClient.setCustomerStatus(anyString(), anyBoolean())).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.setCustomerStatus("1", true);

        // Assert
        assertNotNull(result);
        assertEquals(customerDto.getId(), result.getId());
        assertTrue(result.isActive());

        // Verify
        verify(customerApiClient, times(1)).setCustomerStatus("1", true);
    }
}