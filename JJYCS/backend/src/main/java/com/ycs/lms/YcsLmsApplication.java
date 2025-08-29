package com.ycs.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class YcsLmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(YcsLmsApplication.class, args);
        System.out.println("=".repeat(60));
        System.out.println("🚀 YCS 물류관리 시스템(LMS) 서버가 시작되었습니다!");
        System.out.println("📍 API 서버: http://localhost:8080/api");
        System.out.println("📊 H2 콘솔: http://localhost:8080/api/h2-console");
        System.out.println("   - JDBC URL: jdbc:h2:mem:ycsdb");
        System.out.println("   - Username: sa");
        System.out.println("   - Password: (비어있음)");
        System.out.println("=".repeat(60));
    }
}