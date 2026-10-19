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
import java.lang.reflect.Method;

public class MainActivity extends Activity {

    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // إنشاء الواجهة
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

        // حالة إذن Shizuku
        statusTextView = new TextView(this);
        statusTextView.setTextSize(16);
        statusTextView.setPadding(30, 30, 30, 30);
        statusTextView.setGravity(Gravity.CENTER);
        mainLayout.addView(statusTextView);

        // زر طلب الإذن
        Button requestPermissionBtn = createStyledButton("طلب إذن Shizuku", Color.parseColor("#1E88E5"));
        requestPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestShizukuPermission();
            }
        });
        mainLayout.addView(requestPermissionBtn);

        addSeparator(mainLayout);

        // خيارات إدارة الجهاز
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

        checkShizukuPermission();
    }

    // فحص حالة الإذن والتأكد من تشغيل الخدمة
    private void checkShizukuPermission() {
        try {
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method pingMethod = shizukuClass.getMethod("pingBinder");
            boolean isAlive = (boolean) pingMethod.invoke(null);

            if (!isAlive) {
                updateStatus("خدمة Shizuku غير شغالة!", Color.parseColor("#E53935"));
                return;
            }

            Method checkPermMethod = shizukuClass.getMethod("checkSelfPermission");
            int result = (int) checkPermMethod.invoke(null);

            if (result == 0) { // PERMISSION_GRANTED
                updateStatus("حالة الوصول: تم منح الإذن بنجاح ✔️", Color.parseColor("#4CAF50"));
            } else {
                updateStatus("حالة الوصول: الإذن غير ممنوح ❌", Color.parseColor("#FB8C00"));
            }
        } catch (Exception e) {
            updateStatus("لم يتم العثور على مكتبة Shizuku!", Color.parseColor("#E53935"));
        }
    }

    // طلب الإذن
    private void requestShizukuPermission() {
        try {
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method requestMethod = shizukuClass.getMethod("requestPermission", int.class);
            requestMethod.invoke(null, 1001);
        } catch (Exception e) {
            Toast.makeText(this, "خطأ أثناء طلب الإذن: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // تنفيذ أمر Shell باستخدام Shizuku
    private void executeShellCommand(String command) {
        try {
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method newProcessMethod = shizukuClass.getMethod("newProcess", String[].class, String[].class, String.class);
            
            Process process = (Process) newProcessMethod.invoke(null, new String[]{"sh", "-c", command}, null, null);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
            Toast.makeText(this, "تم تنفيذ الأمر بنجاح", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "فشل التنفيذ (تأكد من إعطاء الإذن): " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateStatus(String text, int color) {
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
