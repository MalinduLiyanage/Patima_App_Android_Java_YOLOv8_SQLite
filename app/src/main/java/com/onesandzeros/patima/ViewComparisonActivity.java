package com.onesandzeros.patima;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ViewComparisonActivity extends AppCompatActivity {

    ImageView baseImg, processedImg;
    ImageButton feedbackBtn, homeBtn, returnBtn;
    int img_Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comparison);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        String userTypest = sharedPreferences.getString("userType", "");

        Intent intent = getIntent();
        if (intent != null) {
            img_Id = intent.getIntExtra("imgId", 0); // 0 is the default value if imgId is not found
        } else {
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
        }

        baseImg = findViewById(R.id.base_image);
        processedImg = findViewById(R.id.processed_image);
        feedbackBtn = findViewById(R.id.feedback_Btn);
        homeBtn = findViewById(R.id.home_Btn);
        returnBtn = findViewById(R.id.return_button);

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (userTypest.equals("General Public User")) {
            feedbackBtn.setVisibility(View.GONE);
            feedbackBtn.setEnabled(false);
        }

        // Load images from database using img_Id
        loadImagePathsFromDatabase(img_Id);

        processedImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        baseImg.setVisibility(View.VISIBLE);
                        processedImg.setVisibility(View.GONE);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        baseImg.setVisibility(View.GONE);
                        processedImg.setVisibility(View.VISIBLE);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Optional: You can add behavior for ACTION_MOVE if needed.
                        // For example, keep the base image visible while moving.
                        baseImg.setVisibility(View.VISIBLE);
                        processedImg.setVisibility(View.GONE);
                        return true;
                }
                return false;
            }
        });

        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewComparisonActivity.this, FeedbackActivity.class);
                intent.putExtra("imgId", img_Id);
                startActivity(intent);
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadImagePathsFromDatabase(int imgId) {
        SQLiteHelper dbHelper = new SQLiteHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query to get Output_Image_Path and Input_Image_Path based on imgId
        String query = "SELECT Output_Image_Path, Input_Image_Path FROM IMAGE WHERE Image_Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(imgId)});

        if (cursor != null && cursor.moveToFirst()) {
            String outputImagePath = cursor.getString(cursor.getColumnIndex("Output_Image_Path"));
            String inputImagePath = cursor.getString(cursor.getColumnIndex("Input_Image_Path"));

            //Toast.makeText(this, "Input Image Path: " + inputImagePath, Toast.LENGTH_SHORT).show();

            // Load images into ImageView using Glide
            Glide.with(this)
                    .load(outputImagePath)
                    .into(processedImg);

            Glide.with(this)
                    .load(inputImagePath)
                    .into(baseImg);

            cursor.close();
        }
    }
}
