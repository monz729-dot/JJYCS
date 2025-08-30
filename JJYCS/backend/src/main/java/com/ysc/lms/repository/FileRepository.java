package com.ysc.lms.repository;

import com.ysc.lms.entity.File;
import com.ysc.lms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    
    // 기본 조회
    @EntityGraph(attributePaths = {"uploadedBy"})
    Optional<File> findByStoredName(String storedName);
    
    @EntityGraph(attributePaths = {"uploadedBy"})
    Page<File> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // 사용자별 파일 조회
    @EntityGraph(attributePaths = {"uploadedBy"})
    Page<File> findByUploadedByOrderByCreatedAtDesc(User uploadedBy, Pageable pageable);
    
    @EntityGraph(attributePaths = {"uploadedBy"})
    List<File> findByUploadedByAndIsActiveOrderByCreatedAtDesc(User uploadedBy, Boolean isActive);
    
    // 파일 타입별 조회
    @EntityGraph(attributePaths = {"uploadedBy"})
    Page<File> findByFileTypeOrderByCreatedAtDesc(File.FileType fileType, Pageable pageable);
    
    @EntityGraph(attributePaths = {"uploadedBy"})
    Page<File> findByCategoryOrderByCreatedAtDesc(File.FileCategory category, Pageable pageable);
    
    // 연관 엔티티별 파일 조회
    @EntityGraph(attributePaths = {"uploadedBy"})
    List<File> findByRelatedEntityTypeAndRelatedEntityIdOrderByCreatedAtDesc(
        String relatedEntityType, Long relatedEntityId);
    
    @EntityGraph(attributePaths = {"uploadedBy"})
    Page<File> findByRelatedEntityTypeAndRelatedEntityIdOrderByCreatedAtDesc(
        String relatedEntityType, Long relatedEntityId, Pageable pageable);
    
    // 고급 검색
    @Query(value = "SELECT f FROM File f JOIN FETCH f.uploadedBy WHERE " +
           "(:originalName IS NULL OR f.originalName LIKE %:originalName%) AND " +
           "(:fileType IS NULL OR f.fileType = :fileType) AND " +
           "(:category IS NULL OR f.category = :category) AND " +
           "(:uploadedBy IS NULL OR f.uploadedBy = :uploadedBy) AND " +
           "(:startDate IS NULL OR f.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR f.createdAt <= :endDate) AND " +
           "(:isActive IS NULL OR f.isActive = :isActive) " +
           "ORDER BY f.createdAt DESC",
           countQuery = "SELECT COUNT(f) FROM File f WHERE " +
           "(:originalName IS NULL OR f.originalName LIKE %:originalName%) AND " +
           "(:fileType IS NULL OR f.fileType = :fileType) AND " +
           "(:category IS NULL OR f.category = :category) AND " +
           "(:uploadedBy IS NULL OR f.uploadedBy = :uploadedBy) AND " +
           "(:startDate IS NULL OR f.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR f.createdAt <= :endDate) AND " +
           "(:isActive IS NULL OR f.isActive = :isActive)")
    Page<File> searchFiles(@Param("originalName") String originalName,
                          @Param("fileType") File.FileType fileType,
                          @Param("category") File.FileCategory category,
                          @Param("uploadedBy") User uploadedBy,
                          @Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate,
                          @Param("isActive") Boolean isActive,
                          Pageable pageable);
    
    // 공개 파일 조회
    @EntityGraph(attributePaths = {"uploadedBy"})
    Page<File> findByIsPublicAndIsActiveOrderByCreatedAtDesc(Boolean isPublic, Boolean isActive, Pageable pageable);
    
    // 만료된 파일 조회
    @Query("SELECT f FROM File f WHERE f.expiresAt IS NOT NULL AND f.expiresAt <= :now")
    List<File> findExpiredFiles(@Param("now") LocalDateTime now);
    
    // 임시 파일 조회 (24시간 이상 된 것)
    @Query("SELECT f FROM File f WHERE f.category = 'TEMP' AND f.createdAt <= :cutoffTime")
    List<File> findOldTempFiles(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // 통계 쿼리
    @Query("SELECT f.fileType, COUNT(f) FROM File f WHERE f.isActive = true GROUP BY f.fileType")
    List<Object[]> getFileTypeStats();
    
    @Query("SELECT f.category, COUNT(f) FROM File f WHERE f.isActive = true GROUP BY f.category")
    List<Object[]> getFileCategoryStats();
    
    @Query("SELECT SUM(f.fileSize) FROM File f WHERE f.isActive = true")
    Long getTotalFileSize();
    
    @Query("SELECT SUM(f.fileSize) FROM File f WHERE f.uploadedBy = :user AND f.isActive = true")
    Long getTotalFileSizeByUser(@Param("user") User user);
    
    @Query("SELECT COUNT(f) FROM File f WHERE f.uploadedBy = :user AND f.isActive = true")
    Long getFileCountByUser(@Param("user") User user);
    
    // 다운로드 통계
    @Query("SELECT f FROM File f WHERE f.lastDownloadAt >= :since ORDER BY f.downloadCount DESC")
    List<File> findPopularFiles(@Param("since") LocalDateTime since, Pageable pageable);
    
    // 파일 정리
    @Modifying
    @Query("UPDATE File f SET f.isActive = false WHERE f.id = :id")
    void softDeleteFile(@Param("id") Long id);
    
    @Modifying
    @Query("UPDATE File f SET f.isActive = false WHERE f.expiresAt IS NOT NULL AND f.expiresAt <= :now")
    int deactivateExpiredFiles(@Param("now") LocalDateTime now);
    
    // 파일 존재 확인
    boolean existsByStoredName(String storedName);
    
    boolean existsByRelatedEntityTypeAndRelatedEntityIdAndCategory(
        String relatedEntityType, Long relatedEntityId, File.FileCategory category);
}