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
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController extends BaseController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<ProductDto>>>> getAllProducts(
            @RequestParam(value = "filter", required = false) List<String> filter,
            Pageable pageable
    ) {
        ApiResponse<PagedModel<EntityModel<ProductDto>>> response = ApiResponse.<PagedModel<EntityModel<ProductDto>>>builder()
                .message("Failed to fetch all products")
                .data(null)
                .build();

        try {
            PagedModel<EntityModel<Product>> productPagedModel = productService.getAllProducts(filter, pageable);

            List<EntityModel<ProductDto>> dtoContent = productPagedModel.getContent().stream()
                    .map(entityModel -> {
                        ProductDto dto = ProductMapper.toDto(entityModel.getContent());
                        assert dto != null;
                        return EntityModel.of(dto);
                    })
                    .collect(Collectors.toList());

            PagedModel<EntityModel<ProductDto>> dtoPagedModel = PagedModel.of(
                    dtoContent,
                    productPagedModel.getMetadata(),
                    productPagedModel.getLinks()
            );

            response.setMessage("Fetched all products successfully!");
            response.setData(dtoPagedModel);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setMessage(e.getMessage());

            log.error("{} - Failed to fetch all products - {}", Utils.getClassAndMethod(), response.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProductBySku(@PathVariable String id) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .message("Failed to delete product")
                .build();

        try {
            boolean doesExists = productService.existsById(id);

            if (!doesExists) {
                response.setMessage("Product not found");
            } else {
                productService.deleteProductById(id);
                response.setMessage("Product deleted successfully");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());

            log.error("{} - Failed to delete product - {}", Utils.getClassAndMethod(), response.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}
