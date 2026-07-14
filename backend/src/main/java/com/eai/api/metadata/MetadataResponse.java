package com.eai.api.metadata;

import java.util.List;

public record MetadataResponse(
        String locale,
        List<MetadataOptionResponse> leadStatuses,
        List<MetadataOptionResponse> leadSources,
        List<MetadataOptionResponse> followUpStatuses,
        List<MetadataOptionResponse> userRoles,
        List<MetadataOptionResponse> userStatuses,
        List<MetadataOptionResponse> tenantStatuses,
        List<MetadataOptionResponse> messageTemplateTypes,
        List<MetadataOptionResponse> leadDistributionModes,
        List<MetadataOptionResponse> emailAccountStatuses,
        List<MetadataOptionResponse> emailProtocols,
        List<MetadataOptionResponse> conversationMessageDirections,
        List<MetadataOptionResponse> conversationMessageTypes,
        List<MetadataOptionResponse> conversationMessageStatuses
) {
}
