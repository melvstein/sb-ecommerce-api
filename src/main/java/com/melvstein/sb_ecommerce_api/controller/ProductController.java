package com.melvstein.sb_ecommerce_api.controller;

import com.melvstein.sb_ecommerce_api.dto.ApiResponseDto;
import com.melvstein.sb_ecommerce_api.dto.ProductDto;
import com.melvstein.sb_ecommerce_api.mapper.ProductMapper;
import com.melvstein.sb_ecommerce_api.model.Product;
import com.melvstein.sb_ecommerce_api.service.ProductService;
import com.melvstein.sb_ecommerce_api.util.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<ProductDto>>> getAllProducts() {
        ApiResponseDto<List<ProductDto>> response = ApiResponseDto.<List<ProductDto>>builder()
                .message("Failed to fetch all products")
                .data(null)
                .build();

        try {
            List<Product> products = productService.findAll();

            response.setMessage("Fetched all products successfully!");
            response.setData(ProductMapper.toDtos(products));

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
    public ResponseEntity<ApiResponseDto<ProductDto>> saveProduct(@RequestBody Product product) {
        ApiResponseDto<ProductDto> response = ApiResponseDto.<ProductDto>builder()
                .message("Failed to save product")
                .data(null)
                .build();

        try {
            Product existingProduct = productService.findBySku(product.getSku());

            if (existingProduct != null) {
                response.setMessage("Product already exists");
                response.setData(ProductMapper.toDto(existingProduct));
            } else {
                Product savedProduct = productService.save(product);
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
