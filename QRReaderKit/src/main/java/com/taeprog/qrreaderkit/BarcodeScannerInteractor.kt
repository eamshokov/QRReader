package com.taeprog.qrreaderkit

import android.content.Context
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Interactor with scanner to use in activity or fragment
 *
 * @property context context to create executor, get system services
 * @property previewView to display video stream from camera
 * @property lifecycle to observe and connect to parent views lifecycle events
 * @property onBarcodeDetected Callback for barcode detected event
 * @constructor
 *
 * @param lifecycleOwner
 */
class BarcodeScannerInteractor(
    val context: Context,
    lifecycleOwner: LifecycleOwner,
    val previewView: PreviewView,
    val lifecycle: Lifecycle,
    var onBarcodeDetected:(result:String, List<PointF>)->Unit = {_,_->}
):LifecycleObserver {
    val barcodeScanner:BarcodeScanner = BarcodeScanner(context, lifecycleOwner, previewView)


    constructor(activity: AppCompatActivity, previewView: PreviewView, onBarcodeDetected:(result:String, List<PointF>)->Unit = {_,_->}) : this(activity, activity, previewView, activity.lifecycle, onBarcodeDetected)
    constructor(frament: Fragment, previewView: PreviewView, onBarcodeDetected:(result:String, List<PointF>)->Unit = {_,_->}):this(frament.requireContext(), frament, previewView, frament.lifecycle, onBarcodeDetected)

    init {
        lifecycle.addObserver(this)
        previewView.flashlightToggle = {
            barcodeScanner.switchTorchState()
        }
        barcodeScanner.onBarcodeDetected = {
            result, corners->
            CoroutineScope(Dispatchers.Main).launch {
                onBarcodeDetected(result, corners)
            }
        }
    }

    /**
     * Stop scanning
     *
     */
    public fun stopScanning(){
        barcodeScanner.stopScanning()
    }

    /**
     * Start scanning
     *
     */
    public fun startScanning(){
        barcodeScanner.startScanning()
    }

    /**
     * Connects to parents lifecycles onCreate method
     *
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(){
        previewView.post{
            barcodeScanner.registerDisplayListener()
            barcodeScanner.bindCameraUseCases()
        }
    }


    /**
     * Connects to parents lifecycles onDestroy method
     *
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(){
        barcodeScanner.shutdownCamera()
    }


}