package com.ycs.lms.dto.orders;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 주문 수정 요청 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderRequest {

    @Valid
    @NotNull(message = "수취인 정보는 필수입니다.")
    private CreateOrderRequest.Recipient recipient;

    @Valid
    @NotNull(message = "주문 상품은 필수입니다.")
    private List<CreateOrderRequest.Item> items;

    @Valid
    @NotNull(message = "주문 박스는 필수입니다.")
    private List<CreateOrderRequest.Box> boxes;

    @Valid
    @NotNull(message = "배송 정보는 필수입니다.")
    private CreateOrderRequest.Shipping shipping;

    @Valid
    @NotNull(message = "결제 정보는 필수입니다.")
    private CreateOrderRequest.Payment payment;
} 