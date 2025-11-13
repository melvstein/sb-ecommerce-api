package com.melvstein.ecommerce.api.domain.customer.service;

import com.melvstein.ecommerce.api.config.ImageProperties;
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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PagedResourcesAssembler<Customer> customerPagedResourcesAssembler;
    private final S3Client s3Client;
    private final String bucketName = "customers-bucket";
    private final ImageProperties imageProperties;

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

        // --- Step 1: Delete old image if exists ---
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
                System.err.println("Failed to delete old image: " + e.awsErrorDetails().errorMessage());
            }
        }

        // --- Step 2: Generate unique key ---
        String key = Utils.generateFileName(file, customer.getUsername());

        // --- Step 3: Read image ---
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            // Not an image, upload as-is
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            String imageUrl = publicBucketUrl + "/" + key;
            customer.setProfileImageUrl(imageUrl);
            customerRepository.save(customer);
            return imageUrl;
        }

        // --- Step 4: Convert to RGB with white background ---
        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        g.setColor(Color.WHITE); // White background
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.drawImage(image, 0, 0, null);
        g.dispose();
        image = rgbImage;

        // --- Step 5: Resize and compress using properties ---
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        float quality = 1.0f;
        int maxWidth = imageProperties.getMaxWidth();
        int maxHeight = imageProperties.getMaxHeight();

        // Resize if larger than max dimensions
        if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
            BufferedImage resizedImage = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = resizedImage.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, maxWidth, maxHeight);
            g2.drawImage(image, 0, 0, maxWidth, maxHeight, null);
            g2.dispose();
            image = resizedImage;
        }

        while (true) {
            outputStream.reset();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
                writer.dispose();
            }

            if (outputStream.size() / 1024 <= imageProperties.getMaxSizeKb()
                    || quality <= imageProperties.getMinQuality()) {
                break;
            }
            quality -= 0.05f;
        }

        // --- Step 6: Upload compressed image ---
        InputStream compressedInputStream = new ByteArrayInputStream(outputStream.toByteArray());
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("image/jpeg")
                .build();

        s3Client.putObject(putObjectRequest,
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(compressedInputStream, outputStream.size()));

        // --- Step 7: Update customer record ---
        String imageUrl = publicBucketUrl + "/" + key;
        customer.setProfileImageUrl(imageUrl);
        customerRepository.save(customer);

        return imageUrl;
    }
}
