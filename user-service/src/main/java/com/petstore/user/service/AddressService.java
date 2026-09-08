package com.petstore.user.service;

import com.petstore.user.dto.AddressRequest;
import com.petstore.user.dto.AddressResponse;
import com.petstore.user.entity.Address;
import com.petstore.user.entity.User;
import com.petstore.user.exception.ResourceNotFoundException;
import com.petstore.user.repository.AddressRepository;
import com.petstore.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressResponse getAddress(Long id, Long userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));
        return AddressResponse.fromEntity(address);
    }

    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        boolean isDefault = request.isDefault();
        long count = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).size();
        if (count == 0) {
            isDefault = true; // First address is default by default
        }

        if (isDefault) {
            addressRepository.unsetDefaultForUser(userId);
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
                isDefault
        );

        Address saved = addressRepository.save(address);
        logger.info("Created address {} for user {}", saved.getId(), userId);
        return AddressResponse.fromEntity(saved);
    }

    @Transactional
    public AddressResponse updateAddress(Long id, Long userId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        if (request.isDefault() && !address.isDefault()) {
            addressRepository.unsetDefaultForUser(userId);
            address.setDefault(true);
        }

        address.setName(request.getName().trim());
        address.setPhone(request.getPhone().trim());
        address.setAddressLine1(request.getAddressLine1().trim());
        address.setAddressLine2(request.getAddressLine2() != null ? request.getAddressLine2().trim() : null);
        address.setCity(request.getCity().trim());
        address.setState(request.getState().trim());
        address.setPincode(request.getPincode().trim());

        Address updated = addressRepository.save(address);
        logger.info("Updated address {} for user {}", updated.getId(), userId);
        return AddressResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteAddress(Long id, Long userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        boolean wasDefault = address.isDefault();
        addressRepository.delete(address);
        logger.info("Deleted address {} for user {}", id, userId);

        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setDefault(true);
                addressRepository.save(newDefault);
                logger.info("Promoted address {} to default for user {}", newDefault.getId(), userId);
            }
        }
    }

    @Transactional
    public AddressResponse setDefaultAddress(Long id, Long userId) {
        Address address = addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", id));

        addressRepository.unsetDefaultForUser(userId);
        address.setDefault(true);
        Address updated = addressRepository.save(address);
        logger.info("Set address {} as default for user {}", id, userId);
        return AddressResponse.fromEntity(updated);
    }
}
