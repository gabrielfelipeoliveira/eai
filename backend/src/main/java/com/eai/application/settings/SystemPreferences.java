package com.eai.application.settings;

public record SystemPreferences(
        String timezone,
        String locale,
        String dateFormat,
        boolean notificationsEnabled
) {
    public static SystemPreferences defaults() {
        return new SystemPreferences("America/Sao_Paulo", "pt-BR", "dd/MM/yyyy", true);
    }
}
