package com.microservice.customer.cucumber;

import com.microservice.customer.client.CustomerApiClient;
import com.microservice.customer.client.AddressApiClient;
import com.microservice.customer.dto.CustomerDto;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class CustomerStepDefinitions {
    
    @LocalServerPort
    private int port;
    
    @MockBean
    private CustomerApiClient customerApiClient;
    
    @MockBean
    private AddressApiClient addressApiClient;
    
    private Response response;
    private CustomerDto customer;
    
    @After
    public void cleanup() {
        // Reset mocks instead of clearing database
        Mockito.reset(customerApiClient, addressApiClient);
    }
    
    @Given("the API is available")
    public void theAPIIsAvailable() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
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
        
        // Mock the customerApiClient to return this customer
        when(customerApiClient.getCustomerById(anyString())).thenReturn(customerDto);
        when(customerApiClient.getCustomerByEmail(anyString())).thenReturn(customerDto);
        
        this.customer = customerDto;
        assertNotNull(this.customer.getId());
    }
    
    @When("I create a customer with the following details:")
    public void iCreateACustomerWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> customerData = rows.get(0);
        
        CustomerDto customerDto = CustomerDto.builder()
                .firstName(customerData.get("firstName"))
                .lastName(customerData.get("lastName"))
                .email(customerData.get("email"))
                .phoneNumber(customerData.get("phoneNumber"))
                .addresses(new ArrayList<>())
                .active(true)
                .build();
        
        RequestSpecification request = given()
                .contentType(ContentType.JSON)
                .body(customerDto);
        
        response = request.when().post("/api/v1/customers");
    }
    
    @Then("the customer should be created successfully")
    public void theCustomerShouldBeCreatedSuccessfully() {
        response.then()
                .statusCode(201)
                .body("status", equalTo("SUCCESS"))
                .body("data", notNullValue())
                .body("data.id", notNullValue());
    }
    
    @Then("the response should contain the customer details")
    public void theResponseShouldContainTheCustomerDetails() {
        response.then()
                .body("data.firstName", equalTo(customer.getFirstName()))
                .body("data.lastName", equalTo(customer.getLastName()))
                .body("data.email", equalTo(customer.getEmail()))
                .body("data.phoneNumber", equalTo(customer.getPhoneNumber()));
    }
    
    @When("I request the customer by ID")
    public void iRequestTheCustomerByID() {
        response = given()
                .when()
                .get("/api/v1/customers/" + customer.getId());
    }
    
    @Then("the response should contain the correct customer details")
    public void theResponseShouldContainTheCorrectCustomerDetails() {
        response.then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data.id", equalTo(customer.getId()))
                .body("data.firstName", equalTo(customer.getFirstName()))
                .body("data.lastName", equalTo(customer.getLastName()))
                .body("data.email", equalTo(customer.getEmail()));
    }
    
    @When("I update the customer with the following details:")
    public void iUpdateTheCustomerWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> customerData = rows.get(0);
        
        CustomerDto customerDto = CustomerDto.builder()
                .id(customer.getId())
                .firstName(customerData.get("firstName"))
                .lastName(customerData.get("lastName"))
                .email(customerData.get("email"))
                .phoneNumber(customerData.get("phoneNumber"))
                .addresses(new ArrayList<>())
                .active(true)
                .build();
        
        response = given()
                .contentType(ContentType.JSON)
                .body(customerDto)
                .when()
                .put("/api/v1/customers/" + customer.getId());
    }
    
    @Then("the customer should be updated successfully")
    public void theCustomerShouldBeUpdatedSuccessfully() {
        response.then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data", notNullValue())
                .body("data.id", equalTo(customer.getId()));
    }
    
    @Then("the response should contain the updated details")
    public void theResponseShouldContainTheUpdatedDetails() {
        response.then()
                .body("data.firstName", equalTo(customer.getFirstName()))
                .body("data.lastName", equalTo(customer.getLastName()))
                .body("data.email", equalTo(customer.getEmail()));
    }
    
    @When("I delete the customer")
    public void iDeleteTheCustomer() {
        response = given()
                .when()
                .delete("/api/v1/customers/" + customer.getId());
    }
    
    @Then("the customer should be deleted successfully")
    public void theCustomerShouldBeDeletedSuccessfully() {
        response.then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"));
        
        // Verify the customer no longer exists in the database
        when(customerApiClient.getCustomerById(customer.getId())).thenReturn(null);
        assertNull(customerApiClient.getCustomerById(customer.getId()));
    }
}