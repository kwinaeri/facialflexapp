package com.example.facialflex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import java.util.HashMap;
import java.util.regex.Pattern;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editName, editUsername, editEmail, editPassword, editAge, editBirthday, editAddress, editDescription;
    private Button btnSave, btnSelectImage, btnDelete;
    private ImageView profileImage;

    private DatabaseReference userRef;
    private StorageReference storageRef;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Edit Profile");
        // Enable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize fields
        editName = findViewById(R.id.edit_name);
        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        editAge = findViewById(R.id.edit_age);
        editBirthday = findViewById(R.id.edit_birthday);
        editAddress = findViewById(R.id.edit_address);
        editDescription = findViewById(R.id.edit_description);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        btnSelectImage = findViewById(R.id.btn_select_image);
        profileImage = findViewById(R.id.profile_image);

        // Retrieve user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        // Initialize Firebase Database reference using userId
        userRef = FirebaseDatabase.getInstance().getReference("user").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        // Fetch and populate the existing user data
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Populate the form fields with existing data
                    String name = dataSnapshot.child("fname").getValue(String.class);
                    String username = dataSnapshot.child("uname").getValue(String.class);
                    String email = dataSnapshot.child("em").getValue(String.class);
                    String password = dataSnapshot.child("pword").getValue(String.class);
                    String age = dataSnapshot.child("age").getValue(String.class);
                    String birthday = dataSnapshot.child("birthday").getValue(String.class);
                    String address = dataSnapshot.child("address").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profile_image").getValue(String.class);

                    // Set the retrieved data to the EditText fields
                    editName.setText(name);
                    editUsername.setText(username);
                    editEmail.setText(email);
                    editPassword.setText(password);
                    editAge.setText(age);
                    editBirthday.setText(birthday);
                    editAddress.setText(address);
                    editDescription.setText(description);
                    // Load the profile image if exists
                    if (profileImageUrl != null) {
                        Glide.with(EditProfileActivity.this).load(profileImageUrl).apply(RequestOptions.circleCropTransform()).into(profileImage);
                    }else{
                        profileImage.setImageResource(R.drawable.baseline_person_24);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
                Toast.makeText(EditProfileActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });

        // Save button onClick listener
        btnSave.setOnClickListener(v -> saveUpdatedProfile());

        // Button to select profile image
        btnSelectImage.setOnClickListener(v -> selectImage());

        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void selectImage() {
        // Open the file picker using the Storage Access Framework (SAF)
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle the result from the gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                profileImage.setImageURI(imageUri); // Set image to ImageView
                uploadImageToFirebase();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");
            UploadTask uploadTask = fileReference.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Get the URL of the uploaded image
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    // Save the image URL to Firebase (you may want to update this)
                    userRef.child("profile_image").setValue(imageUrl);
                    Toast.makeText(EditProfileActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(EditProfileActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private boolean isPasswordValid(String password) {
        // Password validation pattern: at least one uppercase letter, one digit, one special symbol, and length 8-16
        String passwordPattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$";
        return Pattern.compile(passwordPattern).matcher(password).matches();
    }

    private void saveUpdatedProfile() {
        // Retrieve the updated values from the form
        String updatedName = editName.getText().toString().trim();
        String updatedUsername = editUsername.getText().toString().trim();
        String updatedEmail = editEmail.getText().toString().trim();
        String updatedPassword = editPassword.getText().toString().trim();
        String updatedAge = editAge.getText().toString().trim();
        String updatedBirthday = editBirthday.getText().toString().trim();
        String updatedAddress = editAddress.getText().toString().trim();
        String updatedDescription = editDescription.getText().toString().trim();

        // Validate input fields
        if (updatedName.isEmpty() || updatedUsername.isEmpty() || updatedEmail.isEmpty()) {
            Toast.makeText(EditProfileActivity.this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password if the user has entered a new one
        if (!updatedPassword.isEmpty() && !isPasswordValid(updatedPassword)) {
            Toast.makeText(EditProfileActivity.this, "Password must be 8-16 characters long, include at least one uppercase letter, one digit, and one special symbol.", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        // Create a query to check for an existing username
        Query usernameQuery = FirebaseDatabase.getInstance().getReference("user")
                .orderByChild("uname").equalTo(updatedUsername);

        usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !dataSnapshot.child(userId).exists()) {
                    // Username already exists and it's not the current user
                    editUsername.setError("Username is already taken!");
                } else {
                    // Username does not exist or belongs to the current user, proceed with the update

                    // Create a HashMap to store the updated data
                    HashMap<String, Object> updatedData = new HashMap<>();
                    updatedData.put("fname", updatedName);
                    updatedData.put("uname", updatedUsername);
                    updatedData.put("em", updatedEmail);
                    updatedData.put("age", updatedAge);
                    updatedData.put("birthday", updatedBirthday);
                    updatedData.put("address", updatedAddress);
                    updatedData.put("description", updatedDescription);

                    // Only update the password if a new one is entered
                    if (!updatedPassword.isEmpty()) {
                        updatedData.put("pword", updatedPassword);
                    }

                    // Upload profile image if selected
                    if (imageUri != null) {
                        StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");
                        fileReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                updatedData.put("profile_image", imageUrl);
                                updateProfileData(updatedData);
                            });
                        });
                    } else {
                        // If no new image, just update the rest
                        updateProfileData(updatedData);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
                Toast.makeText(EditProfileActivity.this, "Error checking username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfileData(HashMap<String, Object> updatedData) {
        // Update the user data in Firebase
        userRef.updateChildren(updatedData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update SharedPreferences with the new data
                SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", updatedData.get("uname").toString());
                // Add other fields if necessary
                editor.apply();

                Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();  // Close the activity
            } else {
                Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        // Create an alert dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAccount();  // Call the deleteAccount method if user confirms
                    }
                })
                .setNegativeButton("No", null)  // Do nothing if the user clicks "No"
                .show();
    }

    private void deleteAccount() {
        // Retrieve userId from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        if (userId != null) {
            // Reference to the user's node in the Firebase Realtime Database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(userId);

            // Update the accountstatus to "deactivated"
            userRef.child("accountstatus").setValue("deactivated").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Update successful
                    Toast.makeText(EditProfileActivity.this, "User Account and Data Deleted successfully", Toast.LENGTH_SHORT).show();

                    // Clear SharedPreferences to log out the user
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    // Redirect to LoginActivity
                    Intent intent = new Intent(EditProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                    startActivity(intent);
                    finish(); // Finish the current activity
                } else {
                    // Update failed
                    Toast.makeText(EditProfileActivity.this, "Failed to deactivate account. Try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back arrow click here
        onBackPressed();
        return true;
    }

}
