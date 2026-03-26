package com.vms.vendor.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.vms.vendor.client.AuthFeignClient;
import com.vms.vendor.dto.request.VendorRequest;
import com.vms.vendor.dto.response.VendorResponse;
import com.vms.vendor.entity.VendorEntity;
import com.vms.vendor.entity.VendorStatus;
import com.vms.vendor.exception.ResourceNotFoundException;
import com.vms.vendor.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final VendorRepository repository;
    private final ModelMapper mapper;
    private final AuthFeignClient authFeignClient;

    @Override
    public VendorResponse createVendor(VendorRequest request) {
        VendorEntity vendor = mapper.map(request, VendorEntity.class);
        vendor.setStatus(VendorStatus.PENDING);
        vendor.setCreatedAt(LocalDateTime.now());
        vendor = repository.save(vendor);

        authFeignClient.createVendorUser(
            Map.of("email", request.getEmail())
        );

        return mapper.map(vendor, VendorResponse.class);
    }

    @Override
    public List<VendorResponse> getAllVendors() {
        return repository.findAll()
                .stream()
                .map(vendor -> mapper.map(vendor, VendorResponse.class))
                .toList();
    }

    @Override
    public VendorResponse approveVendor(UUID id) {
        VendorEntity vendor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendor.setStatus(VendorStatus.APPROVED);
        repository.save(vendor);
        return mapper.map(vendor, VendorResponse.class);
    }

    @Override
    public VendorResponse rejectVendor(UUID id) {
        VendorEntity vendor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendor.setStatus(VendorStatus.REJECTED);
        repository.save(vendor);
        return mapper.map(vendor, VendorResponse.class);
    }

   

    // ✅ NEW: Find vendor by email — used by GET /api/vendors/me
    // The JWT subject (username) is the vendor's email, so we look up by that.
    @Override
    public VendorResponse getByEmail(String email) {
        VendorEntity vendor = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found for email: " + email));
        return mapper.map(vendor, VendorResponse.class);
    }
}