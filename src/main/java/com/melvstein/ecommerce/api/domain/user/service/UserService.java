package com.melvstein.ecommerce.api.domain.user.service;

import com.melvstein.ecommerce.api.domain.user.document.User;
import com.melvstein.ecommerce.api.domain.user.dto.LoginRequestDto;
import com.melvstein.ecommerce.api.domain.user.enums.Role;
import com.melvstein.ecommerce.api.domain.user.enums.UserResponseCode;
import com.melvstein.ecommerce.api.domain.user.repository.UserRepository;
import com.melvstein.ecommerce.api.shared.exception.ApiException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PagedResourcesAssembler<User> userPagedResourcesAssembler;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public PagedModel<EntityModel<User>> getAllUsers(@Nullable List<String> filter, Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPagedResourcesAssembler.toModel(userPage);
    }

    public Optional<User> findUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        if (!Role.isValid(user.getRole())) {
            throw new ApiException(
                    UserResponseCode.INVALID_ROLE.getCode(),
                    UserResponseCode.INVALID_ROLE.getMessage() + " '" + user.getRole() + "'; must be in " + Arrays.toString(Role.values()).toLowerCase(),
                    HttpStatus.BAD_REQUEST
            );
        }

        user.setRole(user.getRole().toLowerCase());
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public boolean userAlreadyExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public void deleteUserById(String id) {
        userRepository.deleteById(id);
    }

    public boolean isAuthenticated(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return authentication.isAuthenticated();
    }
}
