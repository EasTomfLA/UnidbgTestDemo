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

    public static native int init(int cmdId, Object[] objects);
    public static String getString(int n) {
        switch(n) {
            case 7719:
                return "sign";
            case 16944:
                return "uid";
            case 4005:
                return "cid";
            case 457:
                return "q";
            case 30491:
                return "cryptoDD";
            default:
                return "default";
        }
    }
}
