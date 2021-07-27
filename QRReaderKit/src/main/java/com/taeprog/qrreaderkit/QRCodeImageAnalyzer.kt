package com.taeprog.qrreaderkit

import android.graphics.ImageFormat.*
import android.graphics.PointF
import android.util.Log
import android.util.Size
import android.util.SizeF
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.taeprog.qrreaderkit.interfaces.IQrCodeImageAnalizer

/**
 * Qr code image analyzer
 *
 * @property onDetect callback for barcode detected event
 * @property onNotDetected callback for barcode not detected event
 * @constructor
 */
class QRCodeImageAnalyzer(
        val onDetect: (String, Size, List<PointF>)->Unit= {_,_,_->},
        val onNotDetected: ()->Unit={}
): IQrCodeImageAnalizer {
    private var scanning:Boolean = true

    override fun startScanning(){
        scanning = true
    }

    override fun stopScanning(){
        scanning = false
    }

    override fun analyze(image: ImageProxy) {

        if(scanning &&(image.format == YUV_420_888 || image.format == YUV_422_888 ||
                image.format == YUV_444_888)){

            val source = getSource(image)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            try{
                val result = QRCodeReader().decode(binaryBitmap)
                onDetect(result.text, Size(image.width, image.height), result.resultPoints.map {
                    PointF(it.x, it.y)
                } )
            }catch(e:Exception){
                onNotDetected()
            }
        }
        image.close()
    }

    private fun getSource(image: ImageProxy): PlanarYUVLuminanceSource {
        val byteBuffer = image.planes[0].buffer
        val imageData = ByteArray(byteBuffer.capacity())
        byteBuffer.get(imageData)
        return PlanarYUVLuminanceSource(
            imageData, image.width, image.height, 0 ,0,
            image.width, image.height, false
        )
    }
}