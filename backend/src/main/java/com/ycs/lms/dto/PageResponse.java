package com.ycs.lms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private PageInfo page;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private int number;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;
    }
    
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;
        
        PageInfo pageInfo = PageInfo.builder()
            .number(page)
            .size(size)
            .totalElements(total)
            .totalPages(totalPages)
            .first(page == 0)
            .last(page == totalPages - 1)
            .hasNext(hasNext)
            .hasPrevious(hasPrevious)
            .build();
        
        return PageResponse.<T>builder()
            .content(content)
            .page(pageInfo)
            .build();
    }
}