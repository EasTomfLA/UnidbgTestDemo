package com.netease.unidbgtestdemo;

import android.os.Binder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class PackagesListService extends Binder {
    public String readPackagesList() {
        String packagesList = "";
        try {
            File file = new File("/data/system/packages.list");
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line;
            while ((line = br.readLine()) != null) {
                packagesList += line + "\n";
            }

            br.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packagesList;
    }
}
