package com.onesandzeros.patima;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

public class ProcessActivity extends AppCompatActivity {

    LottieAnimationView lottie;
    TextView processTxt;

    private Handler handler = new Handler();
    private int step = 0;
    private String[] steps = {"Initializing...","Segmenting...", "Identifying...", "Magic Logic...", "Completed"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        int img_Id = 0;
        lottie = findViewById(R.id.lottieAnimationView);
        processTxt = findViewById(R.id.process_txt);

        lottie.playAnimation();
        lottie.loop(true);

        updateText();

        Intent intent = getIntent();
        if (intent != null) {
            img_Id = intent.getIntExtra("imgId", 0); // 0 is the default value if INT_VALUE is not found
            //Toast.makeText(this, String.valueOf(img_Id), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
        }

        int finalImg_Id = img_Id;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Action to perform after 5 seconds
                Intent intent = new Intent(ProcessActivity.this, ViewComparisonActivity.class);
                intent.putExtra("imgId", finalImg_Id);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish(); // Finish the activity
            }
        }, 10000); // 10000 milliseconds = 10 seconds

    }

    private void updateText() {
        if (step < steps.length) {
            processTxt.setText(steps[step]);
            applySlideAnimation();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    step++;
                    updateText();
                }
            }, 2000);
        }
    }

    private void applySlideAnimation() {
        TranslateAnimation animate = new TranslateAnimation(-(processTxt.getWidth()), 0, 0, 0);
        animate.setDuration(500); // Animation duration
        processTxt.startAnimation(animate);
    }
}