package com.melvstein.ecommerce.api.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.melvstein.ecommerce.api.domain.auth.refreshtoken.dto.RefreshTokenRequestDto;
import com.melvstein.ecommerce.api.domain.auth.refreshtoken.dto.RefreshTokenResponseDto;
import com.melvstein.ecommerce.api.domain.auth.refreshtoken.service.RefreshTokenService;
import com.melvstein.ecommerce.api.domain.auth.refreshtoken.document.RefreshToken;
import com.melvstein.ecommerce.api.domain.auth.usertoken.service.UserTokenService;
import com.melvstein.ecommerce.api.domain.user.dto.LoginResponseDto;
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

import java.time.Instant;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController extends BaseController {
    private final UserService userService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final UserTokenService userTokenService;
    private final RefreshTokenService refreshTokenService;

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
    public ResponseEntity<ApiResponse<LoginResponseDto>> userLogin(@RequestBody @Valid LoginRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<LoginResponseDto> response = ApiResponse.<LoginResponseDto>builder()
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

            User user = userService
                    .fetchUserByUsername(request.username())
                    .orElseThrow(() -> new ApiException(
                            UserResponseCode.USER_NOT_FOUND.getCode(),
                            UserResponseCode.USER_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            if (!user.isActive()) {
                throw new ApiException(
                        UserResponseCode.USER_ACCESS_DENIED.getCode(),
                        "User is not active",
                        HttpStatus.FORBIDDEN
                );
            }

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());

            String accessToken = jwtService.generateToken(request.username(), extraClaims);
            /*UserToken userToken = userTokenService.generatedUserToken(user.getId(), accessToken);
            UserToken savedToken = userTokenService.saveUserToken(userToken);*/

            RefreshToken refreshToken = refreshTokenService.generatedRefreshToken(user.getId());
            RefreshToken savedRefreshToken = refreshTokenService.saveRefreshToken(refreshToken);

            LoginResponseDto data = LoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(savedRefreshToken.getToken())
                    .build();

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

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDto>> refreshToken(@RequestBody @Valid RefreshTokenRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<RefreshTokenResponseDto> response = ApiResponse.<RefreshTokenResponseDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to refresh token")
                .data(null)
                .build();

        try {
            RefreshToken oldRefreshToken = refreshTokenService
                    .fetchRefreshTokenDetails(request.refreshToken())
                    .orElseThrow(() -> new ApiException(
                            UserResponseCode.INVALID_REFRESH_TOKEN.getCode(),
                            UserResponseCode.INVALID_REFRESH_TOKEN.getMessage(),
                            HttpStatus.BAD_REQUEST
                    ));

            if (oldRefreshToken.getExpiredAt().isBefore(Instant.now())) {
                refreshTokenService.deleteToken(oldRefreshToken.getToken());

                throw new ApiException(
                        UserResponseCode.REFRESH_TOKEN_EXPIRED.getCode(),
                        UserResponseCode.REFRESH_TOKEN_EXPIRED.getMessage(),
                        HttpStatus.UNAUTHORIZED
                );
            }

            if (!oldRefreshToken.getUserId().equals(request.userId())) {
                throw new ApiException(
                        UserResponseCode.USER_NOT_FOUND.getCode(),
                        UserResponseCode.USER_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND
                );
            }

            User user = userService
                    .fetchUserById(request.userId())
                    .orElseThrow(() -> new ApiException(
                            UserResponseCode.USER_NOT_FOUND.getCode(),
                            UserResponseCode.USER_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());

            String accessToken = jwtService.generateToken(user.getUsername(), extraClaims);

            RefreshToken refreshToken = refreshTokenService.generatedRefreshToken(user.getId());
            RefreshToken savedRefreshToken = refreshTokenService.saveRefreshToken(refreshToken);
            refreshTokenService.deleteToken(oldRefreshToken.getToken());

            RefreshTokenResponseDto data = RefreshTokenResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(savedRefreshToken.getToken())
                    .build();

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Refreshed token successfully");
            response.setData(data);

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

    // implement logout endpoint to delete user refresh token
    // create authentication serviec and endpoint to check if user is authenticated

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
    public ResponseEntity<ApiResponse<UserDto>> fetchUserById(@PathVariable String id) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to get user")
                .data(null)
                .build();

        try {
            User user = userService
                    .fetchUserById(id)
                    .orElseThrow(() -> new ApiException(
                            UserResponseCode.USER_NOT_FOUND.getCode(),
                            UserResponseCode.USER_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

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
            User user = userService
                    .fetchUserById(id)
                    .orElseThrow(() -> new ApiException(
                            UserResponseCode.USER_NOT_FOUND.getCode(),
                            UserResponseCode.USER_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

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
            User user = userService
                    .fetchUserById(id)
                    .orElseThrow(() -> new ApiException(
                            UserResponseCode.USER_NOT_FOUND.getCode(),
                            UserResponseCode.USER_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

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
