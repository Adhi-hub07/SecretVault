package com.vault.app;

import android.content.Context;
import android.content.SharedPreferences;

public class PinManager {
    private static final String PREFS = "vault_prefs";
    private static final String KEY_PIN_HASH = "pin_hash";
    private static final String KEY_HIDDEN_APPS = "hidden_apps";
    private static final String KEY_IS_SETUP = "is_setup";

    private final SharedPreferences prefs;

    public PinManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isSetup() { return prefs.getBoolean(KEY_IS_SETUP, false); }

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

    private String hash(String s) {
        int h = 0;
        for (char c : s.toCharArray()) h = h * 31 + c;
        return Integer.toHexString(h);
    }
}
