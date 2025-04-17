package com.example.facialflex;

import static android.app.Activity.RESULT_OK;
import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GalleryPageFragment extends Fragment {

    private GridView gridView;
    private GalleryAdapter galleryAdapter;
    private ArrayList<ImageData> imageList;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;
    private String currentPhotoPath;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery_page, container, false);

        gridView = view.findViewById(R.id.gridView);
        imageList = new ArrayList<>();
        galleryAdapter = new GalleryAdapter(getContext(), imageList);
        gridView.setAdapter(galleryAdapter);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        fetchImagesFromFirebase();

        // Set up the FAB click listener
        FloatingActionButton fabCamera = view.findViewById(R.id.fab_camera);
        fabCamera.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());

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

        fetchImagesFromFirebase();
        // Once done, stop the refresh animation
        swipeRefreshLayout.setRefreshing(false);
    }

    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera(); // Open camera if permission is granted
        } else {
            // Request permission if not granted
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }


    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getContext(), "Error creating file for image", Toast.LENGTH_SHORT).show();
                return;
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        getActivity().getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Toast.makeText(getContext(), "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            uploadImageToFirebase();
        } else {
            Toast.makeText(getContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebase() {
        if (currentPhotoPath != null) {
            File file = new File(currentPhotoPath);
            Uri fileUri = Uri.fromFile(file);
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.getDefault()).format(new Date());
            String storagePath = "monitoring/" + file.getName();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(storagePath);
            UploadTask uploadTask = storageRef.putFile(fileUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                // Get the download URL and save it in the database
                saveImageMetadataToDatabase(uri.toString(), timeStamp, storagePath);
                Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
            })).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error uploading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void saveImageMetadataToDatabase(String url, String timestamp, String filePath) {
        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
            Query query = reference.orderByChild("uname").equalTo(username);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String userKey = userSnapshot.getKey();
                            DatabaseReference monitoringRef = FirebaseDatabase.getInstance().getReference("user").child(userKey).child("monitoring");
                            Map<String, Object> data = new HashMap<>();
                            data.put("url", url);
                            data.put("timestamp", timestamp);
                            data.put("filePath", filePath); // Save the file path here
                            monitoringRef.push().setValue(data);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error saving metadata: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera(); // Open camera if permission granted
            } else {
                Toast.makeText(getContext(), "Camera permission is required to take a picture", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void fetchImagesFromFirebase() {
        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
            Query query = reference.orderByChild("uname").equalTo(username);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String userKey = userSnapshot.getKey();
                            loadImages(userKey);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadImages(String userKey) {
        DatabaseReference monitoringRef = FirebaseDatabase.getInstance().getReference("user").child(userKey).child("monitoring");
        monitoringRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imageList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String url = snapshot.child("url").getValue(String.class);
                    String timestamp = snapshot.child("timestamp").getValue(String.class);
                    if (url != null && timestamp != null) {
                        imageList.add(new ImageData(url, timestamp)); // Add both URL and timestamp
                    }
                }
                galleryAdapter.notifyDataSetChanged(); // Notify adapter about data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error fetching images", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
