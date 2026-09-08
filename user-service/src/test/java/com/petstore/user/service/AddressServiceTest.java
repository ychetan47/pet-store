package com.petstore.user.service;

import com.petstore.user.dto.AddressRequest;
import com.petstore.user.dto.AddressResponse;
import com.petstore.user.entity.Address;
import com.petstore.user.entity.Role;
import com.petstore.user.entity.User;
import com.petstore.user.repository.AddressRepository;
import com.petstore.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressService addressService;

    private User sampleUser;
    private Address sampleAddress;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Chetan Sharma", "chetan@example.com", "encodedPass", "9876543210", Role.CUSTOMER);
        sampleUser.setId(1L);

        sampleAddress = new Address(sampleUser, "Chetan Sharma", "9876543210", "123 Main St", "Apt 4",
                "Bengaluru", "Karnataka", "560001", true);
        sampleAddress.setId(10L);
    }

    @Test
    @DisplayName("Should retrieve addresses for a user")
    void shouldRetrieveUserAddresses() {
        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L))
                .thenReturn(Collections.singletonList(sampleAddress));

        List<AddressResponse> addresses = addressService.getUserAddresses(1L);

        assertNotNull(addresses);
        assertEquals(1, addresses.size());
        assertEquals("Bengaluru", addresses.get(0).getCity());
    }

    @Test
    @DisplayName("Should create address and mark as default if first address")
    void shouldCreateFirstAddressAsDefault() {
        AddressRequest request = new AddressRequest("Chetan", "9876543210", "123 Main St", null,
                "Bengaluru", "Karnataka", "560001", false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(1L)).thenReturn(Collections.emptyList());
        when(addressRepository.save(any(Address.class))).thenReturn(sampleAddress);

        AddressResponse response = addressService.createAddress(1L, request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    @DisplayName("Should delete address")
    void shouldDeleteAddress() {
        when(addressRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(sampleAddress));

        addressService.deleteAddress(10L, 1L);

        verify(addressRepository, times(1)).delete(sampleAddress);
    }
}
