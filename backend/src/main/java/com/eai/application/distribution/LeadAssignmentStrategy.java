package com.eai.application.distribution;

import com.eai.domain.lead.Lead;
import com.eai.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface LeadAssignmentStrategy {

    Optional<User> selectSeller(Lead lead, List<User> sellers);
}
