package com.onesandzeros.patima;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.widget.Toast;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "patimaDB";
    private static final int DB_VERSION = 1;

    public SQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE USER (" +
                "User_Id INTEGER PRIMARY KEY," +
                "Fname TEXT," +
                "Lname TEXT," +
                "Email TEXT," +
                "Profile_Picture TEXT," +
                "is_admin BOOLEAN," +
                "Password TEXT," +
                "Activation_Link TEXT," +
                "Activation_Status BOOLEAN" +
                ")";

        String createGeneralPublicTable = "CREATE TABLE GENERAL_PUBLIC (" +
                "User_Id INTEGER PRIMARY KEY," +
                "FOREIGN KEY (User_Id) REFERENCES USER(User_Id)" +
                ")";


        String createArcheologistTable = "CREATE TABLE ARCHEOLOGIST (" +
                "User_Id INTEGER PRIMARY KEY," +
                "Archeologist_Id INTEGER," +
                "FOREIGN KEY (User_Id) REFERENCES USER(User_Id)" +
                ")";


        String createImageTable = "CREATE TABLE IMAGE (" +
                "Image_Id INTEGER PRIMARY KEY," +
                "User_Id INTEGER," +
                "Output_Image_Path TEXT," +
                "Input_Image_Path TEXT," +
                "Timestamp TEXT," +
                "FOREIGN KEY (User_Id) REFERENCES USER(User_Id)" +
                ")";


        String createFeedbackTable = "CREATE TABLE FEEDBACK (" +
                "Feedback_Id INTEGER PRIMARY KEY," +
                "Description TEXT," +
                "Ratings INTEGER," +
                "Image_Id INTEGER," +
                "User_Id INTEGER," +
                "FOREIGN KEY (User_Id) REFERENCES ARCHEOLOGIST(User_Id)," +
                "FOREIGN KEY (Image_Id) REFERENCES IMAGE(Image_Id)" +
                ")";

        String createImageTagTable = "CREATE TABLE IMAGE_TAG (" +
                "Image_Id INTEGER," +
                "Tags TEXT," +
                "PRIMARY KEY (Image_Id, Tags)," +
                "FOREIGN KEY (Image_Id) REFERENCES IMAGE(Image_Id)" +
                ")";

        String createContactAdminMessageTable = "CREATE TABLE ContactAdminMessage (" +
                "Message_Id INTEGER PRIMARY KEY," +
                "Message TEXT," +
                "Email TEXT," +
                "Name TEXT," +
                "Check_Status BOOLEAN" +
                ")";

        db.execSQL(createUserTable);
        db.execSQL(createGeneralPublicTable);
        db.execSQL(createArcheologistTable);
        db.execSQL(createImageTable);
        db.execSQL(createFeedbackTable);
        db.execSQL(createImageTagTable);
        db.execSQL(createContactAdminMessageTable);

        // Insert initial records
        insertInitialRecords(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS USER");
        db.execSQL("DROP TABLE IF EXISTS GENERAL_PUBLIC");
        db.execSQL("DROP TABLE IF EXISTS ARCHEOLOGIST");
        db.execSQL("DROP TABLE IF EXISTS IMAGE");
        db.execSQL("DROP TABLE IF EXISTS FEEDBACK");
        db.execSQL("DROP TABLE IF EXISTS IMAGE_TAG");
        db.execSQL("DROP TABLE IF EXISTS ContactAdminMessage");
        onCreate(db);
    }

    private void insertInitialRecords(SQLiteDatabase db) {
        // Add initial records to USER table (removed_adminid)
        addUser(db, 1, "Kasun", "Perera", "kasunperera101@gmail.com", "https://wallpapersmug.com/download/320x240/484769/colorful-waves-digital-art.jpg", true, "password", "preloadeduser", true);
        addUser(db, 2, "Thushara", "Deegalla", "thush455@gmail.com", "https://wallpapersmug.com/download/320x240/484769/colorful-waves-digital-art.jpg", false, "password", "preloadeduser", true);
        addUser(db, 3, "Charana", "Gamage","charanagamage@gmail.com", "https://wallpapersmug.com/download/320x240/484769/colorful-waves-digital-art.jpg", false, "password", "preloadeduser", true);
        addUser(db, 4, "Hasith", "Ranasinghe", "hasi878@gmail.com", "https://wallpapersmug.com/download/320x240/484769/colorful-waves-digital-art.jpg", false, "password", "preloadeduser", true);
        addUser(db, 5, "Sithu", "Bhagya", "sithusithu@gmail.com", "https://wallpapersmug.com/download/320x240/484769/colorful-waves-digital-art.jpg", false, "password", "preloadeduser", true);
        addUser(db, 6, "Amaa", "Chethana", "amaache44@gmail.com", "https://wallpapersmug.com/download/320x240/484769/colorful-waves-digital-art.jpg", false, "password", "preloadeduser", true);
        addUser(db, 7, "Yasith", "Weerasinghe", "yasithweerasinha@gmail.com", "https://wallpapersmug.com/download/320x240/484769/colorful-waves-digital-art.jpg", false, "password", "preloadeduser", true);

        // Add initial records to GENERAL_PUBLIC table (removed imageid)
        addGeneralPublic(db, 2);
        addGeneralPublic(db, 3);
        addGeneralPublic(db, 4);

        // Add initial records to ARCHEOLOGIST table (removed imageid, feedbackid)
        addArcheologist(db, 5, 101);
        addArcheologist(db, 6, 102);
        addArcheologist(db, 7, 103);

        // Add initial records to IMAGE table (added userid)
        addImage(db, 1, 2,"https://i.pinimg.com/736x/65/20/70/652070abf6c84fbeb26f84f26127e3a4.jpg", "https://i.pinimg.com/1200x/3c/8a/3a/3c8a3a0e86a93652c6a3fd4e46b94a18.jpg", "2024-06-12 12:00:00");
        addImage(db, 2, 3,"https://i.pinimg.com/736x/65/20/70/652070abf6c84fbeb26f84f26127e3a4.jpg", "https://i.pinimg.com/1200x/3c/8a/3a/3c8a3a0e86a93652c6a3fd4e46b94a18.jpg", "2024-06-12 13:00:00");
        addImage(db, 3, 4,"https://i.pinimg.com/736x/65/20/70/652070abf6c84fbeb26f84f26127e3a4.jpg", "https://i.pinimg.com/1200x/3c/8a/3a/3c8a3a0e86a93652c6a3fd4e46b94a18.jpg", "2024-06-12 14:00:00");
        addImage(db, 4, 5,"https://i.pinimg.com/736x/65/20/70/652070abf6c84fbeb26f84f26127e3a4.jpg", "https://i.pinimg.com/1200x/3c/8a/3a/3c8a3a0e86a93652c6a3fd4e46b94a18.jpg", "2024-06-12 15:00:00");
        addImage(db, 5, 6,"https://i.pinimg.com/736x/65/20/70/652070abf6c84fbeb26f84f26127e3a4.jpg", "https://i.pinimg.com/1200x/3c/8a/3a/3c8a3a0e86a93652c6a3fd4e46b94a18.jpg", "2024-06-12 16:00:00");
        addImage(db, 6, 7,"https://i.pinimg.com/736x/65/20/70/652070abf6c84fbeb26f84f26127e3a4.jpg", "https://i.pinimg.com/1200x/3c/8a/3a/3c8a3a0e86a93652c6a3fd4e46b94a18.jpg", "2024-06-12 17:00:00");

        // Add initial records to FEEDBACK table
        addFeedback(db, 1, "Very good image generation$$Somewhat well$$Neutral$$Not applicable", 5, 4,5);
        addFeedback(db, 2, "I expected much more$$Somewhat well$$Neutral$$Not applicable", 3, 5,6);
        addFeedback(db, 3, "Its good for your level$$Somewhat well$$Neutral$$Not applicable", 5, 6,7);

        // Add initial records to IMAGE_TAG table (locations)
        addImageTag(db, 1, "8.3114, 80.4037");
        addImageTag(db, 2, "8.3114, 80.4037");
        addImageTag(db, 3, "8.3114, 80.4037");
        addImageTag(db, 4, "8.3114, 80.4037");
        addImageTag(db, 5, "8.3114, 80.4037");
        addImageTag(db, 6, "8.3114, 80.4037");

        // Add initial records to ContactAdminMessage table
        addContactAdminMessage(db, 1, "Need help with account", "nimalwanasinghe@gmail.com", "Nimal Wanasinghe", false);
        addContactAdminMessage(db, 2, "Bug report", "supun23444@gmail.com", "Supun", true);
        addContactAdminMessage(db, 3, "Feature request", "sithara22@example.com", "Sithara Perera", false);
    }

    private void addUser(SQLiteDatabase db, int userId, String fname, String lname, String email, String profilePicture, boolean isAdmin, String password, String activationLink, boolean activationStatus) {
        ContentValues values = new ContentValues();
        values.put("User_Id", userId);
        values.put("Fname", fname);
        values.put("Lname", lname);
        values.put("Email", email);
        values.put("Profile_Picture", profilePicture);
        values.put("is_admin", isAdmin);
        values.put("Password", password);
        values.put("Activation_Link", activationLink);
        values.put("Activation_Status", activationStatus);
        db.insert("USER", null, values);
    }

    private void addGeneralPublic(SQLiteDatabase db, int userId) {
        ContentValues values = new ContentValues();
        values.put("User_Id", userId);
        db.insert("GENERAL_PUBLIC", null, values);
    }

    private void addArcheologist(SQLiteDatabase db, int userId, int archeologistId) {
        ContentValues values = new ContentValues();
        values.put("User_Id", userId);
        values.put("Archeologist_Id", archeologistId);
        db.insert("ARCHEOLOGIST", null, values);
    }

    private void addImage(SQLiteDatabase db, int imageId, int userId, String outputImagePath, String inputImagePath, String timestamp) {
        ContentValues values = new ContentValues();
        values.put("Image_Id", imageId);
        values.put("User_Id", userId);
        values.put("Output_Image_Path", outputImagePath);
        values.put("Input_Image_Path", inputImagePath);
        values.put("Timestamp", timestamp);
        db.insert("IMAGE", null, values);
    }

    private void addFeedback(SQLiteDatabase db, int feedbackId, String description, int ratings, int imageId, int userId) {
        ContentValues values = new ContentValues();
        values.put("Feedback_Id", feedbackId);
        values.put("Description", description);
        values.put("Ratings", ratings);
        values.put("Image_Id", imageId);
        values.put("User_Id", userId);
        db.insert("FEEDBACK", null, values);
    }

    private void addImageTag(SQLiteDatabase db, int imageId, String tags) {
        ContentValues values = new ContentValues();
        values.put("Image_Id", imageId);
        values.put("Tags", tags);
        db.insert("IMAGE_TAG", null, values);
    }

    private void addContactAdminMessage(SQLiteDatabase db, int messageId, String message, String email, String name, boolean checkStatus) {
        ContentValues values = new ContentValues();
        values.put("Message_Id", messageId);
        values.put("Message", message);
        values.put("Email", email);
        values.put("Name", name);
        values.put("Check_Status", checkStatus);
        db.insert("ContactAdminMessage", null, values);
    }

    public Cursor getUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM USER WHERE Email = ? AND Password = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
        return cursor;
    }

    public String getUserEmail(int userid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String email = null;
        String query = "SELECT Email FROM USER WHERE User_Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userid)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndex("Email"));  // Ensure the case matches the table definition
            }
            cursor.close();
        }
        db.close();
        return email;
    }

    public String getUserPassword(int userid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String password = null;
        String query = "SELECT Password FROM USER WHERE User_Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userid)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                password = cursor.getString(cursor.getColumnIndex("Password"));  // Ensure the case matches the table definition
            }
            cursor.close();
        }
        db.close();
        return password;
    }

    public String getUserName(int userid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String full_name = null;
        String query = "SELECT Fname, Lname FROM USER WHERE User_Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userid)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String fname = cursor.getString(cursor.getColumnIndex("Fname"));
                String lname = cursor.getString(cursor.getColumnIndex("Lname"));  // Ensure the case matches the table definition
                full_name = fname + " " + lname;
            }
            cursor.close();
        }
        db.close();
        return full_name;
    }

    public String getUseradmintype(int userid) {
        SQLiteDatabase db = this.getReadableDatabase();
        int isAdmin = 0;
        String query = "SELECT is_admin FROM USER WHERE User_Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userid)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                isAdmin = cursor.getInt(cursor.getColumnIndex("is_admin"));
            }
            cursor.close();
        }
        db.close();
        if (isAdmin == 1) {
            return "Yes";
        } else {
            return "No";
        }

    }

    public String getProfilepicture(int userid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String path = null;
        String query = "SELECT Profile_Picture FROM USER WHERE User_Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userid)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex("Profile_Picture"));

            }
            cursor.close();
        }
        db.close();
        return path;
    }

    public void deleteUser(int userId, Context context) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "User_Id = ?";
        String[] whereArgs = {String.valueOf(userId)};

        // Perform the delete operation
        int rowsAffected = db.delete("USER", whereClause, whereArgs);

        // Close the database
        db.close();

        // Show a toast message
        if (rowsAffected > 0) {
            Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
        }
    }



    public String getOutputImagepath(int imageid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String path = null;
        String query = "SELECT Output_Image_Path FROM IMAGE WHERE Image_Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(imageid)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex("Output_Image_Path"));

            }
            cursor.close();
        }
        db.close();
        return path;
    }

    public String getInputImagepath(int imageid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String path = null;
        String query = "SELECT Input_Image_Path FROM IMAGE WHERE Image_Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(imageid)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex("Input_Image_Path"));

            }
            cursor.close();
        }
        db.close();
        return path;
    }

    public String getImagetag(int imgid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String location_info = null;
        String query = "SELECT Tags FROM IMAGE_TAG WHERE Image_Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(imgid)});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                location_info = cursor.getString(cursor.getColumnIndex("Tags"));
            }
            cursor.close();
        }
        db.close();
        return location_info;
    }


    public boolean isUserGeneralPublic(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM GENERAL_PUBLIC WHERE User_Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean isUserArcheologist(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM ARCHEOLOGIST WHERE User_Id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    public boolean addUserRecord(String fname, String lname, String email, String profilePicture, boolean isAdmin, String password, String activationLink, boolean activationStatus, String arcId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        long userId = -1;
        try {
            ContentValues values = new ContentValues();
            values.put("Fname", fname);
            values.put("Lname", lname);
            values.put("Email", email);
            values.put("Profile_Picture", profilePicture);
            values.put("is_admin", isAdmin);
            values.put("Password", password);
            values.put("Activation_Link", activationLink);
            values.put("Activation_Status", activationStatus);
            userId = db.insert("USER", null, values);

            if (userId == -1) {
                return false;
            }

            if (!TextUtils.isEmpty(arcId)) {
                ContentValues arcValues = new ContentValues();
                arcValues.put("User_Id", userId);
                arcValues.put("Archeologist_Id", Integer.parseInt(arcId));
                db.insert("ARCHEOLOGIST", null, arcValues);
            } else {
                ContentValues genPubValues = new ContentValues();
                genPubValues.put("User_Id", userId);
                db.insert("GENERAL_PUBLIC", null, genPubValues);
            }

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            return false;
        } finally {
            db.endTransaction();
            // Send activation email outside of the transaction
            if (userId != -1) {
                try {
                    MailSender mailSender = new MailSender();
                    mailSender.sendMail(email,"Activate Patima account", "Click the link to activate your account\n\n" + activationLink);
                } catch (Exception e) {
                    e.printStackTrace(); // Log the exception
                    // Optionally, handle the case where the email fails to send
                }
            }
        }
    }

    public void activateAccount(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Activation_Status", 1);

        String selection = "Email = ?";
        String[] selectionArgs = { String.valueOf(email) };

        db.update("USER", values, selection, selectionArgs);
    }

    public int forgotPassword(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Password", "patimauser");

        String selection = "Email = ?";
        String[] selectionArgs = { String.valueOf(email) };

        int status = db.update("USER", values, selection, selectionArgs);

        if (status > 0) {
            // Rows updated successfully
            MailSender mailSender = new MailSender();
            mailSender.sendMail(email, "Reset Password - Patima account", "Your password change request is accepted.\n\nYour new password is: patimauser");
            return 1;
        } else {
            // No rows updated (email not found)
            return -1;
        }
    }

    public boolean updateUser(int userId, String firstName, String lastName, String profilePicturePath, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Fname", firstName);
        values.put("Lname", lastName);
        values.put("Profile_Picture", profilePicturePath);
        values.put("Password", password); // Assuming you need to update the password too

        // Define the criteria for the row(s) to update
        String selection = "User_Id = ?";
        String[] selectionArgs = { String.valueOf(userId) };

        // Update the row(s)
        int count = db.update("USER", values, selection, selectionArgs);
        return count > 0; // Return true if at least one row was updated
    }


    public int addImage(int userId, String outputImagePath, String inputImagePath, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("User_Id", userId);
        values.put("Output_Image_Path", outputImagePath);
        values.put("Input_Image_Path", inputImagePath);
        values.put("Timestamp", timestamp);

        // Insert the row into the IMAGE table
        long imageId = db.insert("IMAGE", null, values);

        // Check if insertion was successful
        if (imageId == -1) {
            // Insertion failed
            return -1;
        } else {
            // Insertion successful, return the imageId as int
            return (int) imageId;
        }
    }

    public void addImageTag(int imageId, String tags) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Image_Id", imageId);
        values.put("Tags", tags);
        db.insert("IMAGE_TAG", null, values);
    }

    public String addFeedback(int imageId, String desc, int rating, int userid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Image_Id", imageId);
        values.put("Description", desc);
        values.put("Ratings", rating);
        values.put("User_Id", userid);

        // Insert the row into the IMAGE table
        long feedbackId = db.insert("FEEDBACK", null, values);

        // Check if insertion was successful
        if (feedbackId == -1) {
            // Insertion failed
            return "Failed";
        } else {
            // Insertion successful, return the imageId as int
            return "Feedback Saved!";
        }
    }

    public String addContactAdminMessage(String msg, String email, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Message", msg);
        values.put("Email", email);
        values.put("Name", name);

        // Insert the row into the IMAGE table
        long msgId = db.insert("ContactAdminMessage", null, values);

        // Check if insertion was successful
        if (msgId == -1) {
            // Insertion failed
            return "Failed";
        } else {
            // Insertion successful, return the imageId as int
            return "Message Saved!";
        }
    }
}
