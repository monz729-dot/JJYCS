package com.ycs.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void sendVerificationEmail(String toEmail, String userName, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 이메일 인증을 완료해주세요");
            
            String verificationLink = baseUrl + "/verify-email/" + verificationToken;
            
            String emailContent = String.format(
                "안녕하세요 %s님,\n\n" +
                "YCS 물류관리 시스템에 가입해주셔서 감사합니다.\n\n" +
                "아래 링크를 클릭하여 이메일 인증을 완료해주세요:\n" +
                "%s\n\n" +
                "이 링크는 24시간 동안 유효합니다.\n\n" +
                "감사합니다.\n" +
                "YCS LMS 팀",
                userName, verificationLink
            );
            
            message.setText(emailContent);
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }
    
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[YCS LMS] 비밀번호 재설정");
            
            String resetLink = baseUrl + "/reset-password/" + resetToken;
            
            String emailContent = String.format(
                "안녕하세요 %s님,\n\n" +
                "비밀번호 재설정 요청을 받았습니다.\n\n" +
                "아래 링크를 클릭하여 새 비밀번호를 설정해주세요:\n" +
                "%s\n\n" +
                "이 링크는 1시간 동안 유효합니다.\n\n" +
                "만약 비밀번호 재설정을 요청하지 않으셨다면, 이 이메일을 무시해주세요.\n\n" +
                "감사합니다.\n" +
                "YCS LMS 팀",
                userName, resetLink
            );
            
            message.setText(emailContent);
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("이메일 전송에 실패했습니다.", e);
        }
    }
    
    public void sendApprovalNotificationEmail(String toEmail, String userName, String userType, boolean approved, String notes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            
            String subject = approved ? 
                "[YCS LMS] 계정 승인 완료" : 
                "[YCS LMS] 계정 승인 거부";
            message.setSubject(subject);
            
            String statusText = approved ? "승인되었습니다" : "거부되었습니다";
            String typeText = userType.equals("CORPORATE") ? "기업" : "파트너";
            
            String emailContent;
            if (approved) {
                emailContent = String.format(
                    "안녕하세요 %s님,\n\n" +
                    "%s 계정이 승인되었습니다!\n\n" +
                    "이제 YCS 물류관리 시스템의 모든 기능을 이용하실 수 있습니다.\n" +
                    "로그인하여 서비스를 시작해보세요: %s/login\n\n" +
                    "%s\n\n" +
                    "감사합니다.\n" +
                    "YCS LMS 팀",
                    userName, typeText, baseUrl,
                    notes != null && !notes.trim().isEmpty() ? "관리자 메모: " + notes : ""
                );
            } else {
                emailContent = String.format(
                    "안녕하세요 %s님,\n\n" +
                    "죄송합니다. %s 계정 신청이 거부되었습니다.\n\n" +
                    "거부 사유: %s\n\n" +
                    "추가 문의사항이 있으시면 고객센터로 연락주세요.\n\n" +
                    "감사합니다.\n" +
                    "YCS LMS 팀",
                    userName, typeText, 
                    notes != null && !notes.trim().isEmpty() ? notes : "세부 사유는 고객센터에 문의해주세요."
                );
            }
            
            message.setText(emailContent);
            
            mailSender.send(message);
            log.info("Approval notification email sent to: {} (approved: {})", toEmail, approved);
        } catch (Exception e) {
            log.error("Failed to send approval notification email to: {}", toEmail, e);
            // Don't throw exception here - approval should not fail due to email issues
            log.warn("Continuing with approval process despite email failure");
        }
    }
    
    public String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
    
    public String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}