package com.eai.api.settings;

import com.eai.api.distribution.LeadDistributionConfigResponse;
import com.eai.api.email.EmailAccountResponse;
import com.eai.api.message.MessageTemplateResponse;
import com.eai.api.tenant.CompanyResponse;
import com.eai.api.tenant.StoreResponse;
import com.eai.api.user.UserResponse;
import com.eai.application.settings.SettingsSnapshot;

import java.util.List;

public record SettingsResponse(
        CompanyResponse company,
        StoreResponse store,
        LeadDistributionConfigResponse distribution,
        List<CompanyResponse> availableCompanies,
        List<StoreResponse> availableStores,
        List<UserResponse> users,
        List<MessageTemplateResponse> templates,
        List<EmailAccountResponse> emailAccounts,
        SystemPreferencesResponse system
) {
    public static SettingsResponse fromSnapshot(SettingsSnapshot snapshot) {
        return new SettingsResponse(
                CompanyResponse.fromDomain(snapshot.company()),
                StoreResponse.fromDomain(snapshot.store()),
                LeadDistributionConfigResponse.fromSettings(snapshot.distributionSettings()),
                snapshot.availableCompanies().stream().map(CompanyResponse::fromDomain).toList(),
                snapshot.availableStores().stream().map(StoreResponse::fromDomain).toList(),
                snapshot.users().stream().map(UserResponse::fromDomain).toList(),
                snapshot.templates().stream().map(MessageTemplateResponse::fromDomain).toList(),
                snapshot.emailAccounts().stream().map(EmailAccountResponse::fromDomain).toList(),
                SystemPreferencesResponse.fromApplication(snapshot.systemPreferences())
        );
    }
}
