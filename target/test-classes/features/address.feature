Feature: Address Management
  As an API user
  I want to manage customer addresses
  So that I can maintain correct delivery information

  Background:
    Given the API is available

  Scenario: Set an address as primary
    Given a customer exists with multiple addresses
    When I set a specific address as primary
    Then that address should be marked as primary
    And any previously primary address should be marked as non-primary

  Scenario: Delete an address
    Given a customer exists with at least one address
    When I delete the address
    Then the address should be removed from the customer's profile
    
  Scenario: Search addresses by city
    Given multiple addresses exist in different cities
    When I search for addresses in "New York"
    Then the response should contain only addresses in "New York"
    
  Scenario: Search addresses by state
    Given multiple addresses exist in different states
    When I search for addresses in state "CA"
    Then the response should contain only addresses in state "CA"
    
  Scenario: Search addresses by ZIP code
    Given multiple addresses exist with different ZIP codes
    When I search for addresses with ZIP code "10001"
    Then the response should contain only addresses with ZIP code "10001"