package com.ycs.lms.service;

import com.ycs.lms.entity.User;
import com.ycs.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    
    public User createUser(User user) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // 기업/파트너는 승인 대기 상태로 설정
        if (user.getUserType() == User.UserType.CORPORATE || 
            user.getUserType() == User.UserType.PARTNER) {
            user.setStatus(User.UserStatus.PENDING);
        } else {
            user.setStatus(User.UserStatus.ACTIVE);
            // 일반 사용자는 즉시 멤버 코드 부여
            user.setMemberCode(generateMemberCode("GEN"));
        }
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEmailVerified(false); // 실제로는 이메일 인증 로직 필요
        
        return userRepository.save(user);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public User approveUser(Long userId, String approvedBy, String notes) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getStatus() != User.UserStatus.PENDING) {
            throw new IllegalArgumentException("User is not in pending status");
        }
        
        user.setStatus(User.UserStatus.ACTIVE);
        user.setApprovedAt(LocalDateTime.now());
        user.setApprovedBy(approvedBy);
        user.setApprovalNotes(notes);
        user.setUpdatedAt(LocalDateTime.now());
        
        // 멤버 코드 생성
        if (user.getMemberCode() == null) {
            String prefix = switch (user.getUserType()) {
                case CORPORATE -> "COR";
                case PARTNER -> "PAR";
                case GENERAL -> "GEN";
                case ADMIN -> "ADM";
            };
            user.setMemberCode(generateMemberCode(prefix));
        }
        
        return userRepository.save(user);
    }
    
    public User rejectUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (user.getStatus() != User.UserStatus.PENDING) {
            throw new IllegalArgumentException("User is not in pending status");
        }
        
        user.setStatus(User.UserStatus.REJECTED);
        user.setRejectedAt(LocalDateTime.now());
        user.setRejectionReason(reason);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    private String generateMemberCode(String prefix) {
        // 해당 타입의 기존 멤버 코드 중 최대값 조회하여 +1
        // 간단한 구현 - 실제로는 더 정교한 로직 필요
        long count = userRepository.count();
        return String.format("%s%03d", prefix, count + 1);
    }
}