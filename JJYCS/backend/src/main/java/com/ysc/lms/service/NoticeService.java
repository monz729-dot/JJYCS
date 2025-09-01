package com.ysc.lms.service;

import com.ysc.lms.domain.Notice;
import com.ysc.lms.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {
    private final NoticeRepository noticeRepository;
    
    public Page<Notice> getAllActiveNotices(Pageable pageable) {
        return noticeRepository.findByIsActiveTrueOrderByIsPinnedDescCreatedAtDesc(pageable);
    }
    
    @Transactional
    public Notice getNoticeById(Long id) {
        Notice notice = noticeRepository.findById(id).orElse(null);
        if (notice != null && notice.getIsActive()) {
            noticeRepository.incrementViewCount(id);
        }
        return notice;
    }
    
    @Transactional
    public Notice createNotice(Notice notice) {
        return noticeRepository.save(notice);
    }
    
    @Transactional
    public Notice updateNotice(Long id, Notice updatedNotice) {
        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notice not found"));
        
        notice.setTitle(updatedNotice.getTitle());
        notice.setContent(updatedNotice.getContent());
        notice.setCategory(updatedNotice.getCategory());
        notice.setIsPinned(updatedNotice.getIsPinned());
        notice.setIsActive(updatedNotice.getIsActive());
        
        return noticeRepository.save(notice);
    }
    
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notice not found"));
        notice.setIsActive(false);
        noticeRepository.save(notice);
    }
    
    @Transactional
    public void initializeSampleNotices() {
        if (noticeRepository.count() == 0) {
            List<Notice> sampleNotices = List.of(
                Notice.builder()
                    .title("[중요] 태국 송크란 연휴 배송 안내")
                    .content("4월 13일부터 15일까지 태국 송크란 연휴로 인해 배송 접수가 일시 중단됩니다. 연휴 전 접수를 원하시는 고객님께서는 4월 10일까지 주문 부탁드립니다.")
                    .category("공지")
                    .isPinned(true)
                    .isActive(true)
                    .authorId(1L)
                    .viewCount(0)
                    .build(),
                Notice.builder()
                    .title("항공 운임 요율 조정 안내")
                    .content("국제 유가 상승으로 인해 4월 1일부터 항공 운임이 5% 인상됩니다. 자세한 요율표는 요금안내 페이지를 참고해주세요.")
                    .category("요금")
                    .isPinned(false)
                    .isActive(true)
                    .authorId(1L)
                    .viewCount(0)
                    .build(),
                Notice.builder()
                    .title("신규 창고 오픈 안내")
                    .content("방콕 북부 지역에 새로운 물류창고가 오픈했습니다. 더욱 빠른 집하와 배송 서비스를 제공하겠습니다.")
                    .category("서비스")
                    .isPinned(false)
                    .isActive(true)
                    .authorId(1L)
                    .viewCount(0)
                    .build()
            );
            
            noticeRepository.saveAll(sampleNotices);
        }
    }
}