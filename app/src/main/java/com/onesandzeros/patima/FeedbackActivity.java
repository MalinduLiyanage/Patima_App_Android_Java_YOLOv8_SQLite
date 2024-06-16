package com.onesandzeros.patima;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class FeedbackActivity extends AppCompatActivity {

    TextView ratingTxt, feedbackTxt;
    ImageView processedImg;
    ImageButton submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        int img_Id = 0;

        Intent intent = getIntent();
        if (intent != null) {
            img_Id = intent.getIntExtra("imgId", 0); // 0 is the default value if INT_VALUE is not found
            //Toast.makeText(this, String.valueOf(img_Id), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();
        }

        ratingTxt = findViewById(R.id.rating_text);
        feedbackTxt = findViewById(R.id.feedback_text);
        submitBtn = findViewById(R.id.submitfeedback_Btn);
        processedImg = findViewById(R.id.processed_image);

        loadImagePathsFromDatabase(img_Id);

        int finalImg_Id = img_Id;
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ratingString = ratingTxt.getText().toString();
                if (!ratingString.isEmpty()) {
                    int rating = Integer.parseInt(ratingString);
                    checkUserinputs(finalImg_Id, rating, feedbackTxt.getText().toString());
                } else {
                    Toast.makeText(FeedbackActivity.this, "Please enter a valid rating", Toast.LENGTH_SHORT).show();
                }
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

            // Load images into ImageView using Glide
            Glide.with(this)
                    .load(outputImagePath)
                    .into(processedImg);

            cursor.close();
        }
    }

    private void checkUserinputs(int img_Id,int ratingTxt, String feedbackTxt) {

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        int userid = sharedPreferences.getInt("userId",0);

        if (ratingTxt <= 5 && ratingTxt > 0 && feedbackTxt.length() > 5){
            SQLiteHelper dbHelper = new SQLiteHelper(this);
            String returnedMsg = dbHelper.addFeedback(img_Id, feedbackTxt, ratingTxt,userid);
            Toast.makeText(this, returnedMsg, Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(this, "Please fill all fields. Rating should be within 1 - 5", Toast.LENGTH_LONG).show();
        }

    }
}