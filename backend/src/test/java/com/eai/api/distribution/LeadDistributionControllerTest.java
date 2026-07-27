package com.eai.api.distribution;

import com.eai.application.distribution.LeadDistributionService;
import com.eai.application.distribution.LeadDistributionSettings;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.distribution.LeadDistributionConfig;
import com.eai.domain.distribution.LeadDistributionMode;
import com.eai.domain.distribution.LeadSlaPolicy;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeadDistributionControllerTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();
    private static final AuthenticatedUser USER = new AuthenticatedUser(UUID.randomUUID(), "manager@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.MANAGER));

    private final LeadDistributionService distributionService = mock(LeadDistributionService.class);
    private final LeadDistributionController controller = new LeadDistributionController(distributionService);

    @DisplayName("Consulta configuracao de distribuicao com escopo informado")
    @Test
    void getConfigMapsSettingsToResponse() {
        when(distributionService.getSettings(COMPANY_ID, STORE_ID, USER)).thenReturn(settings(LeadDistributionMode.ROUND_ROBIN, true));

        LeadDistributionConfigResponse response = controller.getConfig(COMPANY_ID, STORE_ID, USER);

        assertThat(response.mode()).isEqualTo(LeadDistributionMode.ROUND_ROBIN);
        assertThat(response.minutesToAssign()).isEqualTo(15);
    }

    @DisplayName("Atualiza configuracao repassando comando ao servico")
    @Test
    void updateConfigDelegatesCommand() {
        when(distributionService.updateSettings(any(), eq(USER))).thenReturn(settings(LeadDistributionMode.LEAST_BUSY, false));

        LeadDistributionConfigResponse response = controller.updateConfig(new LeadDistributionConfigRequest(
                COMPANY_ID,
                STORE_ID,
                LeadDistributionMode.LEAST_BUSY,
                false,
                20,
                60,
                false
        ), USER);

        assertThat(response.mode()).isEqualTo(LeadDistributionMode.LEAST_BUSY);
        assertThat(response.active()).isFalse();
        verify(distributionService).updateSettings(any(), eq(USER));
    }

    private LeadDistributionSettings settings(LeadDistributionMode mode, boolean active) {
        return new LeadDistributionSettings(
                new LeadDistributionConfig(UUID.randomUUID(), COMPANY_ID, STORE_ID, mode, active),
                new LeadSlaPolicy(UUID.randomUUID(), COMPANY_ID, STORE_ID, 15, 45, true)
        );
    }
}
