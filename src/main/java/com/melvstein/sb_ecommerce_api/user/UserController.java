package com.melvstein.sb_ecommerce_api.user;

import com.melvstein.sb_ecommerce_api.controller.BaseController;
import com.melvstein.sb_ecommerce_api.dto.ApiResponse;
import com.melvstein.sb_ecommerce_api.exception.ApiException;
import com.melvstein.sb_ecommerce_api.security.JwtService;
import com.melvstein.sb_ecommerce_api.util.ApiResponseCode;
import com.melvstein.sb_ecommerce_api.util.Utils;
import com.mongodb.DuplicateKeyException;
import io.jsonwebtoken.Claims;
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

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> userRegister(@RequestBody @Valid RegisterRequest request) {
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
    public ResponseEntity<ApiResponse<Object>> userLogin(@RequestBody @Valid LoginRequest request) {
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

            Optional<User> loggedInUser = userService.getUserByUsername(request.username());

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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> deleteUserById(@PathVariable String id) {
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiResponse<UserDto> response = ApiResponse.<UserDto>builder()
                .code(ApiResponseCode.ERROR.getCode())
                .message("Failed to delete user")
                .data(null)
                .build();

        try {
            Optional<User> existingUser = userService.getUserById(id);

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
            response.setCode(e.getCode());
            response.setMessage(e.getMessage());
            httpStatus = e.getStatus();
        } catch (Exception e) {
            response.setMessage(e.getMessage());
        }

        log.error("{} - code={} message={}", Utils.getClassAndMethod(), response.getCode(), response.getMessage());

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }
}
