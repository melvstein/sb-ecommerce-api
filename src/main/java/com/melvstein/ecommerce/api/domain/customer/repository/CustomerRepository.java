package com.melvstein.ecommerce.api.domain.customer.repository;

import com.melvstein.ecommerce.api.domain.customer.document.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String>, CustomerRepositoryCustom {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByContactNumber(String contactNumber);
}
