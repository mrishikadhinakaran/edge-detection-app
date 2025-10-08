package com.example.edgedetection;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "OpenGLRenderer";
    
    // Vertex shader source code
    private static final String VERTEX_SHADER_CODE =
            "attribute vec4 aPosition;" +
            "attribute vec2 aTexCoord;" +
            "varying vec2 vTexCoord;" +
            "void main() {" +
            "  gl_Position = aPosition;" +
            "  vTexCoord = aTexCoord;" +
            "}";
    
    // Fragment shader source code for RGB texture
    private static final String FRAGMENT_SHADER_CODE_RGB =
            "precision mediump float;" +
            "varying vec2 vTexCoord;" +
            "uniform sampler2D uTexture;" +
            "void main() {" +
            "  gl_FragColor = texture2D(uTexture, vTexCoord);" +
            "}";
    
    // Fragment shader source code for grayscale texture
    private static final String FRAGMENT_SHADER_CODE_GRAY =
            "precision mediump float;" +
            "varying vec2 vTexCoord;" +
            "uniform sampler2D uTexture;" +
            "void main() {" +
            "  vec4 color = texture2D(uTexture, vTexCoord);" +
            "  float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));" +
            "  gl_FragColor = vec4(gray, gray, gray, color.a);" +
            "}";
    
    private int programRGB;
    private int programGray;
    private int currentProgram;
    
    private float[] squareCoords = {
            -1.0f,  1.0f, 0.0f,  // top left
            -1.0f, -1.0f, 0.0f,  // bottom left
             1.0f, -1.0f, 0.0f,  // bottom right
             1.0f,  1.0f, 0.0f   // top right
    };
    
    private float[] textureCoords = {
            0.0f, 0.0f,  // top left
            0.0f, 1.0f,  // bottom left
            1.0f, 1.0f,  // bottom right
            1.0f, 0.0f   // top right
    };
    
    private short[] drawOrder = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    
    private int positionHandle;
    private int texCoordHandle;
    private int textureHandle;
    
    private int textureId = -1;
    private boolean textureInitialized = false;
    private boolean useGrayscaleShader = false;
    
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        // Initialize shaders and program
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE);
        int fragmentShaderRGB = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE_RGB);
        int fragmentShaderGray = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE_GRAY);
        
        programRGB = GLES20.glCreateProgram();
        GLES20.glAttachShader(programRGB, vertexShader);
        GLES20.glAttachShader(programRGB, fragmentShaderRGB);
        GLES20.glLinkProgram(programRGB);
        
        programGray = GLES20.glCreateProgram();
        GLES20.glAttachShader(programGray, vertexShader);
        GLES20.glAttachShader(programGray, fragmentShaderGray);
        GLES20.glLinkProgram(programGray);
        
        currentProgram = programRGB;
    }
    
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
    
    @Override
    public void onDrawFrame(GL10 unused) {
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        
        if (textureId != -1) {
            // Use the appropriate shader program
            GLES20.glUseProgram(currentProgram);
            
            // Get handle to vertex shader's aPosition member
            positionHandle = GLES20.glGetAttribLocation(currentProgram, "aPosition");
            
            // Enable a handle to the triangle vertices
            GLES20.glEnableVertexAttribArray(positionHandle);
            
            // Prepare the square coordinates data
            GLES20.glVertexAttribPointer(positionHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, OpenGLUtils.createFloatBuffer(squareCoords));
            
            // Get handle to texture coordinates location
            texCoordHandle = GLES20.glGetAttribLocation(currentProgram, "aTexCoord");
            
            // Enable generic vertex attribute array
            GLES20.glEnableVertexAttribArray(texCoordHandle);
            
            // Prepare the texture coordinates
            GLES20.glVertexAttribPointer(texCoordHandle, 2,
                    GLES20.GL_FLOAT, false,
                    0, OpenGLUtils.createFloatBuffer(textureCoords));
            
            // Get handle to shape's transformation matrix
            textureHandle = GLES20.glGetUniformLocation(currentProgram, "uTexture");
            
            // Set the texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(textureHandle, 0);
            
            // Draw the square
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                    GLES20.GL_UNSIGNED_SHORT, OpenGLUtils.createShortBuffer(drawOrder));
            
            // Disable vertex array
            GLES20.glDisableVertexAttribArray(positionHandle);
            GLES20.glDisableVertexAttribArray(texCoordHandle);
        }
    }
    
    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }
    
    public void setUseGrayscaleShader(boolean useGrayscale) {
        this.useGrayscaleShader = useGrayscale;
        currentProgram = useGrayscale ? programGray : programRGB;
    }
    
    private int loadShader(int type, String shaderCode) {
        // Create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        
        // Add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        
        return shader;
    }
}