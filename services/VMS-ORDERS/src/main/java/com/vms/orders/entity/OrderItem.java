package com.vms.orders.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="order_items")
@Data
public class OrderItem {

    @Id
    @GeneratedValue
    private Long id;


    @ManyToOne
    @JoinColumn(name = "order_id")
    private PurchaseOrder order;

    private String productName;

    private Integer quantity;

    private Double price;
}