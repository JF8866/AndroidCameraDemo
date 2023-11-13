package site.feiyuliuxing.camera2test.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import site.feiyuliuxing.camera2test.R
import site.feiyuliuxing.camera2test.utils.CameraUtil
import site.feiyuliuxing.camera2test.utils.isHasFrontCamera
import site.feiyuliuxing.camera2test.widgets.GraphicOverlay
import java.util.concurrent.Executors

open class ImageAnalysisActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    open lateinit var previewView: PreviewView
    open lateinit var graphicOverlay: GraphicOverlay

    private var imageAnalysis: ImageAnalysis? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK//默认使用后置摄像头
    private val isHasFrontCamera by lazy {
        this.isHasFrontCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        println("是否有前置摄像头: $isHasFrontCamera")

        previewView = findViewById(R.id.previewView)
        graphicOverlay = findViewById(R.id.overlayView)

        previewView.scaleType = PreviewView.ScaleType.FIT_CENTER

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
        val preview: Preview = Preview.Builder()
//            .setTargetResolution(Size(TARGET_WIDTH, TARGET_HEIGHT))
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        preview.setSurfaceProvider(previewView.surfaceProvider)

        imageAnalysis = ImageAnalysis.Builder()
//            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
//            .setTargetResolution(Size(TARGET_WIDTH, TARGET_HEIGHT))
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis?.setAnalyzer(analysisExecutor) { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val formatStr = CameraUtil.imageFormat(imageProxy.format)
            println("图像格式: $formatStr, 尺寸: ${imageProxy.width}*${imageProxy.height}, 旋转角度: $rotationDegrees")
            imageProxy.image?.let { mediaImage ->
                val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    graphicOverlay.setImageSourceInfo(
                        imageProxy.width,
                        imageProxy.height,
                        isImageFlipped
                    )
                } else {
                    graphicOverlay.setImageSourceInfo(
                        imageProxy.height,
                        imageProxy.width,
                        isImageFlipped
                    )
                }
                val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                onImageAvailable(image)
            }
            imageProxy.close()
        }
        var camera = cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )
    }

    open fun onImageAvailable(image: InputImage) {
    }

    override fun onDestroy() {
        super.onDestroy()
        imageAnalysis?.clearAnalyzer()
    }
}


@Composable
fun ImageAnalysisBottomLayout(isHasFrontCamera: Boolean, onCameraIconClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.Black),
        horizontalArrangement = Arrangement.Center
    ) {
        //没前置摄像头就不用切换了
        if (isHasFrontCamera) {
            Icon(
                imageVector = Icons.Filled.FlipCameraIos,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .width(56.dp)
                    .height(56.dp)
                    .clickable {
                        onCameraIconClick()
                    },
            )
        }

    }
}