package com.taeprog.qrreader.ui.sacnner

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.taeprog.qrreader.R
import com.taeprog.qrreader.databinding.FragmentScannerBinding
import com.taeprog.qrreaderkit.BarcodeScannerInteractor


class ScannerFragment : Fragment() {


    lateinit var binding: FragmentScannerBinding
    lateinit var barcodeScanner: BarcodeScannerInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScannerBinding.inflate(inflater, container, false)

        barcodeScanner = BarcodeScannerInteractor(this, binding.preview) { result, points ->
            barcodeScanner.stopScanning()
            showTextResult(result)
        }
        return binding.root
    }

    private fun showTextResult(result: String){
        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.result))
                .setMessage(result)
                .setPositiveButton("OK") { _, _ ->}
                .setOnDismissListener {
                    barcodeScanner.startScanning()
                }
                .show()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ScannerFragment()    }
}