package com.petstore.user.controller;

import com.petstore.user.dto.AddressResponse;
import com.petstore.user.dto.ApiResponse;
import com.petstore.user.service.AddressService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
@Hidden // Internal microservice endpoint, not exposed publicly in Swagger
public class InternalUserController {

    private final AddressService addressService;

    public InternalUserController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/addresses/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getAddressForOrder(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId) {
        // If userId is provided, validate ownership; otherwise fetch by id
        AddressResponse address;
        if (userId != null) {
            address = addressService.getAddress(id, userId);
        } else {
            // Find by ID directly without user restriction for internal verification
            address = addressService.getUserAddresses(userId).stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            if (address == null) {
                address = addressService.getAddress(id, userId);
            }
        }
        return ResponseEntity.ok(ApiResponse.success("Address fetched", address));
    }
}
