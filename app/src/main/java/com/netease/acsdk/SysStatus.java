package com.netease.acsdk;

import static com.netease.unidbgtestdemo.MainActivity.TAG;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.netease.unidbgtestdemo.BuildConfig;

public class SysStatus {

    public static void getIdResult() {
        String id = utils.runShellCMD("id");
        Log.d(TAG, "getIdResult:" + id);
    }
    public static void getAppStaticDebugFlag(Application application) {
        // 获取应用的ApplicationInfo对象 可行
        ApplicationInfo applicationInfo = application.getApplicationInfo();
        // 检查调试标志
        boolean isDebuggable = (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        // 输出调试标志
        System.out.println("调试标志：" + isDebuggable);

        isDebuggable = BuildConfig.DEBUG;
        System.out.println("调试标志：" + isDebuggable);
    }
}
