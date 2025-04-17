package com.example.facialflex;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.animation.ObjectAnimator;

import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Size;
import nl.dionsegijn.konfetti.models.Shape;


public class VideoPlaylistActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private VideoPagerAdapter videoPagerAdapter;
    private List<VideoModel> videoList = new ArrayList<>();
    private String categoryName;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private String currentPhotoPath;

    private KonfettiView konfettiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_playlist);

        konfettiView = findViewById(R.id.konfettiView);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Treatment & Exercises");
        // Enable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        // Get categoryname from the intent
        categoryName = getIntent().getStringExtra("CATEGORY_NAME");

        // Initialize adapter
        videoPagerAdapter = new VideoPagerAdapter(this, videoList);
        viewPager.setAdapter(videoPagerAdapter);

        // Set up TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText("#" + (position + 1))
        ).attach();

        videoPagerAdapter.setOnFinishRestRunnable(() -> {
            int nextItem = viewPager.getCurrentItem() + 1;
            if (nextItem < videoPagerAdapter.getItemCount()) {
                viewPager.setCurrentItem(nextItem, true);
            } else {
                // Last video, show the Done button
                showDoneButton();
            }
        });

        // Fetch videos from Firebase
        fetchVideos(categoryName);
    }

    private void showConfetti() {
        konfettiView.build()
                .addColors(Color.RED, Color.GREEN, Color.BLUE)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.Square.INSTANCE, Shape.Circle.INSTANCE)
                .addSizes(new Size(8, 5f))
                .burst(500); // Number of particles
    }

    private void showDoneButton() {
        // Show the Done button when the last video is finished
        Button doneButton = findViewById(R.id.done_button);
        doneButton.setVisibility(View.VISIBLE);
        doneButton.setOnClickListener(v -> saveExerciseCompletion());

        showConfetti();

        View blurry_background = findViewById(R.id.blurry_background);
        blurry_background.setVisibility(View.VISIBLE);

        LinearLayout bg_show = findViewById(R.id.bg_show);
        bg_show.setVisibility(View.VISIBLE);

        ImageView img_show = findViewById(R.id.img_show);
        img_show.setVisibility(View.VISIBLE);

        TextView txt_show = findViewById(R.id.txt_show);
        txt_show.setVisibility(View.VISIBLE);

        ObjectAnimator animator = ObjectAnimator.ofFloat(img_show, "translationY", 0f, 20f); // Change 20f to adjust movement distance
        animator.setDuration(500); // Duration of the animation
        animator.setRepeatCount(ObjectAnimator.INFINITE); // Repeat infinitely
        animator.setRepeatMode(ObjectAnimator.REVERSE); // Reverse animation when it reaches the end
        animator.start();

    }

    private void saveExerciseCompletion() {
        // Get logged-in user's unique key from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username != null) {
            // Query Firebase to find the user's unique key
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
            Query query = reference.orderByChild("uname").equalTo(username);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String userKey = userSnapshot.getKey();
                            updateDoneCounter(userKey, categoryName);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle possible errors
                    Toast.makeText(VideoPlaylistActivity.this, "Error updating counter: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateDoneCounter(String userKey, String categoryName) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(userKey);
        DatabaseReference categoryRef = userRef.child("category_list").child(categoryName);

        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lastVisitDate = dataSnapshot.child("last_visit_date").getValue(String.class);
                int doneCounter = 0; // Default to 0

                // Get today's date
                String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(new Date());

                if (todayDate.equals(lastVisitDate)) {
                    // If the last visit date is today, increment the counter
                    doneCounter = dataSnapshot.child("donecounter").getValue(Integer.class) != null ?
                            dataSnapshot.child("donecounter").getValue(Integer.class) : 0;
                    doneCounter += 1;
                } else {
                    // If it's a new day, reset the counter to 1
                    doneCounter = 1;
                }

                // Update donecounter and last_visit_date
                Map<String, Object> updates = new HashMap<>();
                updates.put("donecounter", doneCounter);
                updates.put("last_visit_date", todayDate);

                categoryRef.updateChildren(updates);

                // Check and update treat_start if necessary
                userRef.child("treat_start").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot treatStartSnapshot) {
                        String treatStart = treatStartSnapshot.getValue(String.class);
                        if (treatStart == null || treatStart.equals("NA")) {
                            // If treat_start is not set or is "NA", set it to today's date
                            userRef.child("treat_start").setValue(todayDate);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors
                        Toast.makeText(VideoPlaylistActivity.this, "Error updating treat_start: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                // Log the action in the adherence table
                DatabaseReference adherenceRef = userRef.child("adherence").push();
                adherenceRef.setValue(new AdherenceLog(categoryName, todayDate, currentTime));

                // Show appropriate modal message based on donecounter
                showDoneModal(doneCounter);

                Toast.makeText(VideoPlaylistActivity.this, "Exercise completed and saved!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
                Toast.makeText(VideoPlaylistActivity.this, "Error updating data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDoneModal(int doneCounter) {
        String message;
        if (doneCounter < 3) {
            message = "You're " + doneCounter + " / 3 DONE! You need to do this exercise 3 times a day.";
        } else if (doneCounter == 3) {
            message = "You're done with this exercise! Please take a pic.";
            // Redirect to GalleryPageFragment after dismissing modal
            // Here, you would write code to navigate to GalleryPageFragment.
        } else {
            message = "You've done this exercise " + doneCounter + " times! Please take a rest.";
        }

        // Display the modal with the appropriate message
        // You can implement a custom dialog or use AlertDialog for this.
        showModal("Exercise Progress", message);
    }

    private void showModal(String title, String message) {
        // Custom method to show a modal dialog with the title and message.
        // You can implement this using AlertDialog or a custom dialog.
        // For example, using AlertDialog:
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
//                    Intent intent = new Intent(VideoPlaylistActivity.this, GalleryActivity.class);
//                    startActivity(intent); // Start the new activity with the GalleryFragment

                    Intent intent = new Intent(VideoPlaylistActivity.this, MainActivity.class);
                    // Pass the fragment name to show the GalleryFragment
                    intent.putExtra("fragment", "ExercisesHistory");
                    // Start MainActivity, which will display the GalleryFragment
                    startActivity(intent);
                    // Optionally, if you want to finish VideoPlaylistActivity (if it's no longer needed)
                    finish();
                })
                .setNegativeButton("Take a Pic", (dialog, which) -> {
                    dialog.dismiss();
                    checkCameraPermissionAndOpenCamera();
                })
                .show();

    }

    private void checkCameraPermissionAndOpenCamera() {
        // Check if the camera permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take a picture", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create a file to save the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error creating file for image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get logged-in user's unique key from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
            String username = prefs.getString("username", null);

            if (username != null) {
                // Query Firebase to find the user's unique key
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
                Query query = reference.orderByChild("uname").equalTo(username);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String userKey = userSnapshot.getKey();
                                uploadImageToFirebaseStorage(userKey);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors
                        Toast.makeText(VideoPlaylistActivity.this, "Error finding user: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Intent intent = new Intent(VideoPlaylistActivity.this, MainActivity.class);
            // Pass the fragment name to show the GalleryFragment
            intent.putExtra("fragment", "Gallery");
            // Start MainActivity, which will display the GalleryFragment
            startActivity(intent);
            // Optionally, if you want to finish VideoPlaylistActivity (if it's no longer needed)
            finish();

        } else {
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebaseStorage(String userKey) {
        // Upload image to Firebase Storage
        if (currentPhotoPath != null) {
            File file = new File(currentPhotoPath);
            Uri fileUri = Uri.fromFile(file);

            String timeStamp = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault())
                    .format(new Date());

            String storagePath = "monitoring/" + file.getName();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(storagePath);
            UploadTask uploadTask = storageRef.putFile(fileUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Get the download URL and save it in the database
                saveImageMetadataToDatabase(userKey, storagePath, timeStamp, uri.toString());
                Toast.makeText(VideoPlaylistActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            })).addOnFailureListener(e -> {
                Toast.makeText(VideoPlaylistActivity.this, "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveImageMetadataToDatabase(String userKey, String path, String timestamp, String url) {
        DatabaseReference monitoringRef = FirebaseDatabase.getInstance().getReference("user").child(userKey).child("monitoring");

        Map<String, Object> data = new HashMap<>();
        data.put("path", path);
        data.put("timestamp", timestamp);
        data.put("url", url);

        monitoringRef.push().setValue(data);
    }


    // AdherenceLog class to represent the log entry
    public static class AdherenceLog {
        public String category;
        public String date;
        public String time;

        public AdherenceLog(String category, String date, String time) {
            this.category = category;
            this.date = date;
            this.time = time;
        }
    }

    private void fetchVideos(String categoryName) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("videos").child(categoryName);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                videoList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String description = snapshot.child("description").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String url = snapshot.child("url").getValue(String.class);

                    VideoModel videoModel = new VideoModel(name, description, url);
                    videoList.add(videoModel);
                }
                videoPagerAdapter.notifyDataSetChanged(); // Notify adapter about data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(VideoPlaylistActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back arrow click here
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // No need to release player as VideoView does not have a dedicated release method
    }
}
