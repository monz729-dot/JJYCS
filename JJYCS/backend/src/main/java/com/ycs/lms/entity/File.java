package com.ycs.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String originalName; // 원본 파일명

    @Column(nullable = false, length = 255)
    private String storedName; // 저장된 파일명 (UUID 기반)

    @Column(nullable = false, length = 500)
    private String filePath; // 파일 저장 경로

    @Column(nullable = false, length = 100)
    private String mimeType; // MIME 타입

    @Column(nullable = false)
    private Long fileSize; // 파일 크기 (bytes)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileCategory category;

    // 연관 엔티티 정보
    @Column
    private Long relatedEntityId; // 연관된 엔티티의 ID

    @Column(length = 50)
    private String relatedEntityType; // 연관된 엔티티 타입 (Order, User 등)

    // 업로더 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User uploadedBy;

    // 이미지 관련 정보
    @Column
    private Integer width; // 이미지 너비

    @Column
    private Integer height; // 이미지 높이

    @Column(length = 500)
    private String thumbnailPath; // 썸네일 경로

    // 메타데이터
    @Column(length = 1000)
    private String description; // 파일 설명

    @Column(length = 500)
    private String tags; // 태그 (콤마로 구분)

    @Column
    private Boolean isPublic = false; // 공개 여부

    @Column
    private Boolean isActive = true; // 활성 상태

    @Column
    private LocalDateTime expiresAt; // 만료일

    // 다운로드 통계
    @Column
    private Long downloadCount = 0L; // 다운로드 횟수

    @Column
    private LocalDateTime lastDownloadAt; // 마지막 다운로드 시간

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum FileType {
        IMAGE,      // 이미지
        DOCUMENT,   // 문서
        ARCHIVE,    // 압축파일
        VIDEO,      // 비디오
        AUDIO,      // 오디오
        OTHER       // 기타
    }

    public enum FileCategory {
        // 주문 관련
        ORDER_DOCUMENT,     // 주문 서류
        REPACK_PHOTO,       // 리패킹 사진
        DAMAGE_PHOTO,       // 손상 사진
        INVOICE,            // 송장/청구서
        
        // 사용자 관련
        USER_AVATAR,        // 사용자 프로필 사진
        BUSINESS_LICENSE,   // 사업자등록증
        ID_DOCUMENT,        // 신분증
        
        // 시스템 관련
        SYSTEM_LOGO,        // 시스템 로고
        BANNER,             // 배너
        NOTICE_ATTACHMENT,  // 공지사항 첨부파일
        
        // 기타
        TEMP,               // 임시 파일
        OTHER               // 기타
    }

    // 생성자
    public File(String originalName, String storedName, String filePath, 
               String mimeType, Long fileSize, FileType fileType, FileCategory category) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.filePath = filePath;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.category = category;
    }

    // Helper methods
    public String getFileExtension() {
        if (originalName != null && originalName.contains(".")) {
            return originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        double size = fileSize.doubleValue();
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    public boolean isImage() {
        return fileType == FileType.IMAGE;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
        this.lastDownloadAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(User user) {
        return uploadedBy != null && uploadedBy.getId().equals(user.getId());
    }
}