package com.ycs.lms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender emailSender;

    // spring.mail.username이 없어도 부팅되도록 기본값 비움
    @Value("${spring.mail.username:}")
    private String fromEmail;

    // 이메일 발송 on/off 스위치 (기본 false)
    @Value("${app.notification.email-enabled:false}")
    private boolean emailEnabled;

    private boolean mailReady() {
        if (!emailEnabled) {
            log.info("Email disabled via config (app.notification.email-enabled=false)");
            return false;
        }
        if (emailSender == null) {
            log.warn("JavaMailSender bean not present. Skip sending emails.");
            return false;
        }
        if (fromEmail == null || fromEmail.isBlank()) {
            log.warn("spring.mail.username is empty. Skip sending emails.");
            return false;
        }
        return true;
    }

    @Async
    public void sendVerificationEmail(String toEmail, String name, String verificationLink) {
        if (!mailReady()) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 이메일 인증 안내");
            message.setText(String.format(
                    "안녕하세요 %s님!\n\n아래 링크를 클릭하여 이메일 인증을 완료해 주세요:\n%s\n\n감사합니다.",
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
        if (!mailReady()) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 회원 가입 승인 안내");
            message.setText(String.format(
                    "안녕하세요 %s님!\n\nYCS LMS 서비스 회원 가입이 승인되었습니다.\n지금 바로 서비스를 이용하실 수 있습니다.\n\n감사합니다.",
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
        if (!mailReady()) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 비밀번호 재설정 안내");
            message.setText(String.format(
                    "안녕하세요 %s님!\n\n아래 링크를 클릭하여 비밀번호를 재설정해 주세요:\n%s\n\n" +
                            "보안을 위해 이 링크는 일정 시간 후 만료됩니다.\n\n감사합니다.",
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
        if (!mailReady()) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 회원 가입 거부 안내");
            message.setText(String.format(
                    "안녕하세요 %s님!\n\nYCS LMS 서비스 회원 가입 승인이 거부되었습니다.\n\n" +
                            "거부 사유: %s\n\n문의사항이 있으시면 고객센터로 연락해주세요.\n\n감사합니다.",
                    name, reason != null ? reason : "승인 기준 미충족"
            ));
            emailSender.send(message);
            log.info("Rejection email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send rejection email to: {}", toEmail, e);
        }
    }
}
