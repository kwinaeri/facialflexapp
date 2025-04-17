package com.example.facialflex;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.HashMap;
import java.util.Map;

public class PieChartFragment extends Fragment {

    private PieChart pieChart;
    private WebView sketchfabWebView;
    private Map<String, Integer> colorMap;
    private TextView resultsFaceStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pie_chart, container, false);

        // Initialize WebView
        sketchfabWebView = view.findViewById(R.id.sketchfabWebView);

        // Initialize TextView
        resultsFaceStatus = view.findViewById(R.id.results_facestatus);

        // Configure WebView settings
        WebSettings webSettings = sketchfabWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Set WebViewClient to avoid opening in external browser
        sketchfabWebView.setWebViewClient(new WebViewClient());

        // Load the Sketchfab model
        String sketchfabEmbedUrl = "https://sketchfab.com/models/8c1bcc3685cd40b3bd6b42e0445522a5/embed";
        sketchfabWebView.loadUrl(sketchfabEmbedUrl);

        // Initialize PieChart
        pieChart = view.findViewById(R.id.piechart);

        // Initialize color map
        initializeColorMap();

        // Populate the legend
        populateLegend(view);

        // Get the logged-in user's username from SharedPreferences
        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPreferences", getActivity().MODE_PRIVATE);
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

                            String faceStatus = userSnapshot.child("faceStatus").getValue(String.class);

                            if (faceStatus == null) {
                                resultsFaceStatus.setText("Please use 'Detect Me' first");
                            } else if ("Normal".equals(faceStatus)) {
                                resultsFaceStatus.setText("FacialFlex has detected that your facial muscles are Normal");
                            } else if ("Bell's palsy Detected".equals(faceStatus)) {
                                resultsFaceStatus.setText("Facialflex has detected that your facial muscles show signs of Bell's Palsy.");
                            }


                            Map<String, String> resultsMap = new HashMap<>();
                            for (int i = 1; i <= 8; i++) {
                                String resKey = "res" + i;
                                String resValue = userSnapshot.child(resKey).getValue(String.class);
                                if (resValue != null && !resValue.isEmpty()) {
                                    resultsMap.put(resKey, resValue);
                                }
                            }
                            displayResultsOnPieChart(resultsMap);
                        }
                    } else {
                        Toast.makeText(getActivity(), "User not found.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "No logged-in user found.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void initializeColorMap() {
        colorMap = new HashMap<>();
        colorMap.put("BUCCINATOR", android.graphics.Color.parseColor("#FF5733")); // Assign specific color
        colorMap.put("CORRUGATOR SUPERCILII", android.graphics.Color.parseColor("#33FF57"));
        colorMap.put("FRONTALIS", android.graphics.Color.parseColor("#3357FF"));
        colorMap.put("LEVATOR LABII SUPERIORIS", android.graphics.Color.parseColor("#FFC300"));
        colorMap.put("MENTALIS", android.graphics.Color.parseColor("#FF33A8"));
        colorMap.put("ORBICULARIS OCULI", android.graphics.Color.parseColor("#9B33FF"));
        colorMap.put("ORBICULARIS ORIS", android.graphics.Color.parseColor("#174885"));
        colorMap.put("PLATYSMA", android.graphics.Color.parseColor("#FF3389"));
        colorMap.put("PROCERUS & NASALIS", android.graphics.Color.parseColor("#B6FF33"));
        colorMap.put("ZYGOMATICUS", android.graphics.Color.parseColor("#FF8C00"));
    }

    private void displayResultsOnPieChart(Map<String, String> resultsMap) {
        pieChart.clearChart();

        for (Map.Entry<String, String> entry : resultsMap.entrySet()) {
            String[] parts = entry.getValue().split(":");
            String label = parts[0].trim();  // Trim in case of extra spaces
            float value = Float.parseFloat(parts[1]) * 100; // Convert to percentage

            int color = colorMap.getOrDefault(label, getRandomColor()); // Get color from map

            pieChart.addPieSlice(new PieModel(label, value, color));
        }

        pieChart.startAnimation();
    }

    private int getRandomColor() {
        // Generate a random color for each pie slice
        return android.graphics.Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }

    private void populateLegend(View view) {
        LinearLayout legendLayout = view.findViewById(R.id.legendLayout);

        for (Map.Entry<String, Integer> entry : colorMap.entrySet()) {
            LinearLayout legendItem = new LinearLayout(getContext());
            legendItem.setOrientation(LinearLayout.HORIZONTAL);

            // Color indicator
            View colorBox = new View(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
            params.setMargins(0, 0, 16, 0);
            colorBox.setLayoutParams(params);
            colorBox.setBackgroundColor(entry.getValue());  // Set the color

            // Label
            TextView labelText = new TextView(getContext());
            labelText.setText(entry.getKey());
            labelText.setTextSize(9);

            legendItem.addView(colorBox);
            legendItem.addView(labelText);
            legendLayout.addView(legendItem);
        }
    }
}
