package com.netease.unidbgtestdemo;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.netease.acsdk.SysStatus;
import com.netease.acsdk.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Activity2 extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "UnidbgTestDemo";
    private static Activity2 activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2);
        activity = this;

        findViewById(R.id.btnTest2).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.btnTest2:
                MainActivity.getActivity().pthreadTest();
                break;
            case R.id.btnBack:
                finish();
                break;
        }
    }
}