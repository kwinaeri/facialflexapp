package com.example.facialflex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment {

    private TextView profileName, profileUsername, profileEmail, profilePassword, profileAge, profileBirthday, profileAddress, profileDescription;
    private ImageView profileImg;
    private Button editButton, btnFillUpMedicalRecords;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize TextViews
        profileName = view.findViewById(R.id.profile_name);
        profileUsername = view.findViewById(R.id.profile_username);
        profileEmail = view.findViewById(R.id.profile_email);
        profilePassword = view.findViewById(R.id.profile_password);
        profileAge = view.findViewById(R.id.profile_age);
        profileBirthday = view.findViewById(R.id.profile_birthday);
        profileAddress = view.findViewById(R.id.profile_address);
        profileDescription = view.findViewById(R.id.profile_description);
        profileImg = view.findViewById(R.id.profileImg);
        editButton = view.findViewById(R.id.editButton);
        btnFillUpMedicalRecords = view.findViewById(R.id.medicalbtn);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Retrieve username from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        // Fetch user data from Firebase
        fetchUserData(username);

        editButton = view.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        btnFillUpMedicalRecords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the MedicalActivity when button is clicked
                Intent intent = new Intent(getActivity(), MedicalActivity.class);
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
        swipeRefreshLayout.setRefreshing(false);
    }

    private void fetchUserData(String username) {
        if (username == null) return;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String dbUsername = snapshot.child("uname").getValue(String.class);

                    if (username.equals(dbUsername)) {
                        // Get User ID and store it in SharedPreferences
                        String userId = snapshot.getKey(); // Firebase unique key
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
                        sharedPreferences.edit().putString("userId", userId).apply();

                        // Get User details from the snapshot
                        String name = snapshot.child("fname").getValue(String.class);
                        String email = snapshot.child("em").getValue(String.class);
                        String password = snapshot.child("pword").getValue(String.class);
                        String age = snapshot.child("age").getValue(String.class);
                        String birthday = snapshot.child("birthday").getValue(String.class);
                        String address = snapshot.child("address").getValue(String.class);
                        String description = snapshot.child("description").getValue(String.class);
                        String profileImageUrl = snapshot.child("profile_image").getValue(String.class);

                        // Display the fetched data in the TextViews
                        profileName.setText(name != null ? name : "N/A");
                        profileUsername.setText(username != null ? username : "N/A");
                        profileEmail.setText(email != null ? email : "N/A");
                        profilePassword.setText(password != null ? "******" : "N/A");
                        profileAge.setText(age != null ? age : "N/A");
                        profileBirthday.setText(birthday != null ? birthday : "N/A");
                        profileAddress.setText(address != null ? address : "N/A");
                        profileDescription.setText(description != null ? description : "N/A");

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
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
}
