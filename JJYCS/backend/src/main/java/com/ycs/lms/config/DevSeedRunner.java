package com.ycs.lms.config;

import com.ycs.lms.entity.User;
import com.ycs.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevSeedRunner {

    @Bean
    ApplicationRunner seedUsers(UserRepository userRepo, PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() == 0) {
                log.info("Initializing dev database with seed data...");
                
                // 관리자 계정
                User admin = new User();
                admin.setEmail("admin@ycs.com");
                admin.setPassword(encoder.encode("admin123"));
                admin.setName("Administrator");
                admin.setUserType(User.UserType.ADMIN);
                admin.setStatus(User.UserStatus.ACTIVE);
                admin.setMemberCode("ADM001");
                admin.setEmailVerified(true);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                userRepo.save(admin);
                
                // 일반 사용자 계정
                User user = new User();
                user.setEmail("user@test.com");
                user.setPassword(encoder.encode("user123"));
                user.setName("Test User");
                user.setUserType(User.UserType.GENERAL);
                user.setStatus(User.UserStatus.ACTIVE);
                user.setMemberCode("GEN001");
                user.setEmailVerified(true);
                user.setCreatedAt(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userRepo.save(user);
                
                // 파트너 계정
                User partner = new User();
                partner.setEmail("partner@ycs.com");
                partner.setPassword(encoder.encode("partner123"));
                partner.setName("Partner User");
                partner.setUserType(User.UserType.PARTNER);
                partner.setStatus(User.UserStatus.ACTIVE);
                partner.setMemberCode("PAR001");
                partner.setEmailVerified(true);
                partner.setCreatedAt(LocalDateTime.now());
                partner.setUpdatedAt(LocalDateTime.now());
                userRepo.save(partner);
                
                // 창고 담당자 계정
                User warehouse = new User();
                warehouse.setEmail("warehouse@ycs.com");
                warehouse.setPassword(encoder.encode("warehouse123"));
                warehouse.setName("Warehouse Manager");
                warehouse.setUserType(User.UserType.WAREHOUSE);
                warehouse.setStatus(User.UserStatus.ACTIVE);
                warehouse.setMemberCode("WH001");
                warehouse.setEmailVerified(true);
                warehouse.setCreatedAt(LocalDateTime.now());
                warehouse.setUpdatedAt(LocalDateTime.now());
                userRepo.save(warehouse);
                
                // 기업 회원 계정
                User corporate = new User();
                corporate.setEmail("corp@test.com");
                corporate.setPassword(encoder.encode("corp123"));
                corporate.setName("Corporate User");
                corporate.setUserType(User.UserType.CORPORATE);
                corporate.setStatus(User.UserStatus.ACTIVE);
                corporate.setMemberCode("COR001");
                corporate.setEmailVerified(true);
                corporate.setCompanyName("Test Corporation");
                corporate.setBusinessNumber("123-45-67890");
                corporate.setCompanyAddress("서울시 강남구 테헤란로 123");
                corporate.setCreatedAt(LocalDateTime.now());
                corporate.setUpdatedAt(LocalDateTime.now());
                userRepo.save(corporate);
                
                log.info("Seed data initialized successfully - {} users created", userRepo.count());
            } else {
                log.info("Database already contains {} users - skipping seed data", userRepo.count());
            }
        };
    }
}