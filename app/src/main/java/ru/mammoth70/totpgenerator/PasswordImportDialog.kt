package ru.mammoth70.totpgenerator

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment

class PasswordImportDialog(private val onPasswordConfirmed: (String) -> Unit) : DialogFragment() {
    // Диалоговое окно ввода одного пароля.


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Функция создаёт диалоговое окно PasswordImportDialog.

        val customView = layoutInflater.inflate(R.layout.dialog_password_import, null)

        val edPass = customView.findViewById<TextInputEditText>(R.id.edPassword)
        val ilPass = customView.findViewById<TextInputLayout>(R.id.ilPassword)

        edPass.doOnTextChanged { _, _, _, _ -> ilPass.error = null }
        edPass.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) ilPass.error = null
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.import_file_protection))
            .setMessage(getString(R.string.import_file_protection_text))
            .setView(customView)
            .setPositiveButton(getString(R.string.next), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pass = edPass.text.toString()

                if (pass.isEmpty()) {
                    ilPass.error = getString(R.string.error_password_required)
                } else {
                    onPasswordConfirmed(pass)
                    dismiss()
                }
            }
        }

        return dialog
    }
}
