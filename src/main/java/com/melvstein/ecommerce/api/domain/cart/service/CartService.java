package com.melvstein.ecommerce.api.domain.cart.service;

import com.melvstein.ecommerce.api.domain.cart.document.Cart;
import com.melvstein.ecommerce.api.domain.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    public static final String INCREASE = "increase";
    public static final String DECREASE = "decrease";

    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public void deleteCartByCustomerId(String customerId) {
        cartRepository.deleteByCustomerId(customerId);
    }

    public Optional<Cart> getCartByCustomerId(String customerId) {
        return cartRepository.findByCustomerId(customerId);
    }

    public Optional<Cart> getCartById(String id) {
        return cartRepository.findById(id);
    }
}
