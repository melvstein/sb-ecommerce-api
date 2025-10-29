package com.melvstein.ecommerce.api.domain.cart.service;

import com.melvstein.ecommerce.api.domain.cart.document.Checkout;
import com.melvstein.ecommerce.api.domain.cart.repository.CheckoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final CheckoutRepository checkoutRepository;

    public Checkout save(Checkout checkout) {
        return checkoutRepository.save(checkout);
    }

    public Optional<Checkout> getCheckoutById(String id) {
        return checkoutRepository.findById(id);
    }
}
