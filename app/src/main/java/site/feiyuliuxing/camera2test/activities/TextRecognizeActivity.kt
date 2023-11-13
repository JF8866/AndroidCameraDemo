package site.feiyuliuxing.camera2test.activities

import android.os.Bundle
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import site.feiyuliuxing.camera2test.widgets.TextGraphic

/**
 * 文字识别：https://developers.google.cn/ml-kit/vision/text-recognition/v2/android
 */
class TextRecognizeActivity : ImageAnalysisActivity() {
    private lateinit var textRecognizer: TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // When using Latin script library
//        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // When using Chinese script library
        textRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }

    override fun onImageAvailable(image: InputImage) {
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val resultText = visionText.text
                println(resultText)
                graphicOverlay.clear()
                graphicOverlay.add(TextGraphic(graphicOverlay, visionText,
                    shouldGroupTextInBlocks = true,
                    showLanguageTag = false,
                    showConfidence = false
                ))
                graphicOverlay.invalidate()
                /*for (block in visionText.textBlocks) {
                        val blockText = block.text
                        val blockCornerPoints = block.cornerPoints
                        val blockFrame = block.boundingBox
                        for (line in block.lines) {
                            val lineText = line.text
                            val lineCornerPoints = line.cornerPoints
                            val lineFrame = line.boundingBox
                            for (element in line.elements) {
                                val elementText = element.text
                                val elementCornerPoints = element.cornerPoints
                                val elementFrame = element.boundingBox
                            }
                        }
                    }*/
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        textRecognizer.close()
    }
}