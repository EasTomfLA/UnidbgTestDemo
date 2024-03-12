package com.netease.acsdk;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    public static void getSUPermission() {
//        runShellCMD("su");
        runSUCommand("");
    }

    public static String runSUCommand(String command) {
        Process process = null;
        String result = "";
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            is = new DataInputStream(process.getInputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            String line = null;
            while ((line = is.readLine()) != null) {
                result += line;
            }
            process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    public static String runShellCMD(String cmd) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(cmd);
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

    public static int enableInotifyWatch() {
        inotifyWatchStop();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return inotifyWatchStart();
    }

    public static int addInotifyWatchPatch(String path) {
        return inotifyAddWatchPath(path);
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

    private native static int inotifyWatchStart();
    private native static int inotifyWatchStop();
    private native static int inotifyAddWatchPath(String path);
    private native static String accessTest(String path);
}
