package com.eai.application.email;

import com.eai.application.lead.LeadRepository;
import com.eai.application.lead.PhoneNormalizer;
import com.eai.domain.lead.Lead;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DuplicateLeadChecker {

    private final LeadRepository leadRepository;

    public Optional<Lead> findPossibleDuplicate(UUID storeId, String phone) {
        String normalizedPhone = PhoneNormalizer.normalize(phone);
        if (normalizedPhone == null) {
            return Optional.empty();
        }
        return leadRepository.findMostRecentByStoreIdAndAnyPhone(storeId, List.of(normalizedPhone));
    }

    public boolean isPossibleDuplicate(UUID storeId, String phone, String vehicleInterest) {
        return findPossibleDuplicate(storeId, phone).isPresent();
    }
}
