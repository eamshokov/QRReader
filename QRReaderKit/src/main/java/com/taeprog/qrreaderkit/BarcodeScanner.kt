package com.taeprog.qrreaderkit

import android.content.Context
import android.graphics.*
import android.hardware.display.DisplayManager
import android.util.DisplayMetrics
import android.util.Size
import androidx.camera.core.*
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.taeprog.qrreaderkit.interfaces.IQrCodeImageAnalizer
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Implements initialization of cameraX and Qr code analyzer.
 *
 * @property context context to create executor, get system services
 * @property lifecycleOwner to create camera connection
 * @property previewView to feed it video stream from camera
 * @constructor Create empty Barcode scanner
 */
class BarcodeScanner(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {

    /**
     * Indicator of torch state
     */
    var torchEnabled = false
        private set

    /**
     * Callback for barcode detected event
     */
    var onBarcodeDetected : (String, List<PointF>)->Unit = {_,_->}

    /**
     * Callback in case of error
     */
    var onError: ()->Unit={}

    private var imageSize:Size = Size(0,0)
    private lateinit var qrImageAnalyzer: IQrCodeImageAnalizer
    private var camera: Camera? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private val onDetected : (String, Size, List<PointF>)->Unit = {data,size,points->
        imageSize = size
        onBarcodeDetected(data, points)
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = Unit
    }

    private val displayManager by lazy {
        context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    /**
     * Registers display listener on display manager
     * Must be called in onCreateView
     */
    fun registerDisplayListener(){
        displayManager.registerDisplayListener(displayListener, null)
    }

    /**
     * Unregisters display listener and shuts down camera executor
     * Must be called in onDestroy
     */
    fun shutdownCamera(){
        displayManager.unregisterDisplayListener(displayListener)
        cameraExecutor.shutdown()
    }

    /**
     * Starts scanning
     *
     */
    fun startScanning(){
        qrImageAnalyzer.startScanning()
    }

    /**
     * Stops scanning
     *
     */
    fun stopScanning(){
        qrImageAnalyzer.stopScanning()
    }

    /**
     * Turns on and off flashlight
     *
     */
    fun switchTorchState(){
        camera?.cameraControl?.enableTorch(!torchEnabled)
        torchEnabled = !torchEnabled
    }

    /**
     * Camera and analysers setup
     *
     */
    fun bindCameraUseCases() {
        val metrics = DisplayMetrics().also { previewView.display?.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = previewView.display?.rotation
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val previewBuilder = Preview.Builder().setTargetAspectRatio(screenAspectRatio)
            if(rotation != null){
                previewBuilder.setTargetRotation(rotation)
            }
            val preview = previewBuilder.build()
            preview.setSurfaceProvider(previewView.preview.surfaceProvider)
            val imageAnalyzerBuilder = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetAspectRatio(screenAspectRatio)
            if(rotation!=null){
                imageAnalyzerBuilder.setTargetRotation(rotation)
            }
            qrImageAnalyzer = QRCodeImageAnalyzer(onDetected, onError)

            val imageAnalyzer =imageAnalyzerBuilder.build()
                    .also {
                        it.setAnalyzer(cameraExecutor, qrImageAnalyzer)
                    }
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, imageAnalyzer, preview)

        }, ContextCompat.getMainExecutor(context))
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    companion object {

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}