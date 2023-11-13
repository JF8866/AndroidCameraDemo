package site.feiyuliuxing.camera2test.utils

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata

fun Context.isHasFrontCamera(): Boolean {
    val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    return cameraManager.cameraIdList.any { cameraId ->
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
        return@any lensFacing == CameraMetadata.LENS_FACING_FRONT
    }
}

object CameraUtil {

    fun printCameraCharacteristics(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            val facingStr = if (lensFacing == CameraMetadata.LENS_FACING_FRONT) "前" else "后"
            val sss = "${facingStr}置摄像头${cameraId}"
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.let { configurationMap ->
                    val supportedOutputFormats = configurationMap.outputFormats.joinToString(
                        separator = ", ", transform = CameraUtil::imageFormat
                    )
                    println("${sss}支持的输出格式: [$supportedOutputFormats]")
                    for (format in configurationMap.outputFormats) {
                        println("$sss ${imageFormat(format)} 支持的输出尺寸")
                        val sizes = configurationMap.getOutputSizes(format)
                        for (size in sizes) {
                            println("size: ${size.width}*${size.height}")
                        }
                    }
                }
        }
    }


    fun imageFormat(format: Int): String = when (format) {
        ImageFormat.PRIVATE -> "PRIVATE"
        ImageFormat.YUV_420_888 -> "YUV_420_888"
        ImageFormat.JPEG -> "JPEG"
        ImageFormat.DEPTH16 -> "DEPTH16"
        else -> format.toString()
    }
}