package com.ysc.lms.config;

import com.ysc.lms.entity.User;
import com.ysc.lms.entity.Notification;
import com.ysc.lms.repository.UserRepository;
import com.ysc.lms.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * 프로덕션 환경을 위한 관리자 계정 초기화
 * 환경변수를 통해 관리자 계정 정보를 설정할 수 있습니다.
 */
@Configuration
@Profile({"dev", "prod", "production"})
@RequiredArgsConstructor
@Slf4j
public class AdminInitializationRunner {

    @Value("${admin.email:admin@ycs.com}")
    private String adminEmail;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Value("${admin.name:System Administrator}")
    private String adminName;

    @Value("${admin.phone:010-0000-0000}")
    private String adminPhone;

    @Value("${admin.member-code:ADM001}")
    private String adminMemberCode;

    @Bean
    ApplicationRunner initializeAdminAccount(UserRepository userRepo, PasswordEncoder encoder, NotificationRepository notificationRepo) {
        return args -> {
            log.info("프로덕션 환경 관리자 계정 초기화를 시작합니다...");

            userRepo.findByEmail(adminEmail).ifPresentOrElse(user -> {
                log.info("기존 관리자 계정이 존재합니다: {}", user.getEmail());
                
                // 관리자 권한 확인 및 보정
                if (user.getUserType() != User.UserType.ADMIN) {
                    log.warn("사용자 {}의 권한을 ADMIN으로 승격합니다.", user.getEmail());
                    user.setUserType(User.UserType.ADMIN);
                }
                
                // 계정 활성화 확인
                if (user.getStatus() != User.UserStatus.ACTIVE) {
                    log.info("관리자 계정을 활성화합니다.");
                    user.setStatus(User.UserStatus.ACTIVE);
                }
                
                // 이메일 인증 완료 처리
                if (!user.getEmailVerified()) {
                    log.info("관리자 계정의 이메일을 인증 완료로 처리합니다.");
                    user.setEmailVerified(true);
                    user.setEmailVerificationToken(null);
                }
                
                // 멤버 코드 설정
                if (user.getMemberCode() == null) {
                    user.setMemberCode(adminMemberCode);
                    log.info("관리자 계정에 멤버코드를 설정했습니다: {}", adminMemberCode);
                }
                
                // 비밀번호 업데이트 - 매번 새로운 해시로 생성
                String newPassword = encoder.encode(adminPassword);
                user.setPassword(newPassword);
                log.info("관리자 계정의 비밀번호를 업데이트했습니다.");
                
                user.setUpdatedAt(LocalDateTime.now());
                userRepo.save(user);
                
                log.info("기존 관리자 계정 보정 완료: {}", user.getEmail());
                
            }, () -> {
                log.info("새로운 관리자 계정을 생성합니다: {}", adminEmail);
                
                User admin = new User();
                admin.setEmail(adminEmail);
                // Properly encode password using injected encoder
                admin.setPassword(encoder.encode(adminPassword));
                admin.setName(adminName);
                admin.setPhone(adminPhone);
                admin.setUserType(User.UserType.ADMIN);
                admin.setStatus(User.UserStatus.ACTIVE);
                admin.setEmailVerified(true);
                admin.setMemberCode(adminMemberCode);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                
                userRepo.save(admin);
                
                log.info("새로운 관리자 계정이 생성되었습니다:");
                log.info("  - 이메일: {}", adminEmail);
                log.info("  - 이름: {}", adminName);
                log.info("  - 멤버코드: {}", adminMemberCode);
                log.info("  - 상태: ACTIVE");
                log.warn("보안을 위해 기본 비밀번호를 변경하는 것을 권장합니다.");
            });

            // 개발환경에서 테스트 데이터 생성
            createTestUsers(userRepo, encoder);
            
            // 테스트 알림 데이터 생성
            createTestNotifications(userRepo, notificationRepo);

            log.info("관리자 계정 초기화 완료!");
        };
    }

