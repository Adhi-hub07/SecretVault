package com.vault.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;

public class PinManager {
    private static final String PREFS = "vault_prefs";
    private static final String KEY_PIN_HASH = "pin_hash";
    private static final String KEY_HIDDEN_APPS = "hidden_apps";
    private static final String KEY_HIDDEN_COMPS = "hidden_components";
    private static final String KEY_IS_SETUP = "is_setup";
    private static final String KEY_ROOT_HIDDEN = "root_hidden";

    private final SharedPreferences prefs;
    private boolean rootChecked = false;
    private boolean rootAvailable = false;

    public PinManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isSetup() { return prefs.getBoolean(KEY_IS_SETUP, false); }

    public boolean isRooted() {
        if (!rootChecked) {
            rootAvailable = RootHelper.isRootAvailable();
            rootChecked = true;
        }
        return rootAvailable;
    }

    public boolean isRootHidden(String pkg) {
        return prefs.getString(KEY_ROOT_HIDDEN, "").contains(pkg + ",");
    }

    private void setRootHidden(String pkg, boolean hidden) {
        String raw = prefs.getString(KEY_ROOT_HIDDEN, "");
        if (hidden) {
            if (!raw.contains(pkg + ",")) raw += pkg + ",";
        } else {
            raw = raw.replace(pkg + ",", "");
        }
        prefs.edit().putString(KEY_ROOT_HIDDEN, raw).apply();
    }

    public void setPin(String pin) {
        prefs.edit()
            .putString(KEY_PIN_HASH, hash(pin))
            .putBoolean(KEY_IS_SETUP, true)
            .apply();
    }

    public boolean checkPin(String pin) {
        return hash(pin).equals(prefs.getString(KEY_PIN_HASH, ""));
    }

    public void toggleHiddenApp(String pkg) {
        String raw = prefs.getString(KEY_HIDDEN_APPS, "");
        if (raw.contains(pkg + ",")) {
            raw = raw.replace(pkg + ",", "");
        } else {
            raw += pkg + ",";
        }
        prefs.edit().putString(KEY_HIDDEN_APPS, raw).apply();
    }

    public boolean isHidden(String pkg) {
        return prefs.getString(KEY_HIDDEN_APPS, "").contains(pkg + ",");
    }

    public String[] getHiddenApps() {
        String raw = prefs.getString(KEY_HIDDEN_APPS, "");
        if (raw.isEmpty()) return new String[0];
        return raw.split(",");
    }

    public void saveComponentName(String pkg, String comp) {
        String raw = prefs.getString(KEY_HIDDEN_COMPS, "");
        raw = raw.replace(pkg + "=", "");
        raw += pkg + "=" + comp + ",";
        prefs.edit().putString(KEY_HIDDEN_COMPS, raw).apply();
    }

    public void removeComponentName(String pkg) {
        String raw = prefs.getString(KEY_HIDDEN_COMPS, "");
        int start = raw.indexOf(pkg + "=");
        if (start >= 0) {
            int end = raw.indexOf(",", start);
            if (end > start) {
                raw = raw.substring(0, start) + raw.substring(end + 1);
            }
        }
        prefs.edit().putString(KEY_HIDDEN_COMPS, raw).apply();
    }

    public String getComponentName(String pkg) {
        String raw = prefs.getString(KEY_HIDDEN_COMPS, "");
        int start = raw.indexOf(pkg + "=");
        if (start < 0) return null;
        start += pkg.length() + 1;
        int end = raw.indexOf(",", start);
        return end > start ? raw.substring(start, end) : null;
    }

    public static String findLauncherComponent(PackageManager pm, String pkg) {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setPackage(pkg);
        List<ResolveInfo> list = pm.queryIntentActivities(i, 0);
        if (list != null && list.size() > 0) {
            ActivityInfo ai = list.get(0).activityInfo;
            return ai.name;
        }
        return null;
    }

    public void setComponentEnabled(PackageManager pm, String pkg, boolean enabled) {
        String compName = getComponentName(pkg);
        if (compName == null) return;
        int state = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        pm.setComponentEnabledSetting(new ComponentName(pkg, compName), state,
            PackageManager.DONT_KILL_APP);
    }

    public void hideCompletely(PackageManager pm, String pkg) {
        if (isRooted()) {
            RootHelper.pmHide(pkg);
            setRootHidden(pkg, true);
        }
        String cn = findLauncherComponent(pm, pkg);
        if (cn != null) {
            saveComponentName(pkg, cn);
            pm.setComponentEnabledSetting(new ComponentName(pkg, cn),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        }
    }

    public void unhideCompletely(PackageManager pm, String pkg) {
        if (isRootHidden(pkg)) {
            RootHelper.pmUnhide(pkg);
            setRootHidden(pkg, false);
        }
        String compName = getComponentName(pkg);
        if (compName != null) {
            pm.setComponentEnabledSetting(new ComponentName(pkg, compName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
            removeComponentName(pkg);
        }
    }

    public void tempEnableForLaunch(PackageManager pm, final String pkg) {
        if (isRootHidden(pkg)) {
            RootHelper.pmUnhide(pkg);
        }
        String compName = getComponentName(pkg);
        if (compName != null) {
            pm.setComponentEnabledSetting(new ComponentName(pkg, compName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        }
    }

    public void tempDisableAfterLaunch(PackageManager pm, final String pkg) {
        if (isRootHidden(pkg)) {
            RootHelper.pmHide(pkg);
        }
        String compName = getComponentName(pkg);
        if (compName != null) {
            pm.setComponentEnabledSetting(new ComponentName(pkg, compName),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        }
    }

    public String getRootStatus() {
        if (isRooted()) return "Root available - apps hidden from Settings & Play Store";
        return "No root - apps hidden from launcher only";
    }

    private String hash(String s) {
        int h = 0;
        for (char c : s.toCharArray()) h = h * 31 + c;
        return Integer.toHexString(h);
    }
}
