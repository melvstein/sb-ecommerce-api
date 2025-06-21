package com.melvstein.ecommerce.api.domain.security.authentication.controller;

import com.melvstein.ecommerce.api.domain.security.authentication.dto.AuthenticationResponseDto;
import com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.document.RefreshToken;
import com.melvstein.ecommerce.api.domain.security.authentication.refreshtoken.service.RefreshTokenService;
import com.melvstein.ecommerce.api.domain.user.document.User;
import com.melvstein.ecommerce.api.domain.user.dto.LoginRequestDto;
import com.melvstein.ecommerce.api.domain.user.dto.LoginResponseDto;
import com.melvstein.ecommerce.api.domain.user.enums.UserResponseCode;
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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtService jwtService;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

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

            String accessToken = jwtService.generateAccessToken(user.getUsername(), extraClaims);

            refreshTokenService.deleteAllTokensByUserId(user.getId());
            RefreshToken newRefreshToken = refreshTokenService.generatedRefreshToken(user);
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
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }
}
