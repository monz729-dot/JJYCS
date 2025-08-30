package com.ysc.lms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    @PostConstruct
    public void init() {
        log.info("EmailService initialized with emailEnabled={}, fromEmail={}, fromName={}", 
            emailEnabled, fromEmail, fromName);
    }

    @Value("${spring.mail.username:monz729@gmail.com}")
    private String fromEmail;

    @Value("${app.notification.email.from-name:YCS 물류관리시스템}")
    private String fromName;

    @Value("${app.frontend.base-url:http://localhost:3006}")
    private String frontendBaseUrl;
    
    @Value("${app.backend.base-url:http://localhost:8080/api}")
    private String backendBaseUrl;

    /**
     * 이메일 인증 메일 발송 (인증 코드 방식)
     */
    @Async
    public void sendVerificationEmail(String to, String name, String code) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Verification code: {}", code);
            return;
        }

        try {
            String subject = "[YCS LMS] 이메일 인증 코드";
            
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; margin: 40px; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                        <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center;">
                            <h1 style="margin: 0; font-size: 28px;">YCS 물류관리시스템</h1>
                            <p style="margin: 10px 0 0 0; opacity: 0.9;">이메일 인증 코드</p>
                        </div>
                        <div style="padding: 40px 30px;">
                            <h2 style="color: #2c3e50; margin-bottom: 20px;">안녕하세요, %s님!</h2>
                            <p style="line-height: 1.6; margin-bottom: 25px;">
                                YCS 물류관리시스템에 회원가입해 주셔서 감사합니다.<br>
                                아래 인증 코드를 입력하여 이메일 인증을 완료해주세요.
                            </p>
                            <div style="text-align: center; margin: 35px 0;">
                                <div style="background: #f8f9fa; border: 2px dashed #667eea; padding: 20px; border-radius: 10px; display: inline-block;">
                                    <p style="margin: 0 0 10px 0; font-size: 14px; color: #666;">인증 코드</p>
                                    <div style="font-family: 'Courier New', monospace; font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 8px; margin: 10px 0;">
                                        %s
                                    </div>
                                    <p style="margin: 10px 0 0 0; font-size: 12px; color: #999;">5분 내에 입력해주세요</p>
                                </div>
                            </div>
                            <div style="background: #fff3cd; padding: 20px; border-radius: 8px; border-left: 4px solid #ffc107;">
                                <p style="margin: 0; font-size: 14px; color: #856404;">
                                    <strong>⚠️ 보안 안내</strong><br>
                                    이 인증 코드를 타인과 공유하지 마세요. 본인이 요청하지 않았다면 이 메일을 무시해주세요.
                                </p>
                            </div>
                            <p style="font-size: 12px; color: #6c757d; margin-top: 30px; text-align: center;">
                                이 인증 코드는 5분 후 만료됩니다.
                            </p>
                        </div>
                        <div style="background: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #dee2e6;">
                            <p style="margin: 0; font-size: 12px; color: #6c757d;">
                                © 2024 YCS 물류관리시스템. All rights reserved.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, name, code);

            sendHtmlEmail(to, subject, htmlContent);
            log.info("Verification code email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send verification code email to: {}", to, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    /**
     * 비밀번호 재설정 메일 발송
     */
    public void sendPasswordResetEmail(String to, String name, String token) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Password reset token: {}", token);
            return;
        }

        try {
            String subject = "[YCS LMS] 비밀번호 재설정 요청";
            String resetUrl = frontendBaseUrl + "/reset-password/" + token;
            
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; margin: 40px; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                        <div style="background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; padding: 30px; text-align: center;">
                            <h1 style="margin: 0; font-size: 28px;">YCS 물류관리시스템</h1>
                            <p style="margin: 10px 0 0 0; opacity: 0.9;">비밀번호 재설정</p>
                        </div>
                        <div style="padding: 40px 30px;">
                            <h2 style="color: #2c3e50; margin-bottom: 20px;">안녕하세요, %s님!</h2>
                            <p style="line-height: 1.6; margin-bottom: 25px;">
                                비밀번호 재설정을 요청하셨습니다.<br>
                                아래 버튼을 클릭하여 새로운 비밀번호를 설정해주세요.
                            </p>
                            <div style="text-align: center; margin: 35px 0;">
                                <a href="%s" style="background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; padding: 15px 40px; text-decoration: none; border-radius: 25px; display: inline-block; font-weight: bold; font-size: 16px; box-shadow: 0 4px 15px rgba(240, 147, 251, 0.4);">
                                    🔒 비밀번호 재설정
                                </a>
                            </div>
                            <div style="background: #fff3cd; padding: 20px; border-radius: 8px; border-left: 4px solid #ffc107;">
                                <p style="margin: 0; font-size: 14px; color: #856404;">
                                    <strong>⚠️ 보안 안내</strong><br>
                                    본인이 요청하지 않았다면 이 메일을 무시하고 즉시 계정을 확인해주세요.
                                </p>
                            </div>
                            <div style="background: #f8f9fa; padding: 20px; border-radius: 8px; border-left: 4px solid #f5576c; margin-top: 20px;">
                                <p style="margin: 0; font-size: 14px; color: #6c757d;">
                                    <strong>링크가 작동하지 않나요?</strong><br>
                                    다음 URL을 복사하여 브라우저 주소창에 붙여넣으세요:<br>
                                    <code style="background: #e9ecef; padding: 2px 6px; border-radius: 4px; word-break: break-all;">%s</code>
                                </p>
                            </div>
                            <p style="font-size: 12px; color: #6c757d; margin-top: 30px; text-align: center;">
                                이 링크는 1시간 후 만료됩니다.
                            </p>
                        </div>
                        <div style="background: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #dee2e6;">
                            <p style="margin: 0; font-size: 12px; color: #6c757d;">
                                © 2024 YCS 물류관리시스템. All rights reserved.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, name, resetUrl, resetUrl);

            sendHtmlEmail(to, subject, htmlContent);
            log.info("Password reset email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("비밀번호 재설정 메일 발송에 실패했습니다.", e);
        }
    }

    /**
     * 간단한 텍스트 이메일 발송
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Would send email to: {} with subject: {}", to, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    /**
     * HTML 이메일 발송
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new MessagingException("Failed to send HTML email", e);
        }
    }

    /**
     * 메일 발송 테스트 (개발용)
     */
    public void sendTestEmail(String to) {
        sendSimpleEmail(to, 
            "[YCS LMS] 메일 발송 테스트", 
            "YCS 물류관리시스템 메일 발송이 정상적으로 작동합니다.\\n\\n발송 시간: " + java.time.LocalDateTime.now());
    }
    
    /**
     * 이메일 변경 인증 메일 발송
     */
    public void sendEmailChangeVerification(String to, String token) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Email change verification token: {}", token);
            return;
        }

        try {
            String subject = "[YCS LMS] 이메일 변경 인증을 완료해주세요";
            String verificationUrl = backendBaseUrl + "/auth/verify-email?token=" + token;
            
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; margin: 40px; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                        <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center;">
                            <h1 style="margin: 0; font-size: 28px;">YCS 물류관리시스템</h1>
                        </div>
                        <div style="padding: 40px;">
                            <h2 style="color: #333; margin-bottom: 20px;">이메일 변경 인증</h2>
                            <p style="line-height: 1.6; color: #666;">
                                안녕하세요,<br><br>
                                귀하의 계정에 대한 이메일 변경 요청이 접수되었습니다.<br>
                                아래 버튼을 클릭하여 새 이메일 주소를 인증해주세요.
                            </p>
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="display: inline-block; padding: 15px 40px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; border-radius: 5px; font-weight: bold;">
                                    이메일 인증하기
                                </a>
                            </div>
                            <p style="color: #999; font-size: 14px; margin-top: 30px;">
                                이 링크는 30분간 유효합니다.<br>
                                본인이 요청하지 않은 경우, 이 메일을 무시하셔도 됩니다.
                            </p>
                            <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                            <p style="color: #999; font-size: 12px; text-align: center;">
                                버튼이 작동하지 않는 경우, 아래 링크를 브라우저에 복사하여 접속해주세요:<br>
                                <a href="%s" style="color: #667eea; word-break: break-all;">%s</a>
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, verificationUrl, verificationUrl, verificationUrl);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email change verification email sent to {}", to);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send email change verification email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * 회원 승인 알림 이메일 발송
     */
    @Async
    public void sendApprovalEmail(String to, String name, String userType) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Would send approval email to: {} ({})", to, name);
            return;
        }

        try {
            String subject = "[YCS LMS] 계정 승인 완료 안내";
            String loginUrl = frontendBaseUrl + "/login";
            String userTypeKorean = getUserTypeKorean(userType);
            
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; margin: 40px; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                        <div style="background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: white; padding: 30px; text-align: center;">
                            <h1 style="margin: 0; font-size: 28px;">YCS 물류관리시스템</h1>
                            <p style="margin: 10px 0 0 0; opacity: 0.9;">계정 승인 완료</p>
                        </div>
                        <div style="padding: 40px 30px;">
                            <h2 style="color: #2c3e50; margin-bottom: 20px;">축하합니다, %s님!</h2>
                            <p style="line-height: 1.6; margin-bottom: 25px;">
                                %s 계정 승인이 완료되었습니다.<br>
                                이제 YCS 물류관리시스템의 모든 기능을 이용하실 수 있습니다.
                            </p>
                            <div style="background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 25px 0;">
                                <h3 style="margin: 0 0 10px 0; color: #495057;">계정 정보</h3>
                                <p style="margin: 5px 0; color: #6c757d;">이메일: %s</p>
                                <p style="margin: 5px 0; color: #6c757d;">계정 유형: %s</p>
                                <p style="margin: 5px 0; color: #6c757d;">승인일: %s</p>
                            </div>
                            <div style="text-align: center; margin: 35px 0;">
                                <a href="%s" style="display: inline-block; padding: 15px 40px; background: linear-gradient(135deg, #28a745 0%%, #20c997 100%%); color: white; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px;">
                                    지금 로그인하기
                                </a>
                            </div>
                            <div style="border-top: 1px solid #dee2e6; padding-top: 25px; margin-top: 35px;">
                                <h4 style="color: #495057; margin-bottom: 15px;">다음 단계</h4>
                                <ul style="color: #6c757d; line-height: 1.6;">
                                    <li>로그인 후 프로필을 완성해보세요</li>
                                    <li>첫 주문을 생성해보세요</li>
                                    <li>고객지원팀과 연락하여 추가 설정을 진행하세요</li>
                                </ul>
                            </div>
                            <p style="color: #6c757d; font-size: 14px; margin-top: 25px;">
                                문의사항이 있으시면 언제든 고객지원팀으로 연락주세요.
                            </p>
                        </div>
                        <div style="background: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #dee2e6;">
                            <p style="margin: 0; font-size: 12px; color: #6c757d;">
                                © 2024 YCS 물류관리시스템. All rights reserved.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, name, userTypeKorean, to, userTypeKorean, 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")), 
                loginUrl);

            sendHtmlEmail(to, subject, htmlContent);
            log.info("Approval email sent to: {} ({})", to, name);

        } catch (Exception e) {
            log.error("Failed to send approval email to: {}", to, e);
        }
    }
    
    /**
     * 회원 거절 알림 이메일 발송
     */
    @Async
    public void sendRejectionEmail(String to, String name, String userType, String reason) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Would send rejection email to: {} ({})", to, name);
            return;
        }

        try {
            String subject = "[YCS LMS] 계정 심사 결과 안내";
            String userTypeKorean = getUserTypeKorean(userType);
            
            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; margin: 40px; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                        <div style="background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%); color: white; padding: 30px; text-align: center;">
                            <h1 style="margin: 0; font-size: 28px;">YCS 물류관리시스템</h1>
                            <p style="margin: 10px 0 0 0; opacity: 0.9;">계정 심사 결과</p>
                        </div>
                        <div style="padding: 40px 30px;">
                            <h2 style="color: #2c3e50; margin-bottom: 20px;">%s님께</h2>
                            <p style="line-height: 1.6; margin-bottom: 25px;">
                                안녕하세요.<br>
                                YCS 물류관리시스템 %s 계정 심사 결과를 안내드립니다.
                            </p>
                            <div style="background: #fff3cd; border: 1px solid #ffeaa7; padding: 20px; border-radius: 8px; margin: 25px 0;">
                                <h3 style="margin: 0 0 15px 0; color: #856404;">심사 결과: 승인 보류</h3>
                                <p style="margin: 0; color: #856404; line-height: 1.6;">
                                    죄송하지만 현재 제출해주신 정보로는 계정 승인이 어려운 상황입니다.
                                </p>
                            </div>
                            %s
                            <div style="border-top: 1px solid #dee2e6; padding-top: 25px; margin-top: 35px;">
                                <h4 style="color: #495057; margin-bottom: 15px;">다음 단계</h4>
                                <ul style="color: #6c757d; line-height: 1.6;">
                                    <li>필요한 서류를 보완하여 다시 신청해주세요</li>
                                    <li>고객지원팀(support@ycs.com)으로 문의주세요</li>
                                    <li>평일 09:00-18:00 전화상담도 가능합니다</li>
                                </ul>
                            </div>
                            <p style="color: #6c757d; font-size: 14px; margin-top: 25px;">
                                더 나은 서비스를 제공하기 위한 조치이니 양해 부탁드립니다.<br>
                                언제든 재신청이 가능하오니 서류 보완 후 다시 시도해주세요.
                            </p>
                        </div>
                        <div style="background: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #dee2e6;">
                            <p style="margin: 0; font-size: 12px; color: #6c757d;">
                                © 2024 YCS 물류관리시스템. All rights reserved.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, name, userTypeKorean, 
                reason != null && !reason.trim().isEmpty() ? 
                    String.format("<div style=\"background: #f8d7da; border: 1px solid #f5c6cb; padding: 20px; border-radius: 8px; margin: 25px 0;\"><h4 style=\"margin: 0 0 10px 0; color: #721c24;\">사유</h4><p style=\"margin: 0; color: #721c24; line-height: 1.6;\">%s</p></div>", reason) : 
                    "");

            sendHtmlEmail(to, subject, htmlContent);
            log.info("Rejection email sent to: {} ({})", to, name);

        } catch (Exception e) {
            log.error("Failed to send rejection email to: {}", to, e);
        }
    }
    
    /**
     * 사용자 타입을 한국어로 변환
     */
    private String getUserTypeKorean(String userType) {
        return switch (userType.toUpperCase()) {
            case "CORPORATE" -> "기업회원";
            case "PARTNER" -> "파트너";
            case "GENERAL" -> "일반회원";
            case "ADMIN" -> "관리자";
            default -> userType;
        };
    }
}