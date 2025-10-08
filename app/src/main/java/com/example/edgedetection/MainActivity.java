package com.example.edgedetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    
    private TextureView textureView;
    private Button btnToggleCamera;
    private Button btnToggleFilter;
    private TextView statsTextView;
    
    private CameraHelper cameraHelper;
    private ImageProcessorNative imageProcessor;
    
    private boolean isCameraOpen = false;
    private boolean isEdgeDetectionEnabled = false;
    
    // Frame statistics
    private long frameCount = 0;
    private long lastFrameTime = 0;
    private double fps = 0.0;
    private long totalProcessingTime = 0;
    private int averageProcessingTime = 0;
    private Handler statsHandler = new Handler(Looper.getMainLooper());
    private Runnable statsUpdater = new Runnable() {
        @Override
        public void run() {
            updateStatsDisplay();
            statsHandler.postDelayed(this, 1000); // Update every second
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupListeners();
        initCamera();
        initImageProcessor();
        checkCameraPermission();
        
        // Start stats updater
        statsHandler.post(statsUpdater);
    }
    
    private void initViews() {
        textureView = findViewById(R.id.textureView);
        btnToggleCamera = findViewById(R.id.btnToggleCamera);
        btnToggleFilter = findViewById(R.id.btnToggleFilter);
        statsTextView = findViewById(R.id.statsTextView);
    }
    
    private void setupListeners() {
        btnToggleCamera.setOnClickListener(v -> toggleCamera());
        btnToggleFilter.setOnClickListener(v -> toggleFilter());
    }
    
    private void initCamera() {
        cameraHelper = new CameraHelper(this);
        cameraHelper.setStateCallback(new CameraHelper.CameraStateCallback() {
            @Override
            public void onOpened() {
                runOnUiThread(() -> {
                    isCameraOpen = true;
                    Toast.makeText(MainActivity.this, "Camera opened", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    isCameraOpen = false;
                    Toast.makeText(MainActivity.this, "Camera error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
        
        cameraHelper.setImageAvailableListener(this::processImage);
    }
    
    private void initImageProcessor() {
        imageProcessor = new ImageProcessorNative();
        boolean initialized = imageProcessor.initialize();
        Log.d(TAG, "Image processor initialized: " + initialized);
    }
    
    private void processImage(Image image) {
        if (image == null) return;
        
        long startTime = System.currentTimeMillis();
        
        // Get image data
        // For simplicity, we'll just close the image without processing
        // In a real implementation, we would convert the image to a byte array and process it
        
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        
        // Update statistics
        frameCount++;
        totalProcessingTime += processingTime;
        averageProcessingTime = (int) (totalProcessingTime / frameCount);
        
        if (lastFrameTime > 0) {
            long frameTime = System.currentTimeMillis() - lastFrameTime;
            fps = 0.9 * fps + 0.1 * (1000.0 / frameTime);
        }
        lastFrameTime = System.currentTimeMillis();
        
        // Close the image
        image.close();
    }
    
    private void updateStatsDisplay() {
        String stats = String.format("FPS: %.1f\nProcessing Time: %d ms\nFrames: %d", 
                fps, averageProcessingTime, frameCount);
        statsTextView.setText(stats);
    }
    
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            openCamera();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    private void openCamera() {
        cameraHelper.startBackgroundThread();
        if (textureView.isAvailable()) {
            cameraHelper.openCamera(textureView);
        } else {
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(android.graphics.SurfaceTexture surface, int width, int height) {
                    cameraHelper.openCamera(textureView);
                }

                @Override
                public void onSurfaceTextureSizeChanged(android.graphics.SurfaceTexture surface, int width, int height) {}

                @Override
                public boolean onSurfaceTextureDestroyed(android.graphics.SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(android.graphics.SurfaceTexture surface) {}
            });
        }
    }
    
    private void closeCamera() {
        cameraHelper.closeCamera();
        cameraHelper.stopBackgroundThread();
        isCameraOpen = false;
        Toast.makeText(this, "Camera closed", Toast.LENGTH_SHORT).show();
    }
    
    private void toggleCamera() {
        if (isCameraOpen) {
            closeCamera();
        } else {
            openCamera();
        }
    }
    
    private void toggleFilter() {
        isEdgeDetectionEnabled = !isEdgeDetectionEnabled;
        String filterMode = isEdgeDetectionEnabled ? getString(R.string.edge_detection) : getString(R.string.raw_feed);
        Toast.makeText(this, "Filter: " + filterMode, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onPause() {
        closeCamera();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (isCameraOpen) {
            openCamera();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.release();
        }
        statsHandler.removeCallbacks(statsUpdater);
    }
}