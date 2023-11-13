package site.feiyuliuxing.camera2test.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import site.feiyuliuxing.camera2test.R
import site.feiyuliuxing.camera2test.utils.CameraUtil
import site.feiyuliuxing.camera2test.utils.YuvToRgbConverter
import site.feiyuliuxing.camera2test.utils.isHasFrontCamera
import site.feiyuliuxing.camera2test.widgets.FaceGraphic
import site.feiyuliuxing.camera2test.widgets.GraphicOverlay
import site.feiyuliuxing.camera2test.widgets.MyPreviewView
import java.util.concurrent.Executors

open class NoPreviewActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    open lateinit var previewView: MyPreviewView
    open lateinit var graphicOverlay: GraphicOverlay

    private var imageAnalysis: ImageAnalysis? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK//默认使用后置摄像头
    private val isHasFrontCamera by lazy {
        this.isHasFrontCamera()
    }

    private lateinit var yuvToRgbConverter: YuvToRgbConverter
    private val matrix = Matrix()

    private val faceDetector: FaceDetector by lazy {
        // High-accuracy landmark detection and face classification
        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()
        FaceDetection.getClient(highAccuracyOpts)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_preview)

        supportActionBar?.hide()
        yuvToRgbConverter = YuvToRgbConverter(this)

        println("是否有前置摄像头: $isHasFrontCamera")

        previewView = findViewById(R.id.previewView)
        graphicOverlay = findViewById(R.id.overlayView)

        findViewById<ComposeView>(R.id.composeView).apply {
            visibility = View.VISIBLE
        }.setContent {
            ImageAnalysisBottomLayout(isHasFrontCamera) {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                    CameraSelector.LENS_FACING_BACK
                } else {
                    CameraSelector.LENS_FACING_FRONT
                }
                val cameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                imageAnalysis?.clearAnalyzer()
                bindPreview(cameraProvider)
            }
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        imageAnalysis = ImageAnalysis.Builder()
//            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .setTargetRotation(Surface.ROTATION_90)
//            .setTargetResolution(Size(1920, 1080))
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis?.setAnalyzer(analysisExecutor) { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val formatStr = CameraUtil.imageFormat(imageProxy.format)
            val width = imageProxy.width
            val height = imageProxy.height
            println("图像格式: $formatStr, 尺寸: ${width}*${height}, 旋转角度: $rotationDegrees")
            imageProxy.image?.let { mediaImage ->
                var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                yuvToRgbConverter.yuvToRgb(mediaImage, bitmap)
//                if(rotationDegrees != 0) {
                matrix.setRotate(90f, width / 2f, height / 2f)
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
//                }
                previewView.update(bitmap)
                onBitmapAvailable(bitmap)

                graphicOverlay.setImageSourceInfo(
                    bitmap.width,
                    bitmap.height,
                    false
                )
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                detectFace(inputImage)

                /*val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    graphicOverlay.setImageSourceInfo(
                        width,
                        height,
                        isImageFlipped
                    )
                } else {
                    graphicOverlay.setImageSourceInfo(
                        height,
                        width,
                        isImageFlipped
                    )
                }
                val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                detectFace(image)*/
            }
            imageProxy.close()
        }
        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
    }

    open fun onBitmapAvailable(bitmap: Bitmap) {
    }

    private fun detectFace(image: InputImage) {
        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                graphicOverlay.clear()
                for (face in faces) {
                    graphicOverlay.add(FaceGraphic(graphicOverlay, face))
                }
                graphicOverlay.invalidate()
            }
            .addOnFailureListener { }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageAnalysis?.clearAnalyzer()
        faceDetector.close()
    }
}