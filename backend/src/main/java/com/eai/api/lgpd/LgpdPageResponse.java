package com.eai.api.lgpd;

import com.eai.application.lgpd.LgpdRequestPageResult;

import java.util.List;
import java.util.function.Function;

public record LgpdPageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T, R> LgpdPageResponse<R> from(LgpdRequestPageResult<T> page, Function<T, R> mapper) {
        return new LgpdPageResponse<>(
                page.content().stream().map(mapper).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }
}
