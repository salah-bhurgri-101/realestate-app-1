package com.example.userservice.service;

import com.example.userservice.domain.Address;
import com.example.userservice.dto.request.AddressRequest;
import com.example.userservice.dto.response.AddressResponse;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.repository.AddressRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses(UUID userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse addAddress(UUID userId, AddressRequest request) {
        if (request.isDefault()) {
            clearExistingDefault(userId);
        }
        Address address = Address.builder()
                .userId(userId)
                .label(request.getLabel())
                .line1(request.getLine1())
                .line2(request.getLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(request.isDefault())
                .build();
        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        Address address = addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Address " + addressId + " not found for user " + userId));

        if (request.isDefault() && !address.isDefault()) {
            clearExistingDefault(userId);
        }

        address.setLabel(request.getLabel());
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setDefault(request.isDefault());

        return toResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Address " + addressId + " not found for user " + userId));
        addressRepository.delete(address);
    }

    private void clearExistingDefault(UUID userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(existing -> {
            existing.setDefault(false);
            addressRepository.save(existing);
        });
    }

    private AddressResponse toResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .label(a.getLabel())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .city(a.getCity())
                .state(a.getState())
                .postalCode(a.getPostalCode())
                .country(a.getCountry())
                .isDefault(a.isDefault())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
