package com.vms.performance.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderResponse {
    private Long orderId;
    private String vendorId;
    private String status;
    private LocalDateTime createdAt;
}
