package com.petstore.address.controller;

import com.petstore.address.dto.AddressDto;
import com.petstore.address.dto.CreateAddressRequest;
import com.petstore.address.dto.UpdateAddressRequest;
import com.petstore.address.service.AddressService;
import com.petstore.auth.security.UserPrincipal;
import com.petstore.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Addresses", description = "Customer delivery address management")
@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressDto>>> getUserAddresses(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<AddressDto> addresses = addressService.getUserAddresses(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressDto>> getAddressById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        AddressDto address = addressService.getAddressById(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressDto>> createAddress(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateAddressRequest request) {
        AddressDto created = addressService.createAddress(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Address added successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressDto>> updateAddress(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAddressRequest request) {
        AddressDto updated = addressService.updateAddress(currentUser.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Address updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        addressService.deleteAddress(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Address deleted successfully", null));
    }
}
