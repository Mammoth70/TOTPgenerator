package ru.mammoth70.totpgenerator

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ru.mammoth70.totpgenerator.App.Companion.appSecrets
import androidx.core.view.size

class SecretBox: DialogFragment() {
    // Диалоговое окно с формой OTPauth.
    // Умеет добавлять, просматривать и удалять OTPauth.

    companion object {
        const val INTENT_TOTP_ACTION = "totp_action"
        const val INTENT_TOTP_NUM = "totp_num"
        const val ACTION_TOTP_VIEW = "totp_view"
        const val ACTION_TOTP_ADD = "totp_add"
        const val ACTION_TOTP_DELETE = "totp_delete"
    }

    interface OnAddResultListener {
        fun onAddResult(auth: OTPauth)
    }
    private lateinit var addListener: OnAddResultListener
    fun setOnAddResultListener(listener: OnAddResultListener) {
        this.addListener = listener
    }
    interface OnDeleteResultListener {
        fun onDeleteResult(num: Int)
    }
    private lateinit var deleteListener: OnDeleteResultListener
    fun setOnDeleteResultListener(listener: OnDeleteResultListener) {
        this.deleteListener = listener
    }

    private val dlg: AlertDialog by lazy { dialog as AlertDialog }
    private val ilLabel: TextInputLayout by lazy { dlg.findViewById(R.id.ilLabel)!!}
    private val edLabel: TextInputEditText by lazy { dlg.findViewById(R.id.edLabel)!! }
    private val ilKey: TextInputLayout by lazy { dlg.findViewById(R.id.ilKey)!! }
    private val edKey: TextInputEditText by lazy { dlg.findViewById(R.id.edKey)!! }
    private val ilPeriod: TextInputLayout by lazy { dlg.findViewById(R.id.ilPeriod)!! }
    private val edPeriod: TextInputEditText by lazy { dlg.findViewById(R.id.edPeriod)!! }
    private val radioHash: RadioGroup by lazy { dlg.findViewById(R.id.radioHash)!! }
    private val radioSHA1: RadioButton by lazy { dlg.findViewById(R.id.SHA1)!! }
    private val radioSHA256: RadioButton by lazy { dlg.findViewById(R.id.SHA256)!! }
    private val radioSHA512: RadioButton by lazy { dlg.findViewById(R.id.SHA512)!! }
    private val radioDigits: RadioGroup by lazy { dlg.findViewById(R.id.radioDigits)!! }
    private val radioDigits6: RadioButton by lazy { dlg.findViewById(R.id.digits6)!! }
    private val radioDigits7: RadioButton by lazy { dlg.findViewById(R.id.digits7)!! }
    private val radioDigits8: RadioButton by lazy { dlg.findViewById(R.id.digits8)!! }

    private val action: String by lazy { requireArguments().getString(INTENT_TOTP_ACTION,"") }
    private val secret: OTPauth by lazy { appSecrets[requireArguments().getInt(INTENT_TOTP_NUM)] }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(R.layout.frame_dialog_key)
        builder.setCancelable(false)
        when (action) {
            ACTION_TOTP_VIEW -> {
                builder.setTitle(getString(R.string.view_key))
                builder.setNegativeButton(R.string.cancel) { _, _ -> }
            }

            ACTION_TOTP_ADD -> {
                builder.setTitle(getString(R.string.add_key))
                builder.setNegativeButton(R.string.cancel) { _, _ -> }
                builder.setPositiveButton(R.string.ok) { _, _ -> }
            }

            ACTION_TOTP_DELETE -> {
                builder.setTitle(getString(R.string.delete_key))
                builder.setNegativeButton(R.string.cancel) { _, _ -> }
                builder.setPositiveButton(R.string.ok) { _, _ -> }
            }
        }
        val dialog = builder.create()
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        return dialog
    }

    override fun onResume() {
        super.onResume()

        if ((action == ACTION_TOTP_VIEW) || (action == ACTION_TOTP_DELETE)) {
            fillFields()
        }

        if (action == ACTION_TOTP_DELETE) {
            dlg.getButton(Dialog.BUTTON_POSITIVE)?.setOnClickListener {
                deleteSecret()
            }
        }

        if (action == ACTION_TOTP_ADD) {
            dlg.getButton(Dialog.BUTTON_POSITIVE)?.setOnClickListener {
                addSecret()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun fillFields() {
        if (secret.issuer.isBlank()) {
            edLabel.setText(secret.label)
        } else {
            edLabel.setText(secret.issuer + ":" + secret.label)
        }
        edKey.setText(secret.secret)
        edPeriod.setText(secret.period.toString())
        when (secret.hash) {
            SHA256 -> radioHash.check(R.id.SHA256)
            SHA512 -> radioHash.check(R.id.SHA512)
            else -> radioHash.check(R.id.SHA1)
        }
        when (secret.digits) {
            7 -> radioDigits.check(R.id.digits7)
            8 -> radioDigits.check(R.id.digits8)
            else -> radioDigits.check(R.id.digits6)
        }
        for (i in 0..<radioHash.size) {
            (radioHash.getChildAt(i) as RadioButton).isClickable = false
        }
        for (i in 0..<radioDigits.size) {
            (radioDigits.getChildAt(i) as RadioButton).isClickable = false
        }
    }

    fun deleteSecret() {
        deleteListener.onDeleteResult(secret.id)
        dismiss()
    }

    fun addSecret() {
        var isChecked = true
        var selectedHash = SHA1
        var selectedDigits = 6

        if (radioSHA1.isChecked) {
            selectedHash = SHA1
        }
        if (radioSHA256.isChecked) {
            selectedHash = SHA256
        }
        if (radioSHA512.isChecked) {
            selectedHash = SHA512
        }
        if (radioDigits6.isChecked) {
            selectedDigits = 6
        }
        if (radioDigits7.isChecked) {
            selectedDigits = 7
        }
        if (radioDigits8.isChecked) {
            selectedDigits = 8
        }
        edLabel.setOnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (hasFocus) {
                ilLabel.error = null
            }
        }
        edKey.setOnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (hasFocus) {
                ilKey.error = null
            }
        }
        edPeriod.setOnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (hasFocus) {
                ilPeriod.error = null
            }
        }
        if (edLabel.text.toString().isEmpty()) {
            ilLabel.error = getString(R.string.err_empty_label)
            isChecked = false
        }
        if (edKey.text.toString().isEmpty()) {
            ilKey.error = getString(R.string.err_empty_key)
            isChecked = false
        }
        if (edPeriod.text.toString().isEmpty()) {
            ilPeriod.error = getString(R.string.err_empty_period)
            isChecked = false
        }
        if ((action == ACTION_TOTP_ADD) && (edLabel.text.toString() in appSecrets.map(OTPauth::label))) {
            ilLabel.error = getString(R.string.err_not_unique_label)
            isChecked = false
        }
        if (isChecked) {
            val secretNew = OTPauth(
                label = edLabel.text.toString(),
                period = edPeriod.text.toString().toInt(),
                hash = selectedHash, digits = selectedDigits,
                secret = edKey.text.toString()
            )
            addListener.onAddResult(secretNew)
            dismiss()
        }

    }
}