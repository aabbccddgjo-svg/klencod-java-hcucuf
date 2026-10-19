package com.jfjfigiggi.iufcu;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // إنشاء الواجهة الأساسية
        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        // العنوان
        TextView titleTextView = new TextView(this);
        titleTextView.setText("لوحة التحكم بالجهاز (Shizuku)");
        titleTextView.setTextSize(22);
        titleTextView.setTextColor(Color.WHITE);
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setPadding(0, 0, 0, 30);
        mainLayout.addView(titleTextView);

        // نص حالة إذن Shizuku
        statusTextView = new TextView(this);
        statusTextView.setTextSize(16);
        statusTextView.setPadding(30, 30, 30, 30);
        statusTextView.setGravity(Gravity.CENTER);
        mainLayout.addView(statusTextView);

        // زر التحقق والطلب
        Button requestPermissionBtn = createStyledButton("فحص وطلب إذن SHIZUKU", Color.parseColor("#1E88E5"));
        requestPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestShizuku();
            }
        });
        mainLayout.addView(requestPermissionBtn);

        addSeparator(mainLayout);

        // أزرار التحكم بالجهاز
        mainLayout.addView(createActionButton("تشغيل Wi-Fi", "svc wifi enable"));
        mainLayout.addView(createActionButton("إيقاف Wi-Fi", "svc wifi disable"));
        mainLayout.addView(createActionButton("تشغيل البلوتوث", "svc bluetooth enable"));
        mainLayout.addView(createActionButton("إيقاف البلوتوث", "svc bluetooth disable"));
        mainLayout.addView(createActionButton("تفعيل بيانات الهاتف", "svc data enable"));
        mainLayout.addView(createActionButton("إيقاف بيانات الهاتف", "svc data disable"));
        mainLayout.addView(createActionButton("تفعيل وضع الطيران", "settings put global airplane_mode_on 1 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"));
        mainLayout.addView(createActionButton("إيقاف وضع الطيران", "settings put global airplane_mode_on 0 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false"));
        mainLayout.addView(createActionButton("قفل الشاشة", "input keyevent 26"));
        mainLayout.addView(createActionButton("إعادة تشغيل الجهاز", "reboot"));

        scrollView.addView(mainLayout);
        scrollView.setBackgroundColor(Color.parseColor("#121212"));
        setContentView(scrollView);

        // التحقق عند الفتح
        checkAndRequestShizuku();
    }

    // الفحص عبر أوامر النظام المباشرة لخدمة Shizuku
    private void checkAndRequestShizuku() {
        String result = runCommand("rish -c 'id' || su -c 'id'");
        if (result.contains("uid=0") || result.contains("context=")) {
            updateStatus("حالة الوصول: تم منح الإذن بنجاح ✔️", Color.parseColor("#4CAF50"));
        } else {
            // محاولة إرسال طلب الوصول عبر rikkagroup shizuku CLI
            String reqResult = runCommand("am start -n rikka.shizuku/rikka.shizuku.MainActivity");
            if (reqResult.contains("Error")) {
                updateStatus("خدمة Shizuku غير مفعّلة في الهاتف!", Color.parseColor("#E53935"));
            } else {
                updateStatus("يرجى منح الإذن من تطبيق Shizuku ❌", Color.parseColor("#FB8C00"));
            }
        }
    }

    // دالة تنفيذ أوامر Shell عبر Shizuku/rish
    private void executeShellCommand(String command) {
        // تنفيذ الأمر باستخدام بيئة rish الخاصة بـ Shizuku
        String fullCommand = "rish -c '" + command + "'";
        String output = runCommand(fullCommand);

        if (output.startsWith("ERROR")) {
            Toast.makeText(this, "فشل التنفيذ! تأكد من تشغيل Shizuku وإعطاء الإذن.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "تم تنفيذ الأمر بنجاح", Toast.LENGTH_SHORT).show();
        }
    }

    // دالة مساعدة لتشغيل أوامر Shell
    private String runCommand(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            process.waitFor();
            return sb.toString();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private void updateStatus(final String text, final int color) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusTextView.setText(text);
                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(color);
                drawable.setCornerRadius(15);
                statusTextView.setBackground(drawable);
                statusTextView.setTextColor(Color.WHITE);
            }
        });
    }

    private Button createActionButton(final String title, final String command) {
        Button button = createStyledButton(title, Color.parseColor("#2C2C2C"));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeShellCommand(command);
            }
        });
        return button;
    }

    private Button createStyledButton(String text, int bgColor) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(20);
        shape.setColor(bgColor);
        button.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 15, 0, 15);
        button.setLayoutParams(params);

        return button;
    }

    private void addSeparator(LinearLayout layout) {
        View separator = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        );
        params.setMargins(0, 30, 0, 30);
        separator.setLayoutParams(params);
        separator.setBackgroundColor(Color.GRAY);
        layout.addView(separator);
    }
}
