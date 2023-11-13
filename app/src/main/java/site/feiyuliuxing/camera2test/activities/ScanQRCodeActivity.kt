package site.feiyuliuxing.camera2test.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import site.feiyuliuxing.camera2test.utils.PermissionUtil
import site.feiyuliuxing.camera2test.R
import site.feiyuliuxing.camera2test.widgets.BarcodeGraphic
import site.feiyuliuxing.camera2test.widgets.GraphicOverlay

/**
 * 识别二维码
 */
class ScanQRCodeActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var cameraController: LifecycleCameraController
    private lateinit var graphicOverlay: GraphicOverlay

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //申请权限
        PermissionUtil.requestPermissions(this)

        graphicOverlay = findViewById(R.id.overlayView)
        cameraController = LifecycleCameraController(this)
        cameraController.bindToLifecycle(this)
        previewView = findViewById(R.id.previewView)
        previewView.controller = cameraController
        listenBarcodeScanning()
    }

    override fun onResume() {
        super.onResume()
        graphicOverlay.clear()
    }

    private fun listenBarcodeScanning() {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val barcodeScanner = BarcodeScanning.getClient(options)
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result ->
                result.getValue(barcodeScanner)?.let { barcodeList ->
                    graphicOverlay.clear()
                    barcodeList.forEach { barcode ->
                        //barcode还包含其他一些信息，比如根据位置，区域等可以绘制自己的UI
                        println("### ${barcode.rawValue}")
                        graphicOverlay.add(BarcodeGraphic(graphicOverlay, barcode))
                    }
                    graphicOverlay.invalidate()
                }
            })
    }
}