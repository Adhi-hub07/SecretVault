package com.vault.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


public class SecretVault extends Activity implements View.OnClickListener {

    private static final String SECRET_CODE = "2007";

    private PinManager pm;
    private Handler handler;

    private FrameLayout frame;
    private LinearLayout calcView;
    private View pinView;
    private TextView calcDisplay;
    private TextView calcPreview;
    private String calcInput = "";
    private double calcValue = 0;
    private String calcOp = "";
    private boolean freshInput = true;
    private boolean hasResult = false;

    private TextView pinDisplay;
    private TextView pinStatus;
    private String enteredPin = "";
    private boolean setupMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(Looper.getMainLooper());
        pm = new PinManager(this);

        frame = new FrameLayout(this);
        frame.setBackgroundColor(0xFF000000);
        frame.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));

        buildCalculator();
        buildPinView();

        setContentView(frame);

        if (!pm.isSetup()) {
            showPinSetup();
        }
    }

    void buildCalculator() {
        calcView = new LinearLayout(this);
        calcView.setOrientation(LinearLayout.VERTICAL);

        calcPreview = new TextView(this);
        calcPreview.setText("");
        calcPreview.setTextColor(0xFF666666);
        calcPreview.setTextSize(16);
        calcPreview.setGravity(Gravity.END);
        calcPreview.setPadding(24, 100, 24, 4);

        calcDisplay = new TextView(this);
        calcDisplay.setText("0");
        calcDisplay.setTextColor(0xFFFFFFFF);
        calcDisplay.setTextSize(52);
        calcDisplay.setGravity(Gravity.END);
        calcDisplay.setPadding(24, 4, 24, 24);

        calcView.addView(calcPreview);
        calcView.addView(calcDisplay);

        String[][] rows = {
            {"C", "\u00B1", "%", "\u00F7"},
            {"7", "8", "9", "\u00D7"},
            {"4", "5", "6", "\u2212"},
            {"1", "2", "3", "+"},
            {null, "0", ".", "="}
        };

        for (int r = 0; r < rows.length; r++) {
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);

            for (int c = 0; c < rows[r].length; c++) {
                String label = rows[r][c];
                if (label == null) {
                    View spacer = new View(this);
                    spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 0.5f));
                    ll.addView(spacer);
                    continue;
                }

                Button b = new Button(this);
                b.setText(label);
                b.setTextSize(22);
                b.setPadding(0, 18, 0, 18);

                if (label.equals("=")) {
                    b.setBackgroundColor(0xFF4CAF50);
                    b.setTextColor(0xFFFFFFFF);
                } else if ("\u00F7\u00D7\u2212+".contains(label)) {
                    b.setBackgroundColor(0xFFE67E22);
                    b.setTextColor(0xFFFFFFFF);
                } else if (label.equals("C")) {
                    b.setBackgroundColor(0xFFE74C3C);
                    b.setTextColor(0xFFFFFFFF);
                } else if (label.equals("\u00B1") || label.equals("%")) {
                    b.setBackgroundColor(0xFF555555);
                    b.setTextColor(0xFFFFFFFF);
                } else {
                    b.setBackgroundColor(0xFF333333);
                    b.setTextColor(0xFFFFFFFF);
                }

                if (r == 4 && c == 1) {
                    b.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.5f));
                } else {
                    b.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
                }
                ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) b.getLayoutParams();
                if (mp != null) mp.setMargins(3, 3, 3, 3);

                b.setOnClickListener(this);
                ll.addView(b);
            }
            calcView.addView(ll);
        }

        frame.addView(calcView, new FrameLayout.LayoutParams(-1, -1));
    }

    void buildPinView() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setGravity(Gravity.CENTER);
        v.setBackgroundColor(0xCC0F3460);
        v.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));

        Button backBtn = new Button(this);
        backBtn.setText("\u2190 Back");
        backBtn.setTextColor(0xFF8899AA);
        backBtn.setBackgroundColor(0x00000000);
        backBtn.setTextSize(14);
        backBtn.setPadding(16, 24, 16, 16);
        backBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v2) {
                pinView.setVisibility(View.GONE);
                calcView.setVisibility(View.VISIBLE);
            }
        });

        pinStatus = new TextView(this);
        pinStatus.setTextColor(0xFFFFFFFF);
        pinStatus.setTextSize(22);
        pinStatus.setGravity(Gravity.CENTER);
        pinStatus.setPadding(0, 0, 0, 10);

        pinDisplay = new TextView(this);
        pinDisplay.setTextColor(0xFF53D8FB);
        pinDisplay.setTextSize(36);
        pinDisplay.setGravity(Gravity.CENTER);
        pinDisplay.setPadding(0, 0, 0, 20);

        v.addView(backBtn);
        v.addView(pinStatus);
        v.addView(pinDisplay);

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
                    b.setBackgroundColor(0xFF333333);
                    b.setTextSize(22);
                    b.setPadding(0, 16, 0, 16);
                    b.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v2) {
                            onPinKey(((Button)v2).getText().toString());
                        }
                    });
                } else if (k == -2) {
                    b.setText("DEL");
                    b.setTextColor(0xFFE74C3C);
                    b.setBackgroundColor(0xFF222222);
                    b.setTextSize(18);
                    b.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v2) {
                            if (enteredPin.length() > 0)
                                enteredPin = enteredPin.substring(0, enteredPin.length() - 1);
                            updatePinDots();
                        }
                    });
                } else {
                    b.setVisibility(View.INVISIBLE);
                }
                ll.addView(b);
            }
            v.addView(ll);
        }

        pinView = v;
        pinView.setVisibility(View.GONE);
        frame.addView(pinView, new FrameLayout.LayoutParams(-1, -1));
    }

    void openVault() {
        if (setupMode) return;
        if (!pm.isSetup()) return;
        pinView.setVisibility(View.GONE);
        calcView.setVisibility(View.VISIBLE);
        startActivity(new Intent(this, VaultActivity.class));
    }

    void showPinSetup() {
        setupMode = true;
        enteredPin = "";
        pinStatus.setText("Set PIN (4+ digits)");
        updatePinDots();
        calcView.setVisibility(View.GONE);
        pinView.setVisibility(View.VISIBLE);
    }

    void showPinAuth() {
        setupMode = false;
        enteredPin = "";
        pinStatus.setText("Enter PIN");
        updatePinDots();
        calcView.setVisibility(View.GONE);
        pinView.setVisibility(View.VISIBLE);
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
                        pinView.setVisibility(View.GONE);
                        calcView.setVisibility(View.VISIBLE);
                        pinStatus.setText("Enter PIN");
                    }
                }, 500);
            } else if (pm.checkPin(enteredPin)) {
                pinStatus.setText("Opening...");
                handler.postDelayed(new Runnable() {
                    public void run() { openVault(); }
                }, 200);
            } else {
                pinStatus.setText("Wrong PIN!");
                enteredPin = "";
                handler.postDelayed(new Runnable() {
                    public void run() { updatePinDots(); }
                }, 600);
            }
        }
    }

    void updatePinDots() {
        String d = "";
        for (int i = 0; i < enteredPin.length(); i++) d += "\u2022";
        pinDisplay.setText(d);
    }

    @Override
    public void onClick(View v) {
        String label = ((Button) v).getText().toString();
        handleCalc(label);
    }

    void handleCalc(String label) {
        if (label.equals("C")) {
            calcInput = "";
            calcValue = 0;
            calcOp = "";
            freshInput = true;
            hasResult = false;
            calcPreview.setText("");
            calcDisplay.setText("0");
            return;
        }

        if (label.equals("\u00B1")) {
            if (calcInput.isEmpty() || calcInput.equals("0")) return;
            if (calcInput.startsWith("-"))
                calcInput = calcInput.substring(1);
            else
                calcInput = "-" + calcInput;
            calcDisplay.setText(calcInput);
            return;
        }

        if (label.equals("%")) {
            if (calcInput.isEmpty()) return;
            double v = Double.parseDouble(calcInput) / 100;
            calcInput = formatNum(v);
            calcDisplay.setText(calcInput);
            return;
        }

        if ("0123456789.".contains(label)) {
            if (hasResult) {
                calcInput = label;
                hasResult = false;
                calcOp = "";
                calcValue = 0;
                calcPreview.setText("");
            } else if (freshInput || calcInput.equals("0")) {
                calcInput = label;
                freshInput = false;
            } else {
                if (label.equals(".") && calcInput.contains(".")) return;
                if (calcInput.isEmpty()) calcInput = "0";
                if (calcInput.equals("0") && !label.equals("."))
                    calcInput = label;
                else
                    calcInput += label;
            }
            calcDisplay.setText(calcInput);
            return;
        }

        if ("+\u2212\u00D7\u00F7".contains(label)) {
            if (!calcInput.isEmpty()) {
                if (!calcOp.isEmpty() && !freshInput) {
                    double result = compute(calcValue, Double.parseDouble(calcInput), calcOp);
                    calcInput = formatNum(result);
                    calcDisplay.setText(calcInput);
                }
                calcValue = Double.parseDouble(calcInput);
                calcOp = label;
                freshInput = true;
                hasResult = false;
                calcPreview.setText(formatNum(calcValue) + " " + label);
            }
            return;
        }

        if (label.equals("=")) {
            if (calcOp.isEmpty()) {
                if (calcInput.equals(SECRET_CODE)) {
                    if (pm.isSetup())
                        showPinAuth();
                    else
                        showPinSetup();
                    return;
                }
                if (calcInput.isEmpty() || calcInput.equals("0")) return;
                hasResult = true;
                calcPreview.setText(calcInput + " =");
                return;
            }
            if (!calcInput.isEmpty()) {
                double result = compute(calcValue, Double.parseDouble(calcInput), calcOp);
                String expr = formatNum(calcValue) + " " + calcOp + " " + calcInput;
                calcInput = formatNum(result);
                calcDisplay.setText(calcInput);
                calcPreview.setText(expr + " =");
                calcOp = "";
                freshInput = true;
                hasResult = true;
            }
        }
    }

    String formatNum(double n) {
        if (n == (long) n) return String.valueOf((long) n);
        String s = String.valueOf(n);
        if (s.endsWith(".0")) return s.substring(0, s.length() - 2);
        return s;
    }

    double compute(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "\u2212": return a - b;
            case "\u00D7": return a * b;
            case "\u00F7": return b != 0 ? a / b : 0;
        }
        return 0;
    }
}
