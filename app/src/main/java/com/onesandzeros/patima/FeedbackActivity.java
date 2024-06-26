package com.onesandzeros.patima;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class FeedbackActivity extends AppCompatActivity {

    TextView feedbackTxt, ratingLvlTxt;
    ImageView processedImg;
    ImageButton submitBtn, starOne, starTwo, starThree, starFour, starFive;
    Spinner spinnerOne, spinnerTwo, spinnerThree;
    String spinnerOneValue = "Not accurate", spinnerTwoValue = "Not accurate", spinnerThreeValue = "Not accurate";
    int rating = 0;

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

        ratingLvlTxt = findViewById(R.id.star_lvl_desc);
        feedbackTxt = findViewById(R.id.feedback_text);
        submitBtn = findViewById(R.id.submitfeedback_Btn);
        processedImg = findViewById(R.id.processed_image);

        spinnerOne = findViewById(R.id.spinner_one);
        spinnerTwo = findViewById(R.id.spinner_two);
        spinnerThree = findViewById(R.id.spinner_three);
        spinnerSetup();

        starOne = findViewById(R.id.star_lvl1);
        starTwo = findViewById(R.id.star_lvl2);
        starThree = findViewById(R.id.star_lvl3);
        starFour = findViewById(R.id.star_lvl4);
        starFive = findViewById(R.id.star_lvl5);

        starOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starOne.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starTwo.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_notfilled));
                starThree.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_notfilled));
                starFour.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_notfilled));
                starFive.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_notfilled));
                rating = 1;
                ratingLvlTxt.setText("1 out of 5 : Not Good at all!");
            }
        });

        starTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starOne.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starTwo.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starThree.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_notfilled));
                starFour.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_notfilled));
                starFive.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_notfilled));
                rating = 2;
                ratingLvlTxt.setText("2 out of 5 : Seems Okay");
            }
        });

        starThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starOne.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starTwo.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starThree.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starFour.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_notfilled));
                starFive.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_notfilled));
                rating = 3;
                ratingLvlTxt.setText("3 out of 5 : Neutral");
            }
        });

        starFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starOne.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starTwo.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starThree.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starFour.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starFive.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_notfilled));
                rating = 4;
                ratingLvlTxt.setText("4 out of 5 : Good!");
            }
        });

        starFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starOne.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starTwo.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starThree.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starFour.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                starFive.setImageDrawable(getResources().getDrawable(R.drawable.ic_rating_filled));
                rating = 5;
                ratingLvlTxt.setText("5 out of 5 : Great!");
            }
        });

        loadImagePathsFromDatabase(img_Id);

        int finalImg_Id = img_Id;

        spinnerOne.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the selected item
                String selectedOption = parent.getItemAtPosition(position).toString();

                // Do something with the selected option
                spinnerOneValue = selectedOption;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerOneValue = "Very well";
            }
        });

        spinnerTwo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the selected item
                String selectedOption = parent.getItemAtPosition(position).toString();

                // Do something with the selected option
                spinnerTwoValue = selectedOption;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerTwoValue = "Very well";
            }
        });

        spinnerThree.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the selected item
                String selectedOption = parent.getItemAtPosition(position).toString();

                // Do something with the selected option
                spinnerThreeValue = selectedOption;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinnerThreeValue = "Very Satisfied";
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUserinputs(finalImg_Id, rating, feedbackTxt.getText().toString());
            }
        });

    }

    private void spinnerSetup() {
        // Create ArrayAdapter using the string array and custom spinner item layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.options,
                R.layout.spinner_item_layout // Use custom layout for spinner items
        );

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_item_layout);

        ArrayAdapter<CharSequence> adapterthree = ArrayAdapter.createFromResource(
                this,
                R.array.options_three,
                R.layout.spinner_item_layout // Use custom layout for spinner items
        );

        // Specify the layout to use when the list of choices appears
        adapterthree.setDropDownViewResource(R.layout.spinner_item_layout);

        // Apply the adapter to the spinner
        spinnerOne.setAdapter(adapter);
        spinnerTwo.setAdapter(adapter);
        spinnerThree.setAdapter(adapter);
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
            String concat = feedbackTxt + "$$" + spinnerOneValue + "$$" + spinnerTwoValue + "$$" + spinnerThreeValue;
            String returnedMsg = dbHelper.addFeedback(img_Id, concat, ratingTxt,userid);
            Toast.makeText(this, returnedMsg, Toast.LENGTH_SHORT).show();
            finish();
        }else{
            Toast.makeText(this, "Please fill all fields. Rating should be within 1 - 5", Toast.LENGTH_LONG).show();
        }

    }

}