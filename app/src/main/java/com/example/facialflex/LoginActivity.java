package com.example.facialflex;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import android.os.AsyncTask;
import android.util.Log;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyAppPreferences";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USERNAME = "username";

    EditText loginUsername, loginPassword;
    Button loginButton;
    TextView signupRedirectText, forgotPasswordButton;
    boolean isPasswordVisible = false;

    private static final String TAG = "EmailSender";

    // SMTP Server Info (Using Gmail in this case)
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String EMAIL_FROM = "facialflex.cpt@gmail.com";  // Your email address
    private static final String EMAIL_PASSWORD = "tzow kdpn tiso iffc";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUsername = findViewById(R.id.login_uname);
        loginPassword = findViewById(R.id.login_pword);
        loginButton = findViewById(R.id.login_btn);
        signupRedirectText = findViewById(R.id.signupRedirectText);

        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Check if already logged in
        if (isLoggedIn()) {

        }


        // Handle password visibility toggle via drawableRight touch
        loginPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2; // Index for drawableRight

                if (loginPassword.getError() != null) {
                    return false; // Don't handle the visibility toggle if there's an error
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (loginPassword.getRight() - loginPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (isPasswordVisible) {
                            // Hide password
                            loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            loginPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_24, 0);
                        } else {
                            // Show password
                            loginPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            loginPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_off_24, 0);
                        }
                        // Move cursor to the end of the text
                        loginPassword.setSelection(loginPassword.getText().length());
                        isPasswordVisible = !isPasswordVisible;
                        return true; // Indicate that the touch event was handled
                    }
                }
                return false;
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = loginUsername.getText().toString();

                if (!validateUsername() | !validatePassword()) {

                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(KEY_LOGGED_IN, true);
                    editor.putString(KEY_USERNAME, username); // Store username
                    editor.apply();
                    checkUser();
                }
            }
        });

        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPasswordResetDialog();
            }
        });

    }

    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    private void showPasswordResetDialog() {
        Dialog dialog = new Dialog(LoginActivity.this, R.style.BlurryDialogTheme);
        dialog.setContentView(R.layout.dialog_reset_password); // Create a dialog layout for inputting the email

        // Set rounded background for the dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog);
        }

        EditText resetEmail = dialog.findViewById(R.id.resetEmailEditText);
        Button resetButton = dialog.findViewById(R.id.resetPasswordButton);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = resetEmail.getText().toString().trim();
                if (!email.isEmpty()) {
                    queryUserDatabaseAndSendEmail(email);
                    dialog.dismiss();
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void queryUserDatabaseAndSendEmail(String email) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
        Query query = reference.orderByChild("em").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String password = userSnapshot.child("pword").getValue(String.class);

                        if (password != null) {
                            sendEmailWithPassword(email, password);
                            Toast.makeText(LoginActivity.this, "Password sent to your email.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Password not found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Email not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Send the email with the password
    public static void sendEmailWithPassword(final String email, final String password) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    // Set email properties
                    Properties properties = new Properties();
                    properties.put("mail.smtp.host", SMTP_HOST);
                    properties.put("mail.smtp.port", SMTP_PORT);
                    properties.put("mail.smtp.auth", "true");
                    properties.put("mail.smtp.starttls.enable", "true");

                    // Set up the session
                    Session session = Session.getInstance(properties, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                        }
                    });

                    // Compose the email
                    String subject = "Your FacialFlex Password";
                    String message = "Hello,\n\nYour password is: " + password + "\n\nThanks,\nFacialFlex Team";

                    Message mimeMessage = new MimeMessage(session);
                    mimeMessage.setFrom(new InternetAddress(EMAIL_FROM));
                    mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
                    mimeMessage.setSubject(subject);
                    mimeMessage.setText(message);

                    // Send the email
                    Transport.send(mimeMessage);
                    Log.d(TAG, "Email sent successfully!");
                } catch (MessagingException e) {
                    Log.e(TAG, "Error sending email: " + e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    public Boolean validateUsername() {
        String val = loginUsername.getText().toString();
        if (val.isEmpty()) {
            loginUsername.setError("Username cannot be empty!");
            return false;
        } else {
            loginUsername.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String val = loginPassword.getText().toString();
        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty!");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    public void saveLoginLog(String username, String status) {
        // Get the current date and time
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Reference to the userlogs table in Firebase
        DatabaseReference logsReference = FirebaseDatabase.getInstance().getReference("userlogs");

        // Create a unique key for each log entry
        String logId = logsReference.push().getKey();

        // Create a log entry
        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("datetime", dateTime);
        logEntry.put("status", status);
        logEntry.put("username", username);
        logEntry.put("role", "patient");


        // Save the log entry to Firebase
        if (logId != null) {
            logsReference.child(logId).setValue(logEntry)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Log saved successfully
                            } else {
                                // Log saving failed
                            }
                        }
                    });
        }
    }


    public void checkUser() {
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
        Query checkUserDatabase = reference.orderByChild("uname").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    loginUsername.setError(null);

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String passwordFromDB = userSnapshot.child("pword").getValue(String.class);
                        String roleFromDB = userSnapshot.child("role").getValue(String.class);
                        String accountStatusFromDB = userSnapshot.child("accountstatus").getValue(String.class);

                        if ("patient".equals(roleFromDB)) {
                            if ("activated".equals(accountStatusFromDB)) {
                                if (passwordFromDB != null && passwordFromDB.equals(userPassword)) {
                                    loginUsername.setError(null);

                                    // Save login session and the userKey (Firebase unique key)
                                    String userKey = userSnapshot.getKey();  // Firebase user unique key

                                    // Save login session
                                    SharedPreferences prefs = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putString("username", userUsername);
                                    editor.putString("userkey", userKey);  // Save the userKey
                                    editor.putBoolean("isLoggedIn", true);
                                    editor.apply();

                                    saveLoginLog(userUsername, "Logged In");

                                    String nameFromDB = userSnapshot.child("fname").getValue(String.class);
                                    String emailFromDB = userSnapshot.child("em").getValue(String.class);
                                    String usernameFromDB = userSnapshot.child("uname").getValue(String.class);

                                    // Intent to MainActivity
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("fname", nameFromDB);
                                    intent.putExtra("em", emailFromDB);
                                    intent.putExtra("uname", usernameFromDB);
                                    intent.putExtra("pword", passwordFromDB);

                                    startActivity(intent);
                                    finish(); // Close LoginActivity so the user can't go back to it

                                    // Check and update the category_list RESET DONECOUNTER
                                    String userId = userSnapshot.getKey(); // Get the user's unique ID
                                    DatabaseReference categoryListRef = FirebaseDatabase.getInstance().getReference("user").child(userId).child("category_list");

                                    categoryListRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                                            if (categorySnapshot.exists()) {
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                                String todayString = sdf.format(new Date()); // Get today's date as string

                                                for (DataSnapshot category : categorySnapshot.getChildren()) {
                                                    String lastVisitDateStr = category.child("last_visit_date").getValue(String.class);
                                                    int doneCounter = category.child("donecounter").getValue(Integer.class);

                                                    // Check if last_visit_date exists and is not equal to today's date
                                                    if (lastVisitDateStr != null && !lastVisitDateStr.equals(todayString)) {
                                                        // Reset donecounter to 0
                                                        doneCounter = 0;

                                                        // Update the last_visit_date to today's date
                                                        lastVisitDateStr = todayString;

                                                        // Update in Firebase
                                                        category.getRef().child("donecounter").setValue(doneCounter);
                                                        category.getRef().child("last_visit_date").setValue(lastVisitDateStr); // Store date as string
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // Handle potential errors here
                                        }
                                    });
                                } else {
                                    loginPassword.setError("Invalid Credentials!");
                                    loginPassword.requestFocus();
                                }
                            }else{
                                loginUsername.setError("Account already deactivated! Please Contact Support.");
                                loginUsername.requestFocus();
                            }
                        } else {
                            loginUsername.setError("Admins are not allowed to log in to this app! Please access on a Web Browser");
                            loginUsername.requestFocus();
                        }
                    }
                } else {
                    loginUsername.setError("User does not exist!");
                    loginUsername.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors here
            }
        });
    }
}