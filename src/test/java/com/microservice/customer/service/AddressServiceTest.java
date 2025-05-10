package com.microservice.customer.service;

import com.microservice.customer.client.AddressApiClient;
import com.microservice.customer.dto.AddressDto;
import com.microservice.customer.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressApiClient addressApiClient;

    @InjectMocks
    private AddressServiceImpl addressService;

    private AddressDto addressDto;
    private final String customerId = "customer123";
    private final String addressId = "address123";

    @BeforeEach
    void setUp() {
        addressDto = AddressDto.builder()
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
                .build();
    }

    @Test
    void createAddress_Success() {
        // Arrange
        when(addressApiClient.createAddress(anyString(), any(AddressDto.class))).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.createAddress(customerId, addressDto);

        // Assert
        assertNotNull(result);
        assertEquals(addressDto.getStreet(), result.getStreet());
        assertEquals(addressDto.getCity(), result.getCity());

        // Verify
        verify(addressApiClient, times(1)).createAddress(customerId, addressDto);
    }

    @Test
    void getAddressById_Success() {
        // Arrange
        when(addressApiClient.getAddressById(anyString())).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.getAddressById(addressId);

        // Assert
        assertNotNull(result);
        assertEquals(addressDto.getStreet(), result.getStreet());
        assertEquals(addressDto.getCity(), result.getCity());

        // Verify
        verify(addressApiClient, times(1)).getAddressById(addressId);
    }

    @Test
    void getAddressesByCustomerId_Success() {
        // Arrange
        List<AddressDto> addresses = Arrays.asList(addressDto);
        when(addressApiClient.getAddressesByCustomerId(anyString())).thenReturn(addresses);

        // Act
        List<AddressDto> results = addressService.getAddressesByCustomerId(customerId);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(addressDto.getStreet(), results.get(0).getStreet());

        // Verify
        verify(addressApiClient, times(1)).getAddressesByCustomerId(customerId);
    }

    @Test
    void updateAddress_Success() {
        // Arrange
        when(addressApiClient.updateAddress(anyString(), any(AddressDto.class))).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.updateAddress(addressId, addressDto);

        // Assert
        assertNotNull(result);
        assertEquals(addressDto.getId(), result.getId());
        assertEquals(addressDto.getStreet(), result.getStreet());

        // Verify
        verify(addressApiClient, times(1)).updateAddress(addressId, addressDto);
    }

    @Test
    void deleteAddress_Success() {
        // Arrange
        doNothing().when(addressApiClient).deleteAddress(anyString());

        // Act
        addressService.deleteAddress(addressId);

        // Verify
        verify(addressApiClient, times(1)).deleteAddress(addressId);
    }

    @Test
    void setPrimaryAddress_Success() {
        // Arrange
        when(addressApiClient.setPrimaryAddress(anyString(), anyString())).thenReturn(addressDto);

        // Act
        AddressDto result = addressService.setPrimaryAddress(customerId, addressId);

        // Assert
        assertNotNull(result);
        assertEquals(addressDto.getId(), result.getId());
        assertTrue(result.isPrimary());

        // Verify
        verify(addressApiClient, times(1)).setPrimaryAddress(customerId, addressId);
    }

    @Test
    void searchAddressesByCity_Success() {
        // Arrange
        List<AddressDto> addresses = Arrays.asList(addressDto);
        when(addressApiClient.searchAddressesByCity(anyString())).thenReturn(addresses);

        // Act
        List<AddressDto> results = addressService.searchAddressesByCity("New York");

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("New York", results.get(0).getCity());

        // Verify
        verify(addressApiClient, times(1)).searchAddressesByCity("New York");
    }

    @Test
    void searchAddressesByState_Success() {
        // Arrange
        List<AddressDto> addresses = Arrays.asList(addressDto);
        when(addressApiClient.searchAddressesByState(anyString())).thenReturn(addresses);

        // Act
        List<AddressDto> results = addressService.searchAddressesByState("NY");

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("NY", results.get(0).getState());

        // Verify
        verify(addressApiClient, times(1)).searchAddressesByState("NY");
    }

    @Test
    void searchAddressesByZipCode_Success() {
        // Arrange
        List<AddressDto> addresses = Arrays.asList(addressDto);
        when(addressApiClient.searchAddressesByZipCode(anyString())).thenReturn(addresses);

        // Act
        List<AddressDto> results = addressService.searchAddressesByZipCode("10001");

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("10001", results.get(0).getZipCode());

        // Verify
        verify(addressApiClient, times(1)).searchAddressesByZipCode("10001");
    }
}