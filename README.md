# Android_launcher
最近在研究安卓桌面管理，顺便写个小桌面管家demo

下面是效果图：<br>
![image](https://github.com/weioule/Android_Launcher/blob/master/app/img/img01.png)&nbsp;&nbsp;
![image](https://github.com/weioule/Android_Launcher/blob/master/app/img/img02.png)&nbsp;&nbsp;
![image](https://github.com/weioule/Android_Launcher/blob/master/app/img/img03.png)&nbsp;&nbsp;

桌面管家是对设备上的App进行定制动态管理，屏蔽所有系统设置入口，根据权限分为管理者模式与用户模式
<br>主要设计功能为：屏蔽黑名单、文件查询与上传、以及静默处理升级、降级、卸载、安装新包、重启、关机等功能.

1 屏蔽黑名单，通过对白名单的编辑锁定无权限使用的应用，并提交白名单

2 文件查询与上传，主要为各种错误日志与流程记录日志上传，通过定时器请求相应任务添加队列顺序执行，并指定定时器间隔时间

3 更新任务，将更新任务根据是否即时或定时进行静默处理，动态控制桌面应用

首页addRightView()函数为对部分手机屏蔽右上角的其他设置入口做的兼容，否则将做不到完全控制
<br>if (Build.BRAND.equals("xxxx") && Build.MODEL.equals("xxxx")). 这里的xxxx则为手机的品牌与型号

例如下图的定位设置页面：
<br>
![image](https://github.com/weioule/Android_Launcher/blob/master/app/img/img04.png)
<br>

要注意的是：因为涉及主桌面的更换与关机重启等危险权限，需要授予系统权限否则运行不起来，低版本4.4.4及以下是可以直接运行，高版本需要在清单文件里添加 android:sharedUserId="android.uid.system" ，将自己的程序加入到了系统的进程中，然后通过对apk进行系统签名 或 对系统进行root才能运行

关于系统签名可以自行百度或参考：https://blog.csdn.net/anlory/article/details/80360203

代码里都有注释，详细具体请看代码
