package com.onesandzeros.patima;

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

public class PasswordForgotActivity extends AppCompatActivity {

    EditText emailTxt;
    Button forgotBtn;
    private SQLiteHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_forgot);

        emailTxt = findViewById(R.id.email_text);
        forgotBtn = findViewById(R.id.submit_btn);
        dbHelper = new SQLiteHelper(this);

        forgotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTxt.getText().toString();
                if (!email.isEmpty()) {
                    int result = dbHelper.forgotPassword(email);
                    if (result == 1) {
                        Toast.makeText(PasswordForgotActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(PasswordForgotActivity.this, "Email not found", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(PasswordForgotActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}