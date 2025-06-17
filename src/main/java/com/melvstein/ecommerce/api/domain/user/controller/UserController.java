package com.melvstein.ecommerce.api.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melvstein.ecommerce.api.shared.controller.BaseController;
import com.melvstein.ecommerce.api.domain.user.enums.Role;
import com.melvstein.ecommerce.api.domain.user.mapper.UserMapper;
import com.melvstein.ecommerce.api.domain.user.enums.UserResponseCode;
import com.melvstein.ecommerce.api.domain.user.document.User;
import com.melvstein.ecommerce.api.domain.user.dto.LoginRequestDto;
import com.melvstein.ecommerce.api.domain.user.dto.RegisterRequestDto;
import com.melvstein.ecommerce.api.domain.user.dto.UserDto;
import com.melvstein.ecommerce.api.domain.user.service.UserService;
import com.melvstein.ecommerce.api.shared.dto.ApiResponse;
import com.melvstein.ecommerce.api.shared.exception.ApiException;
import com.melvstein.ecommerce.api.security.JwtService;
import com.melvstein.ecommerce.api.shared.util.ApiResponseCode;
import com.melvstein.ecommerce.api.shared.util.Utils;
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

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController extends BaseController {
    private final UserService userService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> userRegister(@RequestBody @Valid RegisterRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to register user")
                .data(null)
                .build();

        try {
            /*boolean userAlreadyExists = userService.userAlreadyExists(request.email());

            if (userAlreadyExists) {
                throw new ApiException(
                        UserResponseCode.USER_ALREADY_EXISTS.getCode(),
                        UserResponseCode.USER_ALREADY_EXISTS.getMessage(),
                        HttpStatus.BAD_REQUEST
                );
            }*/

            if (!Role.isValid(request.role())) {
                throw new ApiException(
                        UserResponseCode.INVALID_ROLE.getCode(),
                        UserResponseCode.INVALID_ROLE.getMessage() + " '" + request.role() + "'; must be in " + Arrays.toString(Role.values()).toLowerCase(),
                        HttpStatus.BAD_REQUEST
                );
            }

            User userRegister = User.builder()
                    .role(request.role())
                    .email(request.email())
                    .username(request.username())
                    .password(request.password())
                    .build();

            User user = userService.saveUser(userRegister);

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("User registered successfully");
            response.setData(UserMapper.toDto(user));

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());

            if (e.getMessage() != null && e.getMessage().contains("E11000")) {
                String fieldInfo = e.getMessage().split("dup key:")[1].trim();
                httpStatus = HttpStatus.BAD_REQUEST;
                response.setCode(UserResponseCode.USER_ALREADY_EXISTS.getCode());
                response.setMessage("Duplicate entry: " + fieldInfo);
            }
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> userLogin(@RequestBody @Valid LoginRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to login user")
                .data(null)
                .build();

        try {
            if (!userService.isAuthenticated(request)) {
                throw new ApiException(
                        UserResponseCode.USER_UNAUTHORIZED.getCode(),
                        "Invalid username and password",
                        HttpStatus.UNAUTHORIZED
                );
            }

            Optional<User> loggedInUser = userService.findUserByUsername(request.username());

            if (loggedInUser.isEmpty()) {
                throw new ApiException(
                        UserResponseCode.USER_NOT_FOUND.getCode(),
                        UserResponseCode.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND
                );
            }

            User user = loggedInUser.get();

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());
            extraClaims.put("role", user.getRole());
            extraClaims.put("email", user.getEmail());
            extraClaims.put("username", user.getUsername());

            String token = jwtService.generateToken(request.username(), extraClaims);

            Map<String, String> data = new HashMap<>();
            data.put("token", token);

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("User logged in successfully");
            response.setData(data);

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (BadCredentialsException e) {
            httpStatus = HttpStatus.UNAUTHORIZED;
            response.setMessage("Invalid username or password");
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedModel<EntityModel<UserDto>>>> getAllUsers(
            @RequestParam(value = "filter", required = false) List<String> filter,
            Pageable pageable
    ) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<PagedModel<EntityModel<UserDto>>> response = ApiResponse.<PagedModel<EntityModel<UserDto>>>builder()
                .code(ApiResponseCode.ERROR.getCode())
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

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Fetched all users successfully");
            response.setData(userDtoPagedModel);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> findUserById(@PathVariable String id) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to get user")
                .data(null)
                .build();

        try {
            Optional<User> existingUser = userService.findUserById(id);

            if (existingUser.isEmpty()) {
                throw new ApiException(
                        UserResponseCode.USER_NOT_FOUND.getCode(),
                        UserResponseCode.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND
                );
            }

            User user = existingUser.get();

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Fetched user details successfully");
            response.setData(UserMapper.toDto(user));

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable String id, @RequestBody Map<String, Object> request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to update user details")
                .data(null)
                .build();

        try {
            Optional<User> existingUser = userService.findUserById(id);

            if (existingUser.isEmpty()) {
                throw new ApiException(
                        UserResponseCode.USER_NOT_FOUND.getCode(),
                        UserResponseCode.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND
                );
            }

            User user = existingUser.get();

            request.forEach((key, value) -> {
                switch (key) {
                    case "role":
                        if (value instanceof String) {
                            user.setRole((String) value);
                        }
                        break;
                    case "email":
                        if (value instanceof String) {
                            user.setEmail((String) value);
                        }
                        break;
                    case "username":
                        if (value instanceof String) {
                            user.setUsername((String) value);
                        }
                        break;
                    case "password":
                        if (value instanceof String) {
                            user.setPassword((String) value);
                        }
                        break;
                    case "profileImageUrl":
                        if (value instanceof String) {
                            user.setProfileImageUrl((String) value);
                        }
                        break;
                    case "isActive":
                        if (value instanceof Boolean) {
                            user.setActive((Boolean) value);
                        }
                        break;
                    case "isVerified":
                        if (value instanceof Boolean) {
                            user.setVerified((Boolean) value);
                        }
                    case "lastLoginAt":
                        if (value instanceof Date) {
                            log.info("lastLoginAt Date {}", value);
                            user.setLastLoginAt(((Date) value).toInstant());
                        } else if (value instanceof String) {
                            log.info("lastLoginAt String {}", value);
                            user.setLastLoginAt(Utils.fromDateStringToInstant((String) value));
                        }
                        break;
                    default:
                        log.warn("{} - Ignore unknown field key={} value={}", Utils.getClassAndMethod(), key, value);
                        break;
                }
            });

            User savedUser = userService.saveUser(user);

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("User details updated successfully");
            response.setData(UserMapper.toDto(savedUser));

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> deleteUserById(@PathVariable String id) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to delete user")
                .data(null)
                .build();

        try {
            Optional<User> existingUser = userService.findUserById(id);

            if (existingUser.isEmpty()) {
                throw new ApiException(
                        UserResponseCode.USER_NOT_FOUND.getCode(),
                        UserResponseCode.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND
                );
            }

            User user = existingUser.get();
            userService.deleteUserById(user.getId());

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("User deleted successfully");
            response.setData(UserMapper.toDto(user));

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }
}
