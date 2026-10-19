package com.jfjfigiggi.iufcu;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.IBinder;
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
    private boolean isShizukuReady = false;

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

        // نص حالة Shizuku
        statusTextView = new TextView(this);
        statusTextView.setTextSize(16);
        statusTextView.setPadding(30, 30, 30, 30);
        statusTextView.setGravity(Gravity.CENTER);
        mainLayout.addView(statusTextView);

        // زر فتح Shizuku
        Button requestPermissionBtn = createStyledButton("فتح Shizuku لمنح الإذن / التفعيل", Color.parseColor("#1E88E5"));
        requestPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShizukuApp();
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

        checkShizukuService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkShizukuService();
    }

    private void checkShizukuService() {
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            Method getService = serviceManager.getMethod("getService", String.class);
            IBinder binder = (IBinder) getService.invoke(null, "moe.shizuku.server.IShizukuService");

            if (binder != null && binder.isBinderAlive()) {
                isShizukuReady = true;
                updateStatus("حالة الوصول: خدمة Shizuku مفعّلة وجاهزة ✔️", Color.parseColor("#4CAF50"));
            } else {
                isShizukuReady = false;
                updateStatus("خدمة Shizuku غير مفعّلة في الهاتف! ❌", Color.parseColor("#E53935"));
            }
        } catch (Exception e) {
            isShizukuReady = false;
            updateStatus("لم يتم العثور على خدمة Shizuku! ❌", Color.parseColor("#E53935"));
        }
    }

    private void openShizukuApp() {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage("rikka.shizuku");
            if (intent != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "تطبيق Shizuku غير مثبت على هذا الهاتف!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "خطأ أثناء فتح Shizuku: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void executeShellCommand(String command) {
        if (!isShizukuReady) {
            Toast.makeText(this, "يرجى تشغيل خدمة Shizuku أولاً!", Toast.LENGTH_LONG).show();
            openShizukuApp();
            return;
        }

        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();

            Toast.makeText(this, "تم تنفيذ الأمر بنجاح!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "حدث خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
