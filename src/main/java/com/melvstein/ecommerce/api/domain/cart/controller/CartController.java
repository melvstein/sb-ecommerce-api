package com.melvstein.ecommerce.api.domain.cart.controller;

import com.melvstein.ecommerce.api.domain.cart.document.Cart;
import com.melvstein.ecommerce.api.domain.cart.dto.AddToCartRequestDto;
import com.melvstein.ecommerce.api.domain.cart.dto.CartDto;
import com.melvstein.ecommerce.api.domain.cart.dto.MinusToCartRequestDto;
import com.melvstein.ecommerce.api.domain.cart.dto.RemoveItemFromCart;
import com.melvstein.ecommerce.api.domain.cart.mapper.CartMapper;
import com.melvstein.ecommerce.api.domain.cart.mapper.ItemMapper;
import com.melvstein.ecommerce.api.domain.cart.service.CartService;
import com.melvstein.ecommerce.api.shared.dto.ApiResponse;
import com.melvstein.ecommerce.api.shared.exception.ApiException;
import com.melvstein.ecommerce.api.shared.util.ApiResponseCode;
import com.melvstein.ecommerce.api.shared.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {
    private final CartService cartService;

    @PostMapping("/add-items")
    public ResponseEntity<ApiResponse<CartDto>> addToCart(@RequestBody @Valid AddToCartRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<CartDto> response = ApiResponse.<CartDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("An unexpected error occurred")
                .data(null)
                .build();

        try {
            Optional<Cart> existingCart = cartService.getCartByCustomerId(request.CustomerId());

            if (existingCart.isPresent()) {
                Cart cart = existingCart.get();

                request.items().forEach(newItem -> {
                    cart.getItems().stream()
                            .filter(i -> i.getProductId().equals(newItem.productId()))
                            .findFirst()
                            .ifPresentOrElse(
                                    existingItem ->
                                    {
                                        existingItem.setQuantity(existingItem.getQuantity() + newItem.quantity());
                                        existingItem.setUpdatedAt(Instant.now());
                                    },
                                    () -> {
                                        cart.getItems().add(ItemMapper.toDocument(newItem));
                                    }
                            );
                });

                Cart updatedCart = cartService.saveCart(cart);

                response.setMessage("Cart updated successfully");
                response.setData(CartMapper.toDto(updatedCart));
            } else {
                Cart cart = cartService.saveCart(CartMapper.toDocument(request));

                response.setMessage(ApiResponseCode.SUCCESS.getMessage());
                response.setData(CartMapper.toDto(cart));
            }

            response.setCode(ApiResponseCode.SUCCESS.getCode());

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

    @PostMapping("/minus-items")
    public ResponseEntity<ApiResponse<CartDto>> minusToCart(@RequestBody @Valid MinusToCartRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<CartDto> response = ApiResponse.<CartDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("An unexpected error occurred")
                .data(null)
                .build();

        try {
            Optional<Cart> existingCart = cartService.getCartByCustomerId(request.CustomerId());

            if (existingCart.isPresent()) {
                Cart cart = existingCart.get();

                request.items().forEach(newItem -> {
                    cart.getItems().stream()
                            .filter(i -> i.getProductId().equals(newItem.productId()))
                            .findFirst()
                            .ifPresentOrElse(
                                    existingItem -> {
                                        boolean isZeroQuantity = existingItem.getQuantity() - newItem.quantity() <= 0;

                                        if (existingItem.getQuantity() > 0 && !isZeroQuantity) {
                                            existingItem.setQuantity(existingItem.getQuantity() - newItem.quantity());
                                            existingItem.setUpdatedAt(Instant.now());
                                        } else {
                                            cart.getItems().remove(existingItem);
                                        }
                                    },
                                    () -> {
                                        throw new ApiException(
                                                ApiResponseCode.NOT_FOUND.getCode(),
                                                "Item with productId " + newItem.productId() + " not found in cart",
                                                HttpStatus.NOT_FOUND
                                        );
                                    }
                            );
                });

                Cart updatedCart = cartService.saveCart(cart);

                response.setMessage("Cart updated successfully");
                response.setData(CartMapper.toDto(updatedCart));
            } else {
                Cart cart = cartService.saveCart(CartMapper.toDocument(request));

                response.setMessage(ApiResponseCode.SUCCESS.getMessage());
                response.setData(CartMapper.toDto(cart));
            }

            response.setCode(ApiResponseCode.SUCCESS.getCode());

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

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CartDto>> getCartByCustomerId(@PathVariable String customerId) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<CartDto> response = ApiResponse.<CartDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("An unexpected error occurred")
                .data(null)
                .build();

        try {
            Cart cart = cartService.getCartByCustomerId(customerId)
                    .orElseThrow(()-> new ApiException(
                       ApiResponseCode.NOT_FOUND.getCode(),
                       ApiResponseCode.NOT_FOUND.getMessage(),
                       HttpStatus.NOT_FOUND
                    ));

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage(ApiResponseCode.SUCCESS.getMessage());
            response.setData(CartMapper.toDto(cart));

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

    @DeleteMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CartDto>> deleteCartByCustomerId(@PathVariable String customerId) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<CartDto> response = ApiResponse.<CartDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("An unexpected error occurred")
                .data(null)
                .build();

        try {
            Cart cart = cartService.getCartByCustomerId(customerId)
                    .orElseThrow(()-> new ApiException(
                            ApiResponseCode.NOT_FOUND.getCode(),
                            ApiResponseCode.NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            cartService.deleteCartByCustomerId(customerId);

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Cart deleted successfully");
            response.setData(CartMapper.toDto(cart));

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

    @PostMapping("remove-item")
    public ResponseEntity<ApiResponse<CartDto>> removeItemFromCart(@RequestBody RemoveItemFromCart request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<CartDto> response = ApiResponse.<CartDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("An unexpected error occurred")
                .data(null)
                .build();

        try {
            Cart cart = cartService.getCartByCustomerId(request.customerId())
                    .orElseThrow(()-> new ApiException(
                            ApiResponseCode.NOT_FOUND.getCode(),
                            ApiResponseCode.NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(request.productId()));

            if (!removed) {
                throw new ApiException(
                        ApiResponseCode.NOT_FOUND.getCode(),
                        "Item with productId " + request.productId() + " not found in cart",
                        HttpStatus.NOT_FOUND
                );
            }

            Cart updatedCart = cartService.saveCart(cart);

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Item removed from cart successfully");
            response.setData(CartMapper.toDto(updatedCart));

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
