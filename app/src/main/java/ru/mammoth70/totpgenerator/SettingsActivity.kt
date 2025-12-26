package ru.mammoth70.totpgenerator

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppActivity(), PinBox.OnPinResultListener {
    // Activity показывает и позволяет изменять настройки.

    override val idLayout = R.layout.activity_settings
    override val idActivity = R.id.frameSettingsActivity

    private val btnSave: Button by lazy { findViewById(R.id.btnAction) }
    private val btnChangePin: Button by lazy { findViewById(R.id.btnChangePIN) }
    private val btnDeletePin: Button by lazy { findViewById(R.id.btnDeletePIN) }
    private val toggleButton: MaterialButtonToggleGroup by lazy { findViewById(R.id.toggleGroup) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        topAppBar.setTitle(R.string.title_settings)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        if (appPinCode.isBlank()) {
            btnChangePin.setText(R.string.set_PIN)
            btnDeletePin.visibility = View.GONE
        }

        if (appPassed) {
            toggleButton.check(R.id.btnPassed)
        } else {
            toggleButton.check(R.id.btnRemaining)
        }
        toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.btnPassed ->  appPassed = isChecked
                R.id.btnRemaining ->  appPassed = !isChecked
            }
            setPassed(appPassed)
        }

    }

    fun showSnackbar(message: String) {
        // Функция выводит Snackbar со строкой message.
        Snackbar.make(btnSave, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnackbar(resId: Int) {
        // Функция выводит Snackbar со строкой, хранимой в ресурсе resId.
        showSnackbar(getString(resId))
    }

    fun onActionClicked(@Suppress("UNUSED_PARAMETER")ignored: View) {
    }

    fun onPinChangeClicked(@Suppress("UNUSED_PARAMETER")ignored: View) {
        // Вызов окна смены PIN
        val bundle = Bundle()
        bundle.putString(PinBox.INTENT_PIN_ACTION, PinBox.ACTION_UPDATE_PIN)
        val pinBox = PinBox()
        pinBox.setArguments(bundle)
        pinBox.isCancelable = false
        pinBox.setOnPinResultListener(this)
        pinBox.show(this.supportFragmentManager, "PIN_DIALOG")
    }

    fun onPinDeleteClicked(@Suppress("UNUSED_PARAMETER")ignored: View) {
        // Вызов окна удаления PIN
        val bundle = Bundle()
        bundle.putString(PinBox.INTENT_PIN_ACTION, PinBox.ACTION_DELETE_PIN)
        val pinBox = PinBox()
        pinBox.setArguments(bundle)
        pinBox.isCancelable = false
        pinBox.setOnPinResultListener(this)
        pinBox.show(this.supportFragmentManager, "PIN_DIALOG")
    }

    override fun onPinResult(action: String, result: Boolean, message: String, pin: String) {
        // Обработчик возврата из PinDialog.
        when (action) {
            PinBox.ACTION_DELETE_PIN -> {
                if (result) {
                    appPinCode = ""
                    setPin(appPinCode)
                    btnChangePin.setText(R.string.set_PIN)
                    btnDeletePin.visibility = View.GONE
                    showSnackbar(R.string.PIN_deleted)
                } else {
                    showSnackbar(message)
                }
            }

            PinBox.ACTION_UPDATE_PIN -> {
                if (result) {
                    setPin(appPinCode)
                    btnChangePin.setText(R.string.change_PIN)
                    btnDeletePin.visibility = View.VISIBLE
                    showSnackbar(R.string.PIN_deleted)
                }
                showSnackbar(message)
            }
        }
    }

}