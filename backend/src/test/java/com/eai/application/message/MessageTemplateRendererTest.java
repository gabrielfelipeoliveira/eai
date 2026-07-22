package com.eai.application.message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MessageTemplateRendererTest {

    @DisplayName("Renderizador substitui placeholders nomeados pelos dados disponiveis")
    @Test
    void rendersNamedPlaceholders() {
        String rendered = MessageTemplateRenderer.render(
                "Ola {cliente}, aqui e {vendedor} da {loja}.",
                Map.of("cliente", "Maria", "vendedor", "Ana", "loja", "EAI Motors")
        );

        assertThat(rendered).isEqualTo("Ola Maria, aqui e Ana da EAI Motors.");
    }

    @DisplayName("Parametros Meta preservam ordem e repeticao dos placeholders")
    @Test
    void extractsPlaceholderNamesInOrderWithRepetitions() {
        assertThat(MessageTemplateRenderer.placeholderNamesInOrder("Ola {cliente}. {cliente}, veja {veiculo}."))
                .containsExactly("cliente", "cliente", "veiculo");
    }
}
