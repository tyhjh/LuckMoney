>阅读原文：https://www.jianshu.com/p/5a44b6eaba20

快到年底了，又到了拼手速抢红包的时候了；其实很早之前就做过抢红包软件了，包括QQ和微信；但是大家都懂的，自己一个月前写的代码现在看起来都像是一坨shit一样；所以自己开始重新写一个抢红包的软件（其实是因为实在是太简单了），只做微信，因为QQ发红包的确用的太少了，而且QQ红包花样也太多了，什么唱歌、画画、成语接龙...

## 目标

 1. 快，天下武功无坚不摧、唯快不破，肯定要比人的手速快
 2. 准，只要你手机解锁了，在任意一个界面都可以快速抢到红包
 3. 狠，其实狠不狠没什么关系了，最重要的是全自动，自己不用任何操作，不然怎么解放双手
 4. 稳，肯定要能一直抢红包，来一个抢一个，来两个抢两个，抢红包一时爽，一直抢一直爽；
 
## 手机配置要求

 1. Android系统 7.0及以上，辅助功能7.0以上支持模拟点击，模拟点击不是必须的，但是对于实现**快**很重要
 2. 手机不能太垃圾了，手机慢有外挂也发挥不出来呀

## 实现原理
实现方法就是利用Android辅助功能，开启辅助功能相当于开启了一个服务，在手机界面改变的时候，就能监听到该页面的一些信息并且能拿到界面的一些控件，然后可以对控件进行模拟点击，从而实现我们想要的功能。

除此以外，不仅能够对获取到的控件进行模拟点击，在Android7.0及以上的版本，我们可以模拟任意位置的点击包括触摸、滑动等等，就是说我们可以实现任何**人能够进行的操作**，这个是很有用的，可以做出很多有意思的东西，如果再配上截图、录屏和图像识别，就更有意思了。

模拟点击，就是说我们的手机界面自己动，整个流程像是一只手在帮你操作一样的；其实我见过更牛逼的方法，连解锁都不需要直接就领了红包，界面没有任何变化的；感觉上是通过通信，发数据给微信服务器实现的，当然这种是需要root权限的，并且得去解析微信的通信协议，我自然没时间去搞（其实有时间也不一定能搞出来）。

## 具体实现
### 辅助功能
首先是辅助功能，新建一个**Service**继承**AccessibilityService**
```java
public class LuckMoneyService extends AccessibilityService
```
然后去**AndroidManifest**文件里面去注册一下这个Service
```xml
    <service
            android:name=".service.LuckMoneyService"
            android:label="小圆脸的红包助手"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessible_service_wx_config" />
        </service>
```
在`meta-data`节点下有个`resource`值，这是个xml文件，里面配置了该辅助的一些信息，在res目录下新建一个文件夹，名字叫xml，然后新建一个xml文件，名字和`resource`配置的一样就行了
```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged|typeWindowContentChanged|typeNotificationStateChanged"
    android:accessibilityFeedbackType="feedbackAllMask"
    android:accessibilityFlags="flagDefault|flagRetrieveInteractiveWindows|flagIncludeNotImportantViews|flagReportViewIds"
    android:canRetrieveWindowContent="true"
    android:canRequestFilterKeyEvents="true"
    android:description="@string/wx_luck_money"
    android:canRequestEnhancedWebAccessibility="true"
    android:notificationTimeout="20"
    android:packageNames="com.tencent.mm"
    android:canPerformGestures="true" />
```
里面配置了一些参数，比如`notificationTimeout`是指定多少毫秒监听一次界面变化的，`packageNames`是指定监听哪个应用的，删掉这个配置就是监听全局，**建议一定要删除掉**，我这里只是展示用，`description`是对于该辅助的描述，其他配置不管也罢。

然后在LuckMoneyService里面重写一下onAccessibilityEvent方法
```java
  @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    //界面发生了变化
    }
```
每当界面改变的时候就会回调这个方法，通过`event`我们就可以获取到界面的信息包括界面上的控件

#### 简单的用法

```java
//获取当前界面包名
String packageName = event.getPackageName().toString();
//获取当前类名
String className = event.getClassName().toString();
//获取当前界面父布局的控件
AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
//在父布局里面根据子控件**显示的文字**找到该子控件
List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText(text);
//在父布局里面根据子控件的**id**找到该子控件
List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
//点击该控件
nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
```
上面的操作都比较基础，根据控件显示的文字查找控件，找出来的肯定是TextView和Button了，根据ID查找控件，ID就是指的写布局文件的时候设置的控件的ID

#### 模拟触摸
模拟触摸就是可以模拟人的触摸动作，也比较简单
```java
   protected void gestureOnScreen(Path path, long startTime, long duration,
                                   AccessibilityService.GestureResultCallback callback) {
        GestureDescription.Builder builde = new GestureDescription.Builder();
        builde.addStroke(new GestureDescription.StrokeDescription(path, startTime, duration));
        GestureDescription gestureDescription = builde.build();
        dispatchGesture(gestureDescription, callback, null);
    }
```
可以看到需要传入`path`就是一个路径嘛，模拟滑动的路径，用canvas画过画的都知道这东西还是比较简单的，不清楚也没关系，继续看，`startTime`就是多久后开始模拟事件，`duration`就是该滑动的时间，其他回调什么的为空就可以了；

