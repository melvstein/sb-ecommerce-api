package com.melvstein.ecommerce.api.domain.customer.repository;

import com.melvstein.ecommerce.api.domain.customer.document.Customer;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerRepositoryCustom {
    List<Customer> filter(List<String> filter, Pageable pageable);
}
