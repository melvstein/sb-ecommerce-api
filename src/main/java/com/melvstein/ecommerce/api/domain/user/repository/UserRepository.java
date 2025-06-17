package com.melvstein.ecommerce.api.domain.user.repository;

import com.melvstein.ecommerce.api.domain.user.document.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    public Optional<User> findByUsername(String username);
    public Optional<User> findByEmail(String email);
    public boolean existsByEmail(String email);
}
