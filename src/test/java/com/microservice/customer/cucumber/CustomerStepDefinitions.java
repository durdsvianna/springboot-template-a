package com.microservice.customer.cucumber;

import com.microservice.customer.dto.CustomerDto;
import com.microservice.customer.model.Address;
import com.microservice.customer.model.Customer;
import com.microservice.customer.repository.AddressRepository;
import com.microservice.customer.repository.CustomerRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class CustomerStepDefinitions {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    private Response response;
    private Customer customer;
    private CustomerDto customerDto;
    
    @After
    public void cleanup() {
        addressRepository.deleteAll();
        customerRepository.deleteAll();
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
        
        Customer customer = Customer.builder()
                .firstName(customerData.get("firstName"))
                .lastName(customerData.get("lastName"))
                .email(customerData.get("email"))
                .phoneNumber(customerData.get("phoneNumber"))
                .active(true)
                .build();
        
        this.customer = customerRepository.save(customer);
        assertNotNull(this.customer.getId());
    }
    
    @When("I create a customer with the following details:")
    public void iCreateACustomerWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> customerData = rows.get(0);
        
        customerDto = CustomerDto.builder()
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
                .body("data.firstName", equalTo(customerDto.getFirstName()))
                .body("data.lastName", equalTo(customerDto.getLastName()))
                .body("data.email", equalTo(customerDto.getEmail()))
                .body("data.phoneNumber", equalTo(customerDto.getPhoneNumber()));
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
        
        customerDto = CustomerDto.builder()
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
                .body("data.firstName", equalTo(customerDto.getFirstName()))
                .body("data.lastName", equalTo(customerDto.getLastName()))
                .body("data.email", equalTo(customerDto.getEmail()));
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
        assertFalse(customerRepository.existsById(customer.getId()));
    }
}