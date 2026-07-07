package com.eai.application.email;

import com.eai.application.lead.LeadRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class DuplicateLeadChecker {

    private final LeadRepository leadRepository;

    public DuplicateLeadChecker(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    public boolean isPossibleDuplicate(UUID storeId, String phone, String vehicleInterest) {
        if (phone == null || phone.isBlank() || vehicleInterest == null || vehicleInterest.isBlank()) {
            return false;
        }
        Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
        return leadRepository.existsByStoreIdAndPhoneAndVehicleSince(storeId, normalizePhone(phone), vehicleInterest.trim(), since);
    }

    private String normalizePhone(String phone) {
        return phone.replaceAll("\\D", "");
    }
}