    private void createTestUsers(UserRepository userRepo, PasswordEncoder encoder) {
        log.info("테스트 사용자 데이터를 생성합니다...");

        // Corporate users (PENDING approval)
        createTestUser(userRepo, encoder, "corporate1@samsung.co.kr", "Samsung Electronics", 
                      "02-2255-0114", User.UserType.CORPORATE, User.UserStatus.PENDING);
        createTestUser(userRepo, encoder, "procurement@lg.co.kr", "LG Electronics", 
                      "02-3777-1114", User.UserType.CORPORATE, User.UserStatus.PENDING);
        createTestUser(userRepo, encoder, "logistics@hyundai.com", "Hyundai Motor", 
                      "02-3464-1114", User.UserType.CORPORATE, User.UserStatus.ACTIVE);

        // Partner users (PENDING approval)  
        createTestUser(userRepo, encoder, "partner@busantrading.co.kr", "Busan Trading Co", 
                      "051-123-4567", User.UserType.PARTNER, User.UserStatus.PENDING);
        createTestUser(userRepo, encoder, "info@seoullogistics.com", "Seoul Logistics", 
                      "02-9876-5432", User.UserType.PARTNER, User.UserStatus.PENDING);
        createTestUser(userRepo, encoder, "partner@approved.co.kr", "Approved Partner Co", 
                      "031-555-7777", User.UserType.PARTNER, User.UserStatus.ACTIVE);

        // General users
        createTestUser(userRepo, encoder, "user@example.com", "General User", 
                      "010-1234-5678", User.UserType.GENERAL, User.UserStatus.ACTIVE);
        createTestUser(userRepo, encoder, "inactive@test.com", "Suspended User", 
                      "010-9999-0000", User.UserType.GENERAL, User.UserStatus.SUSPENDED);

        // Warehouse staff  
        createTestUser(userRepo, encoder, "warehouse@ycs.com", "Warehouse Manager", 
                      "02-5555-1234", User.UserType.WAREHOUSE, User.UserStatus.ACTIVE);

        log.info("테스트 사용자 데이터 생성 완료!");
    }

    private void createTestUser(UserRepository userRepo, PasswordEncoder encoder, String email, String name, 
                               String phone, User.UserType userType, User.UserStatus status) {
        if (userRepo.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(encoder.encode("password123")); // 테스트용 비밀번호
            user.setName(name);
            user.setPhone(phone);
            user.setUserType(userType);
            user.setStatus(status);
            user.setEmailVerified(userType != User.UserType.PARTNER || !email.equals("info@seoullogistics.com")); // 하나는 미인증으로
            user.setCreatedAt(LocalDateTime.now().minusDays(userType == User.UserType.CORPORATE ? 1 : 0));
            user.setUpdatedAt(LocalDateTime.now());
            
            userRepo.save(user);
            log.info("테스트 사용자 생성: {} ({})", name, userType);
        }
    }

    private void createTestNotifications(UserRepository userRepo, NotificationRepository notificationRepo) {
        log.info("테스트 알림 데이터를 생성합니다...");

        // Find active general user for test notifications
        User generalUser = userRepo.findByEmail("user@example.com").orElse(null);
        User corporateUser = userRepo.findByEmail("logistics@hyundai.com").orElse(null);

        if (generalUser != null) {
            // Check if notifications already exist
            if (notificationRepo.countUnreadByUserId(generalUser.getId()) == 0) {
                // Create test notifications for general user
                Notification notification1 = new Notification(generalUser, Notification.NotificationType.ORDER_STATUS_CHANGED, 
                    "주문 상태 변경", "주문 #ORD001의 상태가 '배송 중'으로 변경되었습니다.");
                notification1.setCreatedAt(LocalDateTime.now().minusHours(2));
                notificationRepo.save(notification1);

                Notification notification2 = new Notification(generalUser, Notification.NotificationType.ORDER_ARRIVED, 
                    "창고 도착", "주문 #ORD002가 방콕 창고에 도착했습니다.");
                notification2.setCreatedAt(LocalDateTime.now().minusHours(1));
                notificationRepo.save(notification2);

                Notification notification3 = new Notification(generalUser, Notification.NotificationType.PAYMENT_REQUIRED, 
                    "결제 필요", "주문 #ORD001에 대한 배송비 결제가 필요합니다.");
                notification3.setCreatedAt(LocalDateTime.now().minusMinutes(30));
                notificationRepo.save(notification3);

                log.info("일반 사용자 알림 생성: {}", generalUser.getEmail());
            }
        }

        if (corporateUser != null) {
            // Check if notifications already exist
            if (notificationRepo.countUnreadByUserId(corporateUser.getId()) == 0) {
                // Create test notifications for corporate user
                Notification notification4 = new Notification(corporateUser, Notification.NotificationType.USER_APPROVED, 
                    "계정 승인", "기업 계정이 승인되었습니다. 이제 모든 기능을 이용하실 수 있습니다.");
                notification4.setCreatedAt(LocalDateTime.now().minusDays(9));
                notification4.markAsRead(); // This one is read
                notificationRepo.save(notification4);

                Notification notification5 = new Notification(corporateUser, Notification.NotificationType.CBM_THRESHOLD_EXCEEDED, 
                    "CBM 초과 경고", "주문의 총 CBM이 29를 초과하여 항공 운송으로 변경되었습니다.");
                notification5.setCreatedAt(LocalDateTime.now().minusDays(3));
                notificationRepo.save(notification5);

                log.info("기업 사용자 알림 생성: {}", corporateUser.getEmail());
            }
        }

        log.info("테스트 알림 데이터 생성 완료!");
    }
}