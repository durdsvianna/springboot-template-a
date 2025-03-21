package com.microservice.customer.cucumber;

import com.microservice.customer.dto.AddressDto;
import com.microservice.customer.model.Address;
import com.microservice.customer.model.Customer;
import com.microservice.customer.repository.AddressRepository;
import com.microservice.customer.repository.CustomerRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class AddressStepDefinitions {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    private Customer customer;
    private Address address;
    private List<Address> addresses = new ArrayList<>();
    private Response response;
    
    @Given("a customer exists with multiple addresses")
    public void aCustomerExistsWithMultipleAddresses() {
        customer = Customer.builder()
                .firstName("Multi")
                .lastName("Address")
                .email("multi.address@example.com")
                .phoneNumber("+1234567890")
                .active(true)
                .build();
        customer = customerRepository.save(customer);
        
        // Create primary address
        Address primary = Address.builder()
                .street("123 Main St")
                .number("1")
                .neighborhood("Downtown")
                .city("New York")
                .state("NY")
                .country("USA")
                .zipCode("10001")
                .primary(true)
                .customerId(customer.getId())
                .build();
        primary = addressRepository.save(primary);
        addresses.add(primary);
        
        // Create secondary address
        Address secondary = Address.builder()
                .street("456 Oak Ave")
                .number("2")
                .neighborhood("Suburbia")
                .city("Los Angeles")
                .state("CA")
                .country("USA")
                .zipCode("90001")
                .primary(false)
                .customerId(customer.getId())
                .build();
        secondary = addressRepository.save(secondary);
        addresses.add(secondary);
        
        assertEquals(2, addressRepository.findByCustomerId(customer.getId()).size());
    }
    
    @When("I set a specific address as primary")
    public void iSetASpecificAddressAsPrimary() {
        // Use the second (non-primary) address
        Address nonPrimary = addresses.get(1);
        
        response = given()
                .contentType(ContentType.JSON)
                .when()
                .patch("/api/v1/customers/" + customer.getId() + "/addresses/" + nonPrimary.getId() + "/primary");
    }
    
    @Then("that address should be marked as primary")
    public void thatAddressShouldBeMarkedAsPrimary() {
        response.then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data.primary", equalTo(true));
        
        // Verify in database
        Address updatedAddress = addressRepository.findById(addresses.get(1).getId()).orElseThrow();
        assertTrue(updatedAddress.isPrimary());
    }
    
    @Then("any previously primary address should be marked as non-primary")
    public void anyPreviouslyPrimaryAddressShouldBeMarkedAsNonPrimary() {
        // Verify in database
        Address previousPrimary = addressRepository.findById(addresses.get(0).getId()).orElseThrow();
        assertFalse(previousPrimary.isPrimary());
    }
    
    @Given("a customer exists with at least one address")
    public void aCustomerExistsWithAtLeastOneAddress() {
        customer = Customer.builder()
                .firstName("Has")
                .lastName("Address")
                .email("has.address@example.com")
                .phoneNumber("+0987654321")
                .active(true)
                .build();
        customer = customerRepository.save(customer);
        
        address = Address.builder()
                .street("789 Pine Blvd")
                .number("3")
                .neighborhood("Uptown")
                .city("Chicago")
                .state("IL")
                .country("USA")
                .zipCode("60007")
                .primary(true)
                .customerId(customer.getId())
                .build();
        address = addressRepository.save(address);
        
        assertNotNull(address.getId());
    }
    
    @When("I delete the address")
    public void iDeleteTheAddress() {
        response = given()
                .when()
                .delete("/api/v1/addresses/" + address.getId());
    }
    
    @Then("the address should be removed from the customer's profile")
    public void theAddressShouldBeRemovedFromTheCustomersProfile() {
        response.then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"));
        
        // Verify address no longer exists
        Optional<Address> deletedAddress = addressRepository.findById(address.getId());
        assertTrue(deletedAddress.isEmpty());
    }
    
    @Given("multiple addresses exist in different cities")
    public void multipleAddressesExistInDifferentCities() {
        // Create a customer
        customer = Customer.builder()
                .firstName("City")
                .lastName("Searcher")
                .email("city.searcher@example.com")
                .phoneNumber("+1122334455")
                .active(true)
                .build();
        customer = customerRepository.save(customer);
        
        // Create NY address
        Address nyAddress = Address.builder()
                .street("123 Broadway")
                .number("1")
                .neighborhood("Manhattan")
                .city("New York")
                .state("NY")
                .country("USA")
                .zipCode("10001")
                .primary(true)
                .customerId(customer.getId())
                .build();
        addressRepository.save(nyAddress);
        
        // Create Chicago address
        Address chicagoAddress = Address.builder()
                .street("456 Michigan Ave")
                .number("2")
                .neighborhood("Loop")
                .city("Chicago")
                .state("IL")
                .country("USA")
                .zipCode("60601")
                .primary(false)
                .customerId(customer.getId())
                .build();
        addressRepository.save(chicagoAddress);
    }
    
    @When("I search for addresses in {string}")
    public void iSearchForAddressesInCity(String city) {
        response = given()
                .when()
                .get("/api/v1/addresses/search/city/" + city);
    }
    
    @Then("the response should contain only addresses in {string}")
    public void theResponseShouldContainOnlyAddressesInCity(String city) {
        response.then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data", notNullValue())
                .body("data.size()", greaterThan(0));
        
        // Verify all returned addresses are in the specified city
        List<Map<String, Object>> addresses = response.jsonPath().getList("data");
        for (Map<String, Object> address : addresses) {
            assertEquals(city, address.get("city"));
        }
    }
    
    @Given("multiple addresses exist in different states")
    public void multipleAddressesExistInDifferentStates() {
        // Create a customer
        customer = Customer.builder()
                .firstName("State")
                .lastName("Searcher")
                .email("state.searcher@example.com")
                .phoneNumber("+5566778899")
                .active(true)
                .build();
        customer = customerRepository.save(customer);
        
        // Create CA address
        Address caAddress = Address.builder()
                .street("123 Hollywood Blvd")
                .number("1")
                .neighborhood("Hollywood")
                .city("Los Angeles")
                .state("CA")
                .country("USA")
                .zipCode("90028")
                .primary(true)
                .customerId(customer.getId())
                .build();
        addressRepository.save(caAddress);
        
        // Create TX address
        Address txAddress = Address.builder()
                .street("456 Congress Ave")
                .number("2")
                .neighborhood("Downtown")
                .city("Austin")
                .state("TX")
                .country("USA")
                .zipCode("78701")
                .primary(false)
                .customerId(customer.getId())
                .build();
        addressRepository.save(txAddress);
    }
    
    @When("I search for addresses in state {string}")
    public void iSearchForAddressesInState(String state) {
        response = given()
                .when()
                .get("/api/v1/addresses/search/state/" + state);
    }
    
    @Then("the response should contain only addresses in state {string}")
    public void theResponseShouldContainOnlyAddressesInState(String state) {
        response.then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data", notNullValue())
                .body("data.size()", greaterThan(0));
        
        // Verify all returned addresses are in the specified state
        List<Map<String, Object>> addresses = response.jsonPath().getList("data");
        for (Map<String, Object> address : addresses) {
            assertEquals(state, address.get("state"));
        }
    }
    
    @Given("multiple addresses exist with different ZIP codes")
    public void multipleAddressesExistWithDifferentZIPCodes() {
        // Create a customer
        customer = Customer.builder()
                .firstName("Zip")
                .lastName("Searcher")
                .email("zip.searcher@example.com")
                .phoneNumber("+9988776655")
                .active(true)
                .build();
        customer = customerRepository.save(customer);
        
        // Create address with zipcode 10001
        Address zip10001 = Address.builder()
                .street("123 5th Ave")
                .number("1")
                .neighborhood("Flatiron")
                .city("New York")
                .state("NY")
                .country("USA")
                .zipCode("10001")
                .primary(true)
                .customerId(customer.getId())
                .build();
        addressRepository.save(zip10001);
        
        // Create address with zipcode 90210
        Address zip90210 = Address.builder()
                .street("456 Rodeo Dr")
                .number("2")
                .neighborhood("Beverly Hills")
                .city("Los Angeles")
                .state("CA")
                .country("USA")
                .zipCode("90210")
                .primary(false)
                .customerId(customer.getId())
                .build();
        addressRepository.save(zip90210);
    }
    
    @When("I search for addresses with ZIP code {string}")
    public void iSearchForAddressesWithZIPCode(String zipCode) {
        response = given()
                .when()
                .get("/api/v1/addresses/search/zipcode/" + zipCode);
    }
    
    @Then("the response should contain only addresses with ZIP code {string}")
    public void theResponseShouldContainOnlyAddressesWithZIPCode(String zipCode) {
        response.then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data", notNullValue())
                .body("data.size()", greaterThan(0));
        
        // Verify all returned addresses have the specified ZIP code
        List<Map<String, Object>> addresses = response.jsonPath().getList("data");
        for (Map<String, Object> address : addresses) {
            assertEquals(zipCode, address.get("zipCode"));
        }
    }
    
    @When("I add an address with the following details:")
    public void iAddAnAddressWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> addressData = rows.get(0);
        
        AddressDto addressDto = AddressDto.builder()
                .street(addressData.get("street"))
                .number(addressData.get("number"))
                .neighborhood(addressData.get("neighborhood"))
                .city(addressData.get("city"))
                .state(addressData.get("state"))
                .country(addressData.get("country"))
                .zipCode(addressData.get("zipCode"))
                .primary(true)
                .build();
        
        response = given()
                .contentType(ContentType.JSON)
                .body(addressDto)
                .when()
                .post("/api/v1/customers/" + customer.getId() + "/addresses");
    }
    
    @Then("the address should be added successfully")
    public void theAddressShouldBeAddedSuccessfully() {
        response.then()
                .statusCode(201)
                .body("status", equalTo("SUCCESS"))
                .body("data", notNullValue())
                .body("data.id", notNullValue());
    }
    
    @Then("the customer should have the address in their profile")
    public void theCustomerShouldHaveTheAddressInTheirProfile() {
        // Extract address ID from response
        String addressId = response.jsonPath().getString("data.id");
        
        // Verify address exists in database with correct customer ID
        Optional<Address> savedAddress = addressRepository.findById(addressId);
        assertTrue(savedAddress.isPresent());
        assertEquals(customer.getId(), savedAddress.get().getCustomerId());
    }
}