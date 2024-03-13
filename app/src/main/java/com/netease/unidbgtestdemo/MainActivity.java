package com.netease.unidbgtestdemo;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


import android.os.Bundle;
import android.util.Log;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.netease.acsdk.SysStatus;
import com.netease.acsdk.Utils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "UnidbgTestDemo";
    public static final String LOG_FILE_NAME = "testRuntime.log";
    public static final String LOG_FILE_PATH = Environment.getExternalStorageDirectory().getPath()
                                                + File.separator + "demoLog" + File.separator + LOG_FILE_NAME;

    private static Application application;
    private static MainActivity activity;
    private static MyHandler mHandler;
    private TextView tvLog;
    private EditText etTestPaths;
    static {
        System.loadLibrary("nativetest");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        XXPermissions.with(this).permission(Permission.Group.STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            toast("获取部分权限成功，但部分权限未正常授予");
                            return;
                        }
                        toast("获取存储卡权限成功");
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            toast("被永久拒绝授权，请手动授予存储卡权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(activity, permissions);
                        } else {
                            toast("获取存储卡权限失败");
                        }
                    }
                });

        mHandler = new MyHandler(this);
        Button btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(this);
        Button btnGetTaskName = findViewById(R.id.btnGetAllTaskName);
        btnGetTaskName.setOnClickListener(this);
        tvLog = (TextView)findViewById(R.id.tvLog);
        Button btnClearLog = findViewById(R.id.btnClearLog);
        btnClearLog.setOnClickListener(this);
        findViewById(R.id.btnAccessTestPaths).setOnClickListener(this);
        etTestPaths = findViewById(R.id.etTestPaths);
        findViewById(R.id.btnEnableInotifyWatch).setOnClickListener(this);
        findViewById(R.id.btnGetSU).setOnClickListener(this);
        findViewById(R.id.btnGetDbgStatus).setOnClickListener(this);

        application = getApplication();

        Log.d(TAG, stringFromJNI());
        Log.d(TAG, stringFromJNIDynReg());
        Log.d(TAG, "hello world len:" + getStringLen("hello world"));
        Log.d(TAG, "myAdd(1,2)=" + new DemoTest().myAdd(1,2));
        Log.d(TAG, "pkgName:" + usingRefJava());
    }

    public void toast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.btnTest:{
                onBtnTest();
                break;
            }
            case R.id.btnGetAllTaskName:
                onGetAllTaskName();
                break;
            case R.id.btnClearLog:
                onClearLog();
                break;
            case R.id.btnAccessTestPaths:
                onBtnAccessPathTest();
                break;
            case R.id.btnEnableInotifyWatch:
                onBtnEnableInotifyWatch();
                break;
            case R.id.btnGetSU:
                onBtnGetSU();
                break;
            case R.id.btnGetDbgStatus:
                onBtnGetDbgStatus();
                break;
        }
    }

    private void onBtnEnableInotifyWatch() {
        String paths;
        String cfgTestFilePathFile = "/sdcard/tsPathsAccess";
        String pathFromCfgFile = Utils.readAllFileContext(cfgTestFilePathFile);
        if (pathFromCfgFile.isEmpty()) {
            paths = Environment.getExternalStorageDirectory().getPath();
        } else {
            paths = pathFromCfgFile;
        }
        String[] splits = paths.split(";");
        StringBuilder sb = new StringBuilder();
        Utils.stopInotifyWatch();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(String split:splits){
            int results = Utils.addInotifyWatchPatch(split);
            if (results <= 0) {
                sendLogData("test file access", "add watch path \"" + split + "\" failed");
                return;
            }
            sb.append(split).append(" ========== ").append(results == 1).append("\r\n");
        }
        int startRet = Utils.enableInotifyWatch();
        sb.append("start watch status").append(" ========== ").append(startRet == 1).append("\r\n");
        sendLogData("test file access", sb.substring(0, sb.length() - 1));
    }

    private void onBtnAccessPathTest() {
        String paths;
        String cfgTestFilePathFile = "/sdcard/tsPathsExist";
        String pathFromCfgFile = Utils.readAllFileContext(cfgTestFilePathFile);
        if (pathFromCfgFile.isEmpty()) {
            paths = cfgTestFilePathFile;
        } else {
            paths = pathFromCfgFile;
            etTestPaths.setText(paths);
        }
        String[] splits = paths.split(";");
        StringBuilder sb = new StringBuilder();
        for(String split:splits){
            String results = Utils.accessTestPaths(split);
            sb.append(split).append(" ========== ").append(results.equals("0")).append("\r\n");
        }
        sendLogData("test file exist", sb.substring(0, sb.length() - 1));
    }

    public static void sendLogData(String funcName, String data) {
        Message msg = new Message();
        msg.what = MyHandler.LOG_DATA;
        msg.obj = "----------" + funcName + "----------\r\n" + data;
        MainActivity.mHandler.sendMessage(msg);
    }

    private static void onBtnTest() {
        Log.d(TAG, "onBtnTest");
        SysStatus.getAppStaticDebugFlag(application);
    }

    private void onBtnGetDbgStatus() {
        String status = SysStatus.getAppStaticDebugFlag(this.getApplication());
        sendLogData("获取进程调试信息", status);
    }
    private void onBtnGetSU() {
        Utils.getSUPermission();
    }

    private void onGetAllTaskName() {
        String ret = getAllTaskName();
        Log.d(TAG, "allTaskName:\r\n" + ret);
        Message msg = new Message();
        msg.what = MyHandler.LOG_DATA;
        msg.obj = ret;
        mHandler.sendMessage(msg);
    }

    private void onClearLog() {
        MainActivity.this.tvLog.setText("");
        Utils.writeFile(LOG_FILE_PATH, "", false);
    }

    public Handler getHandler() {
        return mHandler;
    }

    private class MyHandler extends Handler {
        private MainActivity mainActivity = null;
        public static final int LOG_DATA = 0;
        public MyHandler(MainActivity ac) {
            mainActivity = ac;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOG_DATA:{
                    String date = (String)msg.obj;
                    onNewLog(date);
                } break;
            }
            super.handleMessage(msg);
        }
    }

    private void onNewLog(String data) {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        // 1
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
//        tvLog.setScrollbarFadingEnabled(false);//滚动条一直显示
//        String text = tvLog.getText().toString().trim();
//        StringBuilder stringBuffer = new StringBuilder();
//        StringBuilder append = stringBuffer.append(text)
//                .append("\n" + formatter.format(date) + "\n" + data)
//                .append("\n----------msg end----------\n")
//                .append("\n");
//        tvLog.setText(append);
//        int offset = getTextViewContentHeight(tvLog);
//        Log.d(TAG, "offset:" + offset + " tvHeight:" +tvLog.getHeight());
//        if (offset > tvLog.getHeight()) {
//            tvLog.scrollTo(0, offset - tvLog.getHeight());
//        }
        // 2
//        StringBuilder stringBuffer = new StringBuilder();
//        StringBuilder append = stringBuffer
//                .append("\n" + formatter.format(date) + "\n" + data)
//                .append("\n----------msg end----------\n")
//                .append("\n");
//        tvLog.append(append);
//        int offset=tvLog.getLineCount()*tvLog.getLineHeight();
//        if(offset>tvLog.getHeight()){
//            tvLog.scrollTo(0,offset-tvLog.getHeight());
//        }
        // 3
        StringBuilder stringBuffer = new StringBuilder();
        StringBuilder append = stringBuffer
                .append("\n" + formatter.format(date) + "\n" + data)
                .append("\n----------msg end----------\n")
                .append("\n");
        tvLog.append(append);
        Utils.writeFile(LOG_FILE_PATH, append.toString(), true);
        Log.d(TAG, "message:" + data);
    }

    private int getTextViewContentHeight(TextView textView) {
        Layout layout = textView.getLayout();
        int desired = layout.getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() + textView.getCompoundPaddingBottom();
        Log.d(TAG, "desired:" + desired+ " padding:" + padding);
        return desired + padding;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.demo.message")) {
                String content = intent.getStringExtra("content");
                if (content == null) {
                    Log.d(TAG, "message == null");
                    return;
                }
                Message msg = new Message();
                msg.what = MyHandler.LOG_DATA;
                msg.obj = content;
                mHandler.sendMessage(msg);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.receiver);
    }

    public static native String getAllTaskName();
    public static native String stringFromJNI();
    public static native String stringFromJNIDynReg();
    public static native int getStringLen(String str);
    public static native String usingRefJava();
}