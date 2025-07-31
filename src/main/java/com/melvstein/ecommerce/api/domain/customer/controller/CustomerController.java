package com.melvstein.ecommerce.api.domain.customer.controller;

import com.melvstein.ecommerce.api.domain.customer.Mapper.CustomerMapper;
import com.melvstein.ecommerce.api.domain.customer.document.Customer;
import com.melvstein.ecommerce.api.domain.customer.dto.CustomerDto;
import com.melvstein.ecommerce.api.domain.customer.service.CustomerService;
import com.melvstein.ecommerce.api.shared.dto.ApiResponse;
import com.melvstein.ecommerce.api.shared.util.ApiResponseCode;
import com.melvstein.ecommerce.api.shared.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {
    private final CustomerService customerService;

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
}
