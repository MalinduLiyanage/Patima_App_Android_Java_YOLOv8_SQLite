package com.onesandzeros.patima;

import static java.lang.Math.cos;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    String basePath = null, detectionPath = null, timeStamp = null;
    private ImageView baseImg;
    private ImageView processedImg;
    int imgId = 0;
    FloatingActionButton feedbackBtn;
    TextView feedbackTextShow, detectTxt, nearbydetectTxt;
    RecyclerView feedbackContainer, nearbyContainer;
    private List<Feedback> feedbackList;
    private FeedbackAdapter feedbackAdapter;
    private List<Location> locationList;
    private LocationAdapter locationAdapter;
    SQLiteHelper dbHelper;
    private static final double EARTH_RADIUS_KM = 6371.0;
    double selectedImg_lat, selectedImg_lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        imgId = getIntent().getIntExtra("imgId", 0);
        basePath = getIntent().getStringExtra("base_path");
        detectionPath = getIntent().getStringExtra("detection_path");
        //timeStamp = getIntent().getStringExtra("timestamp");

        baseImg = findViewById(R.id.base_image);
        processedImg = findViewById(R.id.processed_image);
        feedbackBtn = findViewById(R.id.btn_feedback);
        feedbackTextShow = findViewById(R.id.feedbackshow);
        feedbackContainer = findViewById(R.id.feedback_container);
        nearbyContainer = findViewById(R.id.nearby_container);
        detectTxt = findViewById(R.id.detect_txt);
        nearbydetectTxt = findViewById(R.id.nearby_detect_txt);

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        String userTypest = sharedPreferences.getString("userType", "");
        int userid = sharedPreferences.getInt("userId",0);

        if (userTypest.equals("General Public User")) {
            feedbackBtn.setVisibility(View.INVISIBLE);
            feedbackTextShow.setVisibility(View.GONE);
            feedbackContainer.setVisibility(View.GONE);
            feedbackBtn.setEnabled(false);

        }else{
            loaduserFeedbacks(userid);
        }

        loadImages();
        loadNearby(imgId);
        nearbyImages();

        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, FeedbackActivity.class);
                intent.putExtra("imgId", imgId);
                startActivity(intent);
            }
        });

    }

    private void loadNearby(int imgId) {
        dbHelper = new SQLiteHelper(this);
        String location = dbHelper.getImagetag(imgId);
        String[] locationParts = location.split(", ");
        selectedImg_lat = Double.parseDouble(locationParts[0]);
        selectedImg_lon = Double.parseDouble(locationParts[1]);
    }

    public static boolean isWithinRadius(double lat, double lon, double targetLat, double targetLon, double radiusKm) {
        double deltaLat = toDegrees(radiusKm / EARTH_RADIUS_KM);
        double deltaLon = toDegrees(radiusKm / (EARTH_RADIUS_KM * cos(toRadians(lat))));

        double minLat = lat - deltaLat;
        double maxLat = lat + deltaLat;
        double minLon = lon - deltaLon;
        double maxLon = lon + deltaLon;

        return targetLat >= minLat && targetLat <= maxLat && targetLon >= minLon && targetLon <= maxLon;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        String userTypest = sharedPreferences.getString("userType", "");
        int userid = sharedPreferences.getInt("userId",0);
        if (userTypest.equals("General Public User")) {
            feedbackBtn.setVisibility(View.INVISIBLE);
            feedbackTextShow.setVisibility(View.GONE);
            feedbackContainer.setVisibility(View.GONE);
            feedbackBtn.setEnabled(false);

        }else{
            loaduserFeedbacks(userid);
        }
    }

    private void loaduserFeedbacks(int userid) {

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        String username = sharedPreferences.getString("username","");

        dbHelper = new SQLiteHelper(this);
        feedbackList = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(feedbackList, this, username, dbHelper,true,userid);

        int count = 0;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        feedbackContainer.setLayoutManager(layoutManager);
        feedbackContainer.setAdapter(feedbackAdapter);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"Feedback_Id","Description", "Ratings", "Image_Id"};
        String selection = "User_Id = ?";
        String[] selectionArgs = {String.valueOf(userid)};

        Cursor cursor = db.query("FEEDBACK", projection, selection, selectionArgs, null, null, "Feedback_Id DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int feedback_Id = cursor.getInt(cursor.getColumnIndexOrThrow("Feedback_Id"));
                String desc = cursor.getString(cursor.getColumnIndexOrThrow("Description"));
                int rating = cursor.getInt(cursor.getColumnIndexOrThrow("Ratings"));
                int image_Id = cursor.getInt(cursor.getColumnIndexOrThrow("Image_Id"));

                if(image_Id == imgId ){
                    Feedback feedback = new Feedback(feedback_Id, desc, rating, image_Id);
                    feedbackList.add(feedback);
                    count++;
                }else{

                }

            } while (cursor.moveToNext());
            cursor.close();
            feedbackAdapter.notifyDataSetChanged();
        } else {

        }
        if(count == 0){
            detectTxt.setVisibility(View.VISIBLE);
        }else{
            detectTxt.setVisibility(View.GONE);
        }
        db.close();
    }

    private void loadImages() {

        processedImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        baseImg.setVisibility(View.VISIBLE);
                        processedImg.setVisibility(View.GONE);
                        return true;

                    case MotionEvent.ACTION_UP:
                        baseImg.setVisibility(View.GONE);
                        processedImg.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });

        Glide.with(this)
                .load(detectionPath)
                .into(processedImg);

        Glide.with(this)
                .load(basePath)
                .into(baseImg);


    }

    private void nearbyImages() {
        dbHelper = new SQLiteHelper(this);
        locationList = new ArrayList<>();
        locationAdapter = new LocationAdapter(locationList, this,dbHelper);

        int count = 0;

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        nearbyContainer.setLayoutManager(layoutManager);
        nearbyContainer.setAdapter(locationAdapter);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"Image_Id","Tags"};

        Cursor cursor = db.query("IMAGE_TAG", projection, null, null, null, null, "Image_Id DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int imageid = cursor.getInt(cursor.getColumnIndexOrThrow("Image_Id"));
                String tags = cursor.getString(cursor.getColumnIndexOrThrow("Tags"));

                Location location = new Location(imageid, tags);

                String[] locationParts = tags.split(", ");
                double targetLat = Double.parseDouble(locationParts[0]); // Replace with actual latitude to check
                double targetLon = Double.parseDouble(locationParts[1]);  // Replace with actual longitude to check
                double radiusKm = 5.0;

                if (isWithinRadius(selectedImg_lat, selectedImg_lon, targetLat, targetLon, radiusKm)) {
                    if(imageid != imgId){
                        //Toast.makeText(this, "The location is within 5 km radius.", Toast.LENGTH_SHORT).show();
                        locationList.add(location);
                        count++;
                    }
                } else {
                    //Toast.makeText(this, "The location is not within 5 km radius.", Toast.LENGTH_SHORT).show();
                }

            } while (cursor.moveToNext());
            //detectTxt.setVisibility(View.GONE);
            cursor.close();
            locationAdapter.notifyDataSetChanged();
        } else {
            //detectTxt.setVisibility(View.VISIBLE);
        }

        if(count == 0){
            nearbydetectTxt.setVisibility(View.VISIBLE);
        }

        db.close();
    }
}