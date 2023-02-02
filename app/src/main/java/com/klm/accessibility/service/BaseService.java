package com.klm.accessibility.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


/*
 * 基类，封装了查找定位、点击、手势方法
 * */
public class BaseService extends AccessibilityService {

    private AccessibilityManager mAccessibilityManager;

    private Context mContext;

    private static BaseService mInstance;

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mAccessibilityManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    public static BaseService getInstance() {
        if (mInstance == null) {
            mInstance = new BaseService();
        }
        return mInstance;
    }

    /**
     * Check当前辅助服务是否启用
     *
     * @param serviceName serviceName
     * @return 是否启用
     */
    public boolean checkAccessibilityEnabled(String serviceName) {
        List<AccessibilityServiceInfo> accessibilityServices =
                mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 前往开启辅助服务界面
     */
    public void goAccess() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 模拟点击事件,如果该node不能点击，则点击父node，将点击事件一直向父级传递，直至到根node或者找到一个可以点击的node
     *
     * @param nodeInfo nodeInfo
     */
    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        while (nodeInfo != null) {
            if (nodeInfo.isClickable()) {
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }

    /**
     * 模拟返回操作
     */
    public void performBackClick() {
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    /**
     * 模拟下滑操作
     */
    public void performScrollBackward() {
        performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }

    /**
     * 模拟上滑操作
     */
    public void performScrollForward() {
        performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);

    }

    /**
     * 查找对应文本的View，无论该node能不能点击
     *
     * @param text text
     * @return View
     */
    public AccessibilityNodeInfo findViewByText(String text) {
        AccessibilityNodeInfo viewByText = findViewByText(text, true);
        if (viewByText == null) {
            viewByText = findViewByText(text, false);
        }
        return viewByText;
    }

    /**
     * 查找对应文本的View
     *
     * @param text      text
     * @param clickable 该View是否可以点击
     * @return View
     */
    public AccessibilityNodeInfo findViewByText(String text, boolean clickable) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }

        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null && (nodeInfo.isClickable() == clickable)) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    public AccessibilityNodeInfo findViewByTextInParent(String text, AccessibilityNodeInfo parent) {
        if (parent == null) {
            return null;
        }

        List<AccessibilityNodeInfo> nodeInfoList = parent.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }

    /**
     * 查找对应ID的View
     *
     * @param id id
     * @return View
     */
    public List<AccessibilityNodeInfo> findViewByID(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        return nodeInfoList;
    }


    /**
     * 点击对应文本的一个view，前提是这个view能够点击，即 clickable == true，
     *
     * @param text 要查找的文本
     */
    public void clickViewByText(String text) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo);
                    break;
                }
            }
        }
    }

    /**
     * 点击对应id的一个view，前提是这个view能够点击，即 clickable == true，
     *
     * @param id 要查找的id
     */
    public void clickViewByID(String id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    performViewClick(nodeInfo);
                    break;
                }
            }
        }
    }

    /**
     * 递归遍历node及其子node，点击文本相同的节点，全点击
     *
     * @param text
     * @param parentNode
     */
    public void clickNodesByText(String text, AccessibilityNodeInfo parentNode) {
        if (parentNode == null) {
            return;
        }

        int childCount = parentNode.getChildCount();
        if (childCount == 0) {  //叶节点
            if (parentNode.getText() == null) {
                return;
            }
            if (!text.equals(parentNode.getText().toString())) {
                return;
            }
            Rect rect = new Rect();
            parentNode.getBoundsInScreen(rect);

            int moveToX = (rect.left + rect.right) / 2;
            int moveToY = (rect.top + rect.bottom) / 2;
            int lineToX = (rect.left + rect.right) / 2;
            int lineToY = (rect.top + rect.bottom) / 2;

            gesture(moveToX, moveToY, lineToX, lineToY, 100L, 400L);
            return;
        }

        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo child = parentNode.getChild(i);
            clickNodesByText(text, child);
        }
    }

    /**
     * 根据文本查找节点
     *
     * @param text 要查找的文本
     * @return 与文本相同的节点列表，找不到则返回空
     */
    public List<AccessibilityNodeInfo> findNodesByText(String text) {
        List<AccessibilityNodeInfo> accessibilityNodeInfos = new ArrayList<>();
        Stack<AccessibilityNodeInfo> nodeStack = new Stack<>();
        AccessibilityNodeInfo node = getRootInActiveWindow();

        nodeStack.add(node);
        while (!nodeStack.isEmpty()) {
            node = nodeStack.pop();
            if (node != null && node.getText() != null && node.getText().toString().equals(text)) {
                accessibilityNodeInfos.add(node);
            }

            if (node == null || node.getChildCount() == 0) {
                continue;
            }

            //获得节点的子节点，对于二叉树就是获得节点的左子结点和右子节点
            int childCount = node.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    nodeStack.push(child);
                }
            }
        }
        if (accessibilityNodeInfos.size() > 0) {
            return accessibilityNodeInfos;
        } else {
            return null;
        }

    }

    /**
     * 模拟输入，低版本的输入有所不同，读者请自行百度
     *
     * @param nodeInfo nodeInfo
     * @param text     text
     */
    public void inputText(AccessibilityNodeInfo nodeInfo, String text) {
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

    /**
     * 手势操作，因为path不能小于0，因此小于则直接返回，不操作，另外如果有需求，可以自行修改小于则设置为0或者屏幕的宽高
     *
     * @param moveToX
     * @param moveToY
     * @param lineToX
     * @param lineToY
     * @param startTime
     * @param duration
     */
    public void gesture(int moveToX, int moveToY, int lineToX, int lineToY, long startTime, long duration) {

        if (moveToX < 0 || moveToY < 0 || lineToX < 0 || lineToY < 0) {
            Log.e("path", "path nagative");
            return;
        }

        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(moveToX, moveToY);
        path.lineTo(lineToX, lineToY);
        GestureDescription gestureDescription = builder
                .addStroke(new GestureDescription.StrokeDescription(path, startTime, duration, false))
                .build();
        dispatchGesture(gestureDescription, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
            }
        }, new Handler(Looper.getMainLooper()));
    }

    protected void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i("BaseService", "onAccessibilityEvent");
    }

    @Override
    public void onInterrupt() {
        Log.i("BaseService", "onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i("BaseService", "onServiceConnected");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("BaseService", "onDestroy");
    }
}
