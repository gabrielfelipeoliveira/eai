package com.eai.application.notification;

import com.eai.domain.notification.Notification;
import com.eai.domain.notification.NotificationExternalDeliveryStatus;

public interface NotificationDeliveryPort {

    NotificationExternalDeliveryStatus deliver(Notification notification);
}
