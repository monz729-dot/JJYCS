package com.ycs.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_tracking")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderTracking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrackingStatus status;
    
    @Column(name = "location_code")
    private String locationCode; // Location code (e.g.: ICN, BKK, etc.)
    
    @Column(name = "location_name")
    private String locationName; // Location name (e.g.: "Seoul", "Bangkok" etc.)
    
    @Column(name = "tracking_number")
    private String trackingNumber; // EMS or other tracking number
    
    @Column(columnDefinition = "TEXT")
    private String description; // Event description
    
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime; // Event timestamp
    
    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery; // Estimated delivery time
    
    @Column(name = "is_milestone")
    private Boolean isMilestone = false; // Is major milestone
    
    @Column(name = "operator_name")
    private String operatorName; // Operator name
    
    @Column(columnDefinition = "TEXT")
    private String notes; // Additional notes
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum TrackingStatus {
        // Order processing
        ORDER_RECEIVED("Order Received"),
        PAYMENT_CONFIRMED("Payment Confirmed"),
        PREPARING("Preparing"),
        
        // Warehouse operations
        WAREHOUSE_RECEIVED("Warehouse Received"),
        QUALITY_CHECK("Quality Check"),
        REPACKAGING("Repackaging"),
        PACKAGING_COMPLETE("Packaging Complete"),
        
        // Shipping preparation
        READY_FOR_SHIPPING("Ready for Shipping"),
        DEPARTURE_PREPARATION("Departure Preparation"),
        
        // In transit
        DEPARTED_ORIGIN("Departed Origin"),
        IN_TRANSIT_TO_GATEWAY("In Transit to Gateway"),
        ARRIVED_AT_GATEWAY("Arrived at Gateway"),
        CUSTOMS_CLEARANCE("Customs Clearance"),
        CUSTOMS_CLEARED("Customs Cleared"),
        
        // Delivery
        OUT_FOR_DELIVERY("Out for Delivery"),
        DELIVERY_ATTEMPT("Delivery Attempt"),
        DELIVERED("Delivered"),
        
        // Exception statuses
        DELAYED("Delayed"),
        HELD_AT_CUSTOMS("Held at Customs"),
        RETURNED_TO_SENDER("Returned to Sender"),
        DELIVERY_FAILED("Delivery Failed"),
        CANCELLED("Cancelled");
        
        private final String description;
        
        TrackingStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
    }
    
    public boolean isDelivered() {
        return status == TrackingStatus.DELIVERED;
    }
    
    public boolean isInTransit() {
        return status == TrackingStatus.DEPARTED_ORIGIN || 
               status == TrackingStatus.IN_TRANSIT_TO_GATEWAY ||
               status == TrackingStatus.ARRIVED_AT_GATEWAY ||
               status == TrackingStatus.CUSTOMS_CLEARANCE ||
               status == TrackingStatus.CUSTOMS_CLEARED ||
               status == TrackingStatus.OUT_FOR_DELIVERY;
    }
    
    public boolean isExceptionStatus() {
        return status == TrackingStatus.DELAYED ||
               status == TrackingStatus.HELD_AT_CUSTOMS ||
               status == TrackingStatus.RETURNED_TO_SENDER ||
               status == TrackingStatus.DELIVERY_FAILED ||
               status == TrackingStatus.CANCELLED;
    }
}