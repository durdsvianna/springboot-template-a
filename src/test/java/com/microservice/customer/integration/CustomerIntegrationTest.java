package com.microservice.customer.integration;

import com.microservice.customer.dto.AddressDto;
import com.microservice.customer.dto.CustomerDto;
import com.microservice.customer.repository.AddressRepository;
import com.microservice.customer.repository.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.5");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    private String baseUrl;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
    }

    @AfterEach
    void tearDown() {
        addressRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void createCustomer_Success() {
        // Arrange
        CustomerDto customerDto = createSampleCustomerDto();

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
        // Arrange - Create a customer first
        CustomerDto customerDto = createSampleCustomerDto();
        ResponseEntity<Map<String, Object>> createResponse = restTemplate.exchange(
                baseUrl + "/customers",
                HttpMethod.POST,
                new HttpEntity<>(customerDto),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        Map<String, Object> createdCustomer = (Map<String, Object>) createResponse.getBody().get("data");
        String customerId = (String) createdCustomer.get("id");

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
        // Arrange - Create a customer first
        CustomerDto customerDto = createSampleCustomerDto();
        ResponseEntity<Map<String, Object>> createCustomerResponse = restTemplate.exchange(
                baseUrl + "/customers",
                HttpMethod.POST,
                new HttpEntity<>(customerDto),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        Map<String, Object> createdCustomer = (Map<String, Object>) createCustomerResponse.getBody().get("data");
        String customerId = (String) createdCustomer.get("id");
        
        AddressDto addressDto = createSampleAddressDto();

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
        // Arrange - Create a customer with an address
        CustomerDto customerDto = createSampleCustomerDto();
        ResponseEntity<Map<String, Object>> createCustomerResponse = restTemplate.exchange(
                baseUrl + "/customers",
                HttpMethod.POST,
                new HttpEntity<>(customerDto),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        Map<String, Object> createdCustomer = (Map<String, Object>) createCustomerResponse.getBody().get("data");
        String customerId = (String) createdCustomer.get("id");
        
        AddressDto addressDto = createSampleAddressDto();
        restTemplate.exchange(
                baseUrl + "/customers/" + customerId + "/addresses",
                HttpMethod.POST,
                new HttpEntity<>(addressDto),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

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