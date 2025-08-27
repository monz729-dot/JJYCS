package com.ycs.lms.dto.label;

import lombok.Data;

@Data
public class ShippingLabelRequest {
    private Long orderId;
    private String senderName;
    private String senderAddress;
    private String recipientName;
    private String recipientAddress;
    private String trackingNumber;
    private String carrier;
}