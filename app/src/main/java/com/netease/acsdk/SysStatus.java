package com.netease.acsdk;

import static com.netease.unidbgtestdemo.MainActivity.TAG;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.netease.unidbgtestdemo.BuildConfig;

public class SysStatus {

    public static String getIdResult() {
        return Utils.runShellCMD("id");
    }
    public static String getAppStaticDebugFlag(Application application) {
        // 获取应用的ApplicationInfo对象 可行
        ApplicationInfo applicationInfo = application.getApplicationInfo();
        // 检查调试标志
        boolean isDebuggable = (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        // 输出调试标志
        StringBuilder sb = new StringBuilder();
        sb.append("ApplicationInfo.FLAG_DEBUGGABLE：").append(isDebuggable).append("\n");

        isDebuggable = BuildConfig.DEBUG;
        sb.append("BuildConfig.DEBUG：").append(isDebuggable);
        return sb.toString();
    }
}
