package com.ycs.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 이메일 인증");
            message.setText(String.format(
                "안녕하세요!\n\n" +
                "YCS LMS 서비스 이용을 위해 이메일 인증을 완료해주세요.\n\n" +
                "인증 링크: %s/auth/verify-email?token=%s\n\n" +
                "감사합니다.",
                "http://localhost:3000", token
            ));
            
            emailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendApprovalEmail(String toEmail, String memberCode, String note) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 회원 승인 완료");
            message.setText(String.format(
                "안녕하세요!\n\n" +
                "YCS LMS 서비스 이용 승인이 완료되었습니다.\n\n" +
                "회원 코드: %s\n" +
                "승인 메모: %s\n\n" +
                "이제 서비스를 정상적으로 이용하실 수 있습니다.\n\n" +
                "감사합니다.",
                memberCode, note != null ? note : "승인 완료"
            ));
            
            emailSender.send(message);
            log.info("Approval email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send approval email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 비밀번호 재설정");
            message.setText(String.format(
                "안녕하세요!\n\n" +
                "비밀번호 재설정을 요청하셨습니다.\n\n" +
                "재설정 링크: %s/auth/reset-password?token=%s\n\n" +
                "링크는 30분간 유효합니다.\n\n" +
                "감사합니다.",
                "http://localhost:3000", resetToken
            ));
            
            emailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }
}