package com.netease.unidbgtestdemo;

import android.util.Log;

public class DemoTest {
    public String TAG = "";
    public static String TAGSTATIC = "staticTag";
    public static final String TAGSTATICFINAL = "staticTagFinal";
    public final String TAGFINAL = "tagFinal";

    DemoTest() {
        TAG = "DefaultTag";
        Log.d(TAG, "default constructor with tag:" + TAG);
    }
    DemoTest(String tag) {
        TAG = tag;
        Log.d(TAG, "constructor with tag:" + TAG);
    }

    public native int myAdd(int n, int n2);
}
