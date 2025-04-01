package com.microservice.customer.cucumber;

import com.microservice.customer.client.CustomerApiClient;
import com.microservice.customer.client.AddressApiClient;
import com.microservice.customer.dto.AddressDto;
import com.microservice.customer.dto.CustomerDto;
import com.microservice.customer.service.AddressService;
import com.microservice.customer.service.CustomerService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class AddressStepDefinitions {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private CustomerApiClient customerApiClient;
    
    @Autowired
    private AddressApiClient addressApiClient;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private AddressService addressService;
    
    private CustomerDto customer;
    private AddressDto inputAddress;
    private AddressDto resultAddress;
    private List<AddressDto> addresses = new ArrayList<>();
    private List<AddressDto> searchResults = new ArrayList<>();
    private Exception error;
    
    @After
    public void cleanup() {
        // Reset mocks
        Mockito.reset(customerApiClient, addressApiClient);
        
        // Reset test data
        customer = null;
        inputAddress = null;
        resultAddress = null;
        addresses.clear();
        searchResults.clear();
        error = null;
    }
    
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
        
        // Mock API responses for when service calls them
        when(customerApiClient.getCustomerById(customer.getId())).thenReturn(customer);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
        when(addressApiClient.getAddressById("address1")).thenReturn(primary);
        when(addressApiClient.getAddressById("address2")).thenReturn(secondary);
    }
    
    @When("I add an address with the following details:")
    public void iAddAnAddressWithTheFollowingDetails(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> addressData = rows.get(0);
        
        // Prepare input address DTO
        AddressDto addressDto = AddressDto.builder()
                .street(addressData.get("street"))
                .number(addressData.get("number"))
                .neighborhood(addressData.get("neighborhood"))
                .city(addressData.get("city"))
                .state(addressData.get("state"))
                .country(addressData.get("country"))
                .zipCode(addressData.get("zipCode"))
                .primary(Boolean.parseBoolean(addressData.getOrDefault("primary", "false")))
                .build();
        
        // Save input for validation
        this.inputAddress = addressDto;
        
        // If customer is null, create a test customer
        if (customer == null) {
            CustomerDto customerDto = CustomerDto.builder()
                .id("customer123")
                .firstName("Bob")
                .lastName("Johnson")
                .email("bob.johnson@example.com")
                .phoneNumber("+1122334455")
                .active(true)
                .build();
            
            when(customerApiClient.getCustomerById(anyString())).thenReturn(customerDto);
            this.customer = customerDto;
        }
        
        // Mock the address creation response for when service calls it
        AddressDto createdAddress = AddressDto.builder()
                .id("newAddressId")
                .street(addressData.get("street"))
                .number(addressData.get("number"))
                .neighborhood(addressData.get("neighborhood"))
                .city(addressData.get("city"))
                .state(addressData.get("state"))
                .country(addressData.get("country"))
                .zipCode(addressData.get("zipCode"))
                .primary(Boolean.parseBoolean(addressData.getOrDefault("primary", "false")))
                .build();
                
        when(addressApiClient.createAddress(anyString(), any(AddressDto.class))).thenReturn(createdAddress);
        
        // Call service with explicit exception handling
        try {
            this.resultAddress = addressService.createAddress(customer.getId(), addressDto);
        } catch (Exception e) {
            this.error = e;
        }
    }
    
    @Then("the address should be added successfully")
    public void theAddressShouldBeAddedSuccessfully() {
        assertNull(error, "No exception should be thrown");
        assertNotNull(resultAddress, "Address should be created");
        assertNotNull(resultAddress.getId(), "Address should have an ID");
    }
    
    @Then("the customer should have the address in their profile")
    public void theCustomerShouldHaveTheAddressInTheirProfile() {
        String addressId = resultAddress.getId();
        assertNotNull(addressId);
        
        // Mock a newly created address for when service requests it
        AddressDto newAddress = AddressDto.builder()
                .id(addressId)
                .street(inputAddress.getStreet())
                .number(inputAddress.getNumber())
                .neighborhood(inputAddress.getNeighborhood())
                .city(inputAddress.getCity())
                .state(inputAddress.getState())
                .country(inputAddress.getCountry())
                .zipCode(inputAddress.getZipCode())
                .primary(inputAddress.isPrimary())
                .build();
        
        // Setup mock to return this address when queried by service
        when(addressApiClient.getAddressById(addressId)).thenReturn(newAddress);
        
        // Add the address to our test data
        addresses.add(newAddress);
        customer.setAddresses(addresses);
        
        // Mock the updated list of addresses
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
        
        // Verify address exists through service call
        try {
            AddressDto foundAddress = addressService.getAddressById(addressId);
            assertNotNull(foundAddress, "Address should exist");
            
            List<AddressDto> customerAddresses = addressService.getAddressesByCustomerId(customer.getId());
            assertTrue(customerAddresses.stream().anyMatch(a -> a.getId().equals(addressId)), 
                    "Customer should have the new address");
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }
    
    @When("I set a specific address as primary")
    public void iSetASpecificAddressAsPrimary() {
        // Get the second (non-primary) address for our test
        AddressDto nonPrimary = addresses.get(1);
        
        // Mock the setPrimaryAddress response for when service calls it
        AddressDto updatedPrimaryAddress = AddressDto.builder()
                .id(nonPrimary.getId())
                .street(nonPrimary.getStreet())
                .number(nonPrimary.getNumber())
                .neighborhood(nonPrimary.getNeighborhood())
                .city(nonPrimary.getCity())
                .state(nonPrimary.getState())
                .country(nonPrimary.getCountry())
                .zipCode(nonPrimary.getZipCode())
                .primary(true)
                .build();
                
        when(addressApiClient.setPrimaryAddress(customer.getId(), nonPrimary.getId())).thenReturn(updatedPrimaryAddress);
        
        // Call service with exception handling
        try {
            this.resultAddress = addressService.setPrimaryAddress(customer.getId(), nonPrimary.getId());
        } catch (Exception e) {
            this.error = e;
        }
        
        // Update our test data to reflect these changes
        addresses.get(0).setPrimary(false);
        addresses.get(1).setPrimary(true);
        
        // Update mocks for subsequent service calls
        when(addressApiClient.getAddressById(addresses.get(0).getId())).thenReturn(addresses.get(0));
        when(addressApiClient.getAddressById(addresses.get(1).getId())).thenReturn(addresses.get(1));
    }
    
    @Then("that address should be marked as primary")
    public void thatAddressShouldBeMarkedAsPrimary() {
        assertNull(error, "No exception should be thrown");
        assertNotNull(resultAddress, "Address should not be null");
        assertTrue(resultAddress.isPrimary(), "Address should be marked as primary");
        
        // Verify through service
        try {
            AddressDto updatedAddress = addressService.getAddressById(addresses.get(1).getId());
            assertTrue(updatedAddress.isPrimary(), "Address should be primary in the service result");
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }
    
    @Then("any previously primary address should be marked as non-primary")
    public void anyPreviouslyPrimaryAddressShouldBeMarkedAsNonPrimary() {
        // Verify through service
        try {
            AddressDto previousPrimary = addressService.getAddressById(addresses.get(0).getId());
            assertFalse(previousPrimary.isPrimary(), "Previously primary address should no longer be primary");
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
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
        resultAddress = AddressDto.builder()
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
        addresses.clear();
        addresses.add(resultAddress);
        customer.setAddresses(addresses);
        
        // Mock API responses for when service calls them
        when(customerApiClient.getCustomerById(customer.getId())).thenReturn(customer);
        when(addressApiClient.getAddressById(resultAddress.getId())).thenReturn(resultAddress);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
    }
    
    @When("I delete the address")
    public void iDeleteTheAddress() {
        // Call service with exception handling
        try {
            addressService.deleteAddress(resultAddress.getId());
        } catch (Exception e) {
            this.error = e;
        }
        
        // Update mocks to simulate deletion for subsequent service calls
        addresses.clear();
        when(addressApiClient.getAddressById(resultAddress.getId())).thenReturn(null);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
    }
    
    @Then("the address should be removed from the customer's profile")
    public void theAddressShouldBeRemovedFromTheCustomersProfile() {
        assertNull(error, "No exception should be thrown");
        
        // Verify address no longer exists using the service
        try {
            AddressDto deletedAddress = addressService.getAddressById(resultAddress.getId());
            assertNull(deletedAddress, "Address should no longer exist");
            
            List<AddressDto> remainingAddresses = addressService.getAddressesByCustomerId(customer.getId());
            assertTrue(remainingAddresses.isEmpty(), "Customer should have no addresses");
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
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
        
        // Mock API responses for when service calls them
        when(customerApiClient.getCustomerById(customer.getId())).thenReturn(customer);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
        when(addressApiClient.searchAddressesByCity("New York")).thenReturn(List.of(nyAddress));
        when(addressApiClient.searchAddressesByCity("Chicago")).thenReturn(List.of(chicagoAddress));
    }
    
    @When("I search for addresses in {string}")
    public void iSearchForAddressesInCity(String city) {
        // Call service with exception handling
        try {
            this.searchResults = addressService.searchAddressesByCity(city);
        } catch (Exception e) {
            this.error = e;
        }
    }
    
    @Then("the response should contain only addresses in {string}")
    public void theResponseShouldContainOnlyAddressesInCity(String city) {
        assertNull(error, "No exception should be thrown");
        assertNotNull(searchResults, "Search results should not be null");
        assertFalse(searchResults.isEmpty(), "Search results should not be empty");
        
        // Verify all returned addresses are in the specified city
        for (AddressDto address : searchResults) {
            assertEquals(city, address.getCity(), "Address should be in the specified city");
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
        
        // Mock API responses for when service calls them
        when(customerApiClient.getCustomerById(customer.getId())).thenReturn(customer);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
        when(addressApiClient.searchAddressesByState("CA")).thenReturn(List.of(caAddress));
        when(addressApiClient.searchAddressesByState("TX")).thenReturn(List.of(txAddress));
    }
    
    @When("I search for addresses in state {string}")
    public void iSearchForAddressesInState(String state) {
        // Call service with exception handling
        try {
            this.searchResults = addressService.searchAddressesByState(state);
        } catch (Exception e) {
            this.error = e;
        }
    }
    
    @Then("the response should contain only addresses in state {string}")
    public void theResponseShouldContainOnlyAddressesInState(String state) {
        assertNull(error, "No exception should be thrown");
        assertNotNull(searchResults, "Search results should not be null");
        assertFalse(searchResults.isEmpty(), "Search results should not be empty");
        
        // Verify all returned addresses are in the specified state
        for (AddressDto address : searchResults) {
            assertEquals(state, address.getState(), "Address should be in the specified state");
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
        
        // Mock API responses for when service calls them
        when(customerApiClient.getCustomerById(customer.getId())).thenReturn(customer);
        when(addressApiClient.getAddressesByCustomerId(customer.getId())).thenReturn(addresses);
        when(addressApiClient.searchAddressesByZipCode("10001")).thenReturn(List.of(zip10001));
        when(addressApiClient.searchAddressesByZipCode("90210")).thenReturn(List.of(zip90210));
    }
    
    @When("I search for addresses with ZIP code {string}")
    public void iSearchForAddressesWithZIPCode(String zipCode) {
        // Call service with exception handling
        try {
            this.searchResults = addressService.searchAddressesByZipCode(zipCode);
        } catch (Exception e) {
            this.error = e;
        }
    }
    
    @Then("the response should contain only addresses with ZIP code {string}")
    public void theResponseShouldContainOnlyAddressesWithZIPCode(String zipCode) {
        assertNull(error, "No exception should be thrown");
        assertNotNull(searchResults, "Search results should not be null");
        assertFalse(searchResults.isEmpty(), "Search results should not be empty");
        
        // Verify all returned addresses have the specified ZIP code
        for (AddressDto address : searchResults) {
            assertEquals(zipCode, address.getZipCode(), "Address should have the specified ZIP code");
        }
    }
}