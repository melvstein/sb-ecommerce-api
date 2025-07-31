package com.melvstein.ecommerce.api.domain.customer.document;

import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Jacksonized
public class Customer {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Email
    @Indexed(unique = true)
    private String email;

    private String firstName;
    private String middleName;
    private String lastName;
    private String contactNumber;
    private String provider;
    private String profileImageUrl;

    private Address address;

    @Builder.Default
    private boolean isActive = true;

    @Builder.Default
    private boolean isVerified = false;

    private Instant lastLoginAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
