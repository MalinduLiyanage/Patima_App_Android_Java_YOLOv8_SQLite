package com.onesandzeros.patima;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ViewSingleFeedbackActivity extends AppCompatActivity {

    ImageView processedImg;
    EditText ratingTxt, spinnerOneTxt, spinnerTwoTxt, spinnerThreeTxt, feedbackTxt;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_feedback);

        processedImg = findViewById(R.id.processed_image);
        spinnerOneTxt = findViewById(R.id.spinner_one);
        spinnerTwoTxt = findViewById(R.id.spinner_two);
        spinnerThreeTxt = findViewById(R.id.spinner_three);
        ratingTxt = findViewById(R.id.rating_text);
        feedbackTxt = findViewById(R.id.feedback_text);

        String feedback = getIntent().getStringExtra("feedback");
        int rating = getIntent().getIntExtra("feedbackrating",0);
        String imagePath = getIntent().getStringExtra("detection_path");

        //Toast.makeText(this, imagePath, Toast.LENGTH_SHORT).show();

        String[] parts = feedback.split("\\$\\$");
        String part1 = parts[0]; // feedbackTxt
        String part2 = parts[1]; // spinnerOneValue
        String part3 = parts[2]; // spinnerTwoValue
        String part4 = parts[3]; // spinnerThreeValue

        feedbackTxt.setText(part1);
        spinnerOneTxt.setText(part2);
        spinnerTwoTxt.setText(part3);
        spinnerThreeTxt.setText(part4);
        ratingTxt.setText(String.valueOf(rating));

        if(imagePath.contains("http")){
            Picasso.get()
                    .load(imagePath)
                    .placeholder(R.drawable.bg_placeholder)
                    .into(processedImg);
        }else{
            File imageFile = new File(imagePath);
            Picasso.get()
                    .load(imageFile)
                    .placeholder(R.drawable.bg_placeholder)
                    .into(processedImg);
        }

    }
}