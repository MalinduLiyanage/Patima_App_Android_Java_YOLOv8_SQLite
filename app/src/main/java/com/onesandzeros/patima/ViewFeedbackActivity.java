package com.onesandzeros.patima;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ViewFeedbackActivity extends AppCompatActivity {

    private RecyclerView feedbackContainer;
    private List<Feedback> feedbackList;
    private FeedbackAdapter feedbackAdapter;
    SQLiteHelper dbHelper;
    TextView detectTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feedback);

        detectTxt = findViewById(R.id.detect_Txt);
        feedbackContainer = findViewById(R.id.feedback_container);

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        int userid = sharedPreferences.getInt("userId",0);

        loaduserFeedbacks(userid);

    }

    private void loaduserFeedbacks(int userid) {

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        String username = sharedPreferences.getString("username","");

        dbHelper = new SQLiteHelper(this);
        feedbackList = new ArrayList<>();
        feedbackAdapter = new FeedbackAdapter(feedbackList, this, username, dbHelper,false,userid);

        int spanCount = 1;
        GridLayoutManager layoutManager = new GridLayoutManager(ViewFeedbackActivity.this, spanCount);
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

                Feedback feedback = new Feedback(feedback_Id, desc, rating, image_Id);
                feedbackList.add(feedback);
            } while (cursor.moveToNext());
            detectTxt.setVisibility(View.GONE);
            cursor.close();
            feedbackAdapter.notifyDataSetChanged();
        } else {
            detectTxt.setVisibility(View.VISIBLE);
        }

        db.close();
    }
}