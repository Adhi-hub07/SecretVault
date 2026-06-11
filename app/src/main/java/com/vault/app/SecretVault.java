package com.vault.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SecretVault extends Activity implements View.OnClickListener {

    private PinManager pm;
    private TextView statusText;
    private LinearLayout container;
    private String enteredPin = "";
    private Handler handler;
    private boolean setupMode = false;

    static class VaultHandler extends Handler {
        SecretVault sv;
        VaultHandler(SecretVault s) { sv = s; }
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                sv.startActivity(new Intent(sv, VaultActivity.class));
                sv.finishAffinity();
            } else if (msg.what == 1) {
                sv.enteredPin = "";
                sv.statusText.setText("Enter PIN");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1A1A2E);
        root.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));

        statusText = new TextView(this);
        statusText.setTextColor(0xFFFFFFFF);
        statusText.setTextSize(22);
        statusText.setGravity(android.view.Gravity.CENTER);
        statusText.setPadding(0, 80, 0, 40);

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.CENTER);
        container.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        root.addView(statusText);
        root.addView(container);
        setContentView(root);

        handler = new VaultHandler(this);
        pm = new PinManager(this);

        if (!pm.isSetup()) { setupMode = true; showSetup(); }
        else { showAuth(); }
    }

    void showSetup() {
        statusText.setText("Set PIN (4+ digits)");
        enteredPin = "";
        buildPad();
    }

    void showAuth() {
        statusText.setText("Enter PIN");
        enteredPin = "";
        buildPad();
    }

    void buildPad() {
        container.removeAllViews();
        int[][] keys = {{1,2,3},{4,5,6},{7,8,9},{-1,0,-2}};
        for (int r = 0; r < keys.length; r++) {
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setGravity(android.view.Gravity.CENTER);
            for (int c = 0; c < keys[r].length; c++) {
                int k = keys[r][c];
                Button b = new Button(this);
                b.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
                ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) b.getLayoutParams();
                if (mp != null) mp.setMargins(8, 8, 8, 8);

                if (k == -1) {
                    b.setVisibility(View.INVISIBLE);
                } else if (k == -2) {
                    b.setText("DEL");
                    b.setBackgroundColor(0x22000000);
                    b.setOnClickListener(this);
                } else {
                    b.setText(String.valueOf(k));
                    b.setBackgroundColor(0x22000000);
                    b.setOnClickListener(this);
                }
                ll.addView(b);
            }
            container.addView(ll);
        }
    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;
        String text = b.getText().toString();

        if (text.equals("DEL")) {
            if (enteredPin.length() > 0)
                enteredPin = enteredPin.substring(0, enteredPin.length() - 1);
            updateDots();
            return;
        }

        enteredPin += text;
        updateDots();

        if (enteredPin.length() >= 4) {
            if (setupMode) {
                pm.setPin(enteredPin);
                statusText.setText("PIN set!");
                Message msg = handler.obtainMessage(0);
                handler.sendMessageDelayed(msg, 200);
            } else if (pm.checkPin(enteredPin)) {
                Message msg = handler.obtainMessage(0);
                handler.sendMessageDelayed(msg, 50);
            } else {
                statusText.setText("Wrong PIN!");
                Message msg = handler.obtainMessage(1);
                handler.sendMessageDelayed(msg, 500);
            }
        }
    }

    void updateDots() {
        String d = "";
        for (int i = 0; i < enteredPin.length(); i++) d += "\u2022";
        statusText.setText((setupMode ? "Set PIN: " : "PIN: ") + d);
    }
}
