package com.petstore.address.service;

import com.petstore.address.dto.AddressDto;
import com.petstore.address.dto.CreateAddressRequest;
import com.petstore.address.dto.UpdateAddressRequest;
import com.petstore.address.entity.Address;
import com.petstore.address.repository.AddressRepository;
import com.petstore.auth.entity.User;
import com.petstore.auth.repository.UserRepository;
import com.petstore.common.exception.ResourceNotFoundException;
import com.petstore.common.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressDto> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(AddressDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressDto getAddressById(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this address");
        }

        return AddressDto.fromEntity(address);
    }

    @Transactional
    public AddressDto createAddress(Long userId, CreateAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Address> existing = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        boolean shouldBeDefault = request.isDefault() || existing.isEmpty();

        if (shouldBeDefault) {
            addressRepository.resetDefaultAddressForUser(userId);
        }

        Address address = new Address(
                user,
                request.getName().trim(),
                request.getPhone().trim(),
                request.getAddressLine1().trim(),
                request.getAddressLine2() != null ? request.getAddressLine2().trim() : null,
                request.getCity().trim(),
                request.getState().trim(),
                request.getPincode().trim(),
                shouldBeDefault
        );

        Address saved = addressRepository.save(address);
        return AddressDto.fromEntity(saved);
    }

    @Transactional
    public AddressDto updateAddress(Long userId, Long addressId, UpdateAddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to update this address");
        }

        if (request.isDefault() && !address.isDefault()) {
            addressRepository.resetDefaultAddressForUser(userId);
            address.setDefault(true);
        } else if (!request.isDefault() && address.isDefault()) {
            address.setDefault(false);
        }

        address.setName(request.getName().trim());
        address.setPhone(request.getPhone().trim());
        address.setAddressLine1(request.getAddressLine1().trim());
        address.setAddressLine2(request.getAddressLine2() != null ? request.getAddressLine2().trim() : null);
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPincode(request.getPincode().trim());

        Address updated = addressRepository.save(address);
        return AddressDto.fromEntity(updated);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this address");
        }

        addressRepository.delete(address);
    }
}
