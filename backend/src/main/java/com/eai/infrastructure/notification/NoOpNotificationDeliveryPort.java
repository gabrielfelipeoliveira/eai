package com.eai.infrastructure.notification;

import com.eai.application.notification.NotificationDeliveryPort;
import com.eai.domain.notification.Notification;
import com.eai.domain.notification.NotificationExternalDeliveryStatus;
import org.springframework.stereotype.Component;

@Component
public class NoOpNotificationDeliveryPort implements NotificationDeliveryPort {

    @Override
    public NotificationExternalDeliveryStatus deliver(Notification notification) {
        return NotificationExternalDeliveryStatus.PENDING_EXTERNAL_DELIVERY;
    }
}
