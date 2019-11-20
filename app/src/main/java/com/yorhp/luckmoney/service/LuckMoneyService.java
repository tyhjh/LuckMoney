package com.yorhp.luckmoney.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Rect;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.yorhp.luckmoney.util.ScreenUtil;

/**
 * 抢红包辅助
 *
 * @author Tyhj
 * @date 2019/6/30
 */

public class LuckMoneyService extends BaseAccessbilityService {

    /**
     * 单独抢一个群
     */
    public static boolean isSingle = true;
    /**
     * 暂停抢红包
     */
    public static boolean isPause = false;

    /**
     * 微信包名
     */
    private static final String WX_PACKAGE_NAME = "com.tencent.mm";

    /**
     * 红包弹出的class的名字
     */
    private static final String ACTIVITY_DIALOG_LUCKYMONEY = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI";

    /**
     * 红包标识字段
     */
    public static final String HONG_BAO_TXT = "[微信红包]";

    /**
     * 联系人列表的红包ID
     */
    private static final String HUMAN_LIST_TXT_ID = "com.tencent.mm:id/bal";

    /**
     * 头像ID
     */
    private static final String AVATAR_ID = "com.tencent.mm:id/po";

    /**
     * 已领取ID
     */
    private static final String AUL_ID = "com.tencent.mm:id/aul";

    /**
     * 联系人列表
     */
    private static final String HUMAN_LIST = "com.tencent.mm:id/dcf";

    /**
     * 红包ID
     */
    private static final String AUM_ID = "com.tencent.mm:id/aum";

    /**
     * 详情界面的聊天List 的ID
     */
    public static final String DETAIL_CHAT_LIST_ID = "com.tencent.mm:id/ag";
    /**
     * 红包详情页
     */
    private static String LUCKY_MONEY_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";

    /**
     * 红包的开字在屏幕中的比例
     */
    private static final float POINT_Y_SCAL = 0.641F;

    /**
     * 获取屏幕宽高
     */
    int screenWidth = ScreenUtil.SCREEN_WIDTH;
    int screenHeight = ScreenUtil.SCREEN_HEIGHT;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        //暂停
        if (isPause) {
            return;
        }

        String packageName = event.getPackageName().toString();
        if (!packageName.contains(WX_PACKAGE_NAME)) {
            //不是微信就退出
            return;
        }

        //当前类名
        String className = event.getClassName().toString();

        AccessibilityNodeInfo nodeInfo;

        //通知栏消息，判断是不是红包消息
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            String content = notification.tickerText.toString();
            String[] msg = content.split(":");
            String text = msg[1].trim();
            if (text.contains(HONG_BAO_TXT)) {
                PendingIntent pendingIntent = notification.contentIntent;
                try {
                    //点击消息，进入聊天界面
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }

        //在聊天详情页
        nodeInfo = findViewByID(DETAIL_CHAT_LIST_ID);
        if (nodeInfo != null) {
            //判断有没有未领取红包并进行点击
            clickItem(nodeInfo);
            return;
        }

        //当前为红包弹出窗（那个开的那个弹窗）
        if (className.equals(ACTIVITY_DIALOG_LUCKYMONEY)) {
            //进行红包开点击
            clickOpen();
            return;
        }

        //红包领取后的详情页面，自动返回
        if (className.equals(LUCKY_MONEY_DETAIL)) {
            performGlobalAction(GLOBAL_ACTION_BACK);
            if (!isSingle) {
                SystemClock.sleep(50);
                performGlobalAction(GLOBAL_ACTION_BACK);
            }
            return;
        }


        //在最近聊天列表，检测有没有红包消息出现
        nodeInfo = findViewByID(HUMAN_LIST);
        //联系人列表
        if (nodeInfo != null) {
            //判断最近聊天列表有没有未领取红包
            clickHumanItem(nodeInfo);
            return;
        }


    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 点击开红包按钮
     */
    private void clickOpen() {
        //等待红包弹窗完成，直接使用模拟点击比较快
        SystemClock.sleep(100);
        for (int i = 0; i < 20; i++) {
            SystemClock.sleep(10);
            clickOnScreen(screenWidth / 2, screenHeight * POINT_Y_SCAL, 1, null);
        }

        /*AccessibilityNodeInfo target = findViewByID("com.tencent.mm:id/dan");
        if (target != null) {
            performViewClick(target);
            return;
        } else {
            //如果没有找到按钮，进行模拟点击
            for (int i = 0; i < 20; i++) {
                SystemClock.sleep(10);
                clickOnScreen(screenWidth / 2, screenHeight * POINT_Y_SCAL, 1, null);
            }
        }*/
    }


    /**
     * 进行消息列表未领取红包的点击
     *
     * @param nodeInfo
     */
    private void clickItem(AccessibilityNodeInfo nodeInfo) {
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo nodeInfoChild = nodeInfo.getChild(i);
            AccessibilityNodeInfo target = findViewByID(nodeInfoChild, AUM_ID);
            AccessibilityNodeInfo avatar = findViewByID(nodeInfoChild, AVATAR_ID);
            boolean selfLuckMoney = false;
            //获取头像的位置，判断红包是否是自己发的，自己发的不抢
            if (avatar != null) {
                Rect rect = new Rect();
                avatar.getBoundsInScreen(rect);
                if (rect.left > screenWidth / 2) {
                    selfLuckMoney = true;
                }
            }
            if (target != null && !selfLuckMoney) {
                if (findViewByID(nodeInfoChild, AUL_ID) == null) {
                    performViewClick(target);
                    return;
                }
            }
        }
    }


    /**
     * 进行联系人列表的红包消息点击
     *
     * @param nodeInfo
     */
    private void clickHumanItem(AccessibilityNodeInfo nodeInfo) {
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo nodeInfoChild = nodeInfo.getChild(i);
            AccessibilityNodeInfo target = findViewByID(nodeInfoChild, HUMAN_LIST_TXT_ID);
            if (target != null && target.getText() != null && target.getText().toString().contains(HONG_BAO_TXT)) {
                performViewClick(target);
                return;
            }
        }
    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        ScreenUtil.getScreenSize(this);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        // 创建唤醒锁
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "WxService:wakeLock");
        // 获得唤醒锁
        wakeLock.acquire();
        performGlobalAction(GLOBAL_ACTION_BACK);
        performGlobalAction(GLOBAL_ACTION_BACK);
        performGlobalAction(GLOBAL_ACTION_BACK);
    }
}
