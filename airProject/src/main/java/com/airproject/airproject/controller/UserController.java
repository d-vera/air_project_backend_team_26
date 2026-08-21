package com.airproject.airproject.controller;

import com.airproject.airproject.dto.AssignRoleRequest;
import com.airproject.airproject.dto.UpdateUserRequest;
import com.airproject.airproject.dto.UserResponse;
import com.airproject.airproject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for user profile self-service and administrative user operations")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get own profile", description = "Retrieves profile details of the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> getOwnProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update own profile", description = "Updates profile details (first name, last name, password) of the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid field values"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> updateOwnProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                         @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateCurrentUser(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }



    @GetMapping
    @Operation(summary = "List all users", description = "Admin endpoint to retrieve all users in the system, including active and inactive accounts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin access required")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Admin endpoint to retrieve details of a specific user, including inactive accounts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin access required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Admin endpoint to update profile details of a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid field values"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin access required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete user", description = "Admin endpoint to soft-delete a user by setting active to false.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin access required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Assign role to user", description = "Admin endpoint to assign or modify a user's role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role value"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin access required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> assignRole(@PathVariable Long id,
                                                   @Valid @RequestBody AssignRoleRequest request) {
        UserResponse response = userService.assignRole(id, request);
        return ResponseEntity.ok(response);
    }
}
