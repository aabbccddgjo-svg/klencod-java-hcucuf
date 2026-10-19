package com.jfjfigiggi.iufcu;

import android.app.Activity;
import android.content.pm.PackageManager;
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

import rikka.shizuku.Shizuku;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private static final int SHIZUKU_CODE = 1001;
    private TextView statusTextView;

    // مستمع لنتيجة طلب إذن Shizuku
    private final Shizuku.OnRequestPermissionResultListener onRequestPermissionResultListener = 
            this::onRequestPermissionResult;

    // مستمع لاستلام اتصال Binder الخاص بـ Shizuku
    private final Shizuku.OnBinderReceivedListener onBinderReceivedListener = 
            this::checkShizukuPermission;

    // مستمع لانقطاع الاتصال بـ Shizuku
    private final Shizuku.OnBinderDeadListener onBinderDeadListener = 
            this::checkShizukuPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // إنشاء الواجهة الأساسية برمجياً
        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        // عنوان التطبيق
        TextView titleTextView = new TextView(this);
        titleTextView.setText("لوحة التحكم بالجهاز (Shizuku)");
        titleTextView.setTextSize(22);
        titleTextView.setTextColor(Color.WHITE);
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setPadding(0, 0, 0, 30);
        mainLayout.addView(titleTextView);

        // نص إظهار حالة وصول إذن Shizuku
        statusTextView = new TextView(this);
        statusTextView.setTextSize(16);
        statusTextView.setPadding(30, 30, 30, 30);
        statusTextView.setGravity(Gravity.CENTER);
        mainLayout.addView(statusTextView);

        // زر طلب الصلاحية
        Button requestPermissionBtn = createStyledButton("طلب إذن Shizuku", Color.parseColor("#1E88E5"));
        requestPermissionBtn.setOnClickListener(v -> requestShizukuPermission());
        mainLayout.addView(requestPermissionBtn);

        // خط فاصل
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

        // تسجيل المستمعين لإعادة فحص الحالة تلقائياً
        Shizuku.addBinderReceivedListenerSticky(onBinderReceivedListener);
        Shizuku.addBinderDeadListener(onBinderDeadListener);
        Shizuku.addRequestPermissionResultListener(onRequestPermissionResultListener);

        checkShizukuPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // تنظيف المستمعين عند إغلاق التطبيق
        Shizuku.removeBinderReceivedListener(onBinderReceivedListener);
        Shizuku.removeBinderDeadListener(onBinderDeadListener);
        Shizuku.removeRequestPermissionResultListener(onRequestPermissionResultListener);
    }

    // دالة التحقق من حالة إذن وصول Shizuku
    private void checkShizukuPermission() {
        if (!Shizuku.pingBinder()) {
            updateStatus("تطبيق Shizuku غير متصل!", Color.parseColor("#E53935"));
            return;
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            updateStatus("حالة الوصول: تم منح الإذن بنجاح ✔️", Color.parseColor("#4CAF50"));
        } else {
            updateStatus("حالة الوصول: الإذن غير ممنوح ❌", Color.parseColor("#FB8C00"));
        }
    }

    // دالة طلب إذن Shizuku
    private void requestShizukuPermission() {
        if (!Shizuku.pingBinder()) {
            Toast.makeText(this, "تأكد من تشغيل خدمة Shizuku أولاً", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "الإذن ممنوح بالفعل", Toast.LENGTH_SHORT).show();
        } else {
            Shizuku.requestPermission(SHIZUKU_CODE);
        }
    }

    // استقبال نتيجة إذن Shizuku
    private void onRequestPermissionResult(int requestCode, int grantResult) {
        if (requestCode == SHIZUKU_CODE) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                updateStatus("حالة الوصول: تم منح الإذن بنجاح ✔️", Color.parseColor("#4CAF50"));
                Toast.makeText(this, "تم قبول الإذن!", Toast.LENGTH_SHORT).show();
            } else {
                updateStatus("حالة الوصول: تم رفض الإذن ❌", Color.parseColor("#E53935"));
                Toast.makeText(this, "تم رفض الإذن!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // تنفيذ أوامر Shell باستخدام Shizuku
    private void executeShellCommand(String command) {
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "يرجى إعطاء إذن Shizuku أولاً!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Process process = Shizuku.newProcess(new String[]{"sh", "-c", command}, null, null);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
            Toast.makeText(this, "تم تنفيذ الأمر بنجاح", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // تحديث مظهر نص الحالة
    private void updateStatus(String text, int color) {
        runOnUiThread(() -> {
            statusTextView.setText(text);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(color);
            drawable.setCornerRadius(15);
            statusTextView.setBackground(drawable);
            statusTextView.setTextColor(Color.WHITE);
        });
    }

    // إنشاء أزرار الخيارات والعمليات
    private Button createActionButton(String title, String command) {
        Button button = createStyledButton(title, Color.parseColor("#2C2C2C"));
        button.setOnClickListener(v -> executeShellCommand(command));
        return button;
    }

    // تصميم وتنسيق الأزرار
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

    // إضافة خط فاصل جمالي بين الأقسام
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
