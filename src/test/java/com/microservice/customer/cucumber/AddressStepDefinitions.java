package com.microservice.customer.cucumber;

import com.microservice.customer.client.CustomerApiClient;
import com.microservice.customer.client.AddressApiClient;
import com.microservice.customer.dto.AddressDto;
import com.microservice.customer.dto.CustomerDto;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
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

public class AddressStepDefinitions {
    
    @LocalServerPort
    private int port;
    
    @MockBean
    private CustomerApiClient customerApiClient;
    
    @MockBean
    private AddressApiClient addressApiClient;
    
    private CustomerDto customer;
    private AddressDto address;
    private List<AddressDto> addresses = new ArrayList<>();
    private Response response;
    
    @Given("a customer exists with multiple addresses")
    public void aCustomerExistsWithMultipleAddresses() {
        // Initialize customer
        customer = CustomerDto.builder()
                .id("customer123")
                .firstName("Multi")
                .lastName("Address")
                .email("multi.address@example.com")
                .phoneNumber("+1234567890")
                .active(true)
                .build();
        
        // Create primary address
        AddressDto primary = AddressDto.builder()
                .id("address1")
                .street("123 Main St")
                .number("1")
                .neighborhood("Downtown")
                .city("New York")
                .state("NY")
                .country("USA")
                .zipCode("10001")
                .primary(true)
                .build();
        
        // Create secondary address
        AddressDto secondary = AddressDto.builder()
                .id("address2")
                .street("456 Oak Ave")
                .number("2")
                .neighborhood("Suburbia")
                .city("Los Angeles")
                .state("CA")
                .country("USA")
                .zipCode("90001")
                .primary(false)
                .build();
        
        // Add addresses to list
        addresses.add(primary);
        addresses.add(secondary);
        
        // Setup customer with addresses
        customer.setAddresses(addresses);
        
        // Mock API responses
        when(customerApiClient.getCustomerById(customer.getId())).thenReturn(customer);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
        when(addressApiClient.getAddressById("address1")).thenReturn(primary);
        when(addressApiClient.getAddressById("address2")).thenReturn(secondary);
    }
    
    @When("I add an address with the following details:")
    public void iAddAnAddressWithTheFollowingDetails(DataTable dataTable) {
        // Setup RestAssured
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        
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
                .primary(Boolean.parseBoolean(addressData.get("primary")))
                .build();
        
        response = given()
                .contentType(ContentType.JSON)
                .body(addressDto)
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
        String addressId = response.jsonPath().getString("data.id");
        assertNotNull(addressId);
        
        // Mock a newly created address
        AddressDto newAddress = AddressDto.builder()
                .id(addressId)
                .street("New Street")
                .number("100")
                .neighborhood("New Area")
                .city("New City")
                .state("NC")
                .country("USA")
                .zipCode("12345")
                .primary(false)
                .build();
        
        // Setup mock to return this address when queried
        when(addressApiClient.getAddressById(addressId)).thenReturn(newAddress);
        
        // Add the address to our customer's addresses
        addresses.add(newAddress);
        customer.setAddresses(addresses);
        
        // Verify address exists and is associated with our customer
        AddressDto foundAddress = addressApiClient.getAddressById(addressId);
        assertNotNull(foundAddress);
        assertTrue(customer.getAddresses().stream().anyMatch(a -> a.getId().equals(addressId)));
    }
    
    @When("I set a specific address as primary")
    public void iSetASpecificAddressAsPrimary() {
        // Get the second address (non-primary one)
        AddressDto nonPrimary = addresses.get(1);
        
        response = given()
                .patch("/api/v1/customers/" + customer.getId() + "/addresses/" + nonPrimary.getId() + "/primary");
        
        // Update our test data to reflect these changes
        addresses.get(0).setPrimary(false);
        addresses.get(1).setPrimary(true);
        
        // Update mocks
        when(addressApiClient.getAddressById(addresses.get(0).getId())).thenReturn(addresses.get(0));
        when(addressApiClient.getAddressById(addresses.get(1).getId())).thenReturn(addresses.get(1));
    }
    
    @Then("the address should be marked as primary")
    public void theAddressShouldBeMarkedAsPrimary() {
        response.then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"))
                .body("data.primary", equalTo(true));
        
        // Verify in API client
        AddressDto updatedAddress = addressApiClient.getAddressById(addresses.get(1).getId());
        assertTrue(updatedAddress.isPrimary());
    }
    
    @Then("any previously primary address should be marked as non-primary")
    public void anyPreviouslyPrimaryAddressShouldBeMarkedAsNonPrimary() {
        // Verify in API client
        AddressDto previousPrimary = addressApiClient.getAddressById(addresses.get(0).getId());
        assertFalse(previousPrimary.isPrimary());
    }
    
    @Given("a customer exists with at least one address")
    public void aCustomerExistsWithAtLeastOneAddress() {
        // Initialize customer
        customer = CustomerDto.builder()
                .id("customer123")
                .firstName("Has")
                .lastName("Address")
                .email("has.address@example.com")
                .phoneNumber("+1234567890")
                .active(true)
                .build();
        
        // Create address
        address = AddressDto.builder()
                .id("address3")
                .street("789 Pine Blvd")
                .number("3")
                .neighborhood("Westside")
                .city("Chicago")
                .state("IL")
                .country("USA")
                .zipCode("60601")
                .primary(true)
                .build();
        
        // Setup customer with address
        addresses.add(address);
        customer.setAddresses(addresses);
        
        // Mock API responses
        when(customerApiClient.getCustomerById(customer.getId())).thenReturn(customer);
        when(addressApiClient.getAddressById(address.getId())).thenReturn(address);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
    }
    
