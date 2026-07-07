package com.eai.application.distribution;

import com.eai.domain.distribution.LeadDistributionConfig;
import com.eai.domain.distribution.LeadSlaPolicy;

public record LeadDistributionSettings(LeadDistributionConfig distributionConfig, LeadSlaPolicy slaPolicy) {
}
