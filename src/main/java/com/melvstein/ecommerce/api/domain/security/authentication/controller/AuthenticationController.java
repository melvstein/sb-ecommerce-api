package com.melvstein.ecommerce.api.domain.security.authentication.controller;

import com.melvstein.ecommerce.api.domain.security.authentication.dto.*;
import com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.document.RefreshToken;
import com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.service.RefreshTokenService;
import com.melvstein.ecommerce.api.domain.user.document.User;
import com.melvstein.ecommerce.api.domain.user.dto.UserDto;
import com.melvstein.ecommerce.api.domain.user.enums.Role;
import com.melvstein.ecommerce.api.domain.user.enums.UserResponseCode;
import com.melvstein.ecommerce.api.domain.user.mapper.UserMapper;
import com.melvstein.ecommerce.api.domain.user.service.UserService;
import com.melvstein.ecommerce.api.security.JwtService;
import com.melvstein.ecommerce.api.shared.dto.ApiResponse;
import com.melvstein.ecommerce.api.shared.exception.ApiException;
import com.melvstein.ecommerce.api.shared.util.ApiResponseCode;
import com.melvstein.ecommerce.api.shared.util.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@RequestBody @Valid RegisterRequestDto request) {
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
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody @Valid LoginRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<LoginResponseDto> response = ApiResponse.<LoginResponseDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to login user")
                .data(null)
                .build();

        try {
            if (!userService.isValidCredentials(request)) {
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

            String accessToken = jwtService.generateAccessToken(request.username(), extraClaims);
            /*UserToken userToken = userTokenService.generatedUserToken(user.getId(), accessToken);
            UserToken savedToken = userTokenService.saveUserToken(userToken);*/

            refreshTokenService.deleteAllTokensByUserId(user.getId());
            RefreshToken refreshToken = refreshTokenService.generatedRefreshToken(user);
            RefreshToken savedRefreshToken = refreshTokenService.saveRefreshToken(refreshToken);

            LoginResponseDto data = LoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(savedRefreshToken.getToken())
                    .build();

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("User logged in successfully");
            response.setData(data);

            userService.updateLastLogin(user);

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
    public ResponseEntity<ApiResponse<AuthenticationResponseDto>> refreshToken(@RequestHeader HttpHeaders headers) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<AuthenticationResponseDto> response = ApiResponse.<AuthenticationResponseDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to refresh token")
                .data(null)
                .build();

        String storedAccessToken = null;
        String storedRefreshToken = null;

        try {
            String authorizationHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new ApiException(
                        ApiResponseCode.UNAUTHORIZED.getCode(),
                        "Authorization header is missing or invalid",
                        HttpStatus.UNAUTHORIZED
                );
            }

            String refreshToken = authorizationHeader.substring(7);
            String username = jwtService.extractUsername(refreshToken);

            RefreshToken oldRefreshToken = refreshTokenService
                    .fetchRefreshTokenDetails(refreshToken)
                    .orElseThrow(() -> new ApiException(
                            ApiResponseCode.UNAUTHORIZED.getCode(),
                            "Refresh token not found",
                            HttpStatus.UNAUTHORIZED
                    ));

            if (username.isBlank()) {
                throw new ApiException(
                        ApiResponseCode.UNAUTHORIZED.getCode(),
                        "Username is missing from the token",
                        HttpStatus.UNAUTHORIZED
                );
            }

            User user = userService
                    .fetchUserByUsername(username)
                    .orElseThrow(() -> new ApiException(
                            UserResponseCode.USER_NOT_FOUND.getCode(),
                            "User not found",
                            HttpStatus.NOT_FOUND
                    ));

            if (!jwtService.isTokenValid(oldRefreshToken.getToken(), user.getUsername())) {
                throw new ApiException(
                        ApiResponseCode.UNAUTHORIZED.getCode(),
                        "Token is invalid or expired",
                        HttpStatus.UNAUTHORIZED
                );
            }

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("Token refreshed successfully");

            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("userId", user.getId());

            String accessToken = storedAccessToken = jwtService.generateAccessToken(user.getUsername(), extraClaims);

            boolean isDeleted = refreshTokenService.deleteAllTokensByUserId(user.getId());

            if (!isDeleted) {
                throw new ApiException(
                        ApiResponseCode.ERROR.getCode(),
                        "Failed to delete old refresh tokens",
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            RefreshToken newRefreshToken = refreshTokenService.generatedRefreshToken(user);
            storedRefreshToken = newRefreshToken.getToken();
            RefreshToken savedRefreshToken = refreshTokenService.saveRefreshToken(newRefreshToken);

            AuthenticationResponseDto data = AuthenticationResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(savedRefreshToken.getToken())
                    .build();

            response.setData(data);

            return ResponseEntity.ok(response);
        } catch (ApiException e) {
            httpStatus = e.getStatus();
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setMessage(e.getMessage());

            if (e.getMessage() != null && e.getMessage().contains("E11000")) {
                String fieldInfo = e.getMessage().split("dup key:")[1].trim();
                httpStatus = HttpStatus.OK;
                response.setCode(ApiResponseCode.SUCCESS.getCode());
                response.setMessage("Duplicate entry: " + fieldInfo);

                AuthenticationResponseDto data = AuthenticationResponseDto.builder()
                        .accessToken(storedAccessToken)
                        .refreshToken(storedRefreshToken)
                        .build();

                response.setData(data);
            }
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@RequestBody() @Valid() LogoutRequestDto request) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to logout user")
                .data(null)
                .build();

        try {
            User user = userService
                    .fetchUserById(request.userId())
                    .orElseThrow(() -> new ApiException(
                            UserResponseCode.USER_NOT_FOUND.getCode(),
                            UserResponseCode.USER_NOT_FOUND.getMessage(),
                            HttpStatus.NOT_FOUND
                    ));

            if (refreshTokenService.getAvailableToken(user.getId()).isEmpty()) {
                throw new ApiException(
                        ApiResponseCode.ERROR.getCode(),
                        "No active refresh token found for user",
                        HttpStatus.BAD_REQUEST
                );
            }

            refreshTokenService.deleteAllTokensByUserId(user.getId());

            response.setCode(ApiResponseCode.SUCCESS.getCode());
            response.setMessage("User logged out successfully");

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
