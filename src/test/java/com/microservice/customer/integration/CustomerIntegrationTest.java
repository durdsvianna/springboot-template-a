package com.microservice.customer.integration;

import com.microservice.customer.client.AddressApiClient;
import com.microservice.customer.client.CustomerApiClient;
import com.microservice.customer.dto.AddressDto;
import com.microservice.customer.dto.CustomerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "api.base-url=https://xyz.net/api/v1/enterprise"
})
class CustomerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private CustomerApiClient customerApiClient;

    @MockBean
    private AddressApiClient addressApiClient;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        baseUrl = "http://localhost:" + port + "/api/v1";
    }

    @Test
    void createCustomer_Success() {
        // Arrange
        CustomerDto customerDto = createSampleCustomerDto();
        CustomerDto createdCustomer = CustomerDto.builder()
                .id("generatedId123")
                .firstName(customerDto.getFirstName())
                .lastName(customerDto.getLastName())
                .email(customerDto.getEmail())
                .phoneNumber(customerDto.getPhoneNumber())
                .active(true)
                .build();
        
        when(customerApiClient.createCustomer(any(CustomerDto.class))).thenReturn(createdCustomer);

        // Act
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/customers",
                HttpMethod.POST,
                new HttpEntity<>(customerDto),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        
        Map<String, Object> customerData = (Map<String, Object>) response.getBody().get("data");
        assertNotNull(customerData);
        assertNotNull(customerData.get("id"));
        assertEquals(customerDto.getFirstName(), customerData.get("firstName"));
        assertEquals(customerDto.getEmail(), customerData.get("email"));
    }

    @Test
    void getCustomerById_Success() {
        // Arrange
        String customerId = "customerId123";
        CustomerDto customerDto = createSampleCustomerDto();
        customerDto.setId(customerId);
        
        when(customerApiClient.getCustomerById(customerId)).thenReturn(customerDto);

        // Act
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/customers/" + customerId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> customerData = (Map<String, Object>) response.getBody().get("data");
        assertEquals(customerId, customerData.get("id"));
        assertEquals(customerDto.getFirstName(), customerData.get("firstName"));
    }

    @Test
    void addAddressToCustomer_Success() {
        // Arrange
        String customerId = "customerId123";
        AddressDto addressDto = createSampleAddressDto();
        AddressDto createdAddress = createSampleAddressDto();
        createdAddress.setId("addressId123");
        
        when(addressApiClient.createAddress(anyString(), any(AddressDto.class))).thenReturn(createdAddress);

        // Act
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/customers/" + customerId + "/addresses",
                HttpMethod.POST,
                new HttpEntity<>(addressDto),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> addressData = (Map<String, Object>) response.getBody().get("data");
        assertNotNull(addressData.get("id"));
        assertEquals(addressDto.getStreet(), addressData.get("street"));
        assertEquals(addressDto.getCity(), addressData.get("city"));
        assertTrue((Boolean) addressData.get("primary"));
    }

    @Test
    void getAddressesByCustomerId_Success() {
        // Arrange
        String customerId = "customerId123";
        AddressDto addressDto = createSampleAddressDto();
        addressDto.setId("addressId123");
        List<AddressDto> addresses = Collections.singletonList(addressDto);
        
        when(addressApiClient.getAddressesByCustomerId(customerId)).thenReturn(addresses);

        // Act
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/customers/" + customerId + "/addresses",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<Map<String, Object>> addressesData = (List<Map<String, Object>>) response.getBody().get("data");
        assertNotNull(addressesData);
        assertFalse(addressesData.isEmpty());
        assertEquals(1, addressesData.size());
        assertEquals(addressDto.getStreet(), addressesData.get(0).get("street"));
    }

    private CustomerDto createSampleCustomerDto() {
        return CustomerDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe" + System.currentTimeMillis() + "@example.com")
                .phoneNumber("+1234567890")
                .addresses(new ArrayList<>())
                .active(true)
                .build();
    }

    private AddressDto createSampleAddressDto() {
        return AddressDto.builder()
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
}