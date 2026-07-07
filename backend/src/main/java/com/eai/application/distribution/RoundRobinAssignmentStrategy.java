package com.eai.application.distribution;

import com.eai.application.lead.LeadRepository;
import com.eai.domain.lead.Lead;
import com.eai.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class RoundRobinAssignmentStrategy implements LeadAssignmentStrategy {

    private final LeadRepository leadRepository;

    public RoundRobinAssignmentStrategy(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Override
    public Optional<User> selectSeller(Lead lead, List<User> sellers) {
        List<User> orderedSellers = sellers.stream()
                .sorted(Comparator.comparing(User::getName).thenComparing(User::getId))
                .toList();
        if (orderedSellers.isEmpty()) {
            return Optional.empty();
        }
        UUID lastSellerId = leadRepository.findMostRecentAssignedSellerId(lead.getStoreId()).orElse(null);
        if (lastSellerId == null) {
            return Optional.of(orderedSellers.get(0));
        }
        for (int index = 0; index < orderedSellers.size(); index++) {
            if (orderedSellers.get(index).getId().equals(lastSellerId)) {
                return Optional.of(orderedSellers.get((index + 1) % orderedSellers.size()));
            }
        }
        return Optional.of(orderedSellers.get(0));
    }
}
