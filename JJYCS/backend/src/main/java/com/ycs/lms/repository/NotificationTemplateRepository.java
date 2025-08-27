package com.ycs.lms.repository;

import com.ycs.lms.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    
    Optional<NotificationTemplate> findByTemplateCode(String templateCode);
    
    List<NotificationTemplate> findByTriggerEventAndIsActiveTrue(NotificationTemplate.TriggerEvent triggerEvent);
    
    List<NotificationTemplate> findByTypeAndIsActiveTrue(NotificationTemplate.NotificationType type);
    
    List<NotificationTemplate> findByTriggerEventAndTypeAndIsActiveTrue(
        NotificationTemplate.TriggerEvent triggerEvent,
        NotificationTemplate.NotificationType type
    );
    
    List<NotificationTemplate> findByLanguageCodeAndIsActiveTrue(String languageCode);
    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.isActive = true " +
           "AND nt.triggerEvent = :triggerEvent " +
           "AND nt.languageCode = :languageCode " +
           "ORDER BY nt.type")
    List<NotificationTemplate> findActiveTemplatesForEvent(
        @Param("triggerEvent") NotificationTemplate.TriggerEvent triggerEvent,
        @Param("languageCode") String languageCode
    );
    
    @Query("SELECT COUNT(nt) FROM NotificationTemplate nt WHERE nt.isActive = true")
    Long countActiveTemplates();
    
    @Query("SELECT nt.type, COUNT(nt) FROM NotificationTemplate nt " +
           "WHERE nt.isActive = true GROUP BY nt.type")
    List<Object[]> getTemplateCountByType();
    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.isActive = true " +
           "AND nt.templateCode LIKE %:keyword%")
    List<NotificationTemplate> searchByTemplateCode(@Param("keyword") String keyword);
}