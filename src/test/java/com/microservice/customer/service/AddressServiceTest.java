package com.microservice.customer.service;

import com.microservice.customer.dto.AddressDto;
import com.microservice.customer.exception.ResourceNotFoundException;
import com.microservice.customer.mapper.CustomerMapper;
import com.microservice.customer.model.Address;
import com.microservice.customer.repository.AddressRepository;
import com.microservice.customer.repository.CustomerRepository;
import com.microservice.customer.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private AddressDto addressDto;
    private Address address;
    private final String customerId = "customer123";
    private final String addressId = "address123";

    @BeforeEach
    void setUp() {
        addressDto = AddressDto.builder()
                .street("123 Main St")
                .number("10")
                .complement("Apt 4B")
                .neighborhood("Downtown")
                .city("New York")
                .state("NY")
                .country("USA")
                .zipCode("10001")
                .primary(true)
                .build();

        address = Address.builder()
                .id(addressId)
                .street("123 Main St")
                .number("10")
                .complement("Apt 4B")
                .neighborhood("Downtown")
                .city("New York")
                .state("NY")
                .country("USA")
                .zipCode("10001")
                .primary(true)
                .customerId(customerId)
                .build();
    }

    @Test
    void createAddress_Success() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(true);
        when(customerMapper.toAddressEntity(any(AddressDto.class), anyString())).thenReturn(address);
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(customerMapper.toAddressDto(any(Address.class))).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.createAddress(customerId, addressDto);

        // Assert
        assertNotNull(result);
        assertEquals(addressDto.getStreet(), result.getStreet());
        assertEquals(addressDto.getCity(), result.getCity());

        // Verify
        verify(customerRepository, times(1)).existsById(customerId);
        verify(addressRepository, times(1)).findByCustomerIdAndPrimaryTrue(customerId);
        verify(customerMapper, times(1)).toAddressEntity(addressDto, customerId);
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(customerMapper, times(1)).toAddressDto(address);
    }

    @Test
    void createAddress_CustomerNotFound_ThrowsException() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> addressService.createAddress(customerId, addressDto));

        // Verify
        verify(customerRepository, times(1)).existsById(customerId);
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void getAddressById_Success() {
        // Arrange
        when(addressRepository.findById(anyString())).thenReturn(Optional.of(address));
        when(customerMapper.toAddressDto(any(Address.class))).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.getAddressById(addressId);

        // Assert
        assertNotNull(result);
        assertEquals(addressDto.getStreet(), result.getStreet());
        assertEquals(addressDto.getCity(), result.getCity());

        // Verify
        verify(addressRepository, times(1)).findById(addressId);
        verify(customerMapper, times(1)).toAddressDto(address);
    }

    @Test
    void getAddressById_NotFound_ThrowsException() {
        // Arrange
        when(addressRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> addressService.getAddressById(addressId));

        // Verify
        verify(addressRepository, times(1)).findById(addressId);
        verify(customerMapper, never()).toAddressDto(any(Address.class));
    }

    @Test
    void getAddressesByCustomerId_Success() {
        // Arrange
        List<Address> addresses = Arrays.asList(address);
        when(customerRepository.existsById(anyString())).thenReturn(true);
        when(addressRepository.findByCustomerId(anyString())).thenReturn(addresses);
        when(customerMapper.toAddressDto(any(Address.class))).thenReturn(addressDto);

        // Act
        List<AddressDto> results = addressService.getAddressesByCustomerId(customerId);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(addressDto.getStreet(), results.get(0).getStreet());

        // Verify
        verify(customerRepository, times(1)).existsById(customerId);
        verify(addressRepository, times(1)).findByCustomerId(customerId);
        verify(customerMapper, times(1)).toAddressDto(address);
    }

    @Test
    void getAddressesByCustomerId_CustomerNotFound_ThrowsException() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> addressService.getAddressesByCustomerId(customerId));

        // Verify
        verify(customerRepository, times(1)).existsById(customerId);
        verify(addressRepository, never()).findByCustomerId(anyString());
    }

    @Test
    void deleteAddress_Success() {
        // Arrange
        when(addressRepository.existsById(anyString())).thenReturn(true);
        doNothing().when(addressRepository).deleteById(anyString());

        // Act
        addressService.deleteAddress(addressId);

        // Verify
        verify(addressRepository, times(1)).existsById(addressId);
        verify(addressRepository, times(1)).deleteById(addressId);
    }

    @Test
    void deleteAddress_NotFound_ThrowsException() {
        // Arrange
        when(addressRepository.existsById(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> addressService.deleteAddress(addressId));

        // Verify
        verify(addressRepository, times(1)).existsById(addressId);
        verify(addressRepository, never()).deleteById(anyString());
    }
}