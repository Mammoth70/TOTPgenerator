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

        if (!isHaveHashPin) {
            btnChangePin.setText(R.string.set_PIN)
            btnDeletePin.visibility = View.GONE
        }

        if (progressClockWise) {
            toggleProgress.check(R.id.btnProgressPassed)
        } else {
            toggleProgress.check(R.id.btnProgressRemaining)
        }
        toggleProgress.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.btnProgressPassed ->  setProgressMode(isChecked)
                R.id.btnProgressRemaining ->  setProgressMode(!isChecked)
            }
        }

        when (appThemeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> toggleTheme.check(R.id.btnThemeDay)
            AppCompatDelegate.MODE_NIGHT_YES -> toggleTheme.check(R.id.btnThemeNight)
            else -> toggleTheme.check(R.id.btnThemeSystem)
        }
        toggleTheme.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnThemeDay -> setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
                    R.id.btnThemeNight -> setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
                    R.id.btnThemeSystem -> setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }

        checkEnableBio.isChecked = enableBiometric
        if (isHaveHashPin && isHaveBiometric) {
            checkEnableBio.visibility = View.VISIBLE
        } else {
            checkEnableBio.visibility = View.GONE
        }

        checkEnableBio.setOnCheckedChangeListener { _: CompoundButton?,
                                                    isChecked: Boolean ->
            setBiometricLogin(isChecked)
        }

        btnChangePin.setOnClickListener { _ ->
            // Вызов окна смены PIN-кода.
            val bundle = Bundle()
            bundle.putString(PinBox.INTENT_PIN_ACTION, PinBox.ACTION_UPDATE_PIN)
            val pinBox = PinBox()
            pinBox.setArguments(bundle)
            pinBox.isCancelable = false
            pinBox.setOnPinResultListener(this)
            pinBox.show(this.supportFragmentManager, "PIN_DIALOG")
        }

        btnDeletePin.setOnClickListener { _ ->
            // Вызов окна удаления PIN-кода.
            val bundle = Bundle()
            bundle.putString(PinBox.INTENT_PIN_ACTION, PinBox.ACTION_DELETE_PIN)
            val pinBox = PinBox()
            pinBox.setArguments(bundle)
            pinBox.isCancelable = false
            pinBox.setOnPinResultListener(this)
            pinBox.show(this.supportFragmentManager, "PIN_DIALOG")
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

    override fun onPinResult(action: String, result: Boolean, message: String) {
        // Обработчик возврата из PinDialog.
        when (action) {
            PinBox.ACTION_DELETE_PIN -> {
                if (result) {
                    btnChangePin.setText(R.string.set_PIN)
                    btnDeletePin.visibility = View.GONE
                    checkEnableBio.visibility = View.GONE
                    showSnackbar(R.string.PIN_deleted)
                } else {
                    showSnackbar(message)
                }
            }

            PinBox.ACTION_UPDATE_PIN -> {
                if (result) {
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