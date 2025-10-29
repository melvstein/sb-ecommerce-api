package com.melvstein.ecommerce.api.domain.cart.controller;

import com.melvstein.ecommerce.api.domain.cart.document.Cart;
import com.melvstein.ecommerce.api.domain.cart.document.Checkout;
import com.melvstein.ecommerce.api.domain.cart.dto.CheckoutDto;
import com.melvstein.ecommerce.api.domain.cart.dto.CheckoutRequestDto;
import com.melvstein.ecommerce.api.domain.cart.mapper.CartMapper;
import com.melvstein.ecommerce.api.domain.cart.mapper.CheckoutMapper;
import com.melvstein.ecommerce.api.domain.cart.service.CartService;
import com.melvstein.ecommerce.api.domain.cart.service.CheckoutService;
import com.melvstein.ecommerce.api.domain.customer.document.Customer;
import com.melvstein.ecommerce.api.domain.customer.service.CustomerService;
import com.melvstein.ecommerce.api.shared.dto.ApiResponse;
import com.melvstein.ecommerce.api.shared.exception.ApiException;
import com.melvstein.ecommerce.api.shared.util.ApiResponseCode;
import com.melvstein.ecommerce.api.shared.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkouts")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {
    private final CheckoutService checkoutService;
    private final CustomerService customerService;
    private final CartService cartService;

    public ResponseEntity<ApiResponse<CheckoutDto>> checkoutItems(@RequestBody @Valid CheckoutRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiResponse<CheckoutDto> response = ApiResponse.<CheckoutDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("An unexpected error occurred")
                .data(null)
                .build();

        try {
            Cart cart = cartService.getCartById(request.cartId())
                    .orElseThrow(() -> new ApiException(
                            ApiResponseCode.NOT_FOUND.getCode(),
                            "Cart not found",
                            HttpStatus.NOT_FOUND
                    ));

            Checkout saveCheckout = Checkout.builder()
                    .cartId(cart.getId())
                    .customerId(cart.getCustomerId())
                    .items(cart.getItems())
                    .build();

            Checkout checkout = checkoutService.save(saveCheckout);

            response.setMessage("Items checkout successfully");
            response.setData(CheckoutMapper.toDto(checkout));
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
