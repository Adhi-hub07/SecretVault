package com.vault.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppPickerActivity extends Activity implements View.OnClickListener {

    private PinManager pm;
    private PackageManager pkgMan;
    private LinearLayout list;

    @Override
    public void onClick(View v) {
        String pkg = (String) v.getTag();
        if (pkg == null) return;
        boolean newState = !pm.isHidden(pkg);
        pm.toggleHiddenApp(pkg);

        if (newState) {
            pm.hideCompletely(pkgMan, pkg);
        } else {
            pm.unhideCompletely(pkgMan, pkg);
        }

        CheckBox cb = (CheckBox) v.findViewWithTag("cb_" + pkg);
        if (cb != null) cb.setChecked(newState);
        String msg = newState ? "Hidden" : "Visible";
        if (newState && pm.isRooted()) msg += " (root: hidden from all)";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF000000);

        int st = getResources().getDisplayMetrics().density > 0 ? 24 : 24;

        TextView statusLine = new TextView(this);
        statusLine.setText("Root: " + (new PinManager(this).isRooted() ? "YES" : "NO"));
        statusLine.setTextColor(0xFF888888);
        statusLine.setTextSize(12);
        statusLine.setPadding(st, st, st, 4);

        TextView title = new TextView(this);
        title.setText("Select apps to hide");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(20);
        title.setPadding(st, 0, st, st);

        root.addView(statusLine);
        root.addView(title);

        ScrollView sv = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        root.addView(sv);

        setContentView(root);

        pm = new PinManager(this);
        pkgMan = getPackageManager();
        loadApps();
    }

    void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resList = pkgMan.queryIntentActivities(mainIntent, 0);
        Collections.sort(resList, new Comparator<ResolveInfo>() {
            public int compare(ResolveInfo a, ResolveInfo b) {
                return a.loadLabel(pkgMan).toString().compareToIgnoreCase(
                    b.loadLabel(pkgMan).toString());
            }
        });

        java.util.Set<String> seen = new java.util.HashSet<>();
        for (ResolveInfo ri : resList) {
            ActivityInfo ai = ri.activityInfo;
            String pkg = ai.packageName;
            if (pkg.equals(getPackageName())) continue;
            if (!seen.add(pkg)) continue;

            Drawable icon = ai.loadIcon(pkgMan);
            String name = ri.loadLabel(pkgMan).toString();
            boolean hidden = pm.isHidden(pkg);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(16, 10, 16, 10);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            row.setTag(pkg);
            row.setOnClickListener(this);

            ImageView iv = new ImageView(this);
            iv.setImageDrawable(icon);
            LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(40, 40);
            ilp.setMargins(0, 0, 12, 0);
            iv.setLayoutParams(ilp);

            TextView tv = new TextView(this);
            tv.setText(name);
            tv.setTextColor(0xFFCCCCDD);
            tv.setTextSize(15);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            CheckBox cb = new CheckBox(this);
            cb.setChecked(hidden);
            cb.setTag("cb_" + pkg);
            cb.setEnabled(false);

            row.addView(iv);
            row.addView(tv);
            row.addView(cb);

            list.addView(row);
        }
    }
}
