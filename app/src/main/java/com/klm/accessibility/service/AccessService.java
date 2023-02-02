package com.klm.accessibility.service;

import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * 操作类，在这里实现具体逻辑
 */
public class AccessService extends BaseService {

    public static final String appPackageName = "com.tencent.mm";
    private boolean wxhblb = true; // 控制在未处理完逻辑前不要进入逻辑空间
    private boolean wxhbdk = true; // 控制在未处理完逻辑前不要进入逻辑空间
    private boolean back = true; // 控制在未处理完逻辑前不要进入逻辑空间

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName() == null ? "" : event.getPackageName().toString();
        if (!packageName.equals(appPackageName)) {// 如果活动APP不是目标APP则不响应
            return;
        }
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:// 捕获窗口内容改变事件
                if (packageName.equals(appPackageName)) {
                    if (wxhblb) {
                        wxhblb = false;
                        List<AccessibilityNodeInfo> wxhbxx = findViewByID("com.tencent.mm:id/fhs");
                        if (wxhbxx != null && wxhbxx.size() > 0) {
                            for (AccessibilityNodeInfo nodeInfo : wxhbxx) {
                                if (nodeInfo.getText().toString().contains("[微信红包]")) {
                                    performViewClick(nodeInfo);
                                }
                            }
                        }
                        List<AccessibilityNodeInfo> wxhb = findViewByID("com.tencent.mm:id/y4");
                        if (wxhb != null && wxhb.size() > 0) {
                            for (AccessibilityNodeInfo nodeInfo : wxhb) {
                                AccessibilityNodeInfo parent = nodeInfo.getParent();
                                if (parent != null) {
                                    AccessibilityNodeInfo ylq = findViewByTextInParent("已领取", parent.getParent());
                                    AccessibilityNodeInfo yblw = findViewByTextInParent("已被领完", parent.getParent());
                                    if (ylq == null && yblw == null) {
                                        performViewClick(nodeInfo);
                                    }
                                }
                            }
                        }
                        // 更多的操作请看BaseService，或者自行百度
                        wxhblb = true;
                    }
                    if (wxhbdk) {
                        wxhbdk = false;
                        clickViewByID("com.tencent.mm:id/giq");
                        AccessibilityNodeInfo sml = findViewByText("手慢了，红包派完了");
                        if (sml != null) {
                            clickViewByID("com.tencent.mm:id/gip");
                        }
                        wxhbdk = true;
                    }
                    if (back) {
                        back = false;
                        clickViewByID("com.tencent.mm:id/k6i");
                        back = true;
                    }
                }
                break;
            default:
                break;
        }
    }
}

