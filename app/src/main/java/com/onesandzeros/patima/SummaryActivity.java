package com.onesandzeros.patima;

import static java.lang.Math.cos;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {

    String basePath = null, detectionPath = null, timeStamp = null;
    private ImageView baseImg;
    private ImageView processedImg;
    int imgId = 0;
    FloatingActionButton feedbackBtn;
    TextView feedbackTextShow, detectTxt, nearbyTxt, nearbydetectTxt, locationTxt, locationName;
    RecyclerView feedbackContainer, nearbyContainer;
    private List<Feedback> feedbackList;
    private FeedbackAdapter feedbackAdapter;
    private List<Location> locationList;
    private LocationAdapter locationAdapter;
    SQLiteHelper dbHelper;
    private static final double EARTH_RADIUS_KM = 6371.0;
    double selectedImg_lat, selectedImg_lon;
    ImageButton returnBtn;
    ImageView feedbackexpanderBtn;
    boolean areFeedbacksexpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
        nearbyTxt = findViewById(R.id.nearby_txt);
        locationTxt = findViewById(R.id.loc_text);
        locationName = findViewById(R.id.loc_name);
        returnBtn = findViewById(R.id.return_button);
        feedbackexpanderBtn = findViewById(R.id.btn_feedback_expand);

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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

        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SummaryActivity.this, FeedbackActivity.class);
                intent.putExtra("imgId", imgId);
                startActivity(intent);
            }
        });

        feedbackexpanderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!areFeedbacksexpanded){
                    areFeedbacksexpanded = true;
                    feedbackexpanderBtn.setImageResource(R.drawable.ic_arrow_down);

                    ViewGroup.LayoutParams params = feedbackContainer.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    feedbackContainer.setLayoutParams(params);

                }else{
                    areFeedbacksexpanded = false;
                    feedbackexpanderBtn.setImageResource(R.drawable.ic_arrow_right);

                    int dpValue = 100;
                    float density = getResources().getDisplayMetrics().density;
                    int heightInPixels = (int) (dpValue * density);
                    ViewGroup.LayoutParams layoutParams = feedbackContainer.getLayoutParams();
                    layoutParams.height = heightInPixels;
                    feedbackContainer.setLayoutParams(layoutParams);

                }
            }
        });

    }

    private boolean loadNearby(int imgId) {
        dbHelper = new SQLiteHelper(this);
        String location = dbHelper.getImagetag(imgId);
        if(!location.contains("No Data")){
            String[] locationParts = location.split(", ");
            selectedImg_lat = Double.parseDouble(locationParts[0]);
            selectedImg_lon = Double.parseDouble(locationParts[1]);
            nearbyImages(selectedImg_lat, selectedImg_lon);
            locationTxt.setText(location);
            reverseGeocode(selectedImg_lat, selectedImg_lon);
            return true;
        }else{
            locationTxt.setText("Image has no Location Data. Location based summary will not be available.");
            nearbydetectTxt.setVisibility(View.GONE);
            nearbyContainer.setVisibility(View.GONE);
            nearbyTxt.setVisibility(View.GONE);
            return false;
        }

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

    private void reverseGeocode(double latitude, double longitude) {

        final String requestString = "https://nominatim.openstreetmap.org/reverse?format=json&lat=" +
                Double.toString(latitude) + "&lon=" + Double.toString(longitude) + "&zoom=18&addressdetails=1";

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(requestString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject jsonResponse = new JSONObject(response.toString());
                        JSONObject address = jsonResponse.getJSONObject("address");

                        String townName = "";
                        if (address.has("town")) {
                            townName = address.getString("town") + " - " + address.getString("country");
                        } else if (address.has("village")) {
                            townName = address.getString("village") + " - " + address.getString("country");
                        }  else if (address.has("state")) {
                            townName = address.getString("state") + " - " + address.getString("country");
                        }

                        final String finalTownName = townName;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!finalTownName.isEmpty()) {
                                    locationName.setText(finalTownName);
                                } else {
                                    locationName.setVisibility(View.GONE);
                                }
                            }
                        });
                    }

                    connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

    private void nearbyImages(double lat, double lon) {
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

                if(!tags.contains("No Data")){
                    Location location = new Location(imageid, tags);

                    String[] locationParts = tags.split(", ");
                    double targetLat = Double.parseDouble(locationParts[0]); // Replace with actual latitude to check
                    double targetLon = Double.parseDouble(locationParts[1]);  // Replace with actual longitude to check
                    double radiusKm = 5.0;

                    if (isWithinRadius(lat, lon, targetLat, targetLon, radiusKm)) {
                        if(imageid != imgId){
                            //Toast.makeText(this, "The location is within 5 km radius.", Toast.LENGTH_SHORT).show();
                            locationList.add(location);
                            count++;
                        }
                    } else {
                        //Toast.makeText(this, "The location is not within 5 km radius.", Toast.LENGTH_SHORT).show();
                    }
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