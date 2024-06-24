package com.onesandzeros.patima;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConfirmEmailActivity extends AppCompatActivity {

    EditText emailText;
    Button doneBtn;
    TextView msgTxt;
    private SQLiteHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_email);

        emailText = findViewById(R.id.email_text);
        doneBtn = findViewById(R.id.done_btn);
        msgTxt = findViewById(R.id.msg_text);

        dbHelper = new SQLiteHelper(this);

        // Get intent data
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            // Extract parameter (email in this case)
            String email = data.getLastPathSegment();
            emailText.setText(email);

            boolean isnotActivated = dbHelper.getActivestatus(email);
            if(isnotActivated){
                dbHelper.activateAccount(email);
            }else{
                msgTxt.setText("Account already activated.");
            }

        }else{
            Toast.makeText(this, "App Error", Toast.LENGTH_SHORT).show();
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