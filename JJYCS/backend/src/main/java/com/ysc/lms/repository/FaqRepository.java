package com.ysc.lms.repository;

import com.ysc.lms.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findByIsActiveTrueOrderByDisplayOrderAscIdAsc();
    List<Faq> findByCategoryAndIsActiveTrueOrderByDisplayOrderAscIdAsc(String category);
}