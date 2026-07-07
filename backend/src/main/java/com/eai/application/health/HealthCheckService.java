package com.eai.application.health;

import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {

    public String status() {
        return "UP";
    }
}
