package com.onesandzeros.patima;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextView forgotTxt, regTxt, contactTxt;
    private EditText emailTxt, passwordTxt;
    private Button loginBtn;
    private SQLiteHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);

        //ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);


        emailTxt = findViewById(R.id.email_text); //Email Txt
        passwordTxt = findViewById(R.id.pass_text); //Password Txt
        forgotTxt = findViewById(R.id.forgot_Txt); //Password Forgot Txt
        regTxt = findViewById(R.id.reg_Txt);
        contactTxt = findViewById(R.id.contact_Txt);
        loginBtn = findViewById(R.id.signin_btn); //Login Btn

        setRegLink();
        setContactLink();

        dbHelper = new SQLiteHelper(this);

        forgotTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, PasswordForgotActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checkUserLogin(apiService, emailTxt.getText().toString(), passwordTxt.getText().toString());
                checkuserloginsql(emailTxt.getText().toString(), passwordTxt.getText().toString());
            }
        });

    }

    private void setRegLink() {

        String textreg = "Don’t have an account? Register";
        SpannableString spannableRegister = new SpannableString(textreg);

        int orangeColor = getResources().getColor(R.color.colorPrimary);
        int registerIndex = textreg.indexOf("Register");

        spannableRegister.setSpan(new ForegroundColorSpan(orangeColor),
                registerIndex, textreg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        NoUnderlineClickableSpan registerSpan = new NoUnderlineClickableSpan(new View.OnClickListener() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });
        spannableRegister.setSpan(registerSpan, registerIndex, spannableRegister.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        regTxt.setText(spannableRegister);
        regTxt.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }
    private void setContactLink() {

        String textcontact = "Have a Problem? Contact Admin";
        SpannableString spannableContact = new SpannableString(textcontact);

        int orangeColor = getResources().getColor(R.color.colorPrimary);
        int contactIndex = textcontact.indexOf("Contact Admin");

        spannableContact.setSpan(new ForegroundColorSpan(orangeColor),
                contactIndex, textcontact.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        NoUnderlineClickableSpan contactSpan = new NoUnderlineClickableSpan(new View.OnClickListener() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(LoginActivity.this, AdminContactActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        spannableContact.setSpan(contactSpan, contactIndex, spannableContact.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        contactTxt.setText(spannableContact);
        contactTxt.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }

    private void checkUserLogin(ApiService apiService, String email, String password) {
        Call<LoginResponse> call = apiService.checkUserLogin(email, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse != null) {
                        //Toast.makeText(LoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("API Success", "Login Status: " + loginResponse.getMessage());

                        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("isStarted", "yes");
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();

                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    Log.e("API Error", "Response Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                Log.e("API Failure", "Error: " + t.getMessage());
            }
        });
    }
    private void checkuserloginsql(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!email.isEmpty() && !password.isEmpty()) {
            Cursor cursor = dbHelper.getUser(email, password);
            if (cursor.moveToFirst()) {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("User_Id"));
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow("Fname"));
                String lastName = cursor.getString(cursor.getColumnIndexOrThrow("Lname"));
                String fullName = firstName + " " + lastName;

                if (dbHelper.isUserGeneralPublic(userId)) {
                    //Toast.makeText(this, "General Public with userId: " + userId, Toast.LENGTH_SHORT).show();

                    editor.putString("isStarted", "yes");
                    editor.putString("userType", "General Public User");
                    editor.putInt("userId", userId);
                    editor.putString("username", fullName);
                    editor.apply();

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else if (dbHelper.isUserArcheologist(userId)) {
                    //Toast.makeText(this, "Archeologist with userId: " + userId, Toast.LENGTH_SHORT).show();

                    editor.putString("isStarted", "yes");
                    editor.putString("userType", "Archeologist User");
                    editor.putInt("userId", userId);
                    editor.putString("username", fullName);
                    editor.apply();

                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            } else {
                Toast.makeText(this, "No matching user", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        } else {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
        }
    }




}