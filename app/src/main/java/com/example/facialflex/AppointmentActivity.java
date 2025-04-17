package com.example.facialflex;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class AppointmentActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private Button scheduleButton;
    private String selectedDate; // This will hold the formatted date
    private TableLayout appointmentsTable;
    private TextView ptName, ptPos, ptEmail, ptNum, ptSchedule;
    private ImageView ptProfile;
    private DatabaseReference doctorsRef;
    private WebView webView;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Appointment");
        // Enable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        webView = findViewById(R.id.webview);

        // Enable JavaScript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Set a WebViewClient to handle page navigation
        webView.setWebViewClient(new WebViewClient());

        // Load a URL
        webView.loadUrl("https://app.mappedin.com/map/67125a5bc2b3ad000b34d1f1");

        calendarView = findViewById(R.id.calendar_view);
        scheduleButton = findViewById(R.id.schedule_button);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // Format date as YYYY-MM-DD
                selectedDate = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth);
                Toast.makeText(AppointmentActivity.this, "Selected Date: " + selectedDate, Toast.LENGTH_SHORT).show();
            }
        });

        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long selectedDateInMillis = calendarView.getDate();
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(selectedDateInMillis);

                // Get year, month, and day directly from the Calendar instance
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1; // Use month + 1 for display
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Now use selectedDate here
                showAppointmentDialog(year, month, day, selectedDate); // Pass selectedDate
            }
        });

        appointmentsTable = findViewById(R.id.appointments_table);

        // Call method to fetch appointments
        fetchAppointments();

        ptName = findViewById(R.id.pt_name);
        ptPos = findViewById(R.id.pt_pos);
        ptEmail = findViewById(R.id.pt_email);
        ptNum = findViewById(R.id.pt_num);
        ptSchedule = findViewById(R.id.pt_schedule);
        ptProfile = findViewById(R.id.pt_profile);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        // Initialize Firebase Database Reference
        doctorsRef = FirebaseDatabase.getInstance().getReference("Doctors");

        // Set up the refresh listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your refresh logic here
                refreshData();
            }
        });

        // Fetch Doctor's Data
        fetchDoctorData();

    }

    private void refreshData() {
        swipeRefreshLayout.setRefreshing(true);

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);

        fetchAppointments();

        // Once done, stop the refresh animation
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showAppointmentDialog(int year, int month, int day, String selectedDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Schedule Appointment");

        // Use the selectedDate for displaying
        String date = selectedDate; // Or format it differently if needed

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_appointment, null);
        builder.setView(dialogView);

        TextView appointmentDate = dialogView.findViewById(R.id.appointment_date);
        appointmentDate.setText("Date: " + date); // Display the formatted date

        Spinner timeSpinner = dialogView.findViewById(R.id.time_spinner);
        Spinner doctorSpinner = dialogView.findViewById(R.id.doctor_spinner);

        // Call the method to fetch and populate doctors
        populateDoctorsSpinner(doctorSpinner);

        // Call checkExistingAppointments to update the time spinner
        checkExistingAppointments(date, timeSpinner); // Ensure this is called here

        // Set positive button
        builder.setPositiveButton("Schedule", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedTime = timeSpinner.getSelectedItem().toString();
                String selectedDoctor = doctorSpinner.getSelectedItem().toString();

                // Save the appointment to Firebase
                saveAppointment(date, selectedTime, selectedDoctor); // Use the date variable
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void populateDoctorsSpinner(Spinner doctorSpinner) {
        // Reference to the Doctors table in Firebase
        DatabaseReference doctorsRef = FirebaseDatabase.getInstance().getReference("Doctors");

        // Fetch the list of doctors from Firebase
        doctorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> doctorNames = new ArrayList<>();

                // Loop through all doctors in the database
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String doctorName = snapshot.child("name").getValue(String.class); // Assuming the field "name" stores the doctor's name
                    doctorNames.add(doctorName);
                }

                // Update the Spinner with the list of doctor names
                ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(AppointmentActivity.this, android.R.layout.simple_spinner_item, doctorNames);
                doctorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                doctorSpinner.setAdapter(doctorAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AppointmentActivity.this, "Failed to load doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkExistingAppointments(String date, Spinner timeSpinner) {
        // Get the user's key from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        String userKey = sharedPreferences.getString("userkey", null); // Ensure this is saved during login

        // Retrieve appointments for the selected date
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments");
        appointmentsRef.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Create a set of occupied time slots
                HashSet<String> occupiedTimes = new HashSet<>();

                for (DataSnapshot appointment : dataSnapshot.getChildren()) {
                    String time = appointment.child("time").getValue(String.class);
                    String status = appointment.child("status").getValue(String.class);
                    // If the status is 'cancelled', make this time available
                    if ("cancelled".equals(status)) {
                        occupiedTimes.remove(time);  // Remove the time from occupied if it's cancelled
                    } else {
                        occupiedTimes.add(time);  // Otherwise, keep it as occupied
                    }
                }

                // Create a list of available times by filtering out occupied ones
                List<String> availableTimes = new ArrayList<>();
                String[] times = {"8:00-9:00 AM", "10:00-11:00 AM", "12:00-1:00 PM", "2:00-3:00 PM", "4:00-5:00 PM"};
                for (String time : times) {
                    if (!occupiedTimes.contains(time)) {
                        availableTimes.add(time);
                    }
                }

                // Update the spinner with available times
                ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(AppointmentActivity.this, android.R.layout.simple_spinner_item, availableTimes);
                timeSpinner.setAdapter(timeAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AppointmentActivity.this, "Failed to retrieve appointments.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveAppointment(String date, String time, String doctor) {
        // Get the current user's username and user key from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", null);
        String userKey = sharedPreferences.getString("userkey", null); // Make sure to save this in SharedPreferences during login

        // Retrieve the full name of the user from Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("user").child(userKey);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fullname = dataSnapshot.child("fname").getValue(String.class);
                    String status = "pending"; // Set the initial status to pending

                    // Prepare the data to save
                    DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference("appointments").push();
                    Appointment appointment = new Appointment(date, doctor, fullname, status, time, userKey, username);

                    appointmentsRef.setValue(appointment)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Appointment saved successfully
                                    Toast.makeText(AppointmentActivity.this, "Appointment scheduled!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Handle failure
                                    Toast.makeText(AppointmentActivity.this, "Failed to schedule appointment.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(AppointmentActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AppointmentActivity.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAppointments() {
        // Get the logged-in user's username from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        String loggedInUsername = sharedPreferences.getString("username", null);

        if (loggedInUsername != null) {
            // Reference to appointments in the Firebase database
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("appointments");

            // Query appointments where username matches the logged-in user
            ref.orderByChild("username").equalTo(loggedInUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //Clear the existing rows if needed before adding new data
                           //appointmentsTable.removeAllViews(); if u want TH to be gone
                            appointmentsTable.removeAllViews(); //so that data wont repeat when refreshed

                            if (dataSnapshot.exists()) {
                                // Loop through all matching appointments
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    // Retrieve appointment data
                                    String date = snapshot.child("date").getValue(String.class);
                                    String doctor = snapshot.child("doctor").getValue(String.class);
                                    String time = snapshot.child("time").getValue(String.class);
                                    String status = snapshot.child("status").getValue(String.class);
                                    String appointmentKey = snapshot.getKey(); // Get the appointment key

                                    // Add each appointment as a new row in the table
                                    addAppointmentRow(date, doctor, time, status, appointmentKey);
                                }
                            } else {
                                // Display "No appointments yet" message
                                appointmentsTable.removeAllViews();

                                TextView noAppointmentsTextView = new TextView(AppointmentActivity.this);
                                noAppointmentsTextView.setText("No appointments yet");
                                noAppointmentsTextView.setGravity(Gravity.CENTER);
                                noAppointmentsTextView.setPadding(10, 10, 10, 10);
                                noAppointmentsTextView.setTextSize(14);
                                noAppointmentsTextView.setTextColor(Color.BLACK); // Set desired text color

                                // Add the TextView to your appointments table or layout
                                appointmentsTable.addView(noAppointmentsTextView);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle any errors here
                            Log.e("fetchAppointments", "Database error: " + error.getMessage());
                        }
                    });
        } else {
            Log.e("fetchAppointments", "Logged-in username not found.");
        }
    }

    private void addAppointmentRow(String date, String doctor, String time, String status, String appointmentKey) {
        // Create the first row for DATE and DOCTOR
        TableRow row1 = new TableRow(this);

        TextView dateLabel = createLabelTextView("DATE");
        TextView doctorLabel = createLabelTextView("PHYSICAL THERAPIST");
        row1.addView(dateLabel);
        row1.addView(doctorLabel);

        TableRow row1Data = new TableRow(this);
        TextView dateView = createDataTextView(date);
        TextView doctorView = createDataTextView(doctor);
        row1Data.addView(dateView);
        row1Data.addView(doctorView);

        // Create the second row for TIME and STATUS
        TableRow row2 = new TableRow(this);

        TextView timeLabel = createLabelTextView("TIME");
        TextView statusLabel = createLabelTextView("STATUS");
        row2.addView(timeLabel);
        row2.addView(statusLabel);

        TableRow row2Data = new TableRow(this);
        TextView timeView = createDataTextView(time);
        TextView statusView = createDataTextView(status);
        row2Data.addView(timeView);
        row2Data.addView(statusView);

        // Create a row with a cancel button
        TableRow row3 = new TableRow(this);
        Button cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        // Set layout parameters for the button to ensure it is centered
        TableRow.LayoutParams layoutParams2 = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, // Width
                TableRow.LayoutParams.WRAP_CONTENT
        );
        layoutParams2.weight = 1; // This will make the button take the full width and center it
        layoutParams2.gravity = Gravity.CENTER;
        cancelButton.setLayoutParams(layoutParams2);
        cancelButton.setOnClickListener(v -> cancelAppointment(appointmentKey, statusView, date));
        row3.addView(cancelButton);

        // Add rows to the TableLayout
        appointmentsTable.addView(row1);
        appointmentsTable.addView(row1Data);
        appointmentsTable.addView(row2);
        appointmentsTable.addView(row2Data);
        appointmentsTable.addView(row3);

        View separator = new View(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, 2);

        separator.setLayoutParams(layoutParams);
        separator.setBackgroundColor(Color.LTGRAY);

        appointmentsTable.addView(separator);
    }

    private TextView createLabelTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(14);
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setGravity(Gravity.CENTER);  // Center the text within the TextView
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f  // Equal weight for each TextView to distribute space evenly
        ));
        textView.setPadding(10, 20, 10, 10);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.lato_black);
        textView.setTypeface(typeface);

        return textView;
    }

    private TextView createDataTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(12);
        textView.setTextColor(getResources().getColor(R.color.black));
        textView.setGravity(Gravity.CENTER);  // Center the text within the TextView
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f  // Equal weight for each TextView to distribute space evenly
        ));
        textView.setPadding(10, 10, 10, 20);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.lato);
        textView.setTypeface(typeface);

        return textView;
    }

    public interface ScheduleCallback {
        void onScheduleFetched(String schedule);
    }

    private void cancelAppointment(String appointmentKey, TextView statusView, String appointmentDate) {
        // Get the current date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  // Match the format of your stored date
        Date currentDate = new Date();  // Get current date

        try {
            Date appointmentDateObj = sdf.parse(appointmentDate);  // Convert appointment date to Date object

            // Get the difference between current date and appointment date
            long diffInMillis = appointmentDateObj.getTime() - currentDate.getTime();
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);  // Convert milliseconds to days

            // If the appointment is within 2 days of the current date, prevent cancellation
            if (diffInDays < 2) {
                Toast.makeText(AppointmentActivity.this, "You can only cancel appointments 2 days in advance.", Toast.LENGTH_SHORT).show();
                return; // Exit the method, don't proceed with cancellation
            }

            // Proceed with cancellation if more than 2 days before the appointment
            DatabaseReference appointmentRef = FirebaseDatabase.getInstance().getReference("appointments").child(appointmentKey);

            // Update the status to "cancelled"
            appointmentRef.child("status").setValue("cancelled")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update the status in the UI
                            statusView.setText("cancelled");
                            Toast.makeText(AppointmentActivity.this, "Appointment cancelled.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AppointmentActivity.this, "Failed to cancel appointment.", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(AppointmentActivity.this, "Error parsing date.", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchDoctorData() {
        doctorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Doctor> doctorList = new ArrayList<>(); // List to hold doctors

                for (DataSnapshot doctorSnapshot : dataSnapshot.getChildren()) {
                    // Fetch doctor details including profile_image
                    String name = doctorSnapshot.child("name").getValue(String.class);
                    String certificate = doctorSnapshot.child("certificate").getValue(String.class);
                    String email = doctorSnapshot.child("email").getValue(String.class);
                    String number = doctorSnapshot.child("number").getValue(String.class);
                    String profileImage = doctorSnapshot.child("profile_image").getValue(String.class); // Ensure this is correct

                    String doctorKey = doctorSnapshot.getKey(); // Get the unique key for the doctor

                    // Fetch the schedule for the specific doctor
                    fetchScheduleForDoctor(doctorKey, new ScheduleCallback() {
                        @Override
                        public void onScheduleFetched(String schedule) {
                            // Create a Doctor object with the fetched schedule
                            Doctor doctor = new Doctor(name, certificate, email, number, profileImage);
                            doctor.setSchedule(schedule); // Set the schedule
                            doctorList.add(doctor); // Add the doctor to the list

                            // Update RecyclerView after all data is fetched
                            if (doctorList.size() == dataSnapshot.getChildrenCount()) {
                                // Set up RecyclerView with the adapter
                                RecyclerView recyclerView = findViewById(R.id.doctors_recycler_view);
                                DoctorAdapter adapter = new DoctorAdapter(doctorList, AppointmentActivity.this);
                                recyclerView.setLayoutManager(new LinearLayoutManager(AppointmentActivity.this));
                                recyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged(); // Notify adapter about data changes
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }


    private void fetchScheduleForDoctor(String doctorKey, ScheduleCallback callback) {
        DatabaseReference scheduleRef = FirebaseDatabase.getInstance().getReference("Doctors").child(doctorKey).child("schedfatima");

        scheduleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder scheduleBuilder = new StringBuilder(); // Holds the schedule for the current doctor
                if (snapshot.exists()) {
                    for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                        String day = daySnapshot.getValue(String.class); // e.g., "MONDAY", "TUESDAY"
                        if (day != null) {
                            scheduleBuilder.append(day).append(", "); // Append each day
                        }
                    }
                    // Remove the last comma and space if not empty
                    if (scheduleBuilder.length() > 0) {
                        scheduleBuilder.setLength(scheduleBuilder.length() - 2);
                    }
                } else {
                    Log.d("ScheduleData", "No schedule data found for doctor: " + doctorKey);
                    scheduleBuilder.append("No schedule available");
                }
                callback.onScheduleFetched(scheduleBuilder.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ScheduleData", "Error fetching schedule data for doctor " + doctorKey + ": " + databaseError.getMessage());
            }
        });
    }


    private void setupRecyclerView(List<Doctor> doctorList) {
        // Set up RecyclerView with the adapter
        RecyclerView recyclerView = findViewById(R.id.doctors_recycler_view);
        DoctorAdapter adapter = new DoctorAdapter(doctorList, AppointmentActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(AppointmentActivity.this));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged(); // Notify adapter about data changes
    }


    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back arrow click here
        onBackPressed();
        return true;
    }
}