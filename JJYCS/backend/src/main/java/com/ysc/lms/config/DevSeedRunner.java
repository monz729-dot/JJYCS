package com.ysc.lms.config;

import com.ysc.lms.entity.User;
import com.ysc.lms.repository.UserRepository;
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
            log.info("개발용 테스트 계정 보정을 시작합니다...");

            // 관리자 계정 보정
            userRepo.findByEmail("admin@ycs.com").ifPresentOrElse(user -> {
                // 이미 있다면 보정
                log.info("기존 admin 계정을 보정합니다: {}", user.getEmail());
                if (!encoder.matches("password", user.getPassword())) {
                    user.setPassword(encoder.encode("password"));
                    log.info("admin 비밀번호를 'password'로 재설정했습니다.");
                }
                user.setStatus(User.UserStatus.ACTIVE);
                user.setEmailVerified(true);
                if (user.getMemberCode() == null) {
                    user.setMemberCode("ADM001");
                }
                user.setUpdatedAt(LocalDateTime.now());
                userRepo.save(user);
            }, () -> {
                // 없으면 생성
                log.info("새로운 admin 계정을 생성합니다.");
                User admin = new User();
                admin.setEmail("admin@ycs.com");
                admin.setPassword(encoder.encode("password"));
                admin.setName("Administrator");
                admin.setPhone("010-0000-0000");
                admin.setUserType(User.UserType.ADMIN);
                admin.setStatus(User.UserStatus.ACTIVE);
                admin.setEmailVerified(true);
                admin.setMemberCode("ADM001");
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                userRepo.save(admin);
            });

            // 일반 사용자 계정 보정  
            userRepo.findByEmail("user@ycs.com").ifPresentOrElse(user -> {
                log.info("기존 user 계정을 보정합니다: {}", user.getEmail());
                if (!encoder.matches("password", user.getPassword())) {
                    user.setPassword(encoder.encode("password"));
                }
                user.setStatus(User.UserStatus.ACTIVE);
                user.setEmailVerified(true);
                if (user.getMemberCode() == null) {
                    user.setMemberCode("GEN001");
                }
                user.setUpdatedAt(LocalDateTime.now());
                userRepo.save(user);
            }, () -> {
                log.info("새로운 user 계정을 생성합니다.");
                User generalUser = new User();
                generalUser.setEmail("user@ycs.com");
                generalUser.setPassword(encoder.encode("password"));
                generalUser.setName("General User");
                generalUser.setPhone("010-1111-1111");
                generalUser.setUserType(User.UserType.GENERAL);
                generalUser.setStatus(User.UserStatus.ACTIVE);
                generalUser.setEmailVerified(true);
                generalUser.setMemberCode("GEN001");
                generalUser.setCreatedAt(LocalDateTime.now());
                generalUser.setUpdatedAt(LocalDateTime.now());
                userRepo.save(generalUser);
            });

            log.info("개발용 테스트 계정 보정 완료!");
        };
    }
}