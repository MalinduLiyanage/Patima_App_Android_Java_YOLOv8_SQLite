package com.onesandzeros.patima;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextView loginTxt, contactTxt;
    private EditText firstnameTxt, lastnameTxt, emailTxt, arcIdTxt, passwordTxt, confirmPasswordTxt;
    private Button registerBtn;
    private SQLiteHelper dbHelper;
    RadioButton generalRegister, archeologistRegister;
    RadioGroup radioGroup;
    boolean isGeneraluser = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_register);

        dbHelper = new SQLiteHelper(this);

        firstnameTxt = findViewById(R.id.firstname_text); //Firstname Txt
        lastnameTxt = findViewById(R.id.lastname_text); //Lastname Txt
        emailTxt = findViewById(R.id.email_text); //Email Txt
        arcIdTxt = findViewById(R.id.arcid_text); //Archeological Id Txt
        passwordTxt = findViewById(R.id.pass_text); //Password Txt
        confirmPasswordTxt = findViewById(R.id.passconfirm_text); //confirm Password Txt
        loginTxt = findViewById(R.id.login_Txt);
        contactTxt = findViewById(R.id.contact_Txt);
        registerBtn = findViewById(R.id.register_btn); //register Btn

        generalRegister = findViewById(R.id.radio_general);
        archeologistRegister = findViewById(R.id.radio_archeologist);
        RadioGroup radioGroup = findViewById(R.id.radio_group);

        setLoginLink();
        setContactLink();

        // Optional: Set a listener on the radio group to handle radio button selection changes
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radio_general) {
                    arcIdTxt.setVisibility(View.GONE);
                    arcIdTxt.setEnabled(false);
                    isGeneraluser = true;
                } else if (checkedId == R.id.radio_archeologist) {
                    arcIdTxt.setVisibility(View.VISIBLE);
                    arcIdTxt.setEnabled(true);
                    isGeneraluser = false;
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (areFieldsEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    if (isValidEmail(emailTxt.getText().toString())){
                        //Toast.makeText(RegisterActivity.this, "a valid email", Toast.LENGTH_SHORT).show();
                        if(isPasswordsmatch(passwordTxt.getText().toString(), confirmPasswordTxt.getText().toString())){
                            //Toast.makeText(RegisterActivity.this, "Passwords match", Toast.LENGTH_SHORT).show();
                            //registerUser();
                            registerUsersql();
                        }else{
                            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(RegisterActivity.this, "invalid email", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    private void registerUser() {

        String email = emailTxt.getText().toString();
        String password = passwordTxt.getText().toString();

        User user = new User();
        user.setEmail(email);
        user.setProfile_picture("test_img");
        user.setIs_admin(false);
        user.setPassword(password);
        user.setAdmin(null);

        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        Call<UserResponse> call = apiService.createUser(user);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    // Handle successful response
                    Log.d("API Success", "User created successfully");
                    Toast.makeText(RegisterActivity.this, "Check your Email Inbox", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else {
                    // Handle error response
                    Log.e("API Error", "Response Code: " + response.code());
                    //Toast.makeText(RegisterActivity.this, "User creation failed "+ response.code(), Toast.LENGTH_SHORT).show();
                    if(response.code() == 400){
                        Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Handle failure
                Log.e("API Failure", "Error: " + t.getMessage());
                Toast.makeText(RegisterActivity.this, "User creation failed", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void registerUsersql() {
        String fname = firstnameTxt.getText().toString();
        String lname = lastnameTxt.getText().toString();
        String email = emailTxt.getText().toString();
        String arcId = arcIdTxt.getText().toString();
        String password = passwordTxt.getText().toString();
        String profilePicture = "https://static.vecteezy.com/system/resources/thumbnails/001/840/612/small_2x/picture-profile-icon-male-icon-human-or-people-sign-and-symbol-free-vector.jpg"; // Placeholder
        boolean isAdmin = false;
        String activationLink = "https://onesandzeros.patima.api/activate/" + email; // Placeholder
        boolean activationStatus = false;

        if (!isGeneraluser) {
            if (arcId.isEmpty() || arcId.length() < 3) {
                Toast.makeText(this, "Please enter your Archeological ID", Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            arcId = "";
        }

        if(!dbHelper.getUserEmailbyEmail(email)){
            boolean result = dbHelper.addUserRecord(fname, lname, email, profilePicture, isAdmin, password, activationLink, activationStatus, arcId);

            if (result) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "User already exists", Toast.LENGTH_LONG).show();
        }


    }

    private void setLoginLink() {

        String textreg = "Already have an account ? Login";
        SpannableString spannableRegister = new SpannableString(textreg);

        int orangeColor = getResources().getColor(R.color.colorPrimary);
        int registerIndex = textreg.indexOf("Login");

        spannableRegister.setSpan(new ForegroundColorSpan(orangeColor),
                registerIndex, textreg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        NoUnderlineClickableSpan registerSpan = new NoUnderlineClickableSpan(new View.OnClickListener() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });
        spannableRegister.setSpan(registerSpan, registerIndex, spannableRegister.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        loginTxt.setText(spannableRegister);
        loginTxt.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
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
                Intent intent = new Intent(RegisterActivity.this, AdminContactActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });

        spannableContact.setSpan(contactSpan, contactIndex, spannableContact.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        contactTxt.setText(spannableContact);
        contactTxt.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
    }
    private static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }

        String emailRegex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@[\\w-]+(?:\\.[\\w-]+)*$";
        Pattern pattern = Pattern.compile(emailRegex);

        return pattern.matcher(email).matches();
    }
    private static boolean isPasswordsmatch(String password, String confirmPassword) {

        if (password == null || password.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    private boolean areFieldsEmpty() {
        return TextUtils.isEmpty(firstnameTxt.getText().toString().trim()) ||
                TextUtils.isEmpty(lastnameTxt.getText().toString().trim()) ||
                TextUtils.isEmpty(emailTxt.getText().toString().trim()) ||
                TextUtils.isEmpty(passwordTxt.getText().toString().trim()) ||
                TextUtils.isEmpty(confirmPasswordTxt.getText().toString().trim());
    }

}