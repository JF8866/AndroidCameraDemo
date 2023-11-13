package site.feiyuliuxing.camera2test.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.foundation.layout.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import site.feiyuliuxing.camera2test.widgets.FaceGraphic

/**
 * 谷歌的人脸检测SDK
 * https://developers.google.cn/ml-kit/vision/face-detection/android
 */
class FaceDetectionActivity : ImageAnalysisActivity() {
    private var faceDetector: FaceDetector? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initFaceDetector()
    }

    private fun initFaceDetector() {
        // High-accuracy landmark detection and face classification
        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()
        faceDetector = FaceDetection.getClient(highAccuracyOpts)
    }

    private fun detectFace(image: InputImage) {
        faceDetector?.let { detector ->
            detector.process(image)
                .addOnSuccessListener { faces ->
                    graphicOverlay.clear()
                    for (face in faces) {
                        graphicOverlay.add(FaceGraphic(graphicOverlay, face))
                    }
                    graphicOverlay.invalidate()
                }
                .addOnFailureListener { }
        }
    }

    override fun onImageAvailable(image: InputImage) {
        detectFace(image)
    }

    override fun onDestroy() {
        super.onDestroy()
        faceDetector?.close()
    }
}

