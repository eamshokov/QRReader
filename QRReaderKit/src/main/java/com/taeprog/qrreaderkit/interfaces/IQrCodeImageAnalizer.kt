package com.taeprog.qrreaderkit.interfaces

import androidx.camera.core.ImageAnalysis

interface IQrCodeImageAnalizer: ImageAnalysis.Analyzer {
    fun startScanning()
    fun stopScanning()
}