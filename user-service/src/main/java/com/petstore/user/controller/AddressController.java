package com.petstore.user.controller;

import com.petstore.user.dto.AddressRequest;
import com.petstore.user.dto.AddressResponse;
import com.petstore.user.dto.ApiResponse;
import com.petstore.user.entity.User;
import com.petstore.user.service.AddressService;
import com.petstore.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@Tag(name = "Addresses", description = "Customer delivery address management")
public class AddressController {

    private final AddressService addressService;
    private final AuthService authService;

    public AddressController(AddressService addressService, AuthService authService) {
        this.addressService = addressService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Get all addresses", description = "Lists all delivery addresses for the authenticated user")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses() {
        User user = authService.getAuthenticatedUser();
        List<AddressResponse> addresses = addressService.getUserAddresses(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Addresses retrieved", addresses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by id", description = "Retrieves a specific address belonging to the authenticated user")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddress(@PathVariable Long id) {
        User user = authService.getAuthenticatedUser();
        AddressResponse address = addressService.getAddress(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Address retrieved", address));
    }

    @PostMapping
    @Operation(summary = "Create address", description = "Adds a new delivery address for the authenticated user")
    public ResponseEntity<ApiResponse<AddressResponse>> createAddress(@Valid @RequestBody AddressRequest request) {
        User user = authService.getAuthenticatedUser();
        AddressResponse address = addressService.createAddress(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address created successfully", address));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update address", description = "Updates an existing delivery address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(@PathVariable Long id,
                                                                      @Valid @RequestBody AddressRequest request) {
        User user = authService.getAuthenticatedUser();
        AddressResponse address = addressService.updateAddress(id, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", address));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address", description = "Removes a delivery address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id) {
        User user = authService.getAuthenticatedUser();
        addressService.deleteAddress(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }

    @PatchMapping("/{id}/default")
    @Operation(summary = "Set default address", description = "Sets an address as the default delivery address")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(@PathVariable Long id) {
        User user = authService.getAuthenticatedUser();
        AddressResponse address = addressService.setDefaultAddress(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Default address updated", address));
    }
}
