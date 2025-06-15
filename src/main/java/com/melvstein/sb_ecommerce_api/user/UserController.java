package com.melvstein.sb_ecommerce_api.user;

import com.melvstein.sb_ecommerce_api.controller.BaseController;
import com.melvstein.sb_ecommerce_api.dto.ApiResponse;
import com.melvstein.sb_ecommerce_api.product.ProductDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController extends BaseController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<UserDto>>>> getAllUsers(
            @RequestParam(value = "filter", required = false) List<String> filter,
            Pageable pageable
    ) {
        ApiResponse<PagedModel<EntityModel<UserDto>>> response = ApiResponse.<PagedModel<EntityModel<UserDto>>>builder()
                .message("Failed to get all users")
                .build();

        try {
            PagedModel<EntityModel<User>> userPagedModel = userService.getAllUsers(filter, pageable);

            List<EntityModel<UserDto>> userDtoContent = userPagedModel.getContent().stream().map(entityModel -> {
                assert entityModel.getContent() != null;
                UserDto userDto =  UserMapper.toDto(entityModel.getContent());
                return EntityModel.of(userDto);
            }).toList();

            PagedModel<EntityModel<UserDto>> userDtoPagedModel = PagedModel.of(
                    userDtoContent,
                    userPagedModel.getMetadata(),
                    userPagedModel.getLinks()
            );

            response.setMessage("Fetched all users successfully");
            response.setData(userDtoPagedModel);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> userRegister(@RequestBody @Valid RegisterRequest request) {

    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDto>> userLogin(@RequestBody @Valid LoginRequest request) {
        ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .message("Failed to login user")
                .data(null)
                .build();

        try {
            Optional<User> existingUser = userService.getUserByEmail(request.email());

            if (existingUser.isEmpty()) {
                response.setMessage("User not found");
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(response);
            }

            User user = existingUser.get();

            response.setMessage("User logged in successfully");
            response.setData(UserMapper.toDto(user));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}
