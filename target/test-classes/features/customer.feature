Feature: Customer Management
  As an API user
  I want to manage customers and their addresses
  So that I can maintain customer information

  Background:
    Given the API is available

  @simple
  Scenario: Create a new customer
    When I create a customer with the following details:
      | firstName | lastName | email               | phoneNumber  |
      | John      | Doe      | john.doe@example.com | +1234567890 |
    Then the customer should be created successfully
    And the response should contain the customer details

  Scenario: Get customer by ID
    Given a customer exists with the following details:
      | firstName | lastName | email                | phoneNumber  |
      | Jane      | Smith    | jane.smith@example.com | +0987654321 |
    When I request the customer by ID
    Then the response should contain the correct customer details

  Scenario: Add an address to a customer
    Given a customer exists with the following details:
      | firstName | lastName | email                | phoneNumber  |
      | Bob       | Johnson  | bob.johnson@example.com | +1122334455 |
    When I add an address with the following details:
      | street     | number | neighborhood | city      | state | country | zipCode |
      | 123 Main St | 10    | Downtown     | New York  | NY    | USA     | 10001   |
    Then the address should be added successfully
    And the customer should have the address in their profile

  Scenario: Update a customer
    Given a customer exists with the following details:
      | firstName | lastName | email                    | phoneNumber  |
      | Alice     | Wilson   | alice.wilson@example.com | +5544332211 |
    When I update the customer with the following details:
      | firstName | lastName | email                    | phoneNumber  |
      | Alicia    | Wilson   | alicia.wilson@example.com | +5544332211 |
    Then the customer should be updated successfully
    And the response should contain the updated details

  Scenario: Delete a customer
    Given a customer exists with the following details:
      | firstName | lastName | email                  | phoneNumber  |
      | Charlie   | Brown    | charlie.brown@example.com | +9988776655 |
    When I delete the customer
    Then the customer should be deleted successfully