package com.eai.application.distribution;

import com.eai.domain.lead.Lead;
import com.eai.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ManualAssignmentStrategy implements LeadAssignmentStrategy {

    @Override
    public Optional<User> selectSeller(Lead lead, List<User> sellers) {
        return Optional.empty();
    }
}
