package com.melvstein.sb_ecommerce_api.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melvstein.sb_ecommerce_api.controller.BaseController;
import com.melvstein.sb_ecommerce_api.dto.ApiResponse;
import com.melvstein.sb_ecommerce_api.exception.ApiException;
import com.melvstein.sb_ecommerce_api.util.ApiResponseCode;
import com.melvstein.sb_ecommerce_api.util.Utils;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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


    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> saveProduct(@RequestBody @Valid ProductRequest request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<ProductDto> response = ApiResponse.<ProductDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to save product")
                .data(null)
                .build();

        try {
            Optional<Product> existingProduct = productService.getProductBySku(request.sku());

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
            Optional<Product> existingProduct = productService.getProductById(id);

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
            Optional<Product> existingProduct = productService.getProductById(id);

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
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }
}
