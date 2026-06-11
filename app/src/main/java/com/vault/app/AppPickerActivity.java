package com.vault.app;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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

    static class AppComparator implements Comparator<ApplicationInfo> {
        PackageManager pm;
        AppComparator(PackageManager p) { pm = p; }
        public int compare(ApplicationInfo a, ApplicationInfo b) {
            return a.loadLabel(pm).toString().compareToIgnoreCase(
                b.loadLabel(pm).toString());
        }
    }

    @Override
    public void onClick(View v) {
        String pkg = (String) v.getTag();
        if (pkg == null) return;
        boolean newState = !pm.isHidden(pkg);
        pm.toggleHiddenApp(pkg);

        if (newState) {
            pm.disableLauncher(pkgMan, pkg);
        } else {
            pm.enableLauncher(pkgMan, pkg);
        }

        CheckBox cb = (CheckBox) v.findViewWithTag("cb_" + pkg);
        if (cb != null) cb.setChecked(newState);
        Toast.makeText(this, newState ? "Hidden (invisible)" : "Visible again", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1A1A2E);

        TextView title = new TextView(this);
        title.setText("Select apps to hide");
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(20);
        title.setPadding(24, 24, 24, 16);
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
        List<ApplicationInfo> apps = pkgMan.getInstalledApplications(0);
        Collections.sort(apps, new AppComparator(pkgMan));

        for (ApplicationInfo ai : apps) {
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) continue;
            String pkg = ai.packageName;
            if (pkg.equals(getPackageName())) continue;

            Drawable icon = ai.loadIcon(pkgMan);
            String name = ai.loadLabel(pkgMan).toString();
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
