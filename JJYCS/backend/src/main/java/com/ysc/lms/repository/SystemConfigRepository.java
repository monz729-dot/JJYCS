package com.ysc.lms.repository;

import com.ysc.lms.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    Optional<SystemConfig> findByConfigKey(String configKey);
    
    Optional<SystemConfig> findByConfigKeyAndIsActive(String configKey, Boolean isActive);
    
    List<SystemConfig> findByCategory(String category);
    
    List<SystemConfig> findByCategoryAndIsActive(String category, Boolean isActive);
    
    List<SystemConfig> findByIsActive(Boolean isActive);
    
    @Query("SELECT c FROM SystemConfig c WHERE c.configKey LIKE %:keyPattern% AND c.isActive = true")
    List<SystemConfig> findByConfigKeyContaining(@Param("keyPattern") String keyPattern);
    
    boolean existsByConfigKey(String configKey);
}