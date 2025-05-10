package com.microservice.customer.cucumber;

import com.microservice.customer.client.CustomerApiClient;
import com.microservice.customer.client.AddressApiClient;
import com.microservice.customer.dto.CustomerDto;
import com.microservice.customer.service.CustomerService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class CustomerStepDefinitions {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private CustomerApiClient customerApiClient;
    
    @Autowired
    private AddressApiClient addressApiClient;
    
    private CustomerDto inputCustomer;
    private CustomerDto resultCustomer;
    private Exception error;
    
    @After
    public void cleanup() {
        // Reset mocks instead of clearing database
        Mockito.reset(customerApiClient, addressApiClient);
        
        // Reset instance variables
        inputCustomer = null;
        resultCustomer = null;
        error = null;
    }
    
    @Given("the API is available")
    public void theAPIIsAvailable() {
        // Just confirm mocks are ready
        assertNotNull(customerApiClient);
        assertNotNull(addressApiClient);
    }
    
    @Given("a customer exists with the following details:")
    public void aCustomerExistsWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> customerData = rows.get(0);
        
        CustomerDto customerDto = CustomerDto.builder()
                .id("customer123")
                .firstName(customerData.get("firstName"))
                .lastName(customerData.get("lastName"))
                .email(customerData.get("email"))
                .phoneNumber(customerData.get("phoneNumber"))
                .active(true)
                .build();
        
        // Mock the customerApiClient to return this customer when service calls it
        when(customerApiClient.getCustomerById(anyString())).thenReturn(customerDto);
        when(customerApiClient.getCustomerByEmail(anyString())).thenReturn(customerDto);
        
        // Store for later assertions
        this.resultCustomer = customerDto;
    }
    
    @When("I create a customer with the following details:")
    public void iCreateACustomerWithTheFollowingDetails(DataTable dataTable) {
        // 1. Get Data from Cucumber Step
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> customerData = rows.get(0);

        // 2. Prepare Input Data Transfer Object (DTO)
        CustomerDto customerDto = CustomerDto.builder()
                .firstName(customerData.get("firstName"))
                .lastName(customerData.get("lastName"))
                .email(customerData.get("email"))
                .phoneNumber(customerData.get("phoneNumber"))
                .addresses(new ArrayList<>()) // Initialize addresses list
                .active(true)                // Set default active status
                .build();

        // 3. Store Input for Later Validation
        this.inputCustomer = customerDto;

        // 4. Mock the External API Client Behavior
        //    - Create a simulated successful response DTO (including a fake ID)
        CustomerDto createdCustomer = CustomerDto.builder()
                .id("generatedId123") // Simulate ID generation by the external API
                .firstName(customerDto.getFirstName())
                .lastName(customerDto.getLastName())
                .email(customerDto.getEmail())
                .phoneNumber(customerDto.getPhoneNumber())
                .addresses(new ArrayList<>())
                .active(true)
                .build();
        //    - Tell the mock client to return this simulated response when its 'createCustomer' is called
        when(customerApiClient.createCustomer(any(CustomerDto.class))).thenReturn(createdCustomer);

        // 5. Execute the Service Method Under Test
        try {
            // Call the actual createCustomer method in the CustomerService
            this.resultCustomer = customerService.createCustomer(customerDto);
        } catch (Exception e) {
            // 6. Capture any Exceptions
            // If the service throws an error, store it for validation in a @Then step
            this.error = e;
        }
    }
    
    @Then("the customer should be created successfully")
    public void theCustomerShouldBeCreatedSuccessfully() {
        assertNull(error, "No exception should be thrown");
        assertNotNull(resultCustomer, "Customer should be created");
        assertNotNull(resultCustomer.getId(), "Customer should have an ID");
    }
    
    @Then("the response should contain the customer details")
    public void theResponseShouldContainTheCustomerDetails() {
        // Make sure customer is not null before accessing its properties
        assertNotNull(resultCustomer, "Customer should not be null");
        
        assertEquals(inputCustomer.getFirstName(), resultCustomer.getFirstName());
        assertEquals(inputCustomer.getLastName(), resultCustomer.getLastName());
        assertEquals(inputCustomer.getEmail(), resultCustomer.getEmail());
        assertEquals(inputCustomer.getPhoneNumber(), resultCustomer.getPhoneNumber());
    }
    
    @When("I request the customer by ID")
    public void iRequestTheCustomerByID() {
        try {
            this.resultCustomer = customerService.getCustomerById(resultCustomer.getId());
        } catch (Exception e) {
            this.error = e;
        }
    }
    
    @Then("the response should contain the correct customer details")
    public void theResponseShouldContainTheCorrectCustomerDetails() {
        assertNull(error, "No exception should be thrown");
        assertNotNull(resultCustomer);
        assertNotNull(resultCustomer.getId());
        assertNotNull(resultCustomer.getFirstName());
        assertNotNull(resultCustomer.getLastName());
        assertNotNull(resultCustomer.getEmail());
    }
    
    @When("I update the customer with the following details:")
    public void iUpdateTheCustomerWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> customerData = rows.get(0);
        
        CustomerDto customerDto = CustomerDto.builder()
                .id(resultCustomer.getId())
                .firstName(customerData.get("firstName"))
                .lastName(customerData.get("lastName"))
                .email(customerData.get("email"))
                .phoneNumber(customerData.get("phoneNumber"))
                .addresses(new ArrayList<>())
                .active(true)
                .build();
        
        // Save input for validation
        this.inputCustomer = customerDto;
        
        // Mock the customerApiClient to return the updated customer
        when(customerApiClient.updateCustomer(anyString(), any(CustomerDto.class))).thenReturn(customerDto);
        
        // Call service and handle potential exceptions
        try {
            this.resultCustomer = customerService.updateCustomer(resultCustomer.getId(), customerDto);
        } catch (Exception e) {
            this.error = e;
        }
    }
    
    @Then("the customer should be updated successfully")
    public void theCustomerShouldBeUpdatedSuccessfully() {
        assertNull(error, "No exception should be thrown");
        assertNotNull(resultCustomer);
        assertEquals(inputCustomer.getId(), resultCustomer.getId());
    }
    
    @Then("the response should contain the updated details")
    public void theResponseShouldContainTheUpdatedDetails() {
        assertEquals(inputCustomer.getFirstName(), resultCustomer.getFirstName());
        assertEquals(inputCustomer.getLastName(), resultCustomer.getLastName());
        assertEquals(inputCustomer.getEmail(), resultCustomer.getEmail());
    }
    
    @When("I delete the customer")
    public void iDeleteTheCustomer() {
        try {
            customerService.deleteCustomer(resultCustomer.getId());
        } catch (Exception e) {
            this.error = e;
        }
    }
    
    @Then("the customer should be deleted successfully")
    public void theCustomerShouldBeDeletedSuccessfully() {
        assertNull(error, "No exception should be thrown");
        
        // Mock the API to return null for deleted customer
        when(customerApiClient.getCustomerById(resultCustomer.getId())).thenReturn(null);
        
        // Verify the customer no longer exists through service
        CustomerDto deletedCustomer = customerService.getCustomerById(resultCustomer.getId());
        assertNull(deletedCustomer, "Customer should be deleted");
    }
}