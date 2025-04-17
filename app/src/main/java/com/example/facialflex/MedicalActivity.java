package com.example.facialflex;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicalActivity extends AppCompatActivity {

    private RecyclerView quizRecyclerView;
    private QuizAdapter quizAdapter;
    private List<Question> questionList;
    private Button submitButton;
    private DatabaseReference databaseReference;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Medical Record");
        // Enable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        quizRecyclerView = findViewById(R.id.quizRecyclerView);
        submitButton = findViewById(R.id.submitButton);

        // Retrieve username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        String userKey =  sharedPreferences.getString("userkey", null);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("user");

        // Initialize questions
        questionList = new ArrayList<>();
        questionList.add(new Question("Do you have any family member that also has Bell's Palsy?", "Yes", "No"));
        questionList.add(new Question("Do you experience any facial weakness?", "Yes", "No"));
        questionList.add(new Question("Are you taking any medications?", "Yes", "No"));
        questionList.add(new Question("Have you experienced drooping of one side of your face?", "Yes", "No"));
        questionList.add(new Question("Can you close both of your eyes?", "Yes", "No"));
        questionList.add(new Question("Do you experience drooling?", "Yes", "No"));
        questionList.add(new Question("Do you experience increased sensitivity to sound in one ear", "Yes", "No"));
        questionList.add(new Question("Do you experience pain or discomfort around the jaw or behind the ear", "Yes", "No"));

        // Set up RecyclerView
        quizRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        quizAdapter = new QuizAdapter(questionList, new QuizAdapter.OnQuestionAnsweredListener() {
            @Override
            public void onQuestionAnswered(int position, String answer) {
                // Handle the answer (store it in the model)
                questionList.get(position).setAnswer(answer);
            }
        });
        quizRecyclerView.setAdapter(quizAdapter);

        // Handle submit button
        submitButton.setOnClickListener(v -> {
            // Collect all answers and save to Firebase under the "medical_records" node
            Map<String, String> medicalRecords = new HashMap<>();
            for (int i = 0; i < questionList.size(); i++) {
                String questionKey = "Q" + (i + 1);  // For example, Q1, Q2, Q3, ...
                String answer = questionList.get(i).getAnswer();
                medicalRecords.put(questionKey, answer);  // Store the answer as key-value pair
            }

            // Update the medical_records in Firebase under the user node
            databaseReference.child(userKey).child("medical_records").setValue(medicalRecords)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MedicalActivity.this, "Medical records saved successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MedicalActivity.this, "Failed to save medical records", Toast.LENGTH_SHORT).show();
                        }
                    });

            Intent intent = new Intent(MedicalActivity.this, MainActivity.class);
            // Pass the fragment name to show the GalleryFragment
            intent.putExtra("fragment", "Profile");
            // Start MainActivity, which will display the GalleryFragment
            startActivity(intent);
            // Optionally, if you want to finish VideoPlaylistActivity (if it's no longer needed)
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back arrow click here
        onBackPressed();
        return true;
    }
}
