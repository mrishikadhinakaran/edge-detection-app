package com.example.edgedetection;

public class ImageProcessorNative {
    static {
        System.loadLibrary("edgedetection");
    }
    
    private long instance;
    
    public ImageProcessorNative() {
        instance = createInstance();
    }
    
    public boolean initialize() {
        if (instance != 0) {
            return initialize(instance);
        }
        return false;
    }
    
    public byte[] processFrame(byte[] inputFrame, int width, int height) {
        if (instance != 0 && inputFrame != null) {
            return processFrame(instance, inputFrame, width, height);
        }
        return null;
    }
    
    public void release() {
        if (instance != 0) {
            destroyInstance(instance);
            instance = 0;
        }
    }
    
    // Native methods
    private static native long createInstance();
    private static native void destroyInstance(long instance);
    private static native boolean initialize(long instance);
    private static native byte[] processFrame(long instance, byte[] inputFrame, int width, int height);
}