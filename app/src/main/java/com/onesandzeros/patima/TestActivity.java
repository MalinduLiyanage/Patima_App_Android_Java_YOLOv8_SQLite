package com.onesandzeros.patima;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TestActivity extends AppCompatActivity {

    EditText emailText;
    Button doneBtn;
    private SQLiteHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        emailText = findViewById(R.id.email_text);
        doneBtn = findViewById(R.id.done_btn);

        dbHelper = new SQLiteHelper(this);

        // Get intent data
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            // Extract parameter (email in this case)
            String email = data.getLastPathSegment();
            emailText.setText(email);
            dbHelper.activateAccount(email);

        }else{
            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show();
            doneBtn.setVisibility(View.GONE);
            doneBtn.setEnabled(false);
        }

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}