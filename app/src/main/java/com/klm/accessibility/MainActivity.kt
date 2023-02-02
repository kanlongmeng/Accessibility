package com.klm.accessibility

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import com.klm.accessibility.service.BaseService
import com.klm.accessibility.service.NotificationMonitorService

/**
 * 微信抢红包，测试 微信版本8.0.32
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        BaseService.getInstance().init(this);
        if (!BaseService.getInstance().checkAccessibilityEnabled("无障碍抢红包服务")) {
            BaseService.getInstance().goAccess();
        }

        if (NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)) {
            val intent = Intent(this, NotificationMonitorService::class.java)
            startService(intent)
        } else {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }
    }
}