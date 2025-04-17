package com.example.facialflex;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExercisesHistoryFragment extends Fragment {

    //private TableLayout categoryTable;
    private TableLayout adherenceTable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises_history, container, false);

//        categoryTable = view.findViewById(R.id.categoryTable);
        adherenceTable = view.findViewById(R.id.adherenceTable);

        // Clear existing rows
        //categoryTable.removeAllViews();
        adherenceTable.removeAllViews();

        // Fetch data for both tables
        //fetchCategoryList();
        fetchAdherenceList();

        return view;
    }

//    private void fetchCategoryList() {
//        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
//        String username = sharedPreferences.getString("username", null);
//
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user");
//
//        userRef.orderByChild("uname").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
//                        // No need to create a DatabaseReference, just use the userSnapshot
//                        for (DataSnapshot categorySnapshot : userSnapshot.child("category_list").getChildren()) {
//                            String categoryName = categorySnapshot.getKey();
//                            String lastVisitDate = categorySnapshot.child("last_visit_date").getValue(String.class);
//                            Integer doneCounter = categorySnapshot.child("donecounter").getValue(Integer.class);
//
//                            if (categoryName != null && lastVisitDate != null && doneCounter != null) {
//                                addCategoryRow(categoryName, lastVisitDate, doneCounter);
//                            } else {
//                                Log.w("ExercisesHistoryFragment", "Missing data in category: " + categoryName);
//                            }
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e("ExercisesHistoryFragment", "Database error: " + databaseError.getMessage());
//                Toast.makeText(getActivity(), "Failed to load category data.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//
//    private void addCategoryRow(String categoryName, String lastVisitDate, int doneCounter) {
//        TableRow row = new TableRow(getContext());
//        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
//        row.setGravity(Gravity.CENTER); // Center the content in the TableRow
//
//        // Create a LinearLayout to hold the vertical layout
//        LinearLayout verticalLayout = new LinearLayout(getContext());
//        verticalLayout.setOrientation(LinearLayout.VERTICAL);
//        verticalLayout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
//        verticalLayout.setGravity(Gravity.CENTER); // Center content in the vertical layout
//
//        // Create TextView for category name
//        TextView categoryTextView = new TextView(getContext());
//        categoryTextView.setText(categoryName);
//        categoryTextView.setPadding(16, 16, 16, 8);
//        categoryTextView.setTextSize(12);
//        categoryTextView.setTypeface(null, Typeface.BOLD);
//        categoryTextView.setTextColor(getResources().getColor(R.color.white));
//        verticalLayout.addView(categoryTextView);
//
//
//
//        // Configure the ProgressBar
//        ProgressBar progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
//        progressBar.setMax(3);  // Max value of 3 exercises
//        progressBar.setProgress(doneCounter);  // Set progress based on doneCounter
//        progressBar.setLayoutParams(new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));
//        verticalLayout.addView(progressBar);
//
//        // Create a horizontal layout for the done counter and date
//        LinearLayout horizontalLayout = new LinearLayout(getContext());
//        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
//        horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));
//        horizontalLayout.setGravity(Gravity.CENTER); // Center content in the horizontal layout
//
//        // TextView for done counter
//        TextView counterTextView = new TextView(getContext());
//        counterTextView.setText("You are " + doneCounter + "/3 done with this exercise");
//        counterTextView.setPadding(16, 16, 16, 16);
//        counterTextView.setTextSize(12);
//        counterTextView.setTextColor(getResources().getColor(R.color.white));
//        horizontalLayout.addView(counterTextView);
//
//
//        // TextView for date
//        TextView dateTextView = new TextView(getContext());
//        dateTextView.setText(lastVisitDate);
//        dateTextView.setPadding(16, 16, 16, 16);
//        dateTextView.setTextSize(10);
//        dateTextView.setTextColor(getResources().getColor(R.color.white));
//        horizontalLayout.addView(dateTextView);
//
//
//        // Add horizontal layout to the vertical layout
//        verticalLayout.addView(horizontalLayout);
//
//        // Add the vertical layout to the TableRow
//        row.addView(verticalLayout);
//
//        // Add the row to the TableLayout
//        categoryTable.addView(row);
//    }
//

    private void fetchAdherenceList() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user");

        userRef.orderByChild("uname").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<AdherenceEntry> adherenceList = new ArrayList<>();

                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot adherenceSnapshot : userSnapshot.child("adherence").getChildren()) {
                            String category = adherenceSnapshot.child("category").getValue(String.class);
                            String date = adherenceSnapshot.child("date").getValue(String.class);
                            String time = adherenceSnapshot.child("time").getValue(String.class);

                            if (category != null && date != null && time != null) {
                                adherenceList.add(new AdherenceEntry(category, date, time));
                            } else {
                                Log.w("ExercisesHistoryFragment", "Missing data in adherence record.");
                            }
                        }
                    }

                    // Sort the adherence list by date and time in descending order (latest first)
                    Collections.sort(adherenceList, (entry1, entry2) -> {
                        String dateTime1 = entry1.getDate() + " " + entry1.getTime();
                        String dateTime2 = entry2.getDate() + " " + entry2.getTime();
                        return dateTime2.compareTo(dateTime1); // Sort in descending order
                    });

                    // Add the sorted entries to the table
                    for (AdherenceEntry entry : adherenceList) {
                        addAdherenceRow(entry.getCategory(), entry.getDate(), entry.getTime());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ExercisesHistoryFragment", "Database error: " + databaseError.getMessage());
                Toast.makeText(getActivity(), "Failed to load adherence data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAdherenceRow(String category, String date, String time) {
        TableRow row = new TableRow(getContext());
        TextView categoryTextView = new TextView(getContext());
        TextView dateTextView = new TextView(getContext());
        TextView timeTextView = new TextView(getContext());

        categoryTextView.setText(category);
        dateTextView.setText(date);
        timeTextView.setText(time);

        categoryTextView.setPadding(20, 8, 20, 8);
        categoryTextView.setTextSize(12);

        dateTextView.setPadding(20, 8, 20, 8);
        dateTextView.setTextSize(12);

        timeTextView.setPadding(20, 8, 20, 8);
        timeTextView.setTextSize(12);

        // Set layout parameters
        categoryTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        dateTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));
        timeTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1));

        categoryTextView.setTextColor(getResources().getColor(R.color.white));
        dateTextView.setTextColor(getResources().getColor(R.color.white));
        timeTextView.setTextColor(getResources().getColor(R.color.white));

        row.addView(categoryTextView);
        row.addView(dateTextView);
        row.addView(timeTextView);

        adherenceTable.addView(row);
    }

    // Class to hold adherence data
    private static class AdherenceEntry {
        private String category;
        private String date;
        private String time;

        public AdherenceEntry(String category, String date, String time) {
            this.category = category;
            this.date = date;
            this.time = time;
        }

        public String getCategory() {
            return category;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }
    }
}