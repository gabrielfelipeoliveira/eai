package com.eai.api.tenant;

import com.eai.application.tenant.CompanyService;
import com.eai.domain.tenant.Company;
import com.eai.domain.tenant.TenantStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompanyControllerTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();

    private final CompanyService companyService = mock(CompanyService.class);
    private final CompanyController controller = new CompanyController(companyService);

    @DisplayName("Lista e busca empresas convertendo dominio para resposta")
    @Test
    void listAndGetCompaniesMapDomainToResponse() {
        when(companyService.listCompanies()).thenReturn(List.of(company()));
        when(companyService.getCompany(COMPANY_ID)).thenReturn(company());

        assertThat(controller.listCompanies().getFirst().name()).isEqualTo("Empresa EAI");
        assertThat(controller.getCompany(COMPANY_ID).status()).isEqualTo(TenantStatus.ACTIVE);
    }

    @DisplayName("Cria e atualiza empresa repassando comandos ao servico")
    @Test
    void createAndUpdateCompanyDelegateCommands() {
        when(companyService.createCompany(any())).thenReturn(company());
        when(companyService.updateCompany(eq(COMPANY_ID), any())).thenReturn(company());

        assertThat(controller.createCompany(new CompanyCreateRequest("Empresa EAI")).id()).isEqualTo(COMPANY_ID);
        assertThat(controller.updateCompany(COMPANY_ID, new CompanyRequest("Empresa EAI", TenantStatus.ACTIVE)).name()).isEqualTo("Empresa EAI");
        verify(companyService).createCompany(any());
        verify(companyService).updateCompany(eq(COMPANY_ID), any());
    }

    private Company company() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new Company(COMPANY_ID, "Empresa EAI", TenantStatus.ACTIVE, now, now);
    }
}
