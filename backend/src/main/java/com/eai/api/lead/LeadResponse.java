package com.eai.api.lead;

import com.eai.domain.lead.Lead;
import com.eai.domain.lead.LeadSource;
import com.eai.domain.lead.LeadStatus;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record LeadResponse(
        UUID id,
        UUID companyId,
        UUID storeId,
        String customerName,
        String customerPhone,
        String customerEmail,
        String customerCity,
        String vehicleInterest,
        UUID itemId,
        LeadItemResponse item,
        LeadSource source,
        String originalMessage,
        LeadStatus status,
        UUID assignedToUserId,
        Instant assignedAt,
        Instant createdAt,
        Instant updatedAt,
        Instant firstContactAt,
        Instant lastContactAt,
        String lostReason,
        BigDecimal saleValue,
        String saleCurrency,
        boolean overdueToAssign,
        boolean overdueToFirstContact
) {

    public static LeadResponse fromDomain(Lead lead) {
        return fromDomain(lead, null, null, Instant.now());
    }

    public static LeadResponse fromDomain(Lead lead, Integer minutesToAssign, Integer minutesToFirstContact, Instant now) {
        boolean overdueToAssign = minutesToAssign != null
                && lead.getAssignedToUserId() == null
                && Duration.between(lead.getCreatedAt(), now).toMinutes() > minutesToAssign;
        Instant firstContactStart = lead.getAssignedAt() == null ? lead.getCreatedAt() : lead.getAssignedAt();
        boolean overdueToFirstContact = minutesToFirstContact != null
                && lead.getAssignedToUserId() != null
                && lead.getFirstContactAt() == null
                && Duration.between(firstContactStart, now).toMinutes() > minutesToFirstContact;
        return new LeadResponse(
                lead.getId(),
                lead.getCompanyId(),
                lead.getStoreId(),
                lead.getCustomerName(),
                lead.getCustomerPhone(),
                lead.getCustomerEmail(),
                lead.getCustomerCity(),
                lead.getVehicleInterest(),
                lead.getItemId(),
                lead.getItem() == null ? null : LeadItemResponse.fromDomain(lead.getItem()),
                lead.getSource(),
                lead.getOriginalMessage(),
                lead.getStatus(),
                lead.getAssignedToUserId(),
                lead.getAssignedAt(),
                lead.getCreatedAt(),
                lead.getUpdatedAt(),
                lead.getFirstContactAt(),
                lead.getLastContactAt(),
                lead.getLostReason(),
                lead.getSaleValue(),
                lead.getSaleCurrency(),
                overdueToAssign,
                overdueToFirstContact
        );
    }

    public record LeadItemResponse(UUID id, UUID ownerUserId, String name, LeadVehicleResponse vehicle, Instant createdAt, Instant updatedAt) {
        static LeadItemResponse fromDomain(com.eai.domain.item.Item item) {
            return new LeadItemResponse(
                    item.getId(),
                    item.getOwnerUserId(),
                    item.getName(),
                    item.getVehicle() == null ? null : LeadVehicleResponse.fromDomain(item.getVehicle()),
                    item.getCreatedAt(),
                    item.getUpdatedAt()
            );
        }
    }

    public record LeadVehicleResponse(UUID id, UUID itemId, String name, Integer year, String model, BigDecimal value, Instant createdAt, Instant updatedAt) {
        static LeadVehicleResponse fromDomain(com.eai.domain.item.Vehicle vehicle) {
            return new LeadVehicleResponse(
                    vehicle.getId(),
                    vehicle.getItemId(),
                    vehicle.getName(),
                    vehicle.getYear(),
                    vehicle.getModel(),
                    vehicle.getValue(),
                    vehicle.getCreatedAt(),
                    vehicle.getUpdatedAt()
            );
        }
    }
}
