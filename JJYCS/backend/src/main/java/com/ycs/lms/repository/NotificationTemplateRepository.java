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
    
    Optional<NotificationTemplate> findByNameAndIsActiveTrue(String name);
    
    List<NotificationTemplate> findByTypeAndIsActiveTrue(NotificationTemplate.NotificationType type);
    
    List<NotificationTemplate> findByCategoryAndIsActiveTrue(String category);
    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.name = :name AND nt.type = :type AND nt.isActive = true")
    Optional<NotificationTemplate> findByNameAndTypeAndActive(@Param("name") String name, @Param("type") NotificationTemplate.NotificationType type);
    
    List<NotificationTemplate> findByIsActiveTrueOrderByNameAsc();
    
    boolean existsByNameAndType(String name, NotificationTemplate.NotificationType type);
}