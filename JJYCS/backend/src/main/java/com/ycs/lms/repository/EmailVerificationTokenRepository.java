package com.ycs.lms.repository;

import com.ycs.lms.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    
    Optional<EmailVerificationToken> findByTokenAndUsedFalse(String token);
    
    Optional<EmailVerificationToken> findByTokenAndTokenTypeAndUsedFalse(String token, EmailVerificationToken.TokenType tokenType);
    
    List<EmailVerificationToken> findByEmailAndTokenType(String email, EmailVerificationToken.TokenType tokenType);
    
    List<EmailVerificationToken> findByUserIdAndTokenType(Long userId, EmailVerificationToken.TokenType tokenType);
    
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    void deleteByUserIdAndTokenType(Long userId, EmailVerificationToken.TokenType tokenType);
    
    void deleteByEmailAndTokenType(String email, EmailVerificationToken.TokenType tokenType);
}