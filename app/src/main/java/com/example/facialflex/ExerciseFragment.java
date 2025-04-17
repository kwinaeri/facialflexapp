package com.example.facialflex;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.facialflex.RCModel;


public class ExerciseFragment extends Fragment {

    // start webview
    private ViewPager2 viewPager;
    private WebViewAdapter webViewAdapter;
    // end webview
    private RecyclerView recyclerView;
    private ArrayList<RCModel> modelArrayList;
    private RCAdapter rcAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

        // start webview
        // List of YouTube video IDs
        List<String> videoUrls = Arrays.asList(
                "e_-Qa8N9DnQ",
                "T57FU4p-I9o",
                "TwJLWzq_9cU"
        );

        viewPager = view.findViewById(R.id.webview);
        webViewAdapter = new WebViewAdapter(videoUrls);
        viewPager.setAdapter(webViewAdapter);
        // end webview (if remove - remove also item_webview.xml, WebViewAdapter.java)

        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);

        // Retrieve username from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        // Fetch user data from Firebase
        fetchUserData(username);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        modelArrayList = new ArrayList<>();
        rcAdapter = new RCAdapter(getContext(), modelArrayList);
        recyclerView.setAdapter(rcAdapter);

        // Fetch categories and populate RecyclerView
        fetchCategories();

        return view;
    }

    private void fetchUserData(String username) {
        if (username == null) return;

        // Reference to the Firebase Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user");

        // Attach a listener to read the data
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String dbUsername = snapshot.child("uname").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void fetchCategories() {
        // Reference to the Firebase Database
        DatabaseReference categoriesReference = FirebaseDatabase.getInstance().getReference("category");

        // Attach a listener to read the categories data
        categoriesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelArrayList.clear(); // Clear the list to avoid duplication
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String categoryName = snapshot.child("categoryname").getValue(String.class);
                    String categoryImage = snapshot.child("categimg").getValue(String.class); // URL or drawable resource

                    // Create RCModel with category name and image URL or resource
                    RCModel rcModel = new RCModel(categoryName, categoryImage);
                    modelArrayList.add(rcModel);
                }
                rcAdapter.notifyDataSetChanged(); // Notify the adapter of the data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
}