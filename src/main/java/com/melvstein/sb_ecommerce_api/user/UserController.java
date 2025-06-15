package com.melvstein.sb_ecommerce_api.user;

import com.melvstein.sb_ecommerce_api.controller.BaseController;
import com.melvstein.sb_ecommerce_api.dto.ApiResponse;
import com.melvstein.sb_ecommerce_api.product.ProductDto;
import com.melvstein.sb_ecommerce_api.security.JwtService;
import com.melvstein.sb_ecommerce_api.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController extends BaseController {
    private final UserService userService;
    private final JwtService jwtService;

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
        ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .message("Failed to register user")
                .data(null)
                .build();

        try {
            boolean userAlreadyExists = userService.userAlreadyExists(request.email());

            if (userAlreadyExists) {
                response.setMessage("User Already Exists");
            } else {
                User userRegister = User.builder()
                        .role(request.role())
                        .email(request.email())
                        .username(request.username())
                        .password(request.password())
                        .build();

                User user = userService.saveUser(userRegister);

                response.setMessage("User registered successfully");
                response.setData(UserMapper.toDto(user));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());

            log.error("{} - Failed to register user - error={}", Utils.getClassAndMethod(), response.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> userLogin(@RequestBody @Valid LoginRequest request) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .message("Failed to login user")
                .data(null)
                .build();

        try {
            if (!userService.isAuthenticated(request)) {
                return ResponseEntity.internalServerError().body(response);
            }

            String jwtToken = jwtService.generateToken(request.username(), null);

            Map<String, String> data = new HashMap<>();
            data.put("jwtToken", jwtToken);

            response.setMessage("User logged in successfully");
            response.setData(data);

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            response.setMessage("Invalid username or password");

            log.error("{} - Failed to login user - error={}", Utils.getClassAndMethod(), response.getMessage());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());

            log.error("{} - Failed to login user - error={}", Utils.getClassAndMethod(), response.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }
}
