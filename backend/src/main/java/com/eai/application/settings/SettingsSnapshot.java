package com.eai.application.settings;

import com.eai.application.distribution.LeadDistributionSettings;
import com.eai.domain.email.EmailAccount;
import com.eai.domain.message.MessageTemplate;
import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.Store;
import com.eai.domain.user.User;

import java.util.List;

public record SettingsSnapshot(
        Company company,
        Store store,
        LeadDistributionSettings distributionSettings,
        List<Company> availableCompanies,
        List<Store> availableStores,
        List<User> users,
        List<MessageTemplate> templates,
        List<EmailAccount> emailAccounts,
        SystemPreferences systemPreferences
) {
}
