package ru.mammoth70.totpgenerator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppActivity(), PinBox.OnPinResultListener {
    // Activity показывает и позволяет изменять настройки.

    override val idLayout = R.layout.activity_settings
    override val idActivity = R.id.frameSettingsActivity

    private val parentLayout: View by lazy { findViewById(android.R.id.content) }
    private val btnChangePin: Button by lazy { findViewById(R.id.btnChangePIN) }
    private val btnDeletePin: Button by lazy { findViewById(R.id.btnDeletePIN) }
    private val checkEnableBio: CheckBox by lazy { findViewById(R.id.checkEnableBio) }
    private val toggleProgress: MaterialButtonToggleGroup by lazy { findViewById(R.id.toggleProgress) }
    private val toggleTheme: MaterialButtonToggleGroup by lazy { findViewById(R.id.toggleTheme) }

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
            toggleProgress.check(R.id.btnProgressPassed)
        } else {
            toggleProgress.check(R.id.btnProgressRemaining)
        }
        toggleProgress.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.btnProgressPassed ->  appPassed = isChecked
                R.id.btnProgressRemaining ->  appPassed = !isChecked
            }
            setPassed()
        }

        when (appThemeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> toggleTheme.check(R.id.btnThemeDay)
            AppCompatDelegate.MODE_NIGHT_YES -> toggleTheme.check(R.id.btnThemeNight)
            else -> toggleTheme.check(R.id.btnThemeSystem)
        }
        toggleTheme.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnThemeDay -> appThemeMode = AppCompatDelegate.MODE_NIGHT_NO
                    R.id.btnThemeNight -> appThemeMode = AppCompatDelegate.MODE_NIGHT_YES
                    R.id.btnThemeSystem -> appThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                setThemeMode()
            }
        }

        checkEnableBio.isChecked = appEnableBiometric
        if (appPinCode.isNotBlank() && isHaveBiometric) {
            checkEnableBio.visibility = View.VISIBLE
        } else {
            checkEnableBio.visibility = View.GONE
        }

        checkEnableBio.setOnCheckedChangeListener { _: CompoundButton?,
                                                    isChecked: Boolean ->
            appEnableBiometric = isChecked
            setEnableBiometric()
        }


    }

    fun showSnackbar(message: String) {
        // Функция выводит Snackbar со строкой message.
        Snackbar.make(parentLayout, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnackbar(resId: Int) {
        // Функция выводит Snackbar со строкой, хранимой в ресурсе resId.
        showSnackbar(getString(resId))
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
                    setPin()
                    btnChangePin.setText(R.string.set_PIN)
                    btnDeletePin.visibility = View.GONE
                    appEnableBiometric = false
                    setEnableBiometric()
                    checkEnableBio.visibility = View.GONE
                    showSnackbar(R.string.PIN_deleted)
                } else {
                    showSnackbar(message)
                }
            }

            PinBox.ACTION_UPDATE_PIN -> {
                if (result) {
                    setPin()
                    btnChangePin.setText(R.string.change_PIN)
                    btnDeletePin.visibility = View.VISIBLE
                    checkEnableBio.visibility = View.VISIBLE
                    showSnackbar(R.string.PIN_deleted)
                }
                showSnackbar(message)
            }
        }
    }

}