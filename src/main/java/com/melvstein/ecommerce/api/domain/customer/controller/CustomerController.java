package com.melvstein.ecommerce.api.domain.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melvstein.ecommerce.api.domain.customer.Mapper.CustomerMapper;
import com.melvstein.ecommerce.api.domain.customer.document.Address;
import com.melvstein.ecommerce.api.domain.customer.document.Customer;
import com.melvstein.ecommerce.api.domain.customer.dto.AddCustomerRequestDto;
import com.melvstein.ecommerce.api.domain.customer.dto.CustomerDto;
import com.melvstein.ecommerce.api.domain.customer.enums.CustomerResponseCode;
import com.melvstein.ecommerce.api.domain.customer.service.CustomerService;
import com.melvstein.ecommerce.api.shared.dto.ApiResponse;
import com.melvstein.ecommerce.api.shared.exception.ApiException;
import com.melvstein.ecommerce.api.shared.util.ApiResponseCode;
import com.melvstein.ecommerce.api.shared.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<CustomerDto>>>> getAllCustomers(
            @RequestParam(value = "filter", required = false) List<String> filter,
            Pageable pageable
    ) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<PagedModel<EntityModel<CustomerDto>>> response = ApiResponse.<PagedModel<EntityModel<CustomerDto>>>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("An error occurred while fetching customers")
                .data(null)
                .build();

        try {
            PagedModel<EntityModel<Customer>> customerPagedModel = customerService.fetchAllCustomers(filter, pageable);

            List<EntityModel<CustomerDto>> customerDtoContent = customerPagedModel.getContent().stream()
                    .map(entityModel -> {
                        CustomerDto customerDto = CustomerMapper.toDto(entityModel.getContent());
                        assert customerDto != null;
                        return EntityModel.of(customerDto);
                    })
                    .toList();

            PagedModel<EntityModel<CustomerDto>> customerDtoPagedModel = PagedModel.of(
                    customerDtoContent,
                    customerPagedModel.getMetadata(),
                    customerPagedModel.getLinks()
            );

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Fetched all customers successfully");
            response.setData(customerDtoPagedModel);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto>> addCustomer(@RequestBody @Valid AddCustomerRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<CustomerDto> response = ApiResponse.<CustomerDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("An error occurred while adding the customer")
                .data(null)
                .build();

        try {
            Optional<Customer> existingCustomer = customerService.fetchCustomerByEmail(request.email());
            if (existingCustomer.isPresent()) {
                Customer customer = existingCustomer.get();

                response.setMessage("Customer already exists");
                response.setData(CustomerMapper.toDto(customer));
            } else {
                Customer savedCustomer = customerService.saveCustomer(CustomerMapper.toDocument(request));
                CustomerDto customerDto = CustomerMapper.toDto(savedCustomer);

                response.setMessage("Customer added successfully");
                response.setData(customerDto);
            }

            response.setCode(ApiResponseCode.SUCCESS.getCode());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerById(@PathVariable String id) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<CustomerDto> response = ApiResponse.<CustomerDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("An error occurred while fetching the customer")
                .data(null)
                .build();

        try {
            Customer customer = customerService
                    .fetchCustomerById(id)
                    .orElseThrow(() -> new ApiException(
                            CustomerResponseCode.CUSTOMER_NOT_FOUND.getCode(),
                            CustomerResponseCode.CUSTOMER_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Fetched customer details successfully");
            response.setData(CustomerMapper.toDto(customer));

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> deleteCustomerById(@PathVariable String id) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<CustomerDto> response = ApiResponse.<CustomerDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to delete customer")
                .data(null)
                .build();

        try {
            Customer customer = customerService
                    .fetchCustomerById(id)
                    .orElseThrow(() -> new ApiException(
                            CustomerResponseCode.CUSTOMER_NOT_FOUND.getCode(),
                            CustomerResponseCode.CUSTOMER_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            customerService.deleteCustomerById(customer.getId());

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Customer deleted successfully");
            response.setData(CustomerMapper.toDto(customer));

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(@PathVariable String id, @RequestBody Map<String, Object> request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<CustomerDto> response = ApiResponse.<CustomerDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to update customer details")
                .data(null)
                .build();

        try {
            Customer customer = customerService
                    .fetchCustomerById(id)
                    .orElseThrow(() -> new ApiException(
                            CustomerResponseCode.CUSTOMER_NOT_FOUND.getCode(),
                            CustomerResponseCode.CUSTOMER_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            request.remove("password");

            request.forEach((key, value) -> {
                switch (key) {
                    case "provider":
                        if (value instanceof String) {
                            customer.setProvider((String) value);
                        }
                        break;
                    case "username":
                        if (value instanceof String) {
                            customer.setUsername((String) value);
                        }
                        break;
                    case "email":
                        if (value instanceof String) {
                            customer.setEmail((String) value);
                        }
                        break;
                    case "firstName":
                        if (value instanceof String) {
                            customer.setFirstName((String) value);
                        }
                        break;
                    case "middleName":
                        if (value instanceof String) {
                            customer.setMiddleName((String) value);
                        }
                        break;
                    case "lastName":
                        if (value instanceof String) {
                            customer.setLastName((String) value);
                        }
                        break;
                    case "contactNumber":
                        if (value instanceof String) {
                            customer.setContactNumber((String) value);
                        }
                        break;
                    case "profileImageUrl":
                        if (value instanceof String) {
                            customer.setProfileImageUrl((String) value);
                        }
                        break;
                    case "isActive":
                        if (value instanceof Boolean) {
                            customer.setActive((Boolean) value);
                        }
                        break;
                    case "isVerified":
                        if (value instanceof Boolean) {
                            customer.setVerified((Boolean) value);
                        }
                    case "lastLoginAt":
                        if (value instanceof Date) {
                            log.info("lastLoginAt Date {}", value);
                            customer.setLastLoginAt(((Date) value).toInstant());
                        } else if (value instanceof String) {
                            log.info("lastLoginAt String {}", value);
                            customer.setLastLoginAt(Utils.fromDateStringToInstant((String) value));
                        }
                        break;
                    case "address":
                        if (value instanceof Map) {
                            Map<String, Object> addressMap = (Map<String, Object>) value;
                            Address address = customer.getAddress() != null ? customer.getAddress() : new Address();
                            addressMap.forEach((fieldName, fieldValue) -> {
                                switch (fieldName) {
                                    case "addressType":
                                        address.setAddressType((String) fieldValue);
                                        break;
                                    case "street":
                                        address.setStreet((String) fieldValue);
                                        break;
                                    case "district":
                                        address.setDistrict((String) fieldValue);
                                        break;
                                    case "city":
                                        address.setCity((String) fieldValue);
                                        break;
                                    case "province":
                                        address.setProvince((String) fieldValue);
                                        break;
                                    case "country":
                                        address.setCountry((String) fieldValue);
                                        break;
                                    case "zipCode":
                                        if (fieldValue instanceof Number) {
                                            address.setZipCode(((Number) fieldValue).intValue());
                                        }
                                        break;
                                    case "isDefault":
                                        if (fieldValue instanceof Boolean) {
                                            address.setDefault((Boolean) fieldValue);
                                        }
                                        break;
                                }
                            });
                            customer.setAddress(address);
                        }
                        break;
                    default:
                        log.warn("{} - Ignore unknown field key={} value={}", Utils.getClassAndMethod(), key, value);
                        break;
                }
            });

            Customer savedCustomer = customerService.saveCustomer(customer);

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Customer details updated successfully");
            response.setData(CustomerMapper.toDto(savedCustomer));

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }
}
