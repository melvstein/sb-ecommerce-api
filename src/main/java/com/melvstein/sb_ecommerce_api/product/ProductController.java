package com.melvstein.sb_ecommerce_api.product;

import com.melvstein.sb_ecommerce_api.controller.BaseController;
import com.melvstein.sb_ecommerce_api.dto.ApiResponse;
import com.melvstein.sb_ecommerce_api.util.Utils;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController extends BaseController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getAllProducts(@RequestParam(required = false) List<String> filter, Pageable pageable) {
        ApiResponse<Page<ProductDto>> response = ApiResponse.<Page<ProductDto>>builder()
                .message("Failed to fetch all products")
                .data(null)
                .build();

        try {
            Page<Product> products = productService.getAllProducts(filter, pageable);
            Page<ProductDto> productDtos = products.map(ProductMapper::toDto);

            response.setMessage("Fetched all products successfully!");
            response.setData(productDtos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());

            log.error("{} - Failed to fetch all products - {}", Utils.getClassAndMethod(), response.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> saveProduct(@Valid @RequestBody ProductRequest request) {
        ApiResponse<ProductDto> response = ApiResponse.<ProductDto>builder()
                .message("Failed to save product")
                .data(null)
                .build();

        try {
            Product existingProduct = productService.getProductBySku(request.sku());

            if (existingProduct != null) {
                response.setMessage("Product already exists");
                response.setData(ProductMapper.toDto(existingProduct));
            } else {
                Product savedProduct = productService.saveProduct(ProductMapper.toDocument(request));

                response.setMessage("Product saved successfully");
                response.setData(ProductMapper.toDto(savedProduct));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());

            log.error("{} - Failed to save product - {}", Utils.getClassAndMethod(), response.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}
