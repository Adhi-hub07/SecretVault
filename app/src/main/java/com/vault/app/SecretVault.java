package com.vault.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SecretVault extends Activity implements View.OnClickListener, View.OnLongClickListener {

    private PinManager pm;
    private Handler handler;

    private LinearLayout container;
    private TextView calcDisplay;
    private String calcInput = "0";
    private double calcValue = 0;
    private String calcOp = "";
    private boolean freshInput = true;

    private LinearLayout pinOverlay;
    private TextView pinDisplay;
    private TextView pinStatus;
    private String enteredPin = "";
    private boolean setupMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
        pm = new PinManager(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1A1A2E);
        root.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));

        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));

        root.addView(container);
        setContentView(root);

        buildCalculator();
        buildPinContent();
        pinOverlay.setVisibility(View.GONE);

        if (!pm.isSetup()) {
            showPinSetup();
        }
    }

    void buildCalculator() {
        container.removeAllViews();

        calcDisplay = new TextView(this);
        calcDisplay.setText("0");
        calcDisplay.setTextColor(0xFFFFFFFF);
        calcDisplay.setTextSize(48);
        calcDisplay.setGravity(Gravity.END);
        calcDisplay.setPadding(24, 80, 24, 24);
        calcDisplay.setBackgroundColor(0xFF16213E);
        calcDisplay.setOnLongClickListener(this);

        LinearLayout displayRow = new LinearLayout(this);
        displayRow.setOrientation(LinearLayout.HORIZONTAL);
        calcDisplay.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1f));
        displayRow.addView(calcDisplay);
        container.addView(displayRow);

        String[][] rows = {
            {"C", "\u00B1", "%", "\u00F7"},
            {"7", "8", "9", "\u00D7"},
            {"4", "5", "6", "\u2212"},
            {"1", "2", "3", "+"},
            {"0", ".", "=", null}
        };

        for (int r = 0; r < rows.length; r++) {
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setGravity(Gravity.CENTER);

            for (int c = 0; c < rows[r].length; c++) {
                String label = rows[r][c];
                if (label == null) continue;

                Button b = new Button(this);
                b.setText(label);
                b.setTextColor(0xFFFFFFFF);
                b.setTextSize(20);

                boolean isOp = "+-\u00D7\u00F7".contains(label);
                boolean isEq = label.equals("=");
                boolean isClear = label.equals("C");
                boolean isNum = "0123456789.".contains(label);

                if (isOp || isEq) {
                    b.setBackgroundColor(0xFFE94560);
                } else if (isClear) {
                    b.setBackgroundColor(0xFF0F3460);
                } else {
                    b.setBackgroundColor(0xFF1A1A3E);
                }

                if (r == 4 && c == 0) {
                    b.setLayoutParams(new LinearLayout.LayoutParams(0, 72, 2f));
                    ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) b.getLayoutParams();
                    if (mp != null) { mp.setMargins(4, 4, 0, 4); }
                } else if (r == 4 && c == 1) {
                    b.setLayoutParams(new LinearLayout.LayoutParams(0, 72, 1f));
                    ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) b.getLayoutParams();
                    if (mp != null) { mp.setMargins(2, 4, 2, 4); }
                } else if (r == 4 && c == 2) {
                    b.setLayoutParams(new LinearLayout.LayoutParams(0, 72, 1f));
                    ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) b.getLayoutParams();
                    if (mp != null) { mp.setMargins(2, 4, 4, 4); }
                } else if (r == 4 && c == 3) {
                    continue;
                } else {
                    b.setLayoutParams(new LinearLayout.LayoutParams(0, 72, 1f));
                    ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) b.getLayoutParams();
                    if (mp != null) { mp.setMargins(4, 4, 4, 4); }
                }

                b.setOnClickListener(this);
                ll.addView(b);
            }
            container.addView(ll);
        }

        pinOverlay = new LinearLayout(this);
        pinOverlay.setOrientation(LinearLayout.VERTICAL);
        pinOverlay.setGravity(Gravity.CENTER);
        pinOverlay.setBackgroundColor(0xCC0F3460);
        pinOverlay.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        pinOverlay.setVisibility(View.GONE);
        container.addView(pinOverlay);
    }

    void showPinSetup() {
        setupMode = true;
        enteredPin = "";
        buildPinContent();
        pinStatus.setText("Set PIN (4+ digits)");
        updatePinDots();
        pinOverlay.setVisibility(View.VISIBLE);
    }

    void showPinAuth() {
        setupMode = false;
        enteredPin = "";
        buildPinContent();
        pinStatus.setText("Enter PIN");
        updatePinDots();
        pinOverlay.setVisibility(View.VISIBLE);
    }

    void buildPinContent() {
        pinOverlay.removeAllViews();

        pinStatus = new TextView(this);
        pinStatus.setTextColor(0xFFFFFFFF);
        pinStatus.setTextSize(22);
        pinStatus.setGravity(Gravity.CENTER);
        pinStatus.setPadding(0, 40, 0, 20);

        pinDisplay = new TextView(this);
        pinDisplay.setTextColor(0xFF53D8FB);
        pinDisplay.setTextSize(32);
        pinDisplay.setGravity(Gravity.CENTER);
        pinDisplay.setPadding(0, 0, 0, 30);

        Button backBtn = new Button(this);
        backBtn.setText("\u2190 Calculator");
        backBtn.setTextColor(0xFF8899AA);
        backBtn.setBackgroundColor(0x00000000);
        backBtn.setTextSize(14);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pinOverlay.setVisibility(View.GONE);
            }
        });

        pinOverlay.addView(backBtn);
        pinOverlay.addView(pinStatus);
        pinOverlay.addView(pinDisplay);

        int[][] keys = {{1,2,3},{4,5,6},{7,8,9},{-1,0,-2}};
        for (int r = 0; r < keys.length; r++) {
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setGravity(Gravity.CENTER);
            for (int c = 0; c < keys[r].length; c++) {
                int k = keys[r][c];
                Button b = new Button(this);
                b.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
                ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) b.getLayoutParams();
                if (mp != null) mp.setMargins(8, 8, 8, 8);

                if (k >= 0) {
                    b.setText(String.valueOf(k));
                    b.setTextColor(0xFFFFFFFF);
                    b.setBackgroundColor(0x22000000);
                    b.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            onPinKey(((Button)v).getText().toString());
                        }
                    });
                } else if (k == -2) {
                    b.setText("DEL");
                    b.setTextColor(0xFFE94560);
                    b.setBackgroundColor(0x22000000);
                    b.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (enteredPin.length() > 0) {
                                enteredPin = enteredPin.substring(0, enteredPin.length() - 1);
                            }
                            updatePinDots();
                        }
                    });
                } else {
                    b.setVisibility(View.INVISIBLE);
                }
                ll.addView(b);
            }
            pinOverlay.addView(ll);
        }
    }

    void onPinKey(String digit) {
        enteredPin += digit;
        updatePinDots();
        if (enteredPin.length() >= 4) {
            if (setupMode) {
                pm.setPin(enteredPin);
                pinStatus.setText("PIN set!");
                setupMode = false;
                handler.postDelayed(new Runnable() {
                    public void run() {
                        pinOverlay.setVisibility(View.GONE);
                        pinStatus.setText("Enter PIN");
                    }
                }, 500);
            } else if (pm.checkPin(enteredPin)) {
                pinStatus.setText("Opening...");
                handler.postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(SecretVault.this, VaultActivity.class));
                    }
                }, 200);
            } else {
                pinStatus.setText("Wrong PIN!");
                enteredPin = "";
                handler.postDelayed(new Runnable() {
                    public void run() {
                        updatePinDots();
                    }
                }, 600);
            }
        }
    }

    void updatePinDots() {
        String d = "";
        for (int i = 0; i < enteredPin.length(); i++) d += "\u2022";
        pinDisplay.setText(d);
    }

    void doCalc(String label) {
        if ("C".equals(label)) {
            calcInput = "0";
            calcValue = 0;
            calcOp = "";
            freshInput = true;
            calcDisplay.setText("0");
            return;
        }
        if ("\u00B1".equals(label)) {
            double v = Double.parseDouble(calcInput);
            calcInput = String.valueOf(-v);
            if (calcInput.endsWith(".0")) calcInput = calcInput.substring(0, calcInput.length() - 2);
            calcDisplay.setText(calcInput);
            return;
        }
        if ("%".equals(label)) {
            double v = Double.parseDouble(calcInput) / 100;
            calcInput = String.valueOf(v);
            if (calcInput.endsWith(".0")) calcInput = calcInput.substring(0, calcInput.length() - 2);
            calcDisplay.setText(calcInput);
            return;
        }

        if ("0123456789.".contains(label)) {
            if (freshInput) {
                calcInput = label;
                freshInput = false;
            } else {
                if (label.equals(".") && calcInput.contains(".")) return;
                calcInput += label;
            }
            calcDisplay.setText(calcInput);
            return;
        }

        if ("+\u2212\u00D7\u00F7".contains(label)) {
            if (!calcOp.isEmpty()) {
                double result = compute(calcValue, Double.parseDouble(calcInput), calcOp);
                calcInput = String.valueOf(result);
                if (calcInput.endsWith(".0")) calcInput = calcInput.substring(0, calcInput.length() - 2);
                calcDisplay.setText(calcInput);
            }
            calcValue = Double.parseDouble(calcInput);
            calcOp = label;
            freshInput = true;
            return;
        }

        if ("=".equals(label)) {
            if (!calcOp.isEmpty()) {
                double result = compute(calcValue, Double.parseDouble(calcInput), calcOp);
                calcInput = String.valueOf(result);
                if (calcInput.endsWith(".0")) calcInput = calcInput.substring(0, calcInput.length() - 2);
                calcDisplay.setText(calcInput);
                calcOp = "";
                freshInput = true;
            }
        }
    }

    double compute(double a, double b, String op) {
        if ("+".equals(op)) return a + b;
        if ("\u2212".equals(op)) return a - b;
        if ("\u00D7".equals(op)) return a * b;
        if ("\u00F7".equals(op)) return b != 0 ? a / b : 0;
        return 0;
    }

    @Override
    public void onClick(View v) {
        String label = ((Button)v).getText().toString();
        doCalc(label);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == calcDisplay) {
            if (pm.isSetup()) {
                showPinAuth();
            } else {
                showPinSetup();
            }
            return true;
        }
        return false;
    }
}
