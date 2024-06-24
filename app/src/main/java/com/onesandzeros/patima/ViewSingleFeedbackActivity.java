package com.onesandzeros.patima;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
    EditText spinnerOneTxt, spinnerTwoTxt, spinnerThreeTxt, feedbackTxt;
    ImageButton starOne, starTwo, starThree, starFour, starFive;
    TextView ratingLvlTxt;
    SQLiteHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_feedback);

        processedImg = findViewById(R.id.processed_image);
        spinnerOneTxt = findViewById(R.id.spinner_one);
        spinnerTwoTxt = findViewById(R.id.spinner_two);
        spinnerThreeTxt = findViewById(R.id.spinner_three);
        feedbackTxt = findViewById(R.id.feedback_text);
        ratingLvlTxt = findViewById(R.id.star_lvl_desc);

        starOne = findViewById(R.id.star_lvl1);
        starTwo = findViewById(R.id.star_lvl2);
        starThree = findViewById(R.id.star_lvl3);
        starFour = findViewById(R.id.star_lvl4);
        starFive = findViewById(R.id.star_lvl5);

        dbHelper = new SQLiteHelper(this);

        String feedback = getIntent().getStringExtra("feedback");
        int imgid = getIntent().getIntExtra("imgId",0);
        int rating = getIntent().getIntExtra("feedbackrating",0);
        String imagePath = dbHelper.getOutputImagepath(imgid);

        String[] parts = feedback.split("\\$\\$");
        String part1 = parts[0]; // feedbackTxt
        String part2 = parts[1]; // spinnerOneValue
        String part3 = parts[2]; // spinnerTwoValue
        String part4 = parts[3]; // spinnerThreeValue

        feedbackTxt.setText(part1);
        spinnerOneTxt.setText(part2);
        spinnerTwoTxt.setText(part3);
        spinnerThreeTxt.setText(part4);

        if(rating == 1){
            starOne.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            ratingLvlTxt.setText("1 out of 5 : Not Good at all!");
        }else if(rating == 2){
            starOne.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            starTwo.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            ratingLvlTxt.setText("2 out of 5 : Seems Okay");
        }else if(rating == 3){
            starOne.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            starTwo.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            starThree.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            ratingLvlTxt.setText("3 out of 5 : Neutral");
        }else if(rating == 4){
            starOne.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            starTwo.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            starThree.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            starFour.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            ratingLvlTxt.setText("4 out of 5 : Good!");
        }else if(rating == 5){
            starOne.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            starTwo.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            starThree.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            starFour.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            starFive.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled_small));
            ratingLvlTxt.setText("5 out of 5 : Great!");
        }

        if(imagePath.contains("http")){
            Picasso.get()
                    .load(imagePath)
                    .into(processedImg);
        }else{
            File imageFile = new File(imagePath);
            Picasso.get()
                    .load(imageFile)
                    .into(processedImg);
        }

    }
}