辅助功能能做的东西大概就上面这些了，接下来看看

### 微信应用外的红包处理
首先实现在微信界面外怎么抢红包，在微信界面外有红包出现必然会在通知栏会显示微信红包（如果没开通知消息，那你自己开一下不就完事了吗），只需要在回调方法里面判断一下是不是通知消息，如果是通知消息，获取里面的信息，判断是不是微信红包通知消息，是就点击该消息，会自动跳转到聊天界面；

因为我们是监听界面变化来实现功能的，所以在一个界面触发了界面变化的时候，接下来的处理就应该交给下一个界面的方法了，所以微信界面外的操作就是这些了

```java
    /**
     * 红包标识字段
     */
    public static final String HONG_BAO_TXT = "[微信红包]";
    
    //通知栏消息，判断是不是红包消息
        if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
            Notification notification = (Notification) event.getParcelableData();
            //获取通知消息详情
            String content = notification.tickerText.toString();
            //解析消息
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
```

其中**PendingIntent**这个东西写过通知栏的都知道，这个是设置跳转到哪个界面的，所以直接调用它的方法就完成了界面跳转了

### 聊天界面的红包处理
界面外的红包点击通知栏消息就来到了聊天界面，其实所有的界面都必须经过这个界面才能领取到红包，所以这个界面很重要；

#### 实现
思路是这样的，聊天消息肯定是一个列表控件，其实是个**ListView**，而且肯定有控件ID，我们获取到这个ListView，然后遍历它的每个消息（只能遍历到当前界面显示的），判断这个消息是不是微信红包，如果是，并且未被领取，而且这个红包还得是别人发的，不是自己发的，我们才去点击这个消息，触发界面变化，然后丢给下一个界面处理；

```java
        //获取聊天消息列表List控件
        AccessibilityNodeInfo nodeInfo = findViewByID(DETAIL_CHAT_LIST_ID);
        //这个消息列表不为空，那么肯定在聊天详情页
        if (nodeInfo != null) {
            //判断有没有未领取红包并进行点击
            clickItem(nodeInfo);
            return;
        }
        

    /**
     * 进行消息列表未领取红包的点击
     *
     * @param nodeInfo
     */
    private void clickItem(AccessibilityNodeInfo nodeInfo) {
        //遍历消息列表的每个消息
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            //获取到子控件
            AccessibilityNodeInfo nodeInfoChild = nodeInfo.getChild(i);
            //获取红包控件
            AccessibilityNodeInfo target = findViewByID(nodeInfoChild, AUM_ID);
            //获取头像的控件
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
            //如果不是自己发的红包，并且获取到的微信红包这个控件不为空
            if (target != null && !selfLuckMoney) {
                //已领取这个控件为空，红包还没有被领取
                if (findViewByID(nodeInfoChild, AUL_ID) == null) {
                    //点击红包控件
                    performViewClick(target);
                    return;
                }
            }
        }
    }
```

里面每个细节都注释了，获取ListView控件，获取到了说明是在消息界面，获取到消息列表的每一个控件，根据 是否是红包消息，是否是别人发的，是否是未领取的三点，去判断是否是可以领取的红包，然后点击可领取的红包，到达弹出**開**的这个弹窗的界面；


#### monitor
如何获取这个ListView控件的ID呢，而我又是如何知道是ListView的呢，可以通过一个工具来实现，就是在sdk工具下面的一个叫**monitor**的工具，其实之前的AndroidStudio是带这个工具的，但是后来界面上是没有了，但是其实还在的



