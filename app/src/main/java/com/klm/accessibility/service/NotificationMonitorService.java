package com.klm.accessibility.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

public class NotificationMonitorService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        try {
            if (sbn != null) {
                if (TextUtils.equals(AccessService.appPackageName, sbn.getPackageName())) {
                    if (sbn.getNotification() != null && sbn.getNotification().extras != null && !TextUtils.isEmpty(sbn.getNotification().extras.getString(Notification.EXTRA_TEXT))) {
                        if (sbn.getNotification().extras.getString(Notification.EXTRA_TEXT).contains("[微信红包]")) {
                            PendingIntent intent = sbn.getNotification().contentIntent;
                            intent.send();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
