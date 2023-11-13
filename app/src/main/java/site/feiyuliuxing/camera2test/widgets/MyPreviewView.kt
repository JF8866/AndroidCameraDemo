package site.feiyuliuxing.camera2test.widgets

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class MyPreviewView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {
    private var mDrawingThread: DrawingThread? = null
    private var imageWidth = 0
    private var imageHeight = 0
    private var xOffset = 0
    private var yOffset = 0
    private var bitmap: Bitmap? = null

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mDrawingThread = DrawingThread().apply { start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        println("surfaceChanged() - $width x $height")
        val ratio = 16f / 9f
        if(height.toFloat() / width > ratio) {
            imageWidth = width
            imageHeight = (width * ratio).toInt()
            xOffset = 0
            yOffset = (height - imageHeight) / 2
        } else {
            imageHeight = height
            imageWidth = (height / ratio).toInt()
            yOffset = 0
            xOffset = (width - imageWidth) / 2
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mDrawingThread?.cancel()
    }

    fun update(bitmap: Bitmap) {
        this.bitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, false)
        mDrawingThread?.sendMessage()
    }

    private fun draw() {
        bitmap?.let {
            holder.lockCanvas()?.let { canvas ->
                canvas.drawBitmap(it, xOffset.toFloat(), yOffset.toFloat(), null)
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    private inner class DrawingThread : Thread() {
        var handler: Handler? = null

        override fun run() {
            Looper.prepare()
            handler = Handler(Looper.myLooper()!!) {
                draw()
                true
            }
            Looper.loop()
        }

        fun sendMessage() {
            handler?.obtainMessage()?.sendToTarget()
        }

        fun cancel() {
            handler?.looper?.quitSafely()
        }
    }
}