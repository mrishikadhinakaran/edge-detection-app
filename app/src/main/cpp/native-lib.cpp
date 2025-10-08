#include <jni.h>
#include <string>
#include <android/log.h>
#include "ImageProcessor.h"

#define LOG_TAG "EdgeDetection-Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_edgedetection_ImageProcessorNative_createInstance(JNIEnv *env, jobject thiz) {
    ImageProcessor* processor = new ImageProcessor();
    LOGI("Created ImageProcessor instance: %p", processor);
    return reinterpret_cast<jlong>(processor);
}

JNIEXPORT void JNICALL
Java_com_example_edgedetection_ImageProcessorNative_destroyInstance(JNIEnv *env, jobject thiz,
                                                                   jlong instance) {
    ImageProcessor* processor = reinterpret_cast<ImageProcessor*>(instance);
    if (processor) {
        delete processor;
        LOGI("Destroyed ImageProcessor instance: %p", processor);
    }
}

JNIEXPORT jboolean JNICALL
Java_com_example_edgedetection_ImageProcessorNative_initialize(JNIEnv *env, jobject thiz,
                                                              jlong instance) {
    ImageProcessor* processor = reinterpret_cast<ImageProcessor*>(instance);
    if (processor) {
        bool result = processor->initialize();
        LOGI("Initialized ImageProcessor: %s", result ? "success" : "failed");
        return result;
    }
    return false;
}

JNIEXPORT jbyteArray JNICALL
Java_com_example_edgedetection_ImageProcessorNative_processFrame(JNIEnv *env, jobject thiz,
                                                                 jlong instance,
                                                                 jbyteArray inputFrame,
                                                                 jint width, jint height) {
    ImageProcessor* processor = reinterpret_cast<ImageProcessor*>(instance);
    if (!processor) return nullptr;

    jsize frameSize = env->GetArrayLength(inputFrame);
    jbyte* frameData = env->GetByteArrayElements(inputFrame, nullptr);
    
    if (!frameData) return nullptr;
    
    std::vector<uint8_t> result = processor->processFrame(
        reinterpret_cast<uint8_t*>(frameData), 
        static_cast<size_t>(frameSize),
        width, height
    );
    
    env->ReleaseByteArrayElements(inputFrame, frameData, JNI_ABORT);
    
    if (result.empty()) return nullptr;
    
    jbyteArray resultArray = env->NewByteArray(result.size());
    env->SetByteArrayRegion(resultArray, 0, result.size(), 
                           reinterpret_cast<const jbyte*>(result.data()));
    
    return resultArray;
}

}