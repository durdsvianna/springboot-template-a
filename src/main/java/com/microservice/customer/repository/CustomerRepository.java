package com.microservice.customer.repository;

import com.microservice.customer.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    
    Optional<Customer> findByEmail(String email);
    
    List<Customer> findByLastNameContainingIgnoreCase(String lastName);
    
    List<Customer> findByFirstNameContainingIgnoreCase(String firstName);
    
    List<Customer> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(String firstName, String lastName);
    
    List<Customer> findByActive(boolean active);
}