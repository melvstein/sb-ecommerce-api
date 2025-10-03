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

    public Cart saveCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public void deleteCartByCustomerId(String customerId) {
        cartRepository.deleteByCustomerId(customerId);
    }

    public Optional<Cart> getCartByCustomerId(String customerId) {
        return cartRepository.findByCustomerId(customerId);
    }
}
