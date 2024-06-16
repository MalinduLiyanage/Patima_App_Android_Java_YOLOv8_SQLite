package com.onesandzeros.patima;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.regex.Pattern;

public class ProfileActivity extends AppCompatActivity {

    EditText userFname, userLname, userEmail, userPass, userType, userAdminrights;
    Button editBtn;
    ImageButton userPic;
    SQLiteHelper dbHelper;

    boolean editRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new SQLiteHelper(this);
        View parentLayout = findViewById(android.R.id.content);

        userFname = findViewById(R.id.fname_text);
        userLname = findViewById(R.id.lastname_text);
        userEmail = findViewById(R.id.email_text);
        userPass = findViewById(R.id.pass_text);
        userType = findViewById(R.id.usertype_text);
        userAdminrights = findViewById(R.id.admin_text);
        editBtn = findViewById(R.id.btn_edit);
        userPic = findViewById(R.id.user_thumb);

        initialState();

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editRequest){
                    editRequest = true;
                    Snackbar.make(parentLayout, "Now you can edit these fields!", Snackbar.LENGTH_LONG).show();
                    editBtn.setText("Save Changes");
                    changedState();
                }else{
                    editRequest = false;
                    initialState();
                    editBtn.setText("Edit Profile");
                }
            }
        });



    }

    private void initialState() {

        dbHelper = new SQLiteHelper(this);

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        int userid = sharedPreferences.getInt("userId",0);
        String userTypest = sharedPreferences.getString("userType","");

        userFname.setEnabled(false);
        userLname.setEnabled(false);
        userEmail.setEnabled(false);
        userPass.setEnabled(false);
        userType.setEnabled(false);
        userAdminrights.setEnabled(false);

        String email = dbHelper.getUserEmail(userid);
        String fullName = dbHelper.getUserName(userid);
        String profilepicturePath = dbHelper.getProfilepicture(userid);
        String adminRights = dbHelper.getUseradmintype(userid);

        String[] nameParts = fullName.split(" ");
        String firstName = nameParts[0];
        String lastName = nameParts[1];

        if(profilepicturePath.contains("http")){
            Picasso.get()
                    .load(profilepicturePath)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(userPic);
        }else{
            File imageFile = new File(profilepicturePath);
            Picasso.get()
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(userPic);
        }
        userFname.setText(firstName);
        userLname.setText(lastName);
        userEmail.setText(email);
        userType.setText(userTypest);
        userAdminrights.setText(adminRights);
    }

    private void changedState() {

        dbHelper = new SQLiteHelper(this);

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        int userid = sharedPreferences.getInt("userId",0);
        String userTypest = sharedPreferences.getString("userType","");

        userFname.setEnabled(true);
        userLname.setEnabled(true);
        userEmail.setEnabled(false);
        userPass.setEnabled(false);
        userType.setEnabled(false);
        userAdminrights.setEnabled(false);

        String email = dbHelper.getUserEmail(userid);
        String fullName = dbHelper.getUserName(userid);
        String profilepicturePath = dbHelper.getProfilepicture(userid);
        String adminRights = dbHelper.getUseradmintype(userid);

        String[] nameParts = fullName.split(" ");
        String firstName = nameParts[0];
        String lastName = nameParts[1];

        if(profilepicturePath.contains("http")){
            Picasso.get()
                    .load(profilepicturePath)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(userPic);
        }else{
            File imageFile = new File(profilepicturePath);
            Picasso.get()
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(userPic);
        }
        userFname.setText(firstName);
        userLname.setText(lastName);
        userEmail.setText(email);
        userType.setText(userTypest);
        userAdminrights.setText(adminRights);
    }

    private static boolean isPasswordsmatch(String password, String confirmPassword) {

        if (password == null || password.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    private boolean areFieldsEmpty() {
        return TextUtils.isEmpty(userFname.getText().toString().trim()) ||
                TextUtils.isEmpty(userLname.getText().toString().trim()) ||
                TextUtils.isEmpty(userPass.getText().toString().trim());
    }
}