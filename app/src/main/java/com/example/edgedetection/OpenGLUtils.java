package com.example.edgedetection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class OpenGLUtils {
    
    public static FloatBuffer createFloatBuffer(float[] coords) {
        // Initialize the texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = bb.asFloatBuffer();
        floatBuffer.put(coords);
        floatBuffer.position(0);
        return floatBuffer;
    }
    
    public static ShortBuffer createShortBuffer(short[] coords) {
        // Initialize the draw list buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(coords.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        ShortBuffer drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(coords);
        drawListBuffer.position(0);
        return drawListBuffer;
    }
}