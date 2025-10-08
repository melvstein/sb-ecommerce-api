package com.melvstein.ecommerce.api.domain.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melvstein.ecommerce.api.shared.controller.BaseController;
import com.melvstein.ecommerce.api.domain.product.document.Product;
import com.melvstein.ecommerce.api.domain.product.dto.ProductDto;
import com.melvstein.ecommerce.api.domain.product.dto.ProductRequestDto;
import com.melvstein.ecommerce.api.domain.product.enums.ProductResponseCode;
import com.melvstein.ecommerce.api.domain.product.mapper.ProductMapper;
import com.melvstein.ecommerce.api.domain.product.service.ProductService;
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
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController extends BaseController {
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<ProductDto>>>> getAllProducts(
            @RequestParam(value = "filter", required = false) List<String> filter,
            Pageable pageable
    ) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<PagedModel<EntityModel<ProductDto>>> response = ApiResponse.<PagedModel<EntityModel<ProductDto>>>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to fetch all products")
                .data(null)
                .build();

        try {
            PagedModel<EntityModel<Product>> productPagedModel = productService.getAllProducts(filter, pageable);

            List<EntityModel<ProductDto>> productDtoContent = productPagedModel.getContent().stream()
                    .map(entityModel -> {
                        ProductDto productDto = ProductMapper.toDto(entityModel.getContent());
                        assert productDto != null;
                        return EntityModel.of(productDto);
                    })
                    .toList();

            PagedModel<EntityModel<ProductDto>> productDtoPagedModel = PagedModel.of(
                    productDtoContent,
                    productPagedModel.getMetadata(),
                    productPagedModel.getLinks()
            );

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Fetched all products successfully");
            response.setData(productDtoPagedModel);

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
    public ResponseEntity<ApiResponse<ProductDto>> fetchProductById(@PathVariable String id) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<ProductDto> response = ApiResponse.<ProductDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to get product")
                .data(null)
                .build();

        try {
            Optional<Product> existingProduct = productService.fetchProductById(id);

            if (existingProduct.isEmpty()) {
                throw new ApiException(
                        ProductResponseCode.PRODUCT_NOT_FOUND.getCode(),
                        ProductResponseCode.PRODUCT_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND
                );
            }

            Product product = existingProduct.get();

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Fetched product successfully");
            response.setData(ProductMapper.toDto(product));

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

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductBySku(@PathVariable String sku) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<ProductDto> response = ApiResponse.<ProductDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to get product")
                .data(null)
                .build();

        try {
            Optional<Product> existingProduct = productService.fetchProductBySku(sku);

            if (existingProduct.isEmpty()) {
                throw new ApiException(
                        ProductResponseCode.PRODUCT_NOT_FOUND.getCode(),
                        ProductResponseCode.PRODUCT_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND
                );
            }

            Product product = existingProduct.get();

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Fetched product successfully");
            response.setData(ProductMapper.toDto(product));

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

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> saveProduct(@RequestBody @Valid ProductRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<ProductDto> response = ApiResponse.<ProductDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to save product")
                .data(null)
                .build();

        try {
            Optional<Product> existingProduct = productService.fetchProductBySku(request.sku());

            if (existingProduct.isPresent()) {
                Product product = existingProduct.get();

                response.setMessage("Product already exists");
                response.setData(ProductMapper.toDto(product));
            } else {
                Product savedProduct = productService.saveProduct(ProductMapper.toDocument(request));

                response.setMessage("Product saved successfully");
                response.setData(ProductMapper.toDto(savedProduct));
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

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(@PathVariable String id, @RequestBody Map<String, Object> request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<ProductDto> response = ApiResponse.<ProductDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to update product")
                .data(null)
                .build();

        try {
            Optional<Product> existingProduct = productService.fetchProductById(id);

            if (existingProduct.isEmpty()) {
                throw new ApiException(
                        ProductResponseCode.PRODUCT_NOT_FOUND.getCode(),
                        ProductResponseCode.PRODUCT_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND
                );
            }

            Product product = existingProduct.get();

            request.forEach((key, value) -> {
                switch (key) {
                    case "name":
                        if (value instanceof String) {
                            product.setName((String) value);
                        }
                        break;
                    case "brand":
                        if (value instanceof String) {
                            product.setBrand((String) value);
                        }
                        break;
                    case "description":
                        if (value instanceof String) {
                            product.setDescription((String) value);
                        }
                        break;
                    case "price":
                        if (value instanceof Number) {
                            product.setPrice(new BigDecimal(value.toString()));
                        }
                        break;
                    case "stock":
                        if (value instanceof Integer) {
                            product.setStock((Integer) value);
                        }
                        break;
                    case "isActive":
                        if (value instanceof Boolean) {
                            product.setActive((Boolean) value);
                        }
                        break;
                    case "tags":
                        if (value instanceof List<?> rawTags && rawTags.stream().allMatch(elem -> elem instanceof String)) {
                            List<String> tags = rawTags.stream()
                                    .map(elem -> (String) elem)
                                    .toList();

                            product.setTags(tags);
                        }
                        break;
                    default:
                        log.warn("{} - Ignore unknown field key={} value={}", Utils.getClassAndMethod(), key, value);
                        break;
                }
            });

            Product savedProduct = productService.saveProduct(product);
            ProductDto productDto = ProductMapper.toDto(savedProduct);

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Product updated successfully");
            response.setData(productDto);

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
    public ResponseEntity<ApiResponse<ProductDto>> deleteProductBySku(@PathVariable String id) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<ProductDto> response = ApiResponse.<ProductDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to delete product")
                .data(null)
                .build();

        try {
            Optional<Product> existingProduct = productService.fetchProductById(id);

            if (existingProduct.isEmpty()) {
                throw new ApiException(
                        ProductResponseCode.PRODUCT_NOT_FOUND.getCode(),
                        ProductResponseCode.PRODUCT_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND
                );
            }

            Product product = existingProduct.get();
            ProductDto productDto = ProductMapper.toDto(product);

            productService.deleteProductById(id);

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Product deleted successfully");
            response.setData(productDto);

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

    @PostMapping("/{id}/upload-product-images")
    public ResponseEntity<ApiResponse<Object>> uploadProductImages(
            @PathVariable String id,
            @RequestParam("files") List<MultipartFile> files
    ) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to upload product images")
                .data(null)
                .build();

        try {
            Product product = productService
                    .fetchProductById(id)
                    .orElseThrow(() -> new ApiException(
                            ProductResponseCode.PRODUCT_NOT_FOUND.getCode(),
                            ProductResponseCode.PRODUCT_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            if (files == null || files.isEmpty() || files.stream().anyMatch(MultipartFile::isEmpty)) {
                throw new ApiException(
                        ApiResponseCode.FILE_UPLOAD_ERROR.getCode(),
                        "One or more files are empty",
                        HttpStatus.BAD_REQUEST
                );
            }

            for (MultipartFile file : files) {
                String fileType = file.getContentType();
                if (!List.of("image/jpeg", "image/png").contains(fileType)) {
                    throw new ApiException(
                            ApiResponseCode.FILE_UPLOAD_ERROR.getCode(),
                            "Invalid file type: " + fileType,
                            HttpStatus.BAD_REQUEST
                    );
                }
            }

            productService.uploadProductImages(product, files);

            // Fetch updated product to get all images
            Product updatedProduct = productService
                    .fetchProductById(id)
                    .orElseThrow(() -> new ApiException(
                            ProductResponseCode.PRODUCT_NOT_FOUND.getCode(),
                            ProductResponseCode.PRODUCT_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Product images uploaded successfully");
            response.setData(Collections.singletonMap("productImageUrls", updatedProduct.getImages()));

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

    @DeleteMapping("/{id}/delete-product-images")
    public ResponseEntity<ApiResponse<Object>> deleteProductImages(
            @PathVariable String id,
            @RequestBody List<Integer> imageIndexes
    ) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to delete product images")
                .data(null)
                .build();

        try {
            Product product = productService
                    .fetchProductById(id)
                    .orElseThrow(() -> new ApiException(
                            ProductResponseCode.PRODUCT_NOT_FOUND.getCode(),
                            ProductResponseCode.PRODUCT_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            List<String> images = product.getImages();
            if (images == null || images.isEmpty()) {
                throw new ApiException(
                        ApiResponseCode.FILE_UPLOAD_ERROR.getCode(),
                        "No images to delete",
                        HttpStatus.BAD_REQUEST
                );
            }

            List<String> imageUrlsToDelete = imageIndexes.stream()
                    .filter(idx -> idx >= 0 && idx < images.size())
                    .map(images::get)
                    .toList();

            List<String> updatedImages = productService.deleteProductImages(product, imageUrlsToDelete);

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Product images deleted successfully");
            response.setData(Collections.singletonMap("productImageUrls", updatedImages));
            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());
        return ResponseEntity.status(httpStatus).body(response);
    }
}
