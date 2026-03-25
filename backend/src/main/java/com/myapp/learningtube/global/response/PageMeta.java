package com.myapp.learningtube.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "목록 조회 페이징 메타 (1-based page)")
public class PageMeta {

    @Schema(description = "현재 페이지 (1부터)", example = "1")
    private long page;

    @Schema(description = "페이지 크기", example = "20")
    private int size;

    @Schema(description = "전체 행 수", example = "125")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "7")
    private int totalPages;

    @Schema(description = "첫 페이지 여부")
    private boolean first;

    @Schema(description = "마지막 페이지 여부")
    private boolean last;

    @Schema(description = "적용된 정렬 표현", example = "updatedAt,desc")
    private String sort;

    public static PageMeta from(Page<?> springPage, int pageOneBased, String sortExpression) {
        PageMeta m = new PageMeta();
        m.setPage(pageOneBased);
        m.setSize(springPage.getSize());
        m.setTotalElements(springPage.getTotalElements());
        m.setTotalPages(springPage.getTotalPages());
        m.setFirst(springPage.isFirst());
        m.setLast(springPage.isLast());
        m.setSort(sortExpression);
        return m;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
