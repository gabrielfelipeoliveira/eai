package com.eai.application.lgpd;

import java.util.List;

public record LgpdRequestPageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
