package ru.mammoth70.totpgenerator

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoadingDialog : DialogFragment() {
    // Диалоговое окно показа окна длительной расшифровки.


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Функция создаёт диалоговое окно LoadingDialog.

        val customView = layoutInflater.inflate(R.layout.dialog_loading, null)

        isCancelable = false

        return MaterialAlertDialogBuilder(requireContext())
            .setView(customView)
            .create()
    }

}