```java
/Users/Tyhj/Library/Android/sdk/tools/monitor
```
连上手机，打开这个工具，手机上打开你要查看的界面，点击工具手机的小手机的图标，就会截屏，显示出这个界面的信息
![截屏2019-12-09上午1.00.04.png-752.2kB](http://static.zybuluo.com/Tyhj/s2pre1919kyc83vx1d1w489u/%E6%88%AA%E5%B1%8F2019-12-09%E4%B8%8A%E5%8D%881.00.04.png)

### 红包弹窗界面处理

![截屏2019-12-09上午1.11.52.png-613.4kB](http://static.zybuluo.com/Tyhj/kkcwfsysnb3parwmsdv9bahm/%E6%88%AA%E5%B1%8F2019-12-09%E4%B8%8A%E5%8D%881.11.52.png)

同样的红包弹窗这个界面也是必须经过的，十分重要；你要说这个弹窗界面也比较简单，我们判断一下是不是这个界面，然后点击开不就完事儿了；测试可以发现，这个弹窗出现的时候，当前的界面className是这个
```java
    **
     * 红包弹出的class的名字
     */
    private static final String ACTIVITY_DIALOG_LUCKYMONEY = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI";
```

事情没有这么容易，当我去获取这个**開**的这个控件的时候，发现为空，获取不到，其实整个弹窗都获取不到，遇到这个问题的人肯定不少；
```java
AccessibilityNodeInfo target = findViewByID("com.tencent.mm:id/dan");
```
其实深究下去，发现获取根布局都为空了，测试发现必须等待一段时间再去获取这个弹窗才行，但是等多久呢，大概几百毫秒吧，不定时的，不同手机也不一定，那么随便设一个就不行，因为你时间设置小了，程序可能会卡在这里抢不了红包了，肯定不行；设置大了，行，但是影响速度呀。那么开个循环去获取直到获取到不为空行吗？不行，奇怪的就是你一次去获取为空了，之后获取都为空了；只有等待一段时间后第一次去获取才不为空，这TMD就很奇怪了，看了一下的确没法解决。
```java
AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
```
那其实还有个办法就是模拟点击，在红包弹窗弹出来的时候，我疯狂点击这个開字的位置，就行了；開字的位置可以通过屏幕比例来计算出来，这个就算不同的手机屏幕都可以点击到这个開字；

```java
        //当前为红包弹出窗（那个开的那个弹窗）
        if (className.equals(ACTIVITY_DIALOG_LUCKYMONEY)) {
            //进行红包开点击
            clickOpen();
            return;
        }
/**
     * 点击开红包按钮
     */
    private void clickOpen() {
        //等待红包弹窗完成，直接使用模拟点击比较快
        SystemClock.sleep(100);
        for (int i = 0; i < 20; i++) {
            SystemClock.sleep(10);
            //计算了一下这个開字在屏幕中的位置，按照屏幕比例计算
            clickOnScreen(screenWidth / 2, screenHeight * POINT_Y_SCAL, 1, null);
        }

        /*AccessibilityNodeInfo target = findViewByID("com.tencent.mm:id/dan");
        if (target != null) {
            performViewClick(target);
            return;
        } else {
            //如果没有找到按钮，再进行模拟点击
            for (int i = 0; i < 20; i++) {
                SystemClock.sleep(10);
                clickOnScreen(screenWidth / 2, screenHeight * POINT_Y_SCAL, 1, null);
            }
        }*/
    }
```
点击了这个開字后，进入了**红包详情页**，进行下一步处理。

### 红包详情页处理
进入了**红包详情页**，红包已经到手了，想要继续抢红包，肯定需要退出去，这个简单，有返回键的方法；这时候你可以返回聊天界面继续抢这个群的红包(如果专抢一个群的，这样效率高)，也可以返回到**最近消息列表**（微信主页面第一个界面），可以抢其他群的红包（抢其多个群的红包，这样效率高），也可以退回手机主界面（抢红包效率低，因为还需要点击通知栏消息进去）；可以设置一下，如果开启专抢一个群，就退回该群聊天界面，否则退回最近消息列表界面。

```java
        //红包领取后的详情页面，自动返回
        if (className.equals(LUCKY_MONEY_DETAIL)) {
            //返回聊天界面
            performGlobalAction(GLOBAL_ACTION_BACK);
            //如果不是专抢一个群
            if (!isSingle) {
                SystemClock.sleep(50);
                performGlobalAction(GLOBAL_ACTION_BACK);
            }
            return;
        }
```
### 最近消息列表界面处理

![截屏2019-12-09上午2.01.36.png-541kB](http://static.zybuluo.com/Tyhj/qasesdlolfqwyl7icie1m64c/%E6%88%AA%E5%B1%8F2019-12-09%E4%B8%8A%E5%8D%882.01.36.png)

当领完红包后，退出到最近消息列表界面是比较好的选择；这个界面上当收到红包消息通知栏是不会有提醒的；我们需要根据界面的显示去判断有没有红包；其实也是特别简单，它也是一个ListView，同样的遍历一下每个item，判断有没有微信红包消息，然后点击进入聊天消息界面

```java
        //在最近聊天列表，检测有没有红包消息出现
        nodeInfo = findViewByID(HUMAN_LIST);
        //联系人列表
        if (nodeInfo != null) {
            //判断最近聊天列表有没有未领取红包
            clickHumanItem(nodeInfo);
            return;
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
```
看似没有问题，实则有一个问题，就是在这个聊天列表里面，没法判断这个红包是别人发的还是你自己发的，如果是你自己发的那肯定有问题的，这是一个坑，当然可以通过保存一些数据，比如说第一次进去后发现是自己发的红包就退出来，如果界面没变化第二次就不再进行点击了；但是其实问题也不大吧，最多就是你发完红包后自己再发个消息就可以避免了。



## 测试总结
其实到这里就全完成了，实际效果也不错，测了一下，4个人和一个辅助比，发了20次红包，辅助大概能抢到18次吧，并不是百分百抢到，主要是人有准备的话疯狂点屏幕其实也挺快的（单身20年的同学的手速不得不服，毕竟有个地方我还是sleep了100毫秒的，其实去掉应该更快的），一般情况下辅助还是有绝对优势的。


## 项目地址
里面有一些方法是封装了的，方便调用，具体实现可以看代码
原文地址：[Android微信抢红包辅助](https://www.jianshu.com/p/5a44b6eaba20)
github地址：[Android微信抢红包辅助](https://github.com/tyhjh/LuckMoney)


