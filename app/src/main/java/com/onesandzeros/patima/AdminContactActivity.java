package com.onesandzeros.patima;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.regex.Pattern;

public class AdminContactActivity extends AppCompatActivity {

    EditText emailTxt, msgTxt, nameTxt;
    Button submitBtn;
    SQLiteHelper dbHelper;
    int userid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_admin_contact);

        nameTxt = findViewById(R.id.name_text);
        emailTxt = findViewById(R.id.email_text);
        msgTxt = findViewById(R.id.msg_text);
        submitBtn = findViewById(R.id.admin_submit_btn);

        dbHelper = new SQLiteHelper(this);

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        String checkStart = sharedPreferences.getString("isStarted","");

        initialState();

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = msgTxt.getText().toString();
                String email = emailTxt.getText().toString();
                String name = nameTxt.getText().toString();

                if (checkStart.equals("yes")){
                    if(!msg.isEmpty() || msg.length() > 5){

                        SQLiteHelper dbHelper = new SQLiteHelper(AdminContactActivity.this);
                        String returnedMsg = dbHelper.addContactAdminMessage(msg, email, name);
                        Toast.makeText(AdminContactActivity.this, returnedMsg, Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(AdminContactActivity.this, "Please enter your message correctly!", Toast.LENGTH_LONG).show();
                    }
                }else{
                    if(!msg.isEmpty() || isValidEmail(email) || !name.isEmpty()){
                        if(isValidEmail(email)){
                            if(name.length() > 1){
                                if(msg.length() > 5){

                                    SQLiteHelper dbHelper = new SQLiteHelper(AdminContactActivity.this);
                                    String returnedMsg = dbHelper.addContactAdminMessage(msg, email, name);
                                    Toast.makeText(AdminContactActivity.this, returnedMsg, Toast.LENGTH_SHORT).show();
                                    finish();
                                }else{
                                    Toast.makeText(AdminContactActivity.this, "Please enter your message correctly!", Toast.LENGTH_LONG).show();
                                }
                            }else{
                                Toast.makeText(AdminContactActivity.this, "Please enter your name correctly!", Toast.LENGTH_LONG).show();
                            }
                        }else{
                            Toast.makeText(AdminContactActivity.this, "Invalid Email!", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(AdminContactActivity.this, "Please fill all the fields!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    private void initialState() {

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        String checkStart = sharedPreferences.getString("isStarted","");

        if (checkStart.equals("yes")){
            userid = sharedPreferences.getInt("userId",0);
            String email = dbHelper.getUserEmail(userid);
            String name = dbHelper.getUserName(userid);
            emailTxt.setText(email);
            emailTxt.setEnabled(false);
            nameTxt.setText(name);
            nameTxt.setEnabled(false);
        }
    }

    private static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        String emailRegex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@[\\w-]+(?:\\.[\\w-]+)*$";
        Pattern pattern = Pattern.compile(emailRegex);

        return pattern.matcher(email).matches();
    }
}