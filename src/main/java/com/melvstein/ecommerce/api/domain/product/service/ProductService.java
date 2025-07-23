package com.melvstein.ecommerce.api.domain.product.service;

import com.melvstein.ecommerce.api.domain.product.document.Product;
import com.melvstein.ecommerce.api.domain.product.dto.ProductDto;
import com.melvstein.ecommerce.api.domain.product.mapper.ProductMapper;
import com.melvstein.ecommerce.api.domain.product.repository.ProductRepository;
import com.melvstein.ecommerce.api.domain.user.document.User;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final PagedResourcesAssembler<Product> productPagedResourcesAssembler;
    private final S3Client s3Client;
    private final String bucketName = "products-bucket";

    @Value("${r2.public.products-bucket.url}")
    private String publicUrl;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public PagedModel<EntityModel<Product>> getAllProducts(@Nullable List<String> filter, Pageable pageable) {
        List<Product> filteredProducts = productRepository.filter(filter, pageable);
        Page<Product> productPage = new PageImpl<>(filteredProducts, pageable, filteredProducts.size());
        PagedModel<EntityModel<Product>> productPagedModel = productPagedResourcesAssembler.toModel(productPage);

        List<EntityModel<Product>> productDtoContent = productPagedModel.getContent().stream()
                .map(entityModel -> {
                    assert entityModel.getContent() != null;
                    return EntityModel.of(entityModel.getContent());
                })
                .toList();

        return PagedModel.of(
                productDtoContent,
                productPagedModel.getMetadata(),
                productPagedModel.getLinks()
        );
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> fetchProductById(String id) {
        return productRepository.findById(id);
    }

    public Optional<Product> fetchProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    public boolean existsById(String id) {
        return productRepository.existsById(id);
    }

    public void deleteProductById(String id) {
        productRepository.deleteById(id);
    }

    public void deleteProductBySku(String sku) {
        productRepository.deleteBySku(sku);
    }

    public List<String> uploadProductImages(Product product, List<MultipartFile> files) throws IOException {
        List<String> existingImages = product.getImages() != null
                ? new java.util.ArrayList<>(product.getImages())
                : new java.util.ArrayList<>();
        List<String> newImageUrls = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            String key = product.getName() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

            String imageUrl = publicUrl + "/" + key;
            newImageUrls.add(imageUrl);
        }

        existingImages.addAll(newImageUrls);
        product.setImages(existingImages);
        productRepository.save(product);

        return newImageUrls;
    }

    public List<String> deleteProductImages(Product product, List<String> imageUrlsToDelete) {
        if (product.getImages() == null || product.getImages().isEmpty()) {
            return List.of();
        }

        List<String> updatedImages = new java.util.ArrayList<>(product.getImages());

        for (String imageUrl : imageUrlsToDelete) {
            String key = imageUrl.replace(publicUrl + "/", "");
            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
                updatedImages.remove(imageUrl);
            } catch (S3Exception e) {
                // Optionally log or handle the error
            }
        }

        product.setImages(updatedImages);
        productRepository.save(product);

        return updatedImages;
    }
}
