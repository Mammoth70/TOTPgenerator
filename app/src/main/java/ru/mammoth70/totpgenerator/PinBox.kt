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
import ru.mammoth70.totpgenerator.App.Companion.appContext
import ru.mammoth70.totpgenerator.MainActivity.Companion.mainContext

var isHaveBiometric: Boolean = false

fun checkBiometricInDevice() {
    val biometricManager = BiometricManager.from(appContext)
    isHaveBiometric = when (biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            true
        }
        else -> {
            false
        }
    }
}

private const val RETURN_THE_PIN = -1
private const val CHECK_PIN = 0
private const val CHECK_PIN_AND_BIO = 1
private const val CHECK_PIN_WHILE_FALSE = 2
private const val ENTER_NEW_PIN = 3
private const val CHECK_PIN_ENTER_NEW_PIN = 4

class PinBox : DialogFragment() {
    // Диалоговое окно ввода и проверки PIN.

    interface OnPinResultListener {
        fun onPinResult(action: String, result: Boolean, message: String, pin: String)
    }
    companion object {
        const val INTENT_PIN_ACTION = "pin_action"
        const val ACTION_ENTER_PIN = "enter_pin"
        const val ACTION_DELETE_PIN = "delete_pin"
        const val ACTION_SET_NEW_PIN = "set_new_pin"
        const val ACTION_UPDATE_PIN = "update_pin"

        const val INTENT_PIN_SCREEN = "pin_screen"
        const val SCREEN_FULL = "pin_screen_full"
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
    private val pin1: ImageView by lazy { dlg.findViewById(R.id.pin1)!!}
    private val pin2: ImageView by lazy { dlg.findViewById(R.id.pin2)!!}
    private val pin3: ImageView by lazy { dlg.findViewById(R.id.pin3)!!}
    private val pin4: ImageView by lazy { dlg.findViewById(R.id.pin4)!!}
    private val pin5: ImageView by lazy { dlg.findViewById(R.id.pin5)!!}
    private val pin6: ImageView by lazy { dlg.findViewById(R.id.pin6)!!}
    private val errorMessage: TextView by lazy {dlg.findViewById(R.id.errorMessage)!!}


    private val action: String by lazy { requireArguments().getString(INTENT_PIN_ACTION,"") }
    private val screen: String by lazy { requireArguments().getString(INTENT_PIN_SCREEN,"") }

    private var pinCode = ""
    private var pinCode1 = ""
    private var variant = CHECK_PIN
    private var step = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = if (screen == SCREEN_FULL) {
            AlertDialog.Builder(
                requireActivity(),
                // Theme_Material_NoActionBar_Fullscreen
                // Theme_Material_Light_NoActionBar_Fullscreen
                android.R.style.Theme_Material_Light_NoActionBar_Fullscreen
            )
        } else {
            AlertDialog.Builder(requireActivity() )
        }
        builder.setView(R.layout.dialog_pin)
        builder.setCancelable(false)
        when (action) {
            ACTION_ENTER_PIN -> {
                // Проверить PIN и вернуть результат проверки.
                builder.setTitle(getString(R.string.enter_PIN))
                variant = if (appEnableBiometric) {
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
                if (appPinCode.isBlank()) {
                    // Ввод два раза нового PIN, если старый - пустой.
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
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        return dialog
    }

    override fun onResume() {
        super.onResume()
        btn0.setOnClickListener {
            addPin("0")
        }
        btn1.setOnClickListener {
            addPin("1")
        }
        btn2.setOnClickListener {
            addPin("2")
        }
        btn3.setOnClickListener {
            addPin("3")
        }
        btn4.setOnClickListener {
            addPin("4")
        }
        btn5.setOnClickListener {
            addPin("5")
        }
        btn6.setOnClickListener {
            addPin("6")
        }
        btn7.setOnClickListener {
            addPin("7")
        }
        btn8.setOnClickListener {
            addPin("8")
        }
        btn9.setOnClickListener {
            addPin("9")
        }
        btnBack.setOnClickListener {
            deletePin()
        }
        btnCancel.setOnClickListener {
            pinListener.onPinResult(action, false,
                getString(R.string.PIN_cancel), "")
            dismiss()
        }
        if (variant == CHECK_PIN_AND_BIO) {
            btnBiomeric.visibility = View.VISIBLE
            btnBiomeric.setOnClickListener {
                authenticate(mainContext)
            }
        }
    }


    fun addPin(symbol: String) {
        errorMessage.text = ""
        if (pinCode.length < 6) {
            pinCode += symbol
            pinsAdd(pinCode.length)
        }
        if (pinCode.length == 6) {
            check()
        }
    }

    fun deletePin() {
        errorMessage.text = ""
        if (pinCode.isNotEmpty()) {
            pinCode = pinCode.dropLast(1)
            pinsDel(pinCode.length)
        }
    }

    fun pinsAdd(num: Int) {
        when (num) {
            1 -> pin1.setImageResource(R.drawable.circle_on)
            2 -> pin2.setImageResource(R.drawable.circle_on)
            3 -> pin3.setImageResource(R.drawable.circle_on)
            4 -> pin4.setImageResource(R.drawable.circle_on)
            5 -> pin5.setImageResource(R.drawable.circle_on)
            6 -> pin6.setImageResource(R.drawable.circle_on)
        }
    }

    fun pinsDel(num:Int) {
        when (num) {
            0 -> pin1.setImageResource(R.drawable.circle_off)
            1 -> pin2.setImageResource(R.drawable.circle_off)
            2 -> pin3.setImageResource(R.drawable.circle_off)
            3 -> pin4.setImageResource(R.drawable.circle_off)
            4 -> pin5.setImageResource(R.drawable.circle_off)
            5 -> pin6.setImageResource(R.drawable.circle_off)
        }
    }

    fun pinsClear() {
        pin1.setImageResource(R.drawable.circle_off)
        pin2.setImageResource(R.drawable.circle_off)
        pin3.setImageResource(R.drawable.circle_off)
        pin4.setImageResource(R.drawable.circle_off)
        pin5.setImageResource(R.drawable.circle_off)
        pin6.setImageResource(R.drawable.circle_off)
    }

    fun check() {
        when (variant) {
            RETURN_THE_PIN -> {
                // Вариант. Вернуть PIN.
                pinListener.onPinResult(action, true, "",  pinCode )
                dismiss()
            }

            CHECK_PIN -> {
                // Вариант. Проверить PIN и вернуть результат проверки.
                if (appPinCode == pinCode) {
                    pinListener.onPinResult(action, true, "ok", pinCode)
                } else {
                    pinListener.onPinResult(action, false,
                        getString(R.string.PIN_bad), "")
                }
                dismiss()
            }

            CHECK_PIN_WHILE_FALSE, CHECK_PIN_AND_BIO -> {
                // Вариант. Проверять PIN, пока не введётся правильный и вернуть результат проверки..
                if (appPinCode == pinCode) {
                    pinListener.onPinResult(action, true, "ok", pinCode)
                    dismiss()
                } else {
                    errorMessage.setText(R.string.error_PIN)
                    pinCode = ""
                    pinsClear()
                }
            }

            ENTER_NEW_PIN -> {
                // Вариант. Ввод два раза нового PIN. Возврат нового PIN.
                when (step) {
                    0 -> {
                        pinCode1 = pinCode
                        step += 1
                        pinCode = ""
                        pinsClear()
                        dlg.setTitle(getString(R.string.enter_PIN2))
                    }

                    1 -> {
                        if (pinCode1 == pinCode) {
                            appPinCode = pinCode
                            pinListener.onPinResult(action, true,
                                getString(R.string.PIN_changed), pinCode)
                            dismiss()
                        } else {
                            pinListener.onPinResult(action, false,
                                getString(R.string.PIN_new_bad), "")
                            dismiss()
                        }

                    }
                }
            }

            CHECK_PIN_ENTER_NEW_PIN -> {
                // Вариант. Проверка старого PIN, ввод два раза нового PIN. Возврат нового PIN.
                when (step) {
                    0 -> {
                        if (appPinCode == pinCode) {
                            step += 1
                            pinCode = ""
                            pinsClear()
                            dlg.setTitle(getString(R.string.enter_PIN1))
                        } else {
                            pinListener.onPinResult(action, false,
                                getString(R.string.PIN_bad), "")
                            dismiss()
                        }
                    }

                    1 -> {
                        pinCode1 = pinCode
                        step += 1
                        pinCode = ""
                        pinsClear()
                        dlg.setTitle(getString(R.string.enter_PIN2))
                    }

                    2 -> {
                        if (pinCode1 == pinCode) {
                            appPinCode = pinCode
                            pinListener.onPinResult(action, true,
                                getString(R.string.PIN_changed), pinCode)
                            dismiss()
                        } else {
                            pinListener.onPinResult(action, false,
                                getString(R.string.PIN_new_bad), "")
                            dismiss()
                        }

                    }
                }
            }
        }
    }

    fun authenticate(context: FragmentActivity) {
        val executor = context.mainExecutor
        val biometricPrompt = BiometricPrompt(
            context,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    pinListener.onPinResult(action, true, "ok", "")
                    dismiss()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    errorMessage.setText(R.string.Bio_error)
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

}