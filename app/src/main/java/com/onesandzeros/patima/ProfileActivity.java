package com.onesandzeros.patima;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    EditText userFname, userLname, userEmail, userPass, userNewPass, userConfirmNewPass, userType, userAdminrights;
    Button editBtn, delBtn;
    ImageButton userPic;
    SQLiteHelper dbHelper;
    LinearLayout newpassContainer, confirmPassContainer;
    String changedpicture = null;

    boolean editRequest = false;
    private static final int PICK_IMAGE = 1;

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
        delBtn = findViewById(R.id.btn_delete);
        userPic = findViewById(R.id.user_thumb);
        newpassContainer = findViewById(R.id.New_pass);
        confirmPassContainer = findViewById(R.id.confirmNew_pass);
        userNewPass = findViewById(R.id.new_pass_text);
        userConfirmNewPass = findViewById(R.id.confirm_new_pass_text);

        initialState();

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editRequest) {
                    editRequest = true;
                    Snackbar.make(parentLayout, "Now you can edit these fields!", Snackbar.LENGTH_LONG).show();
                    editBtn.setText("Save Changes");
                    changedState();
                } else {

                    editRequest = false;

                    // Get the updated information from the input fields
                    String firstName = userFname.getText().toString().trim();
                    String lastName = userLname.getText().toString().trim();
                    String password = userPass.getText().toString();
                    String newPassword = userNewPass.getText().toString();
                    String confirmNewPassword = userConfirmNewPass.getText().toString();
                    String profilePicturePath;

                    // Update the user information in the database
                    SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
                    int userid = sharedPreferences.getInt("userId", 0);

                    // Assuming you have logic to get or update the profile picture path
                    if(changedpicture != null){
                        profilePicturePath = changedpicture;
                    }else{
                        profilePicturePath = dbHelper.getProfilepicture(userid);
                    }

                    String currentPassword = dbHelper.getUserPassword(userid);

                    if(password.isEmpty()){
                        Toast.makeText(ProfileActivity.this, "Please enter your current password to save changes", Toast.LENGTH_LONG).show();
                    }else{
                        if(password.equals(currentPassword)){
                            if(!newPassword.isEmpty()){
                                if(isPasswordsmatch(newPassword, confirmNewPassword)){
                                    boolean updateSuccess = dbHelper.updateUser(userid, firstName, lastName, profilePicturePath, newPassword);
                                    Snackbar.make(parentLayout, "Saved!", Snackbar.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(ProfileActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                boolean updateSuccess = dbHelper.updateUser(userid, firstName, lastName, profilePicturePath, password);
                                Snackbar.make(parentLayout, "Saved!", Snackbar.LENGTH_SHORT).show();
                            }
                        }else{
                            Toast.makeText(ProfileActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }

                    initialState();
                    editBtn.setText("Edit Profile");
                }
            }
        });

        userPic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setIcon(R.drawable.ic_welcome);
                builder.setMessage("You are going to delete your Account. Are you sure ?");
                builder.setTitle("   Patima");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
                    int userid = sharedPreferences.getInt("userId", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("isStarted", "no");
                    editor.apply();

                    dbHelper.deleteUser(userid, ProfileActivity.this);
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
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

    private void initialState() {
        dbHelper = new SQLiteHelper(this);

        SharedPreferences sharedPreferences = getSharedPreferences("Startup", MODE_PRIVATE);
        int userid = sharedPreferences.getInt("userId", 0);
        String userTypest = sharedPreferences.getString("userType", "");

        userFname.setEnabled(false);
        userLname.setEnabled(false);
        userEmail.setEnabled(false);
        userPass.setEnabled(false);
        userType.setEnabled(false);
        userPic.setEnabled(false);
        userAdminrights.setEnabled(false);

        userPass.setHint("***********");
        newpassContainer.setVisibility(View.GONE);
        confirmPassContainer.setVisibility(View.GONE);

        String email = dbHelper.getUserEmail(userid);
        String fullName = dbHelper.getUserName(userid);
        String profilepicturePath = dbHelper.getProfilepicture(userid);
        String adminRights = dbHelper.getUseradmintype(userid);

        String[] nameParts = fullName.split(" ");
        String firstName = nameParts[0];
        String lastName = nameParts[1];

        if (profilepicturePath.contains("http")) {
            Picasso.get()
                    .load(profilepicturePath)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(userPic);
        } else {
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
        int userid = sharedPreferences.getInt("userId", 0);
        String userTypest = sharedPreferences.getString("userType", "");

        userFname.setEnabled(true);
        userLname.setEnabled(true);
        userEmail.setEnabled(false);
        userPass.setEnabled(true);
        userType.setEnabled(false);
        userPic.setEnabled(true);
        userAdminrights.setEnabled(false);

        userPass.setHint("Enter your current password");
        newpassContainer.setVisibility(View.VISIBLE);
        confirmPassContainer.setVisibility(View.VISIBLE);

        String email = dbHelper.getUserEmail(userid);
        String fullName = dbHelper.getUserName(userid);
        String profilepicturePath = dbHelper.getProfilepicture(userid);
        String adminRights = dbHelper.getUseradmintype(userid);

        String[] nameParts = fullName.split(" ");
        String firstName = nameParts[0];
        String lastName = nameParts[1];

        if (profilepicturePath.contains("http")) {
            Picasso.get()
                    .load(profilepicturePath)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(userPic);
        } else {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                // Get the image path (if needed)
                changedpicture = selectedImage.getPath();
                changedpicture = removeRawSegment(changedpicture);
                Toast.makeText(this, changedpicture, Toast.LENGTH_LONG).show();

                // Set the selected image to the ImageButton
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    userPic.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String removeRawSegment(String path) {
        // Check if "/raw/" exists in the path
        int rawIndex = path.indexOf("/raw/");
        if (rawIndex != -1) {
            // Remove "/raw/" and return the modified path
            return path.substring(0, rawIndex) + path.substring(rawIndex + 5);
        }
        // If "/raw/" does not exist, return the original path
        return path;
    }

}
