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
    public void sendVerificationEmail(String toEmail, String name, String verificationLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 이메일 인증");
            message.setText(String.format(
                "안녕하세요 %s님!\n\n" +
                "YCS LMS 서비스 이용을 위해 이메일 인증을 완료해주세요.\n\n" +
                "인증 링크: %s\n\n" +
                "감사합니다.",
                name, verificationLink
            ));
            
            emailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendApprovalEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 회원 승인 완료");
            message.setText(String.format(
                "안녕하세요 %s님!\n\n" +
                "YCS LMS 서비스 이용 승인이 완료되었습니다.\n\n" +
                "이제 서비스를 정상적으로 이용하실 수 있습니다.\n\n" +
                "감사합니다.",
                name
            ));
            
            emailSender.send(message);
            log.info("Approval email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send approval email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 비밀번호 재설정");
            message.setText(String.format(
                "안녕하세요 %s님!\n\n" +
                "비밀번호 재설정을 요청하셨습니다.\n\n" +
                "재설정 링크: %s\n\n" +
                "링크는 24시간 동안 유효합니다.\n\n" +
                "감사합니다.",
                name, resetLink
            ));
            
            emailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendRejectionEmail(String toEmail, String name, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 회원 가입 거부 안내");
            message.setText(String.format(
                "안녕하세요 %s님!\n\n" +
                "YCS LMS 서비스 회원 가입 승인이 거부되었습니다.\n\n" +
                "거부 사유: %s\n\n" +
                "문의사항이 있으시면 고객센터로 연락해주세요.\n\n" +
                "감사합니다.",
                name, reason != null ? reason : "승인 기준 미충족"
            ));
            
            emailSender.send(message);
            log.info("Rejection email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send rejection email to: {}", toEmail, e);
        }
    }
}