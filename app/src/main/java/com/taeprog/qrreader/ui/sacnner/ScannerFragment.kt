package com.taeprog.qrreader.ui.sacnner

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.taeprog.qrreader.R
import com.taeprog.qrreader.databinding.FragmentScannerBinding
import com.taeprog.qrreaderkit.BarcodeScannerInteractor


class ScannerFragment : Fragment() {

    lateinit var binding: FragmentScannerBinding
    lateinit var barcodeScanner: BarcodeScannerInteractor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScannerBinding.inflate(inflater, container, false)

        barcodeScanner = BarcodeScannerInteractor(this, binding.preview) { result, points ->
            barcodeScanner.stopScanning()
            checkResult(result)
        }
        return binding.root
    }

    private fun checkResult(result: String) {
        when {
            Patterns.WEB_URL.matcher(result).matches() -> showUrlResult(result)
            else -> showTextResult(result)
        }
    }

    private fun showUrlResult(result: String) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.url_title)
            .setMessage(result)
            .setPositiveButton(R.string.open_in_browser) { _, _ ->
                openUrlInBrouser(result)
            }.setNeutralButton(R.string.copy_to_clipboard) { _, _ ->
                copyToClipboard(urlToClipData(result))
            }
            .setOnDismissListener {
                barcodeScanner.startScanning()
            }
            .show()
    }

    private fun openUrlInBrouser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun urlToClipData(result: String): ClipData {
        return ClipData.newUri(
            requireContext().contentResolver,
            "URI",
            Uri.parse(result)
        )
    }

    private fun showTextResult(result: String) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.result)
            .setMessage(result)
            .setPositiveButton(R.string.copy_to_clipboard) { _, _ ->
                copyToClipboard(textToClipData(result))
            }
            .setNegativeButton(android.R.string.cancel){ dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                barcodeScanner.startScanning()
            }
            .show()
    }

    private fun textToClipData(result: String):ClipData{
        return ClipData.newPlainText(QR_RESULT_LABEL, result)
    }

    private fun copyToClipboard(data: ClipData) {
        val clipboard = requireActivity()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(data)
        Toast.makeText(requireContext(), R.string.result_is_copied_to_clipboard, Toast.LENGTH_SHORT)
            .show()
    }

    companion object {
        const val QR_RESULT_LABEL = "Qr code contents"

        @JvmStatic
        fun newInstance() =
            ScannerFragment()
    }
}