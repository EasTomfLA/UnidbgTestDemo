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
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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

import com.netease.acsdk.SysStatus;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "UnidbgTestDemo";
    private static Application application;
    private static MainActivity activity;
    private MyHandler mHandler;
    private TextView tvLog;
    static {
        System.loadLibrary("nativetest");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        mHandler = new MyHandler(this);
        Button btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(this);
        Button btnGetTaskName = findViewById(R.id.btnGetAllTaskName);
        btnGetTaskName.setOnClickListener(this);
        tvLog = (TextView)findViewById(R.id.tvLog);
        Button btnClearLog = findViewById(R.id.btnClearLog);
        btnClearLog.setOnClickListener(this);

        application = getApplication();

        Log.d(TAG, stringFromJNI());
        Log.d(TAG, stringFromJNIDynReg());
        Log.d(TAG, "hello world len:" + getStringLen("hello world"));
        Log.d(TAG, "myAdd(1,2)=" + new DemoTest().myAdd(1,2));
        Log.d(TAG, "pkgName:" + usingRefJava());
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
        }
    }

    private static void onBtnTest() {
        Log.d(TAG, "onBtnTest");
        SysStatus.getIdResult();
        SysStatus.getAppStaticDebugFlag(application);
    }

    private void onGetAllTaskName() {
        String ret = getAllTaskName();
        Log.d(TAG, "allTaskName:\r\n" + ret);
        Message msg = new Message();
        msg.what = MyHandler.LOG_DATA;
        msg.obj = ret;
        MainActivity.this.mHandler.sendMessage(msg);
    }

    private void onClearLog() {
        MainActivity.this.tvLog.setText("");
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
        MainActivity.this.tvLog.append("\n" + formatter.format(date) + "\n" + data);
        MainActivity.this.tvLog.append("\n----------msg end----------\n");
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
                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
                Date date = new Date(System.currentTimeMillis());
                MainActivity.this.tvLog.append("\n" + formatter.format(date) + content);
                MainActivity.this.tvLog.append("\n----------msg end----------\n");
//                int offset=tv_content.getLineCount()*tv_content.getLineHeight();
//                if (offset > tv_content.getHeight()) {
//                    tv_content.scrollTo(0, offset - tv_content.getHeight());
//                }
                Log.d(TAG, "message:" + content);
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