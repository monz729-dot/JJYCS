package com.ysc.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String bankName; // 은행명

    @Column(nullable = false, length = 50)
    private String accountNumber; // 계좌번호

    @Column(nullable = false, length = 100)
    private String accountHolder; // 예금주

    @Column(nullable = false)
    private Boolean isActive = true; // 활성화 여부

    @Column(nullable = false)
    private Boolean isDefault = false; // 기본 계좌 여부

    @Column
    private Integer displayOrder = 0; // 표시 순서

    @Column(length = 500)
    private String description; // 설명

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}