package com.jfjfigiggi.iufcu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // إنشاء تخطيط رئيسي بسيط
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        // إنشاء زر للتقاط الشاشة
        Button captureButton = new Button(this);
        captureButton.setText("التقاط لقطة شاشة");

        // إضافة الزر للتخطيط
        layout.addView(captureButton);
        setContentView(layout);

        // عند الضغط على الزر يتم التقاط الشاشة وحفظها
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // التقاط الشاشة للواجهة الحالية
                View rootView = getWindow().getDecorView().getRootView();
                Bitmap bitmap = takeScreenshot(rootView);

                if (bitmap != null) {
                    saveBitmapToGallery(bitmap);
                } else {
                    Toast.makeText(MainActivity.this, "فشل التقاط الشاشة", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // دالة التقاط الشاشة باستخدام Canvas و Bitmap
    private Bitmap takeScreenshot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    // دالة حفظ الصورة في المعرض (Gallery)
    private void saveBitmapToGallery(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, imageFileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (imageUri != null) {
                    fos = getContentResolver().openOutputStream(imageUri);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    if (fos != null) fos.close();
                    Toast.makeText(this, "تم حفظ لقطة الشاشة بنجاح!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // للأجهزة القديمة
                String savedImageURL = MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        bitmap,
                        imageFileName,
                        "Screenshot"
                );
                if (savedImageURL != null) {
                    Toast.makeText(this, "تم حفظ لقطة الشاشة بنجاح!", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "حدث خطأ أثناء الحفظ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
