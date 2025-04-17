package com.example.facialflex;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.facialflex.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Create notification channel
        NotificationHelper.createNotificationChannel(this);

        // Check and request for notification permission (Android 13 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            } else {
                // Schedule notification if permission is already granted
                scheduleDailyNotification();
            }
        } else {
            // Schedule notification without needing the new permission on Android 12 and below
            scheduleDailyNotification();
        }

        // Check if there is an intent with the fragment name
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("fragment")) {
            String fragmentName = intent.getStringExtra("fragment");
            if ("TreatmentAndExercises".equals(fragmentName)) {
                // Load the ExerciseFragment if specified
                replaceFragment(new ExerciseFragment());
            } else if ("Gallery".equals(fragmentName)) {
                // Load the ExerciseFragment if specified
                replaceFragment(new GalleryFragment(), 1);
            } else if ("ExercisesHistory".equals(fragmentName)) {
                // Load the ExerciseFragment if specified
                replaceFragment(new GalleryFragment(), 2);
            } else if ("Profile".equals(fragmentName)) {
                // Load the ExerciseFragment if specified
                replaceFragment(new ProfileFragment());
            } else {
                // Load default fragment or handle other cases if necessary
                replaceFragment(new DashboardFragment());
            }
        } else {
            // Load the default fragment on first launch
            replaceFragment(new DashboardFragment());
        }

        binding.bottomNavigationView.setBackground(null);

        // Retrieve SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            // User is not logged in, redirect to LoginActivity
            Intent intentLogin = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intentLogin);
            finish(); // Close MainActivity so the user can't go back to it
        } else {
            // User is logged in, proceed with MainActivity
            String username = prefs.getString("username", "");
            // Optionally, display a welcome message or use the username
            Toast.makeText(this, "Welcome, " + username, Toast.LENGTH_SHORT).show();
            // Continue with MainActivity logic
            updateProgress();
        }


        // Handle BottomNavigationView item clicks
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.dashboard) {
                replaceFragment(new DashboardFragment());
            } else if (itemId == R.id.profile) {
                replaceFragment(new ProfileFragment());
            } else if (itemId == R.id.exercise) {
                replaceFragment(new ExerciseFragment());
            } else if (itemId == R.id.gallery) {
                replaceFragment(new GalleryFragment());
            }
            return true;
        });

        // Handle FloatingActionButton click
        binding.detectMe.setOnClickListener(v -> { // Ensure the ID matches
            replaceFragment(new DetectMeFragment());
        });

    }

    private void scheduleDailyNotification() {
        // Create an intent to send a broadcast to the NotificationReceiver
        Intent intent = new Intent(this, NotificationReceiver.class);

        // Use FLAG_IMMUTABLE because you don't need the PendingIntent to be mutable
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // Get system alarm service and schedule the notification
        //AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Cancel any existing alarm before scheduling a new one
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);  // Cancel previous alarm
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8); // Set time to 8:00 AM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            //Toast.makeText(this, "Notification scheduled for 5:08 PM daily", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle the permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, schedule the notification
                scheduleDailyNotification();
            } else {
                // Permission denied, inform the user
                Toast.makeText(this, "Permission to show notifications is required.", Toast.LENGTH_SHORT).show();
                // Optionally, guide the user to enable permission in settings
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }
        }
    }

    public void updateProgress() {
        // Get the userKey from shared preferences
        SharedPreferences prefs = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        String userKey = prefs.getString("userkey", null);

        if (userKey == null) {
            Log.e("UpdateProgress", "User key not found in shared preferences.");
            return; // Exit if the user key is not available
        }

        DatabaseReference categoryListRef = FirebaseDatabase.getInstance().getReference("user").child(userKey).child("category_list");

        categoryListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                if (categorySnapshot.exists()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String todayString = sdf.format(new Date()); // Get today's date as a string

                    for (DataSnapshot category : categorySnapshot.getChildren()) {
                        String lastVisitDateStr = category.child("last_visit_date").getValue(String.class);
                        Integer doneCounter = category.child("donecounter").getValue(Integer.class);

                        if (lastVisitDateStr != null && doneCounter != null) {
                            try {
                                Date lastVisitDate = sdf.parse(lastVisitDateStr);
                                Date todayDate = sdf.parse(todayString);

                                if (lastVisitDate != null && todayDate != null && !sdf.format(lastVisitDate).equals(todayString)) {
                                    // Reset doneCounter if the last visit was not today
                                    category.getRef().child("donecounter").setValue(0);
                                    category.getRef().child("last_visit_date").setValue(todayString);
                                    Log.d("UpdateProgress", "Reset doneCounter for category: " + category.getKey());
                                }
                            } catch (ParseException e) {
                                Log.e("UpdateProgress", "Date parsing error: " + e.getMessage());
                            }
                        } else {
                            Log.w("UpdateProgress", "Missing data in category: " + category.getKey());
                        }
                    }
                } else {
                    Log.w("UpdateProgress", "Category list not found for user: " + userKey);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UpdateProgress", "Database error: " + error.getMessage());
            }
        });
    }


    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void replaceFragment(Fragment fragment, int tabIndex) {
        Bundle bundle = new Bundle();
        bundle.putInt("tabIndex", tabIndex);  // Pass the tab index
        fragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

}