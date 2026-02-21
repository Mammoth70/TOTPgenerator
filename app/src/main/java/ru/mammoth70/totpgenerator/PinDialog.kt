package ru.mammoth70.totpgenerator

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PinDialog : DialogFragment() {
    // Диалоговое окно ввода и проверки PIN.


    companion object {
        const val INTENT_PIN_ACTION = "pin_action"
        const val ACTION_ENTER_PIN = "enter_pin"
        const val ACTION_DELETE_PIN = "delete_pin"
        const val ACTION_SET_NEW_PIN = "set_new_pin"
        const val ACTION_UPDATE_PIN = "update_pin"

        const val INTENT_PIN_SCREEN = "pin_screen"
        const val SCREEN_FULL = "pin_screen_full"

        private var pinBuffer = CharArray(6)
        private var pinBuffer1 = CharArray(6)
        private var pinIndex = 0

        private const val CHECK_PIN = 0
        private const val CHECK_PIN_AND_BIO = 1
        private const val CHECK_PIN_WHILE_FALSE = 2
        private const val ENTER_NEW_PIN = 3
        private const val CHECK_PIN_ENTER_NEW_PIN = 4
    }


    interface OnPinResultListener {
        fun onPinResult(action: String, result: Boolean, message: String)
    }

    private lateinit var pinListener: OnPinResultListener
    fun setOnPinResultListener(listener: OnPinResultListener) {
        this.pinListener = listener
    }


    private val dlg: AlertDialog by lazy { dialog as AlertDialog }
    private val btn0: Button by lazy { dlg.findViewById(R.id.btn0)!!}
    private val btn1: Button by lazy { dlg.findViewById(R.id.btn1)!!}
    private val btn2: Button by lazy { dlg.findViewById(R.id.btn2)!!}
    private val btn3: Button by lazy { dlg.findViewById(R.id.btn3)!!}
    private val btn4: Button by lazy { dlg.findViewById(R.id.btn4)!!}
    private val btn5: Button by lazy { dlg.findViewById(R.id.btn5)!!}
    private val btn6: Button by lazy { dlg.findViewById(R.id.btn6)!!}
    private val btn7: Button by lazy { dlg.findViewById(R.id.btn7)!!}
    private val btn8: Button by lazy { dlg.findViewById(R.id.btn8)!!}
    private val btn9: Button by lazy { dlg.findViewById(R.id.btn9)!!}
    private val btnBack: Button by lazy { dlg.findViewById(R.id.btnBack)!!}
    private val btnCancel: Button by lazy { dlg.findViewById(R.id.btnCancel)!!}
    private val btnBiomeric: Button by lazy { dlg.findViewById(R.id.btnBiomeric)!!}
    private val bullet1: ImageView by lazy { dlg.findViewById(R.id.bullet1)!!}
    private val bullet2: ImageView by lazy { dlg.findViewById(R.id.bullet2)!!}
    private val bullet3: ImageView by lazy { dlg.findViewById(R.id.bullet3)!!}
    private val bullet4: ImageView by lazy { dlg.findViewById(R.id.bullet4)!!}
    private val bullet5: ImageView by lazy { dlg.findViewById(R.id.bullet5)!!}
    private val bullet6: ImageView by lazy { dlg.findViewById(R.id.bullet6)!!}
    private val errorMessage: TextView by lazy {dlg.findViewById(R.id.errorMessage)!!}

    private val action: String by lazy { requireArguments().getString(INTENT_PIN_ACTION,"") }
    private val screen: String by lazy { requireArguments().getString(INTENT_PIN_SCREEN,"") }

    private var variant = CHECK_PIN
    private var step = 0


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Фунция создания диалога.

        val theme = if (screen == SCREEN_FULL) {
            //com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
            android.R.style.Theme_Material_Light_NoActionBar_Fullscreen
        } else {
            0
        }
        val builder = MaterialAlertDialogBuilder(requireContext(), theme)
        builder.setView(R.layout.dialog_pin)
        builder.setCancelable(false)

        when (action) {
            ACTION_ENTER_PIN -> {
                // Проверить PIN и вернуть результат проверки.
                builder.setTitle(getString(R.string.enter_PIN))
                variant = if (isHaveHashPin && SettingsManager.enableBiometric) {
                    CHECK_PIN_AND_BIO
                } else {
                    CHECK_PIN_WHILE_FALSE
                }
            }

            ACTION_DELETE_PIN -> {
                // Проверить PIN и вернуть результат проверки.
                builder.setTitle(getString(R.string.enter_PIN))
                variant = CHECK_PIN
            }

            ACTION_SET_NEW_PIN -> {
                // Ввод два раза нового PIN.
                builder.setTitle(getString(R.string.enter_PIN1))
                variant = ENTER_NEW_PIN
            }

            ACTION_UPDATE_PIN -> {
                if (!isHaveHashPin) {
                    // Ввод два раза нового PIN, если старого нет.
                    builder.setTitle(getString(R.string.enter_PIN1))
                    variant = ENTER_NEW_PIN
                } else {
                    // Проверка старого PIN, ввод два раза нового PIN.
                    builder.setTitle(getString(R.string.enter_old_PIN))
                    variant = CHECK_PIN_ENTER_NEW_PIN
                }

            }
        }
        val dialog = builder.create()
        if (screen == SCREEN_FULL) {
            dialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        return dialog
    }


    override fun onResume() {
        super.onResume()

        clearAllPins()

        // Здесь мы устанавливаем листенеры всех кнопок.
        btn0.setOnClickListener {
            addCharPin('0')
        }
        btn1.setOnClickListener {
            addCharPin('1')
        }
        btn2.setOnClickListener {
            addCharPin('2')
        }
        btn3.setOnClickListener {
            addCharPin('3')
        }
        btn4.setOnClickListener {
            addCharPin('4')
        }
        btn5.setOnClickListener {
            addCharPin('5')
        }
        btn6.setOnClickListener {
            addCharPin('6')
        }
        btn7.setOnClickListener {
            addCharPin('7')
        }
        btn8.setOnClickListener {
            addCharPin('8')
        }
        btn9.setOnClickListener {
            addCharPin('9')
        }
        btnBack.setOnClickListener {
            deleteCharPin()
        }
        btnCancel.setOnClickListener {
            clearAllPins()
            pinListener.onPinResult(action, false,
                getString(R.string.PIN_cancel))
            dismiss()
        }
        if (variant == CHECK_PIN_AND_BIO) {
            errorMessage.visibility = View.VISIBLE
            btnBiomeric.visibility = View.VISIBLE
            btnBiomeric.setOnClickListener {
                errorMessage.text = ""
                clearAllPins()
                bulletsClear()
                bioAuthenticate(requireActivity())
            }

            if (isHaveBiometric) {
                dialog?.window?.decorView?.post {
                    if (isAdded && !isRemoving) {
                        bioAuthenticate(requireActivity())
                    }
                }
            }
        }
    }


    private fun addCharPin(digit: Char) {
        // Функция добавляет ещё один символ в PIN-код, обновляет интерфейс и проверяет (если надо) PIN-код.

        errorMessage.text = ""
        if (pinIndex < pinBuffer.size) {
            pinBuffer[pinIndex] = digit
            pinIndex++
            bulletsAdd(pinIndex)
            if (pinIndex == pinBuffer.size) {
                validatePin()
            }
        }
    }


    private fun deleteCharPin() {
        // Функция удаляет последний символ из PIN-кода и обновляет интерфейс.

        errorMessage.text = ""
        if (pinIndex > 0) {
            pinIndex--
            pinBuffer[pinIndex] = '\u0000'
            bulletDel(pinIndex)
        }
    }

    private fun clearAllPins() {
        // Функция очищает PIN-код, забивая все символы пустым значением.

        pinBuffer.fill('\u0000')
        pinBuffer1.fill('\u0000')
        pinIndex=0
    }


    private fun bulletsAdd(num: Int) {
        // Функция добавляет на панель одну точку.

        when (num) {
            1 -> bullet1.setImageResource(R.drawable.circle_on)
            2 -> bullet2.setImageResource(R.drawable.circle_on)
            3 -> bullet3.setImageResource(R.drawable.circle_on)
            4 -> bullet4.setImageResource(R.drawable.circle_on)
            5 -> bullet5.setImageResource(R.drawable.circle_on)
            6 -> bullet6.setImageResource(R.drawable.circle_on)
        }
    }


    private fun bulletDel(num:Int) {
        // Функция удаляет с панели последнюю точку.

        when (num) {
            0 -> bullet1.setImageResource(R.drawable.circle_off)
            1 -> bullet2.setImageResource(R.drawable.circle_off)
            2 -> bullet3.setImageResource(R.drawable.circle_off)
            3 -> bullet4.setImageResource(R.drawable.circle_off)
            4 -> bullet5.setImageResource(R.drawable.circle_off)
            5 -> bullet6.setImageResource(R.drawable.circle_off)
        }
    }


    private fun bulletsClear() {
        // Функция удаляет с панели все точки.

        bullet1.setImageResource(R.drawable.circle_off)
        bullet2.setImageResource(R.drawable.circle_off)
        bullet3.setImageResource(R.drawable.circle_off)
        bullet4.setImageResource(R.drawable.circle_off)
        bullet5.setImageResource(R.drawable.circle_off)
        bullet6.setImageResource(R.drawable.circle_off)
    }


    private fun validatePin() {
        // Функция реализует разные варианты логики ввода и проверки PIN-кода.

        when (variant) {
            CHECK_PIN -> {
                // Вариант. Проверить PIN и вернуть результат проверки.
                if (checkPin()) {
                    clearAllPins()
                    if (action == ACTION_DELETE_PIN) {
                        deleteHashPin()
                        SettingsManager.enableBiometric = false
                    }
                    pinListener.onPinResult(action, true, "ok")
                } else {
                    clearAllPins()
                    pinListener.onPinResult(action, false,
                        getString(R.string.PIN_bad))
                }
                dismiss()
            }

            CHECK_PIN_WHILE_FALSE, CHECK_PIN_AND_BIO -> {
                // Вариант. Проверять PIN, пока не введётся правильный и вернуть результат проверки.
                if (checkPin()) {
                    clearAllPins()
                    pinListener.onPinResult(action, true, "ok")
                    dismiss()
                } else {
                    clearAllPins()
                    bulletsClear()
                    errorMessage.setText(R.string.error_PIN)
                }
            }

            ENTER_NEW_PIN -> {
                // Вариант. Ввод два раза нового PIN.
                when (step) {
                    0 -> {
                        step += 1
                        pinBuffer.copyInto(pinBuffer1)
                        pinBuffer.fill('\u0000')
                        pinIndex=0
                        bulletsClear()
                        dlg.setTitle(getString(R.string.enter_PIN2))
                    }

                    1 -> {
                        if (pinBuffer.contentEquals(pinBuffer1)) {
                            setHashPin(pinBuffer) // Записать новый PIN в секретное хранище
                            clearAllPins()
                            pinListener.onPinResult(action, true,
                                getString(R.string.PIN_changed))
                            dismiss()
                        } else {
                            clearAllPins()
                            pinListener.onPinResult(action, false,
                                getString(R.string.PIN_new_bad))
                            dismiss()
                        }

                    }
                }
            }

            CHECK_PIN_ENTER_NEW_PIN -> {
                // Вариант. Проверка старого PIN, ввод два раза нового PIN.
                when (step) {
                    0 -> {
                        if (checkPin()) {
                            step += 1
                            pinBuffer.fill('\u0000')
                            pinIndex=0
                            bulletsClear()
                            dlg.setTitle(getString(R.string.enter_PIN1))
                        } else {
                            clearAllPins()
                            pinListener.onPinResult(action, false,
                                getString(R.string.PIN_bad))
                            dismiss()
                        }
                    }

                    1 -> {
                        step += 1
                        pinBuffer.copyInto(pinBuffer1)
                        pinBuffer.fill('\u0000')
                        pinIndex=0
                        bulletsClear()
                        dlg.setTitle(getString(R.string.enter_PIN2))
                    }

                    2 -> {
                        if (pinBuffer.contentEquals(pinBuffer1)) {
                            setHashPin(pinBuffer) // Записать новый PIN в секретное хранище
                            clearAllPins()
                            pinListener.onPinResult(action, true,
                                getString(R.string.PIN_changed))
                            dismiss()
                        } else {
                            clearAllPins()
                            pinListener.onPinResult(action, false,
                                getString(R.string.PIN_new_bad))
                            dismiss()
                        }

                    }
                }
            }
        }
    }


    private fun bioAuthenticate(context: FragmentActivity) {
        // Функция реализует проверку входа по биометрии.

        val executor = context.mainExecutor
        val biometricPrompt = BiometricPrompt(
            context,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    clearAllPins()
                    pinListener.onPinResult(action, true, "ok")
                    dismiss()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                        // Пользователь сам нажал "Отмена".
                    } else {
                        // Какая-то системная ошибка (например, датчик занят).
                        errorMessage.text = errString
                    }
                }

                override fun onAuthenticationFailed() {
                    errorMessage.setText(R.string.Bio_error)
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.Bio_login))
            .setDescription(getString(R.string.use_FingerPrint))
            .setNegativeButtonText(getString(R.string.cancel))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun checkPin(): Boolean {
        // Функция сравнает хеш pinBuffer с хешем из секретного хранилища.
        return verifyPin(pinBuffer)
    }

}