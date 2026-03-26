package com.vms.vendor.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vms.vendor.dto.request.VendorRequest;
import com.vms.vendor.dto.response.VendorResponse;
import com.vms.vendor.entity.VendorEntity;
import com.vms.vendor.repository.VendorRepository;
import com.vms.vendor.service.VendorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService service;
    private final VendorRepository repository;

    @PostMapping
    public ResponseEntity<VendorResponse> createVendor(@RequestBody VendorRequest request) {
        return ResponseEntity.ok(service.createVendor(request));
    }

    @GetMapping
    public ResponseEntity<List<VendorResponse>> getAllVendors() {
        return ResponseEntity.ok(service.getAllVendors());
    }

    // ✅ NEW: Returns the vendor profile for the currently authenticated vendor user.
    // The JWT subject is the vendor's email — we look up by email and return their record.
    // This is what the frontend calls after login to resolve the real vendor UUID.
    @GetMapping("/me")
    public ResponseEntity<VendorResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(service.getByEmail(userDetails.getUsername()));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<VendorResponse> approveVendor(@PathVariable UUID id) {
        return ResponseEntity.ok(service.approveVendor(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<VendorResponse> rejectVendor(@PathVariable UUID id) {
        return ResponseEntity.ok(service.rejectVendor(id));
    }

   

    @GetMapping("/{id}")
    public ResponseEntity<VendorResponse> getVendorById(@PathVariable String id) {
        VendorEntity vendor = repository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Vendor not found"));
        VendorResponse response = new VendorResponse();
        response.setId(vendor.getId());
        response.setCompanyName(vendor.getCompanyName());
        return ResponseEntity.ok(response);
    }
}