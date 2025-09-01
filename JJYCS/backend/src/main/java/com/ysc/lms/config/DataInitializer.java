package com.ysc.lms.config;

import com.ysc.lms.service.FaqService;
import com.ysc.lms.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    private final FaqService faqService;
    private final NoticeService noticeService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Initializing sample data...");
        
        try {
            faqService.initializeSampleFaqs();
            log.info("Sample FAQs initialized");
            
            noticeService.initializeSampleNotices();
            log.info("Sample Notices initialized");
            
        } catch (Exception e) {
            log.error("Error initializing sample data: ", e);
        }
    }
}