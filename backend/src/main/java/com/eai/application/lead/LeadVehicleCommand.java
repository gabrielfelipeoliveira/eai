package com.eai.application.lead;

import java.math.BigDecimal;

public record LeadVehicleCommand(String name, Integer year, String model, BigDecimal value) {

    public boolean hasData() {
        return (name != null && !name.isBlank()) || year != null || (model != null && !model.isBlank()) || value != null;
    }
}
