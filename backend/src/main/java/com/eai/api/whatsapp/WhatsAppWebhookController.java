package com.eai.api.whatsapp;

import com.eai.application.whatsapp.WhatsAppWebhookService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/whatsapp")
public class WhatsAppWebhookController {

    private final WhatsAppWebhookService webhookService;

    public WhatsAppWebhookController(WhatsAppWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge
    ) {
        return webhookService.verifyWebhook(mode, verifyToken, challenge);
    }

    @PostMapping
    public ResponseEntity<Void> receiveEvent(@RequestBody String payload) {
        webhookService.receiveEvent(payload);
        return ResponseEntity.accepted().build();
    }
}
