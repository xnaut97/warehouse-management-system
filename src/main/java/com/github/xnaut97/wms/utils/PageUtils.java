package com.github.xnaut97.wms.utils;

import com.github.xnaut97.wms.dto.common.PageResponse;
import org.springframework.data.domain.Page;

import java.util.function.Function;

public final class PageUtils {

    private PageUtils() {
    }

    public static <T, R> PageResponse<R> from(Page<T> page, Function<T, R> mapper) {

        return PageResponse.<R>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

}