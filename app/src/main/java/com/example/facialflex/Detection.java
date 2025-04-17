package com.example.facialflex;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class Detection extends AppCompatActivity {

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private WebAppInterface webAppInterface;
    private TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Detect Me!");
        // Enable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Request permissions if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1);
        }

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false); // Allow autoplay media

        // Add JavaScript interface
        webAppInterface = new WebAppInterface();
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        // Retrieve the username from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
        String username = prefs.getString("username", null); // Fetch stored username

        // Set WebViewClient and pass username via HTTP headers
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (username != null) {
                        // Pass the username in headers if available
                        request.getRequestHeaders().put("X-Requested-From", "webview");
                        request.getRequestHeaders().put("username", username);  // Pass the username
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });

        // Handle WebChromeClient for camera permissions and file chooser
        webView.setWebChromeClient(new WebChromeClient() {

            // Handle permission request (for camera, etc.)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request.grant(request.getResources());
                    }
                });
            }

            // Handle file chooser for camera
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                Detection.this.filePathCallback = filePathCallback;

                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, captureIntent);

                startActivityForResult(chooserIntent, 2);
                return true;
            }
        });

        // Load the URL
        webView.loadUrl("https://facialflex-olfu.com/detectme_mobile.php");

        // Test TTS Button
        Button testTTSButton = findViewById(R.id.speechBtn);
        testTTSButton.setOnClickListener(v -> {
            webAppInterface.speak("Smile and Blink your eyes. Maintain direct eye contact with the camera.");
        });
    }

    // JavaScript interface class
    public class WebAppInterface {

        public WebAppInterface() {
            tts = new TextToSpeech(Detection.this, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.US); // Set language, you can adjust this
                }
            });
        }

        @JavascriptInterface
        public void speak(String text) {
            if (tts != null && text != null && !text.isEmpty()) {
                Log.d("TTS", "Speaking: " + text);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                Log.d("TTS", "TTS not initialized or text is empty");
            }
        }

        @JavascriptInterface
        public void saveResult(String result) {
            // Save the overall faceStatus (Normal or Bell's Palsy Detected)
            saveToFirebase("faceStatus", result);
        }

        @JavascriptInterface
        public void saveIndividualResult(String key, String result) {
            // Save the individual result (e.g., res1, res2, etc.)
            saveToFirebase(key, result);
        }

        @JavascriptInterface
        public void goToFragment(String fragmentName) {
            runOnUiThread(() -> {
                switch (fragmentName) {
                    case "TreatmentAndExercises":
                        // Start MainActivity
                        Intent intent = new Intent(Detection.this, MainActivity.class);
                        intent.putExtra("fragment", fragmentName);
                        startActivity(intent);
                        break;
                    default:
                        // Handle default case if necessary
                        break;
                }
            });
        }

        private void saveToFirebase(String field, String value) {
            // Get the logged-in user's username from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
            String username = prefs.getString("username", null);

            if (username != null) {
                // Query Firebase to find the user's unique key
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
                Query query = reference.orderByChild("uname").equalTo(username);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                // Get the user's unique key
                                String userKey = userSnapshot.getKey();
                                // Save the field (either faceStatus or res1, res2, etc.) under this user's unique key
                                if (userKey != null) {
                                    reference.child(userKey).child(field).setValue(value);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle possible errors
                    }
                });
            }
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back arrow click here
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(result != null ? new Uri[]{result} : null);
                filePathCallback = null;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

}
