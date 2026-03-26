package com.vms.performance.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vms.performance.client.OrderClient;
import com.vms.performance.client.PaymentClient;
import com.vms.performance.client.VendorClient;
import com.vms.performance.dto.OrderResponse;
import com.vms.performance.dto.PageResponse;
import com.vms.performance.dto.PaymentResponse;
import com.vms.performance.dto.VendorResponse;
import com.vms.performance.entity.VendorPerformance;
import com.vms.performance.repository.VendorPerformanceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorPerformanceService {

    private final VendorPerformanceRepository repository;
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;
    private final VendorClient vendorClient;

    public VendorPerformance calculatePerformance(String vendorId) {

        PageResponse<OrderResponse> page = orderClient.getOrders(vendorId);
        List<OrderResponse> orders = page.getContent();

        List<PaymentResponse> payments = paymentClient.getPayments(vendorId);

        int totalOrders = orders.size();

        int completedOrders = (int) orders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .count();

        int deliveredOrders = (int) orders.stream()
                .filter(o ->
                    "DELIVERED".equals(o.getStatus()) ||
                    "COMPLETED".equals(o.getStatus())
                )
                .count();

        String vendorName = "Unknown Vendor";
        try {
            VendorResponse vendor = vendorClient.getVendor(vendorId);
            log.info("Vendor response: {}", vendor);
            if (vendor != null && vendor.getCompanyName() != null) {
                vendorName = vendor.getCompanyName();
            }
        } catch (Exception e) {
            log.error("Vendor fetch FAILED for id {}", vendorId, e);
        }

        double deliveryScore   = (deliveredOrders * 100.0 / Math.max(totalOrders, 1));
        double qualityScore    = (completedOrders  * 100.0 / Math.max(totalOrders, 1));
        double complianceScore = 100;
        double overallScore    = (deliveryScore * 0.5 + qualityScore * 0.3 + complianceScore * 0.2);

        double avgDeliveryTime = orders.stream()
                .filter(o -> "DELIVERED".equals(o.getStatus()))
                .mapToLong(o -> java.time.Duration.between(
                        o.getCreatedAt(), LocalDateTime.now()).toHours())
                .average()
                .orElse(0);

        VendorPerformance vp = repository.findById(vendorId)
                .orElse(new VendorPerformance());

        vp.setVendorId(vendorId);
        vp.setVendorName(vendorName);
        vp.setTotalOrders(totalOrders);
        vp.setCompletedOrders(completedOrders);
        vp.setOnTimeDeliveries(deliveredOrders);
        vp.setAverageDeliveryTime(avgDeliveryTime);
        vp.setDeliveryScore(deliveryScore);
        vp.setQualityScore(qualityScore);
        vp.setComplianceScore(complianceScore);
        vp.setOverallScore(overallScore);
        vp.setCalculatedAt(LocalDateTime.now());

        return repository.save(vp);
    }

    public List<VendorPerformance> calculateAllPerformance() {
        List<String> vendorIds = vendorClient.getAllVendors()
                .stream()
                .map(v -> v.getId().toString())
                .toList();

        return vendorIds.stream()
                .map(this::calculatePerformance)
                .toList();
    }

    // ✅ FIX: Instead of throwing "No data found" (which causes a 500),
    //    we now try two strategies before giving up:
    //
    //    1. Return the existing record if it exists (happy path).
    //    2. Auto-calculate on first access so a vendor who has never had
    //       their score computed still gets a real response.
    //    3. If calculation also fails (e.g. order service is down),
    //       return a safe empty record with zero scores rather than a 500.
    //
    //    This means the frontend always gets a 200 with valid JSON,
    //    and never crashes with "No data found".
    public VendorPerformance getPerformance(String vendorId) {

        // Strategy 1: record already exists
        if (repository.existsById(vendorId)) {
            return repository.findById(vendorId).get();
        }

        // Strategy 2: auto-calculate on first access
        log.info("No performance record for vendor {} — calculating now", vendorId);
        try {
            return calculatePerformance(vendorId);
        } catch (Exception e) {
            log.warn("Auto-calculation failed for vendor {} — returning empty record. Cause: {}",
                    vendorId, e.getMessage());
        }

        // Strategy 3: return a safe empty record (0 scores) so the frontend
        // renders "No Data" badges instead of crashing with a 500.
        VendorPerformance empty = new VendorPerformance();
        empty.setVendorId(vendorId);
        empty.setVendorName("Unknown Vendor");
        empty.setTotalOrders(0);
        empty.setCompletedOrders(0);
        empty.setOnTimeDeliveries(0);
        empty.setAverageDeliveryTime(0);
        empty.setDeliveryScore(0);
        empty.setQualityScore(0);
        empty.setComplianceScore(0);
        empty.setOverallScore(0);
        empty.setCalculatedAt(LocalDateTime.now());
        return empty;
    }

    public List<VendorPerformance> getAllPerformance() {
        return repository.findAll();
    }
}