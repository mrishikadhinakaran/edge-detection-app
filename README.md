# Edge Detection App

An Android application that captures camera frames, processes them using OpenCV in C++ (via JNI), and displays the processed output using OpenGL ES. Additionally includes a TypeScript-based web viewer to demonstrate bridging native processing results to a web interface.

## Features

### Android App
- Real-time camera feed capture using Camera2 API
- OpenCV C++ processing with JNI integration
- Canny Edge Detection and Grayscale filtering
- OpenGL ES 2.0 rendering pipeline
- Toggle between raw camera feed and edge-detected output
- Real-time FPS counter and frame statistics

### Web Viewer
- TypeScript-based web interface for viewing processed frames
- Frame statistics display (FPS, resolution, processing time)
- Simulated streaming from Android app

## Architecture

```mermaid
graph TD
    A[Android Camera2 API] --> B[Camera Feed]
    B --> C[JNI Layer]
    C --> D[OpenCV C++ Processing]
    D --> E[OpenGL ES Renderer]
    E --> F[Display Output]
    D --> G[Web Viewer WebSocket]
    G --> H[TypeScript Web Interface]
```

### Component Structure
- `/app` - Android Java/Kotlin code
- `/jni` - C++ OpenCV processing
- `/gl` - OpenGL renderer classes
- `/web` - TypeScript web viewer

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK API level 24+
- NDK (Native Development Kit)
- CMake 3.18.1+
- OpenCV 4.11.0 Android SDK

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd edge-detection-app
   ```

2. **Download OpenCV SDK**
   - Download OpenCV 4.11.0 Android SDK from [OpenCV Releases](https://opencv.org/releases/)
   - Extract to `app/src/main/jniLibs/opencv`

3. **Configure OpenCV in build.gradle**
   ```gradle
   android {
       sourceSets {
           main {
               jniLibs.srcDirs = ['src/main/jniLibs']
           }
       }
   }
   
   dependencies {
       implementation project(':opencv')
   }
   ```

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run the Android app**
   ```bash
   ./gradlew installDebug
   ```

6. **Run the Web Viewer**
   ```bash
   cd web
   npm install
   npm start
   ```

## Testing

### Android Tests
```bash
./gradlew test
```

### Web Tests
```bash
cd web
npm test
```

### OpenCV Processing
OpenCV algorithms implemented:
- Grayscale conversion
- Canny Edge Detection
- Gaussian Blur (optional)

### OpenGL Rendering
OpenGL ES 2.0 implementation:
- Vertex and fragment shaders
- Texture mapping
- Real-time rendering pipeline

