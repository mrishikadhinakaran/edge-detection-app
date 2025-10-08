#ifndef IMAGE_PROCESSOR_H
#define IMAGE_PROCESSOR_H

#include <vector>
#include <cstdint>

// Forward declarations for OpenCV classes
namespace cv {
    class Mat;
}

class ImageProcessor {
public:
    ImageProcessor();
    ~ImageProcessor();
    
    bool initialize();
    std::vector<uint8_t> processFrame(const uint8_t* data, size_t dataSize, int width, int height);
    
private:
    bool initialized;
    cv::Mat* inputMat;
    cv::Mat* outputMat;
    cv::Mat* grayMat;
    cv::Mat* edgesMat;
    
    void processWithOpenCV(const uint8_t* data, int width, int height);
};

#endif // IMAGE_PROCESSOR_H