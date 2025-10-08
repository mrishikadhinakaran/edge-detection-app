#include "ImageProcessor.h"
#include <opencv2/opencv.hpp>
#include <android/log.h>

#define LOG_TAG "ImageProcessor"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

ImageProcessor::ImageProcessor() : initialized(false), inputMat(nullptr), outputMat(nullptr), grayMat(nullptr), edgesMat(nullptr) {
    LOGI("ImageProcessor constructor");
}

ImageProcessor::~ImageProcessor() {
    LOGI("ImageProcessor destructor");
    if (inputMat) delete inputMat;
    if (outputMat) delete outputMat;
    if (grayMat) delete grayMat;
    if (edgesMat) delete edgesMat;
}

bool ImageProcessor::initialize() {
    LOGI("Initializing ImageProcessor");
    // Initialize OpenCV components here
    initialized = true;
    return true;
}

std::vector<uint8_t> ImageProcessor::processFrame(const uint8_t* data, size_t dataSize, int width, int height) {
    LOGI("Processing frame: %dx%d, size: %zu", width, height, dataSize);
    
    if (!initialized) {
        LOGE("ImageProcessor not initialized");
        return std::vector<uint8_t>();
    }
    
    // Process the frame with OpenCV
    processWithOpenCV(data, width, height);
    
    // Convert outputMat to byte array
    if (outputMat && !outputMat->empty()) {
        std::vector<uint8_t> result;
        std::vector<int> compression_params;
        compression_params.push_back(cv::IMWRITE_JPEG_QUALITY);
        compression_params.push_back(80);
        
        cv::imencode(".jpg", *outputMat, result, compression_params);
        return result;
    }
    
    return std::vector<uint8_t>();
}

void ImageProcessor::processWithOpenCV(const uint8_t* data, int width, int height) {
    try {
        // Create input matrix from YUV data
        // For simplicity, we'll assume the data is already in RGB format
        if (!inputMat) inputMat = new cv::Mat(height, width, CV_8UC3);
        
        // Copy data to input matrix
        memcpy(inputMat->data, data, width * height * 3);
        
        // Initialize output matrices if needed
        if (!outputMat) outputMat = new cv::Mat(height, width, CV_8UC3);
        if (!grayMat) grayMat = new cv::Mat(height, width, CV_8UC1);
        if (!edgesMat) edgesMat = new cv::Mat(height, width, CV_8UC1);
        
        // Convert to grayscale
        cv::cvtColor(*inputMat, *grayMat, cv::COLOR_RGB2GRAY);
        
        // Apply Canny edge detection
        cv::Canny(*grayMat, *edgesMat, 50, 150);
        
        // Convert edges back to 3-channel for display
        cv::cvtColor(*edgesMat, *outputMat, cv::COLOR_GRAY2RGB);
        
        LOGI("OpenCV processing completed successfully");
    } catch (const std::exception& e) {
        LOGE("Error in OpenCV processing: %s", e.what());
    }
}