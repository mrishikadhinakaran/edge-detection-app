package com.example.edgedetection;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CameraHelper {
    private static final String TAG = "CameraHelper";
    
    private Context context;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    
    private String cameraId;
    private Size previewSize;
    private CaptureRequest.Builder previewRequestBuilder;
    
    private CameraStateCallback stateCallback;
    private ImageAvailableListener imageAvailableListener;
    
    public interface CameraStateCallback {
        void onOpened();
        void onError(String error);
    }
    
    public interface ImageAvailableListener {
        void onImageAvailable(Image image);
    }
    
    public CameraHelper(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }
    
    public void setStateCallback(CameraStateCallback callback) {
        this.stateCallback = callback;
    }
    
    public void setImageAvailableListener(ImageAvailableListener listener) {
        this.imageAvailableListener = listener;
    }
    
    public void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }
    
    public void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted while stopping background thread", e);
            }
        }
    }
    
    public void openCamera(TextureView textureView) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            if (stateCallback != null) {
                stateCallback.onError("Camera permission not granted");
            }
            return;
        }
        
        try {
            // Choose the camera
            chooseCamera();
            
            // Set up image reader for capturing frames
            imageReader = ImageReader.newInstance(
                    previewSize.getWidth(), previewSize.getHeight(),
                    android.graphics.ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(reader -> {
                if (imageAvailableListener != null) {
                    Image image = reader.acquireLatestImage();
                    if (image != null) {
                        imageAvailableListener.onImageAvailable(image);
                    }
                }
            }, backgroundHandler);
            
            // Open the camera
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCameraPreviewSession(textureView);
                    if (stateCallback != null) {
                        stateCallback.onOpened();
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                    if (stateCallback != null) {
                        stateCallback.onError("Camera error: " + error);
                    }
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to open camera", e);
            if (stateCallback != null) {
                stateCallback.onError("Failed to open camera: " + e.getMessage());
            }
        }
    }
    
    private void chooseCamera() throws CameraAccessException {
        String[] cameraIds = cameraManager.getCameraIdList();
        if (cameraIds.length == 0) {
            throw new RuntimeException("No cameras available");
        }
        
        // Use the first available camera (usually the back camera)
        cameraId = cameraIds[0];
        
        CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        
        if (map == null) {
            throw new RuntimeException("Cannot get camera configuration");
        }
        
        // Choose a suitable preview size
        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
        previewSize = chooseOptimalSize(sizes, 1280, 720);
    }
    
    private Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, (lhs, rhs) ->
                    Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                            (long) rhs.getWidth() * rhs.getHeight()));
        } else {
            return choices[0];
        }
    }
    
    private void createCameraPreviewSession(TextureView textureView) {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture == null) {
                if (stateCallback != null) {
                    stateCallback.onError("Texture is null");
                }
                return;
            }
            
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface surface = new Surface(texture);
            
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);
            previewRequestBuilder.addTarget(imageReader.getSurface());
            
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) return;
                            
                            captureSession = session;
                            try {
                                previewRequestBuilder.set(CaptureRequest.CONTROL_MODE,
                                        CaptureRequest.CONTROL_MODE_AUTO);
                                
                                CaptureRequest previewRequest = previewRequestBuilder.build();
                                captureSession.setRepeatingRequest(previewRequest,
                                        null, backgroundHandler);
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "Failed to set up preview", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            if (stateCallback != null) {
                                stateCallback.onError("Failed to configure camera");
                            }
                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to create preview session", e);
        }
    }
    
    public void closeCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }
    
    public Size getPreviewSize() {
        return previewSize;
    }
}