package com.netease.acsdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    public static String runShellCMD(String cmd) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("id");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();
            process.destroy();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return output.toString();
    }

    public static int enableInotifyWatch(String paths) {
        return inotifyWatch(paths);
    }

    public static String accessTestPaths(String paths) {
        return accessTest(paths);
    }

    public static String readAllFileContext(String path) {
        File file = new File(path);
        if (!file.canRead() || !file.exists()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        byte[] tmp = new byte[1024];
        int len;
        try {
            FileInputStream fis = new FileInputStream(file);
            while ((len = fis.read(tmp)) > 0) {
                sb.append(new String(tmp, 0, len));
            }
            fis.close();
            return sb.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private native static int inotifyWatch(String paths);
    private native static String accessTest(String path);
}
