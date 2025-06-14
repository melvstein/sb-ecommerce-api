package com.melvstein.sb_ecommerce_api.user;

import com.melvstein.sb_ecommerce_api.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<User>>>> getAllUsers(
            @RequestParam(value = "filter", required = false) List<String> filter,
            Pageable pageable
    ) {
        ApiResponse<PagedModel<EntityModel<User>>> response = ApiResponse.<PagedModel<EntityModel<User>>>builder()
                .message("Failed to get all users")
                .build();

        try {
            PagedModel<EntityModel<User>> userPagedModel = userService.getAllUsers(filter, pageable);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
