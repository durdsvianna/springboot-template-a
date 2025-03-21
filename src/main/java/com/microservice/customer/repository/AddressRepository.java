package com.microservice.customer.repository;

import com.microservice.customer.model.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {
    
    List<Address> findByCustomerId(String customerId);
    
    Optional<Address> findByCustomerIdAndPrimaryTrue(String customerId);
    
    void deleteByCustomerId(String customerId);
    
    List<Address> findByCity(String city);
    
    List<Address> findByState(String state);
    
    List<Address> findByZipCode(String zipCode);
}