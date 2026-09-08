package com.petstore.user.controller;

import com.petstore.user.dto.ApiResponse;
import com.petstore.user.dto.UpdateProfileRequest;
import com.petstore.user.dto.UserProfileResponse;
import com.petstore.user.entity.User;
import com.petstore.user.service.AuthService;
import com.petstore.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "User profile retrieval and update endpoints")
public class ProfileController {

    private final UserService userService;
    private final AuthService authService;

    public ProfileController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get user profile", description = "Fetches the current authenticated user's profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        User user = authService.getAuthenticatedUser();
        UserProfileResponse profile = userService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", profile));
    }

    @PutMapping
    @Operation(summary = "Update user profile", description = "Updates profile information for the authenticated user")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = authService.getAuthenticatedUser();
        UserProfileResponse profile = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }
}
