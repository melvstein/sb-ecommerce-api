package com.melvstein.ecommerce.api.domain.user.service;

import com.melvstein.ecommerce.api.domain.user.document.User;
import com.melvstein.ecommerce.api.domain.security.authentication.dto.LoginRequestDto;
import com.melvstein.ecommerce.api.domain.user.enums.Role;
import com.melvstein.ecommerce.api.domain.user.enums.UserResponseCode;
import com.melvstein.ecommerce.api.domain.user.repository.UserRepository;
import com.melvstein.ecommerce.api.shared.exception.ApiException;
import com.melvstein.ecommerce.api.shared.util.Utils;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PagedResourcesAssembler<User> userPagedResourcesAssembler;
    private final AuthenticationManager authenticationManager;
    private final S3Client s3Client;
    private final String bucketName = "users-bucket";

    @Value("${r2.public.users-bucket.url}")
    private String publicUrl;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public PagedModel<EntityModel<User>> getAllUsers(@Nullable List<String> filter, Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPagedResourcesAssembler.toModel(userPage);
    }

    public Optional<User> fetchUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> fetchUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> fetchUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        // Normalize role early
        user.setRole(user.getRole().toLowerCase());

        if (!Role.isValid(user.getRole())) {
            throw new ApiException(
                    UserResponseCode.INVALID_ROLE.getCode(),
                    UserResponseCode.INVALID_ROLE.getMessage() +
                            " '" + user.getRole() + "'; must be one of: " +
                            Arrays.stream(Role.values())
                                    .map(Enum::name)
                                    .map(String::toLowerCase)
                                    .collect(Collectors.joining(", ")),
                    HttpStatus.BAD_REQUEST
            );
        }

        if (user.getId() != null) {
            // Update flow
            User existing = userRepository.findById(user.getId()).orElse(null);

            if (existing != null) {
                if (!user.getPassword().equals(existing.getPassword())) {
                    user.setPassword(Utils.bCryptPasswordEncoder().encode(user.getPassword()));
                } else {
                    user.setPassword(existing.getPassword());
                }
            } else {
                // User not found but ID is given, treat as new
                user.setPassword(Utils.bCryptPasswordEncoder().encode(user.getPassword()));
            }
        } else {
            // New user
            user.setPassword(Utils.bCryptPasswordEncoder().encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    private String getPassword(User existing) {
        return existing.getPassword();
    }

    public boolean userAlreadyExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public void deleteUserById(String id) {
        userRepository.deleteById(id);
    }

    public boolean isValidCredentials(LoginRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            return authentication.isAuthenticated();
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean updateLastLogin(User user) {
        if (user == null || user.getId() == null) {
            return false;
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        return true;
    }

    public String uploadUserProfileImage(User user, MultipartFile file) throws IOException {
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            String oldImageUrl = user.getProfileImageUrl();
            String oldKey = oldImageUrl.replace(publicUrl + "/", "");

            try {
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(oldKey)
                        .build();
                s3Client.deleteObject(deleteObjectRequest);
            } catch (S3Exception e) {
                // Optional: Log or handle failure to delete old image
                System.err.println("Failed to delete old image: " + e.awsErrorDetails().errorMessage());
            }
        }

        String key = user.getUsername() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));

        String profileImageUrl = publicUrl + "/" + key;
        user.setProfileImageUrl(profileImageUrl);
        userRepository.save(user);
        return profileImageUrl;
    }
}
