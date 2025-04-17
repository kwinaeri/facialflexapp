package com.example.facialflex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class DashboardFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "MyAppPreferences";
    private static final String KEY_LOGGED_IN = "logged_in";

    private TextView dashboardName;
    private ImageButton logoutButton;
    private Button redirect_appoint;
    private TextView treatStartTextView;
    private TextView totalExerciseTextView;
//    private TextView doneExerciseTextView;
    private TableLayout categoryTable;
    private ImageView profileImg;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize views
        dashboardName = view.findViewById(R.id.name_dashboard);
        treatStartTextView = view.findViewById(R.id.treat_start);  // Initialize treat_start TextView
        logoutButton = view.findViewById(R.id.logout_button);
        totalExerciseTextView = view.findViewById(R.id.totalexercise);
//        doneExerciseTextView = view.findViewById(R.id.doneexercise);
        profileImg = view.findViewById(R.id.profileImg);
        redirect_appoint = view.findViewById(R.id.redirect_appoint);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Retrieve username from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        // Fetch user data from Firebase
        fetchUserData(username);

        // Set Logout button click listener
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username != null) {
                    // Save logout log to Firebase
                    saveLogoutLog(username);

                    // Clear SharedPreferences (destroy the session)
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", false); // Set the session to false
                    editor.remove("username"); // Optionally clear the username or other session data
                    editor.apply(); // Apply changes

                    Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

                    // Redirect to LoginActivity
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
                    startActivity(intent);
                    getActivity().finish(); // Finish the current activity
                }
            }
        });

        categoryTable = view.findViewById(R.id.categoryTable);
        // Clear existing rows
        categoryTable.removeAllViews();
        // Fetch data for both tables
        fetchCategoryList();

        // Set click listener for profileImg (KUNG GUSTO NA PAG PININDOT YUNG PROFILE SA DASHBOARD)
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new instance of ProfileFragment
                ProfileFragment profileFragment = new ProfileFragment();

                // Replace the current fragment with ProfileFragment
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, profileFragment); // Replace fragment_container with your actual container ID
                transaction.addToBackStack(null); // Optional: add to back stack to allow navigation back
                transaction.commit();
            }
        });

        redirect_appoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AppointmentActivity.class);
                startActivity(intent);
            }
        });

        // Set up the refresh listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your refresh logic here
                refreshData();
            }
        });

        return view;
    }

    private void refreshData() {

        // Retrieve username from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        fetchUserData(username);
        fetchCategoryList();
        // Once done, stop the refresh animation
        swipeRefreshLayout.setRefreshing(false);
    }

    private void fetchUserData(String username) {
        if (username == null) return;

        // Reference to the Firebase Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user");
        DatabaseReference categoryReference = FirebaseDatabase.getInstance().getReference("category");

        // Attach a listener to read the data
        databaseReference.orderByChild("uname").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String dbUsername = snapshot.child("uname").getValue(String.class);

                            if (username.equals(dbUsername)) {
                                // Get User details from the snapshot
                                String treatStart = snapshot.child("treat_start").getValue(String.class);  // Fetch treat_start
                                String profileImageUrl = snapshot.child("profile_image").getValue(String.class); //fetch profile

                                // Calculate the number of days since the treatment started
                                int daysSinceTreatment = calculateDaysSince(treatStart);

                                // Display the number of days in the TextView
                                treatStartTextView.setText(daysSinceTreatment + " DAYS");

                                // Display the fetched username
                                dashboardName.setText(username != null ? username : "N/A");

                                // Load the profile image using Glide
                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                                    Glide.with(getContext())
                                            .load(profileImageUrl)
                                            .apply(RequestOptions.circleCropTransform()) // Apply circular cropping
                                            .into(profileImg);
                                } else {
                                    // Set a placeholder if no image URL is provided
                                    profileImg.setImageResource(R.drawable.baseline_person_24);
                                }

                                // Count total exercises from the CATEGORY table
                                categoryReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot categorySnapshot) {
                                        long totalExercises = categorySnapshot.getChildrenCount(); // Count nodes in CATEGORY

                                        // Display the count in the TextView
                                        totalExerciseTextView.setText(totalExercises + " EXERCISES");
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle possible errors
                                    }
                                });

                                // Assuming 'category_list' is under the user's unique key
                                DataSnapshot categoryListSnapshot = snapshot.child("category_list");

                                // Check if the category_list exists
                                if (categoryListSnapshot.exists()) {
                                    // StringBuilder to append all categoryname and donecounter values
                                    StringBuilder doneExercises = new StringBuilder("Successfully Completed Exercises:\n\n");

                                    // Loop through all categories in category_list
                                    for (DataSnapshot categorySnapshot : categoryListSnapshot.getChildren()) {
                                        String categoryName = categorySnapshot.getKey();  // Get category name
                                        Long doneCounter = categorySnapshot.child("donecounter").getValue(Long.class);  // Get donecounter

                                        // Append the categoryname and donecounter in the required format
                                        doneExercises.append(categoryName)
                                                .append(" : ")
                                                .append(doneCounter != null ? doneCounter : 0)
                                                .append("\n");
                                    }

                                    // Display the result in the TextView
                                    //doneExerciseTextView.setText(doneExercises.toString());

                                } else {
                                    // If no category_list exists, display 0 or any default message
                                    totalExerciseTextView.setText("0");
                                    //doneExerciseTextView.setText("No exercises found.");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors
                    }
                });
    }

    private void fetchCategoryList() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user");

        userRef.orderByChild("uname").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean hasCategoryList = false; // Flag to check if the category_list exists

                    // Clear existing rows before refreshing the data
                    categoryTable.removeAllViews();

                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Check if the category_list exists
                        if (userSnapshot.hasChild("category_list")) {
                            hasCategoryList = true; // category_list exists

                            for (DataSnapshot categorySnapshot : userSnapshot.child("category_list").getChildren()) {
                                String categoryName = categorySnapshot.getKey();
                                String lastVisitDate = categorySnapshot.child("last_visit_date").getValue(String.class);
                                Integer doneCounter = categorySnapshot.child("donecounter").getValue(Integer.class);

                                if (categoryName != null && lastVisitDate != null && doneCounter != null) {
                                    addCategoryRow(categoryName, lastVisitDate, doneCounter);
                                } else {
                                    Log.w("ExercisesHistoryFragment", "Missing data in category: " + categoryName);
                                }
                            }
                        }
                    }

                    // If no category_list was found or it's empty, display the no data message
                    if (!hasCategoryList) {
                        displayNoDataMessage();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ExercisesHistoryFragment", "Database error: " + databaseError.getMessage());
                Toast.makeText(getActivity(), "Failed to load category data.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addCategoryRow(String categoryName, String lastVisitDate, int doneCounter) {
        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        row.setGravity(Gravity.CENTER); // Center the content in the TableRow

        // Create a LinearLayout to hold the vertical layout
        LinearLayout verticalLayout = new LinearLayout(getContext());
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        verticalLayout.setGravity(Gravity.CENTER); // Center content in the vertical layout

        // Create TextView for category name
        TextView categoryTextView = new TextView(getContext());
        categoryTextView.setText(categoryName);
        categoryTextView.setPadding(16, 16, 16, 8);
        categoryTextView.setTextSize(12);
        categoryTextView.setTypeface(null, Typeface.BOLD);
        categoryTextView.setTextColor(getResources().getColor(R.color.white));
        verticalLayout.addView(categoryTextView);

        // Configure the ProgressBar
        ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(3);  // Max value of 3 exercises
        progressBar.setProgress(doneCounter);  // Set progress based on doneCounter
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        verticalLayout.addView(progressBar);

        // Create a horizontal layout for the done counter and date
        LinearLayout horizontalLayout = new LinearLayout(getContext());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        horizontalLayout.setGravity(Gravity.CENTER); // Center content in the horizontal layout

        // TextView for done counter
        TextView counterTextView = new TextView(getContext());
        counterTextView.setText("You are " + doneCounter + "/3 done with this exercise");
        counterTextView.setPadding(16, 16, 16, 16);
        counterTextView.setTextSize(12);
        counterTextView.setTextColor(getResources().getColor(R.color.white));
        horizontalLayout.addView(counterTextView);


        // TextView for date
        TextView dateTextView = new TextView(getContext());
        dateTextView.setText(lastVisitDate);
        dateTextView.setPadding(16, 16, 16, 16);
        dateTextView.setTextSize(10);
        dateTextView.setTextColor(getResources().getColor(R.color.white));
        horizontalLayout.addView(dateTextView);


        // Add horizontal layout to the vertical layout
        verticalLayout.addView(horizontalLayout);

        // Add the vertical layout to the TableRow
        row.addView(verticalLayout);

        // Add the row to the TableLayout
        categoryTable.addView(row);
    }

    private void displayNoDataMessage() {
        // Create a TextView to display the message
        TextView noDataTextView = new TextView(getContext());
        noDataTextView.setText("No data found. Please do Treatment & Exercises first.");
        noDataTextView.setPadding(20, 16, 16, 20);
        noDataTextView.setTextSize(12);
        noDataTextView.setTypeface(null, Typeface.NORMAL);
        noDataTextView.setTextColor(getResources().getColor(R.color.white));

        // Add the TextView to the parent layout (assuming it's a TableLayout or some container)
        categoryTable.addView(noDataTextView);
    }


    // Method to calculate the number of days between the fetched date and the current date
    private int calculateDaysSince(String treatStart) {
        if (treatStart == null || treatStart.isEmpty()) {
            return 0; // Return 0 days if no treat_start date is found
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            // Parse the treat_start date
            Date startDate = dateFormat.parse(treatStart);
            // Get the current date
            Date currentDate = new Date();

            // Calculate the difference in milliseconds
            long differenceInMillis = currentDate.getTime() - startDate.getTime();

            // Convert milliseconds to days
            return (int) TimeUnit.DAYS.convert(differenceInMillis, TimeUnit.MILLISECONDS);

        } catch (ParseException e) {
            e.printStackTrace();
            return 0; // Return 0 if there's an error parsing the date
        }
    }

    private void saveLogoutLog(String username) {
        // Get the current date and time
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Reference to the userlogs table in Firebase
        DatabaseReference logsReference = FirebaseDatabase.getInstance().getReference("userlogs");

        // Create a unique key for each log entry
        String logId = logsReference.push().getKey();

        // Create a log entry
        Map<String, String> logEntry = new HashMap<>();
        logEntry.put("datetime", dateTime);
        logEntry.put("status", "Logged Out");
        logEntry.put("username", username);
        logEntry.put("role", "patient"); // Assuming the role is 'patient'

        // Save the log entry to Firebase
        if (logId != null) {
            logsReference.child(logId).setValue(logEntry)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Log saved successfully
                        } else {
                            // Log saving failed
                        }
                    });
        }
    }
}
