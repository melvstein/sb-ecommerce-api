package com.melvstein.ecommerce.api.domain.customer.service;

import com.melvstein.ecommerce.api.domain.customer.document.Customer;
import com.melvstein.ecommerce.api.domain.customer.repository.CustomerRepository;
import com.melvstein.ecommerce.api.shared.util.Utils;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PagedResourcesAssembler<Customer> customerPagedResourcesAssembler;
    private final S3Client s3Client;
    private final String bucketName = "customers-bucket";

    @Value("${r2.public.customers-bucket.url}")
    private String publicBucketUrl;

    public PagedModel<EntityModel<Customer>> fetchAllCustomers(@Nullable List<String> filter, Pageable pageable) {
        List<Customer> filteredCustomers = customerRepository.filter(filter, pageable);
        Page<Customer> customerPage = new PageImpl<>(filteredCustomers, pageable, filteredCustomers.size());
        PagedModel<EntityModel<Customer>> customerPagedModel = customerPagedResourcesAssembler.toModel(customerPage);

        List<EntityModel<Customer>> customerDtoContent = customerPagedModel.getContent().stream()
                .map(entityModel -> {
                    assert entityModel.getContent() != null;
                    return EntityModel.of(entityModel.getContent());
                })
                .toList();

        return PagedModel.of(
                customerDtoContent,
                customerPagedModel.getMetadata(),
                customerPagedModel.getLinks()
        );
    }

    public Optional<Customer> fetchCustomerById(String customerId) {
        return customerRepository.findById(customerId);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomerById(String customerId) {
        customerRepository.deleteById(customerId);
    }

    public boolean customerExistsById(String customerId) {
        return customerRepository.existsById(customerId);
    }

    public Optional<Customer> fetchCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public String uploadProfileImage(Customer customer, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        if (customer.getProfileImageUrl() != null && !customer.getProfileImageUrl().isEmpty()) {
            String oldImageUrl = customer.getProfileImageUrl();
            String oldKey = oldImageUrl.replace(publicBucketUrl + "/", "");

            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(oldKey)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
            } catch (S3Exception e) {
                // Optional: Log or handle failure to delete old image
                System.err.println("Failed to delete old image: " + e.awsErrorDetails().errorMessage());
            }
        }

        String key = Utils.generateFileName(file, customer.getUsername());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket("customers-bucket")
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));
        String imageUrl = publicBucketUrl + "/" + key;
        customer.setProfileImageUrl(imageUrl);
        customerRepository.save(customer);

        return imageUrl;
    }
}
