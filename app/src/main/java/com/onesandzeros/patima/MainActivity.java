package com.onesandzeros.patima;

import static androidx.constraintlayout.motion.widget.Debug.getLocation;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FrameLayout contentViewContainer;
    private LinearLayout bottomNavigationBar;
    private ImageView homeButton, profileButton;
    private Boolean isHome = true;
    private CardView imagecaptureBtn;
    SQLiteHelper dbHelper;
    TextView userName, userType, detectTxt;
    CircleImageView profilePicture;
    private RecyclerView imageContainer;
    private List<Image> imageList;
    private ImageAdapter imageAdapter;
    String latitudeString = null, longitudeString = null;
    LinearLayout profileBtn, feedbackBtn, admincontactBtn, appsettingBtn, logoutBtn;
    ImageButton returnBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


        initialState();

        dbHelper = new SQLiteHelper(MainActivity.this);

        contentViewContainer = findViewById(R.id.content_view_container);
        bottomNavigationBar = findViewById(R.id.bottom_bar);
        homeButton = findViewById(R.id.home_button);
        profileButton = findViewById(R.id.profile_button);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHomeView();
                homeButton.setImageResource(R.drawable.btn_home_round);
                profileButton.setImageResource(R.drawable.btn_user);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileView();
                homeButton.setImageResource(R.drawable.btn_home);
                profileButton.setImageResource(R.drawable.btn_user_round);
            }
        });
        showHomeView();

        ContentValues contentValues = new ContentValues();
        Location location = getLocation(MainActivity.this); // Implement getLocation() method below
        if (location != null) {
            contentValues.put(MediaStore.Images.Media.LATITUDE, location.getLatitude());
            contentValues.put(MediaStore.Images.Media.LONGITUDE, location.getLongitude());

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            latitudeString = String.valueOf(latitude);
            longitudeString = String.valueOf(longitude);

        }else{
            latitudeString = "No Data";
            longitudeString = "No Data";
        }

    }

    private Location getLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location == null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                return location;
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isHome){
            showHomeView();
        }else{
            showProfileView();
        }

    }

    private void initialState() {

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        String checkStart = sharedPreferences.getString("isStarted","");

        if (!checkStart.equals("yes")){
            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }else{
            if (!allPermissionsGranted()) {
                Intent intent = new Intent(MainActivity.this, PermissionActivity.class);
                startActivity(intent);
                finish();
            }

            if (!sharedPreferences.contains("CONFIDENCE_THRESHOLD")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putFloat("CONFIDENCE_THRESHOLD", 0.6F);
                editor.apply();
            }



        }
    }
    private void showHomeView() {

        isHome = true;

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);

        contentViewContainer.removeAllViews();
        View homeView = getLayoutInflater().inflate(R.layout.view_home, null);
        contentViewContainer.addView(homeView);

        imagecaptureBtn = findViewById(R.id.img_capture_btn);
        profilePicture = findViewById(R.id.profile_img);

        detectTxt = findViewById(R.id.detect_Txt);

        String userTypest = sharedPreferences.getString("userType","");
        int userid = sharedPreferences.getInt("userId",0);
        String username = dbHelper.getUserName(userid);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.apply();

        userName = findViewById(R.id.username_text);
        userType = findViewById(R.id.usertype_text);

        userName.setText(sharedPreferences.getString("username",username));
        userType.setText(sharedPreferences.getString("userType",userTypest));

        imagecaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageAcquisitionActivity.class);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                startActivity(intent);
            }
        });

        String profilepicturePath = dbHelper.getProfilepicture(userid);

        if(profilepicturePath.contains("http")){
            Picasso.get()
                    .load(profilepicturePath)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(profilePicture);
        }else{
            File imageFile = new File(profilepicturePath);
            Picasso.get()
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(profilePicture);
        }

        detectedObjects(userid);

    }

    private void detectedObjects(int userId) {
        imageContainer = findViewById(R.id.image_container);
        dbHelper = new SQLiteHelper(this);
        imageList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageList, this);

        int spanCount = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(MainActivity.this, spanCount);
        imageContainer.setLayoutManager(layoutManager);

        imageContainer.setAdapter(imageAdapter);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"Image_Id","Input_Image_Path", "Output_Image_Path", "Timestamp"};
        String selection = "User_Id = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query("IMAGE", projection, selection, selectionArgs, null, null, "Timestamp DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int imageid = cursor.getInt(cursor.getColumnIndexOrThrow("Image_Id"));
                String input_path = cursor.getString(cursor.getColumnIndexOrThrow("Input_Image_Path"));
                String output_path = cursor.getString(cursor.getColumnIndexOrThrow("Output_Image_Path"));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("Timestamp"));

                Image image = new Image(imageid, input_path, output_path, timestamp);
                imageList.add(image);
            } while (cursor.moveToNext());
            detectTxt.setVisibility(View.GONE);
            cursor.close();
            imageAdapter.notifyDataSetChanged();
        } else {
            detectTxt.setVisibility(View.VISIBLE);
        }

        db.close();
    }


    private void showProfileView() {

        isHome = false;

        contentViewContainer.removeAllViews();
        View profileView = getLayoutInflater().inflate(R.layout.view_profile, null);
        contentViewContainer.addView(profileView);

        userName = findViewById(R.id.username_text);
        profilePicture = findViewById(R.id.profile_img);

        profileBtn = findViewById(R.id.menu_profile);
        feedbackBtn = findViewById(R.id.menu_feedback);
        admincontactBtn = findViewById(R.id.menu_contactadmin);
        appsettingBtn = findViewById(R.id.menu_appsettings);
        logoutBtn = findViewById(R.id.menu_logout);
        returnBtn = findViewById(R.id.return_button);

        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHomeView();
                homeButton.setImageResource(R.drawable.btn_home_round);
                profileButton.setImageResource(R.drawable.btn_user);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        int userid = sharedPreferences.getInt("userId",0);
        String userTypest = sharedPreferences.getString("userType","");

        String username = dbHelper.getUserName(userid);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.apply();

        userName.setText(sharedPreferences.getString("username",username));

        String profilepicturePath = dbHelper.getProfilepicture(userid);

        if (userTypest.equals("General Public User")) {
            feedbackBtn.setVisibility(View.GONE);
            feedbackBtn.setEnabled(false);
        }

        if(profilepicturePath.contains("http")){
            Picasso.get()
                    .load(profilepicturePath)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(profilePicture);
        }else{
            File imageFile = new File(profilepicturePath);
            Picasso.get()
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(profilePicture);
        }

        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        feedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewFeedbackActivity.class);
                startActivity(intent);
            }
        });

        admincontactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminContactActivity.class);
                startActivity(intent);
            }
        });

        appsettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ParametersActivity.class);
                startActivity(intent);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setIcon(R.drawable.ic_welcome);
                builder.setMessage("Are you sure you want to logout ?");
                builder.setTitle("   Patima");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("isStarted", "no");
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();

                });
                builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                    dialog.cancel();
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });


    }

    private boolean allPermissionsGranted() {
        String[] REQUIRED_PERMISSIONS_ANDROID_12 = new String[]{
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
        };

        String[] REQUIRED_PERMISSIONS_ANDROID_13 = new String[]{
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // API level 33
            for (String permission : REQUIRED_PERMISSIONS_ANDROID_13) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }else{
            for (String permission : REQUIRED_PERMISSIONS_ANDROID_12) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

}