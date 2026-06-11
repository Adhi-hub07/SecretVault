package com.vault.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class VaultActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

    private PinManager pm;
    private PackageManager pkgMan;
    private LinearLayout appGrid;
    private TextView statusText;
    private Handler handler;

    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        if (tag == null) {
            startActivity(new Intent(this, AppPickerActivity.class));
            return;
        }
        if (tag.equals("__hide__")) {
            hideVaultIcon();
            return;
        }
        final String pkg = tag;
        String compName = pm.getComponentName(pkg);

        if (compName != null) {
            pm.setComponentEnabled(pkgMan, pkg, true);
            try {
                Intent launch = pkgMan.getLaunchIntentForPackage(pkg);
                if (launch != null) startActivity(launch);
            } catch (Exception e) {
                Toast.makeText(this, "Cannot launch", Toast.LENGTH_SHORT).show();
            }
            handler.postDelayed(new Runnable() {
                public void run() {
                    pm.setComponentEnabled(pkgMan, pkg, false);
                }
            }, 3000);
        } else {
            Intent launch = pkgMan.getLaunchIntentForPackage(pkg);
            if (launch != null) startActivity(launch);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        final String pkg = (String) v.getTag();
        if (pkg == null) return false;
        pm.enableLauncher(pkgMan, pkg);
        pm.toggleHiddenApp(pkg);
        Toast.makeText(this, "Removed " + pkg, Toast.LENGTH_SHORT).show();
        loadApps();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1A1A2E);
        root.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        statusText = new TextView(this);
        statusText.setText("Hidden Apps");
        statusText.setTextColor(0xFFFFFFFF);
        statusText.setTextSize(22);
        statusText.setGravity(android.view.Gravity.CENTER);
        statusText.setPadding(0, 32, 0, 16);

        Button btnHide = new Button(this);
        btnHide.setText("Hide Icon");
        btnHide.setTextColor(0xFFE94560);
        btnHide.setBackgroundColor(0x22000000);
        btnHide.setPadding(24, 12, 24, 12);
        btnHide.setTag("__hide__");
        btnHide.setOnClickListener(this);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setPadding(16, 16, 16, 0);
        topBar.addView(statusText, new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        topBar.addView(btnHide);

        appGrid = new LinearLayout(this);
        appGrid.setOrientation(LinearLayout.VERTICAL);
        appGrid.setPadding(16, 8, 16, 16);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(appGrid);

        root.addView(topBar);
        root.addView(scroll);

        setContentView(root);

        pm = new PinManager(this);
        pkgMan = getPackageManager();
        loadApps();
    }

    void loadApps() {
        appGrid.removeAllViews();

        String[] hidden = pm.getHiddenApps();
        if (hidden.length == 0 || hidden[0].isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No hidden apps yet.\nTap + to add apps.");
            empty.setTextColor(0xFF8899AA);
            empty.setTextSize(16);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setPadding(0, 80, 0, 0);
            appGrid.addView(empty);
        } else {
            int sw = getResources().getConfiguration().screenWidthDp;
            int cols = sw / 80;
            if (cols < 1) cols = 3;
            if (cols > 4) cols = 4;

            LinearLayout row = null;
            for (int i = 0; i < hidden.length; i++) {
                String pkg = hidden[i];
                if (pkg.isEmpty()) continue;

                if (i % cols == 0) {
                    row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    appGrid.addView(row);
                }

                try {
                    ApplicationInfo ai = pkgMan.getApplicationInfo(pkg, 0);
                    Drawable icon = ai.loadIcon(pkgMan);
                    String name = ai.loadLabel(pkgMan).toString();

                    LinearLayout item = new LinearLayout(this);
                    item.setOrientation(LinearLayout.VERTICAL);
                    item.setGravity(android.view.Gravity.CENTER);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    lp.setMargins(4, 12, 4, 12);
                    item.setLayoutParams(lp);
                    item.setTag(pkg);
                    item.setOnClickListener(this);
                    item.setOnLongClickListener(this);

                    ImageView iv = new ImageView(this);
                    iv.setImageDrawable(icon);
                    iv.setLayoutParams(new LinearLayout.LayoutParams(56, 56));

                    TextView tv = new TextView(this);
                    tv.setText(name);
                    tv.setTextColor(0xFFCCCCDD);
                    tv.setTextSize(11);
                    tv.setMaxLines(1);
                    tv.setEllipsize(android.text.TextUtils.TruncateAt.END);

                    item.addView(iv);
                    item.addView(tv);

                    if (row != null) row.addView(item);
                } catch (PackageManager.NameNotFoundException e) {
                    pm.enableLauncher(pkgMan, pkg);
                    pm.toggleHiddenApp(pkg);
                    loadApps();
                }
            }
        }

        Button addBtn = new Button(this);
        addBtn.setText("+ Add Apps");
        addBtn.setTextColor(0xFF53D8FB);
        addBtn.setBackgroundColor(0xFF0F3460);
        addBtn.setPadding(32, 14, 32, 14);
        addBtn.setOnClickListener(this);
        appGrid.addView(addBtn);
    }

    void hideVaultIcon() {
        try {
            ComponentName comp = new ComponentName(this,
                "com.vault.app.SecretVault");
            pkgMan.setComponentEnabledSetting(comp,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
            Toast.makeText(this, "Icon hidden! Dial *#*#6789#*#*", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Can't hide on this device", Toast.LENGTH_SHORT).show();
        }
    }
}