    @When("I delete the address")
    public void iDeleteTheAddress() {
        response = given()
                .delete("/api/v1/addresses/" + address.getId());
        
        // Update mocks to simulate deletion
        addresses.clear();
        when(addressApiClient.getAddressById(address.getId())).thenReturn(null);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
    }
    
    @Then("the address should be deleted successfully")
    public void theAddressShouldBeDeletedSuccessfully() {
        response.then()
                .statusCode(200)
                .body("status", equalTo("SUCCESS"));
        
        // Verify address no longer exists
        assertNull(addressApiClient.getAddressById(address.getId()));
        assertTrue(addressApiClient.getAddressesByCustomerId(customer.getId()).isEmpty());
    }
    
    @Given("multiple addresses exist in different cities")
    public void multipleAddressesExistInDifferentCities() {
        // Create a customer
        customer = CustomerDto.builder()
                .id("customer123")
                .firstName("City")
                .lastName("Searcher")
                .email("city.searcher@example.com")
                .phoneNumber("+1122334455")
                .active(true)
                .build();
        
        // Create NY address
        AddressDto nyAddress = AddressDto.builder()
                .id("address4")
                .street("123 Broadway")
                .number("1")
                .neighborhood("Manhattan")
                .city("New York")
                .state("NY")
                .country("USA")
                .zipCode("10001")
                .primary(true)
                .build();
        
        // Create Chicago address
        AddressDto chicagoAddress = AddressDto.builder()
                .id("address5")
                .street("456 Michigan Ave")
                .number("2")
                .neighborhood("Loop")
                .city("Chicago")
                .state("IL")
                .country("USA")
                .zipCode("60601")
                .primary(false)
                .build();
        
        // Add to list
        addresses.clear();
        addresses.add(nyAddress);
        addresses.add(chicagoAddress);
        customer.setAddresses(addresses);
        
        // Mock API responses
        when(customerApiClient.getCustomerById(customer.getId())).thenReturn(customer);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
        when(addressApiClient.searchAddressesByCity("New York")).thenReturn(List.of(nyAddress));
        when(addressApiClient.searchAddressesByCity("Chicago")).thenReturn(List.of(chicagoAddress));
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
        customer = CustomerDto.builder()
                .id("customer123")
                .firstName("State")
                .lastName("Searcher")
                .email("state.searcher@example.com")
                .phoneNumber("+5566778899")
                .active(true)
                .build();
        
        // Create CA address
        AddressDto caAddress = AddressDto.builder()
                .id("address6")
                .street("123 Hollywood Blvd")
                .number("1")
                .neighborhood("Hollywood")
                .city("Los Angeles")
                .state("CA")
                .country("USA")
                .zipCode("90028")
                .primary(true)
                .build();
        
        // Create TX address
        AddressDto txAddress = AddressDto.builder()
                .id("address7")
                .street("456 Congress Ave")
                .number("2")
                .neighborhood("Downtown")
                .city("Austin")
                .state("TX")
                .country("USA")
                .zipCode("78701")
                .primary(false)
                .build();
        
        // Add to list
        addresses.clear();
        addresses.add(caAddress);
        addresses.add(txAddress);
        customer.setAddresses(addresses);
        
        // Mock API responses
        when(customerApiClient.getCustomerById(customer.getId())).thenReturn(customer);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
        when(addressApiClient.searchAddressesByState("CA")).thenReturn(List.of(caAddress));
        when(addressApiClient.searchAddressesByState("TX")).thenReturn(List.of(txAddress));
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
        customer = CustomerDto.builder()
                .id("customer123")
                .firstName("Zip")
                .lastName("Searcher")
                .email("zip.searcher@example.com")
                .phoneNumber("+9988776655")
                .active(true)
                .build();
        
        // Create address with zipcode 10001
        AddressDto zip10001 = AddressDto.builder()
                .id("address8")
                .street("123 5th Ave")
                .number("1")
                .neighborhood("Flatiron")
                .city("New York")
                .state("NY")
                .country("USA")
                .zipCode("10001")
                .primary(true)
                .build();
        
        // Create address with zipcode 90210
        AddressDto zip90210 = AddressDto.builder()
                .id("address9")
                .street("456 Rodeo Dr")
                .number("2")
                .neighborhood("Beverly Hills")
                .city("Los Angeles")
                .state("CA")
                .country("USA")
                .zipCode("90210")
                .primary(false)
                .build();
        
        // Add to list
        addresses.clear();
        addresses.add(zip10001);
        addresses.add(zip90210);
        customer.setAddresses(addresses);
        
        // Mock API responses
        when(customerApiClient.getCustomerById(customer.getId())).thenReturn(customer);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
        when(addressApiClient.searchAddressesByZipCode("10001")).thenReturn(List.of(zip10001));
        when(addressApiClient.searchAddressesByZipCode("90210")).thenReturn(List.of(zip90210));
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
}