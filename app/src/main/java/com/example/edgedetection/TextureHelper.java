package com.example.edgedetection;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class TextureHelper {
    private static final String TAG = "TextureHelper";
    
    public static int loadTexture(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null");
            return -1;
        }
        
        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);
        
        if (textureObjectIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL texture object.");
            return -1;
        }
        
        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);
        
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        
        // Load the bitmap into the bound texture
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        
        // Recycle the bitmap, since its data has been loaded into OpenGL
        bitmap.recycle();
        
        // Unbind from the texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        
        return textureObjectIds[0];
    }
    
    public static int createTexture(int width, int height) {
        final int[] textureObjectIds = new int[1];
        GLES20.glGenTextures(1, textureObjectIds, 0);
        
        if (textureObjectIds[0] == 0) {
            Log.e(TAG, "Could not generate a new OpenGL texture object.");
            return -1;
        }
        
        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectIds[0]);
        
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        
        // Create an empty texture
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        
        // Unbind from the texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        
        return textureObjectIds[0];
    }
    
    public static void updateTexture(int textureId, Bitmap bitmap) {
        if (bitmap == null || textureId == -1) {
            return;
        }
        
        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        
        // Update the texture with new bitmap data
        GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
        
        // Unbind from the texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }
}