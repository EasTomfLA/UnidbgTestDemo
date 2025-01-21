package com.netease.unidbgtestdemo;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.util.Log;

public class DemoTest {
    public String TAG = DemoTest.class.getSimpleName();
    public static String TAGSTATIC = "staticTag";
    public static final String TAGSTATICFINAL = "staticTagFinal";
    public final String TAGFINAL = "tagFinal";

    private boolean aBooleanInstan = true;
    private static boolean aBooleanStatic = true;
    public static boolean aBooleanStaticPub = true;

    DemoTest() {
        TAG = "DefaultTag";
        Log.d(TAG, "default constructor with tag:" + TAG);
    }
    DemoTest(String tag) {
        TAG = tag;
        Log.d(TAG, "constructor with tag:" + TAG);
    }

    public native boolean BoolTest();

    public native int myAdd(int n, int n2);

    public static native byte[] encrypt(byte[] dateInput, int n);
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
    public void testMyMethod() {
        Log.d(TAG, "getPackageManager=" + getPackageManager().toString());
        Log.d(TAG, "getPackageName=" +getPackageName());
        Log.d(TAG, "getPackageCodePath=" +getPackageCodePath());
        Log.d(TAG, "getPackageManager=" + getPackageManager().toString());
    }
    public String getPackageCodePath() {
       return MainActivity.getActivity().getPackageCodePath();
    }
    public String getPackageName() {
        return MainActivity.getActivity().getPackageName();
    }
    public AssetManager getAssetsManager() {
        return MainActivity.getActivity().getAssets();
    }
    public PackageManager getPackageManager() {
        return MainActivity.getActivity().getPackageManager();
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : byteArray) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
