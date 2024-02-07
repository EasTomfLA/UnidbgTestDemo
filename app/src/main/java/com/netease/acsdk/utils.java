package com.netease.acsdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class utils {

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
}
