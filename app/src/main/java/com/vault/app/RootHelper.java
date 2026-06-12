package com.vault.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RootHelper {

    public static boolean isRootAvailable() {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(new String[]{"which", "su"});
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = r.readLine();
            return line != null && !line.isEmpty();
        } catch (Exception e) {
            return false;
        } finally {
            if (p != null) p.destroy();
        }
    }

    public static boolean pmHide(String pkg) {
        return execSu("pm hide " + pkg);
    }

    public static boolean pmUnhide(String pkg) {
        return execSu("pm unhide " + pkg);
    }

    public static boolean pmEnable(String pkg) {
        return execSu("pm enable " + pkg);
    }

    public static boolean pmDisable(String pkg) {
        return execSu("pm disable " + pkg);
    }

    private static boolean execSu(String cmd) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            int code = p.waitFor();
            return code == 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (p != null) p.destroy();
        }
    }
}
