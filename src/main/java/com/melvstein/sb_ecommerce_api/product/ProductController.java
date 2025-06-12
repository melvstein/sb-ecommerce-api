package com.melvstein.sb_ecommerce_api.product;

import com.melvstein.sb_ecommerce_api.controller.BaseController;
import com.melvstein.sb_ecommerce_api.dto.ApiResponse;
import com.melvstein.sb_ecommerce_api.util.Utils;
import jakarta.validation.Valid;
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
public class ProductController extends BaseController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        ApiResponse<List<ProductDto>> response = ApiResponse.<List<ProductDto>>builder()
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
    public ResponseEntity<ApiResponse<ProductDto>> saveProduct(@Valid @RequestBody ProductRequest request) {
        ApiResponse<ProductDto> response = ApiResponse.<ProductDto>builder()
                .message("Failed to save product")
                .data(null)
                .build();

        try {
            Product existingProduct = productService.findBySku(request.sku());

            if (existingProduct != null) {
                response.setMessage("Product already exists");
                response.setData(ProductMapper.toDto(existingProduct));
            } else {
                Product savedProduct = productService.save(ProductMapper.toDocument(request));
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
