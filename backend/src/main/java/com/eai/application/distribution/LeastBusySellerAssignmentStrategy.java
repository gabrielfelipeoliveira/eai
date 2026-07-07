package com.eai.application.distribution;

import com.eai.application.lead.LeadRepository;
import com.eai.domain.lead.Lead;
import com.eai.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class LeastBusySellerAssignmentStrategy implements LeadAssignmentStrategy {

    private final LeadRepository leadRepository;

    public LeastBusySellerAssignmentStrategy(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Override
    public Optional<User> selectSeller(Lead lead, List<User> sellers) {
        return sellers.stream()
                .min(Comparator
                        .comparingLong((User seller) -> leadRepository.countOpenByAssignedToUserId(seller.getId()))
                        .thenComparing(User::getName)
                        .thenComparing(User::getId));
    }
}
