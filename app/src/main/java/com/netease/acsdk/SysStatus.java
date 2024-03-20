package com.netease.acsdk;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.provider.Settings;

import com.netease.unidbgtestdemo.BuildConfig;

import java.lang.reflect.Method;
import java.util.HashMap;

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

    public static boolean getADBEnable(Application application) {
//        https://blog.csdn.net/babytiger/article/details/120477255
        boolean enableAdb = (Settings.Secure.getInt(application.getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1);
        return enableAdb;
    }

    public static boolean getDeveloperMode(Application application) {
//        https://blog.51cto.com/u_16213355/7248283
        boolean enableAdb = (Settings.Secure.getInt(application.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1);
        return enableAdb;
    }

    public static boolean getIsDeviceInUSBDevelperMode(Application application) {
//        插USB调试没有列表
        UsbManager usbDevice = (UsbManager)application.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbDevice.getDeviceList();
        return !deviceList.isEmpty();
    }

    public static String getUSBConfig() {
        String adbEnable = getSystemProperty("persist.sys.usb.config", "");
        return adbEnable;
    }


    private static String getSystemProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> clazz= Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", String.class, String.class);
            value = (String)(get.invoke(clazz, key, ""));
        } catch (Exception e) {

        }
        return value;
    }
}
