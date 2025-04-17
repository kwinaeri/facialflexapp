package com.example.facialflex;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class DetectMeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detect_me, container, false);

        // Find the button by its ID
        Button redirectToDetectButton = view.findViewById(R.id.redirect_to_detect);

        // Set the click listener for the button
        redirectToDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to Detection.java
                Intent intent = new Intent(getActivity(), Detection.class);
                startActivity(intent);
            }
        });

        return view;  // Return the inflated view
    }
}
