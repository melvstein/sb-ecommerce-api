package com.melvstein.sb_ecommerce_api.user;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    public Optional<User> findByUsername(String username);
    public Optional<User> findByEmail(String email);
    public boolean existsByEmail(String email);
}
