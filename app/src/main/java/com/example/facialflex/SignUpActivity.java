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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignUpActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupUsername, signupPassword;
    TextView loginRedirectText, termsAndConditionsRedirectText;
    Button signupButton;
    FirebaseDatabase database;
    DatabaseReference reference;
    private FirebaseAuth mAuth;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        signupName = findViewById(R.id.signup_fname);
        signupEmail = findViewById(R.id.signup_em);
        signupUsername = findViewById(R.id.signup_uname);
        signupPassword = findViewById(R.id.signup_pword);
        signupButton = findViewById(R.id.signup_btn);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        termsAndConditionsRedirectText = findViewById(R.id.TermsAndConditionRedirectText);

        signupPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        signupPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_24, 0);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUsernameAndSignUp();
            }
        });

        // Handle password visibility toggle via drawableRight touch
        signupPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2; // Index for drawableRight

                if (signupPassword.getError() != null) {
                    return false; // Don't handle the visibility toggle if there's an error
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (signupPassword.getRight() - signupPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (isPasswordVisible) {
                            // Hide password
                            signupPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            signupPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_24, 0);
                        } else {
                            // Show password
                            signupPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            signupPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_visibility_off_24, 0);
                        }
                        // Move cursor to the end of the text
                        signupPassword.setSelection(signupPassword.getText().length());
                        isPasswordVisible = !isPasswordVisible;
                        return true; // Indicate that the touch event was handled
                    }
                }
                return false;
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        termsAndConditionsRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTermsAndConditionsDialog();
            }
        });
    }

    private boolean isPasswordValid(String password) {
        // Password validation pattern: at least one uppercase letter, one digit, one special symbol, and length 8-16
        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$";
        return Pattern.compile(passwordPattern).matcher(password).matches();
    }

    private void checkUsernameAndSignUp() {
        String uname = signupUsername.getText().toString().trim();
        String em = signupEmail.getText().toString().trim();
        String fname = signupName.getText().toString().trim();
        String pword = signupPassword.getText().toString().trim();

        if (fname.isEmpty()) {
            signupName.setError("Fullname cannot be empty!");
            return;
        }

        if (uname.isEmpty()) {
            signupUsername.setError("Username cannot be empty!");
            return;
        }

        if (em.isEmpty()) {
            signupEmail.setError("Email cannot be empty!");
            return;
        }

        if (pword.isEmpty()) {
            signupPassword.setError("Password cannot be empty!");
            return;
        }

        if (!isPasswordValid(pword)) {
            signupPassword.setError("Password must be 8-16 characters long, contain at least one uppercase letter, one digit, and one special symbol!");
            return;
        }

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("user");

        // Query to check if the username already exists
        Query usernameQuery = reference.orderByChild("uname").equalTo(uname);

        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Username already exists
                    signupUsername.setError("Username is already taken!");
                } else {
                    // Username does not exist, proceed with registration
                    registerUser();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SignUpActivity.this, "Error checking username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            signupEmail.setError("Email cannot be empty!");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmail.setError("Please enter a valid email address!");
            return false;
        }

        if (password.isEmpty()) {
            signupPassword.setError("Password cannot be empty!");
            return false;
        }

        if (password.length() < 6) {
            signupPassword.setError("Password must be 8-16 characters long, contain at least one uppercase letter, one digit, and one special symbol!");
            return false;
        }

        return true;
    }


    private void registerUser() {
        String fname = signupName.getText().toString();
        String em = signupEmail.getText().toString().trim();
        String pword = signupPassword.getText().toString().trim();
        String uname = signupUsername.getText().toString();

        if (validateInput(em, pword)) {
            // Call Firebase Auth to create user
            mAuth.createUserWithEmailAndPassword(em, pword)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Send verification email
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(verificationTask -> {
                                            if (verificationTask.isSuccessful()) {
                                                // Save user data to Firebase database with is_active = 0
                                                saveUserDataToDatabase(user, fname, uname, pword);

                                                Toast.makeText(SignUpActivity.this,
                                                        "Registration successful. Please check your email for verification.",
                                                        Toast.LENGTH_LONG).show();

                                                // Redirect to login page or keep on current page
                                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(SignUpActivity.this,
                                                        "Failed to send verification email.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            // Sign-up failed
                            Toast.makeText(SignUpActivity.this,
                                    "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveUserDataToDatabase(FirebaseUser user, String fname, String uname, String pword) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user");
        String userId = user.getUid();

        // Get the current date and time in the desired format
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        String uniqueKey = reference.push().getKey();
        HelperClass helperClass = new HelperClass(fname, user.getEmail(), pword, uname, currentDateTime);
        reference.child(uniqueKey).setValue(helperClass);
    }

    private void showTermsAndConditionsDialog() {
        // Create a Dialog
        Dialog dialog = new Dialog(this, R.style.BlurryDialogTheme);
        dialog.setContentView(R.layout.dialog_terms_conditions);
        dialog.setCancelable(true);

        // Set rounded background for the dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.custom_dialog);
        }

        // Find the views in the dialog
        TextView termsConditionsText = dialog.findViewById(R.id.terms_conditions_text);
        Button closeButton = dialog.findViewById(R.id.dialog_close_button);

        // Set any specific terms and conditions text if needed
        termsConditionsText.setText("Acceptance of Terms: By signing in to FacialFlex, you agree to be bound by these Terms and Conditions, which govern your use of our website and services.\n\n" +
                "Purpose of FacialFlex: FacialFlex is designed to detect the presence of Bell's Palsy based on the physical structure of the face and provide exercises to assist in its management. It is not a substitute for professional medical advice.\n\n" +
                "Accuracy of Information: While FacialFlex aims to provide accurate information and recommendations, we cannot guarantee the accuracy, completeness, or timeliness of the content provided. Users are encouraged to consult with a healthcare professional for medical advice.\n\n" +
                "Privacy: FacialFlex respects your privacy and is committed to protecting your personal information. Your data will be handled in accordance with our Privacy Policy. In compliance with the Data Privacy Act of 2012 (Republic Act No. 10173), we ensure that your personal and sensitive information is securely stored and accessed only by authorized personnel. Even after your account is deactivated, FacialFlex may retain and access your medical records for purposes necessary to comply with legal obligations, auditing, and ensuring continuity of care, in accordance with applicable laws and regulations.\n\n" +
                "User Responsibilities: Users of FacialFlex are responsible for providing accurate information about their medical history and symptoms. They should also follow the treatment & exercises provided by FacialFlex responsibly and consult with a healthcare professional if necessary.\n\n" +
                "By signing in, you acknowledge that you have read, understood, and agree to be bound by these Terms and Conditions.");
        // Set the close button click listener
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss(); // Close the dialog
            }
        });

        dialog.show(); // Show the dialog
    }
}