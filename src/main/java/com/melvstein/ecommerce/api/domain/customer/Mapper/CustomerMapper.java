package com.melvstein.ecommerce.api.domain.customer.Mapper;

import com.melvstein.ecommerce.api.domain.customer.document.Customer;
import com.melvstein.ecommerce.api.domain.customer.dto.CustomerDto;
import com.melvstein.ecommerce.api.shared.util.Utils;

public class CustomerMapper {

    public static CustomerDto toDto(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerDto.builder()
                .id(customer.getId())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .middleName(customer.getMiddleName())
                .lastName(customer.getLastName())
                .contactNumber(customer.getContactNumber())
                .provider(customer.getProvider())
                .profileImageUrl(customer.getProfileImageUrl())
                .address(customer.getAddress())
                .isActive(customer.isActive())
                .isVerified(customer.isVerified())
                .lastLoginAt(Utils.fromInstantToDate(customer.getLastLoginAt()))
                .createdAt(Utils.fromInstantToDate(customer.getCreatedAt()))
                .updatedAt(Utils.fromInstantToDate(customer.getUpdatedAt()))
                .build();
    }

    public static Customer toDocument(CustomerDto customerDto) {
        if (customerDto == null) {
            return null;
        }

        return Customer.builder()
                .id(customerDto.id())
                .username(customerDto.username())
                .email(customerDto.email())
                .firstName(customerDto.firstName())
                .middleName(customerDto.middleName())
                .lastName(customerDto.lastName())
                .contactNumber(customerDto.contactNumber())
                .provider(customerDto.provider())
                .profileImageUrl(customerDto.profileImageUrl())
                .address(customerDto.address())
                .isActive(customerDto.isActive())
                .isVerified(customerDto.isVerified())
                .lastLoginAt(customerDto.lastLoginAt().toInstant())
                .createdAt(customerDto.createdAt().toInstant())
                .updatedAt(customerDto.updatedAt().toInstant())
                .build();
    }
}
