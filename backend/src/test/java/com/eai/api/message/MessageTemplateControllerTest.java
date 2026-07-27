package com.eai.api.message;

import com.eai.application.message.MessageTemplateService;
import com.eai.application.security.AuthenticatedUser;
import com.eai.domain.message.MessageTemplate;
import com.eai.domain.message.MessageTemplateMetaStatus;
import com.eai.domain.message.MessageTemplateType;
import com.eai.domain.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageTemplateControllerTest {

    private static final UUID COMPANY_ID = UUID.randomUUID();
    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID TEMPLATE_ID = UUID.randomUUID();
    private static final AuthenticatedUser USER = new AuthenticatedUser(UUID.randomUUID(), "manager@eai.com", COMPANY_ID, STORE_ID, Set.of(UserRole.MANAGER));

    private final MessageTemplateService templateService = mock(MessageTemplateService.class);
    private final MessageTemplateController controller = new MessageTemplateController(templateService);

    @DisplayName("Lista templates gerais e ativos convertendo dominio para resposta")
    @Test
    void listTemplatesMapDomainToResponse() {
        when(templateService.listTemplates(USER)).thenReturn(List.of(template()));
        when(templateService.listActiveTemplates(USER)).thenReturn(List.of(template()));

        assertThat(controller.listTemplates(USER).getFirst().name()).isEqualTo("follow_up");
        assertThat(controller.listActiveTemplates(USER).getFirst().metaStatus()).isEqualTo(MessageTemplateMetaStatus.APPROVED);
    }

    @DisplayName("Busca, cria, atualiza e remove template por id")
    @Test
    void getCreateUpdateAndDeleteTemplate() {
        when(templateService.getTemplate(TEMPLATE_ID, USER)).thenReturn(template());
        when(templateService.createTemplate(any(), eq(USER))).thenReturn(template());
        when(templateService.updateTemplate(eq(TEMPLATE_ID), any(), eq(USER))).thenReturn(template());

        MessageTemplateRequest request = new MessageTemplateRequest(
                COMPANY_ID,
                STORE_ID,
                "follow_up",
                MessageTemplateType.FOLLOW_UP,
                "Ola {cliente}",
                "pt-BR",
                MessageTemplateMetaStatus.APPROVED,
                true
        );

        assertThat(controller.getTemplate(TEMPLATE_ID, USER).id()).isEqualTo(TEMPLATE_ID);
        assertThat(controller.createTemplate(request, USER).type()).isEqualTo(MessageTemplateType.FOLLOW_UP);
        assertThat(controller.updateTemplate(TEMPLATE_ID, request, USER).languageCode()).isEqualTo("pt-BR");
        controller.deleteTemplate(TEMPLATE_ID, USER);
        verify(templateService).deleteTemplate(TEMPLATE_ID, USER);
    }

    private MessageTemplate template() {
        Instant now = Instant.parse("2026-07-07T10:00:00Z");
        return new MessageTemplate(
                TEMPLATE_ID,
                COMPANY_ID,
                STORE_ID,
                "follow_up",
                MessageTemplateType.FOLLOW_UP,
                "Ola {cliente}",
                "pt-BR",
                MessageTemplateMetaStatus.APPROVED,
                true,
                now,
                now,
                null
        );
    }
}
