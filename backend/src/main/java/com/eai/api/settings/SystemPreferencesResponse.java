package com.eai.api.settings;

import com.eai.application.settings.SystemPreferences;

public record SystemPreferencesResponse(
        String timezone,
        String locale,
        String dateFormat,
        boolean notificationsEnabled
) {
    public static SystemPreferencesResponse fromApplication(SystemPreferences preferences) {
        return new SystemPreferencesResponse(
                preferences.timezone(),
                preferences.locale(),
                preferences.dateFormat(),
                preferences.notificationsEnabled()
        );
    }
}
