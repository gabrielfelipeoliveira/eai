package com.eai.api.metadata;

public record MetadataOptionResponse(
        String code,
        String labelKey,
        String label,
        int order,
        String color
) {
}
