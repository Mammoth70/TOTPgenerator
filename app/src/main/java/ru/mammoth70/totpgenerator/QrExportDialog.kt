package ru.mammoth70.totpgenerator

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class QrExportDialog(private val uri: String) : DialogFragment() {
    // Диалоговое окно вывода QR-кода, полученного из списка секретов.


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Функция создаёт диалоговое окно QrExportDialog.

        val customView = layoutInflater.inflate(R.layout.dialog_qr_export, null)

        val ivQr = customView.findViewById<ImageView>(R.id.ivQrCode)
        val btnOk = customView.findViewById<Button>(R.id.btnOk)

        try {
            val bitmap = generateQrCode(uri, 800)
            ivQr.setImageBitmap(bitmap)
        } catch (_: Exception) {
            dismiss()
        }

        btnOk.setOnClickListener { dismiss() }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(customView)
            .create().apply {
            }
    }


    override fun onResume() {
        super.onResume()
        // Устанавливаем максимальную яркость экрана для удобства сканирования.
        dialog?.window?.attributes = dialog?.window?.attributes?.apply { screenBrightness = 1.0f }
    }

}