package com.eai.application.tenant;

import com.eai.application.user.UserRepository;
import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.TenantStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompanyServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");

    private final CompanyRepository companyRepository = mock(CompanyRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CompanyService service = new CompanyService(companyRepository, userRepository);

    @Test
    void blocksDeactivationWhenCompanyHasActiveUsers() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company(TenantStatus.ACTIVE)));
        when(userRepository.existsActiveByCompanyId(COMPANY_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.updateCompany(COMPANY_ID, new UpdateCompanyCommand("EAI", TenantStatus.INACTIVE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("active users");
    }

    @Test
    void deactivatesCompanyWhenThereAreNoActiveUsers() {
        when(companyRepository.findById(COMPANY_ID)).thenReturn(Optional.of(company(TenantStatus.ACTIVE)));
        when(userRepository.existsActiveByCompanyId(COMPANY_ID)).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Company company = service.updateCompany(COMPANY_ID, new UpdateCompanyCommand("EAI", TenantStatus.INACTIVE));

        assertThat(company.getStatus()).isEqualTo(TenantStatus.INACTIVE);
    }

    private Company company(TenantStatus status) {
        Instant now = Instant.parse("2026-07-18T12:00:00Z");
        return new Company(COMPANY_ID, "EAI", status, now, now);
    }
}
