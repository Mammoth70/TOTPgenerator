package ru.mammoth70.totpgenerator

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment

class PasswordExportDialog(private val onPasswordConfirmed: (String) -> Unit) : DialogFragment() {
    // Диалоговое окно ввода двух паролей.


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Функция создаёт диалоговое окно PasswordExportDialog.

        val customView = layoutInflater.inflate(R.layout.dialog_password_export, null)

        val edPass = customView.findViewById<TextInputEditText>(R.id.edPassword)
        val ilPass = customView.findViewById<TextInputLayout>(R.id.ilPassword)
        val edConfirm = customView.findViewById<TextInputEditText>(R.id.edConfirmPassword)
        val ilConfirm = customView.findViewById<TextInputLayout>(R.id.ilConfirmPassword)

        edPass.doOnTextChanged { _, _, _, _ -> ilPass.error = null }
        edPass.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) ilPass.error = null
        }

        edConfirm.doOnTextChanged { _, _, _, _ -> ilConfirm.error = null }
        edConfirm.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) ilConfirm.error = null
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.export_file_protection))
            .setMessage(getString(R.string.export_file_protection_text))
            .setView(customView)
            .setPositiveButton(getString(R.string.next), null)
            .setNeutralButton(getString(R.string.no_encrypting)) { _, _ ->
                onPasswordConfirmed("") // Передаем пустую строку
                dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pass = edPass.text.toString()
                val confirm = edConfirm.text.toString()

                when {
                    pass.length < 8 -> ilPass.error = getString(R.string.error_password_min_length)
                    pass != confirm -> ilConfirm.error = getString(R.string.error_passwords_mismatch)
                    else -> {
                        onPasswordConfirmed(pass)
                        dismiss()
                    }
                }
            }
        }

        return dialog
    }
}
