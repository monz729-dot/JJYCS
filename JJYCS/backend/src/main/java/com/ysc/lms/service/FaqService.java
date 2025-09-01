package com.ysc.lms.service;

import com.ysc.lms.domain.Faq;
import com.ysc.lms.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {
    private final FaqRepository faqRepository;
    
    public List<Faq> getAllActiveFaqs() {
        return faqRepository.findByIsActiveTrueOrderByDisplayOrderAscIdAsc();
    }
    
    public List<Faq> getFaqsByCategory(String category) {
        return faqRepository.findByCategoryAndIsActiveTrueOrderByDisplayOrderAscIdAsc(category);
    }
    
    public Faq getFaqById(Long id) {
        return faqRepository.findById(id).orElse(null);
    }
    
    @Transactional
    public Faq createFaq(Faq faq) {
        return faqRepository.save(faq);
    }
    
    @Transactional
    public Faq updateFaq(Long id, Faq updatedFaq) {
        Faq faq = faqRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("FAQ not found"));
        
        faq.setQuestion(updatedFaq.getQuestion());
        faq.setAnswer(updatedFaq.getAnswer());
        faq.setCategory(updatedFaq.getCategory());
        faq.setDisplayOrder(updatedFaq.getDisplayOrder());
        faq.setIsActive(updatedFaq.getIsActive());
        
        return faqRepository.save(faq);
    }
    
    @Transactional
    public void deleteFaq(Long id) {
        Faq faq = faqRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("FAQ not found"));
        faq.setIsActive(false);
        faqRepository.save(faq);
    }
    
    @Transactional
    public void initializeSampleFaqs() {
        if (faqRepository.count() == 0) {
            List<Faq> sampleFaqs = List.of(
                Faq.builder()
                    .question("배송 기간은 얼마나 걸리나요?")
                    .answer("일반적으로 태국에서 한국까지 항공 배송은 3-5일, 해상 배송은 15-20일이 소요됩니다. 통관 절차에 따라 추가 시간이 소요될 수 있습니다.")
                    .category("배송")
                    .displayOrder(1)
                    .isActive(true)
                    .build(),
                Faq.builder()
                    .question("배송료는 어떻게 계산되나요?")
                    .answer("배송료는 상품의 무게와 부피(CBM)를 기준으로 계산됩니다. 실측 무게와 부피 무게 중 더 큰 값을 기준으로 요금이 책정됩니다.")
                    .category("요금")
                    .displayOrder(2)
                    .isActive(true)
                    .build(),
                Faq.builder()
                    .question("통관 서류는 어떻게 준비하나요?")
                    .answer("저희가 통관에 필요한 모든 서류를 대행 처리해드립니다. 고객님은 상품 정보와 수취인 정보만 정확히 입력해주시면 됩니다.")
                    .category("통관")
                    .displayOrder(3)
                    .isActive(true)
                    .build(),
                Faq.builder()
                    .question("배송 상태는 어떻게 확인하나요?")
                    .answer("마이페이지에서 주문번호를 통해 실시간으로 배송 상태를 확인할 수 있습니다. 주요 진행 상황은 이메일과 SMS로도 안내드립니다.")
                    .category("배송")
                    .displayOrder(4)
                    .isActive(true)
                    .build(),
                Faq.builder()
                    .question("파손이나 분실 시 보상은 어떻게 되나요?")
                    .answer("배송 중 파손이나 분실이 발생한 경우, 보험 가입 여부에 따라 보상이 진행됩니다. 자세한 보상 기준은 고객센터로 문의해주세요.")
                    .category("보상")
                    .displayOrder(5)
                    .isActive(true)
                    .build()
            );
            
            faqRepository.saveAll(sampleFaqs);
        }
    }
}