package com.ycs.lms.util;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * 페이징 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    /**
     * Page 객체로부터 PagedResponse 생성
     */
    public static <T> PagedResponse<T> of(List<T> content, Page<?> page) {
        return PagedResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    /**
     * 빈 PagedResponse 생성
     */
    public static <T> PagedResponse<T> empty() {
        return PagedResponse.<T>builder()
                .content(List.of())
                .page(0)
                .size(0)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .build();
    }
} 