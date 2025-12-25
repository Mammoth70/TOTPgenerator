package ru.mammoth70.totpgenerator

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class PinBox : DialogFragment() {
    // Класс создаёт диалоговое окно ввода PIN.

    interface OnPinResultListener {
        fun onPinResult(action: String, result: Boolean, message: String, pin: String)
    }
    private lateinit var pinListener: OnPinResultListener
    fun setOnPinResultListener(listener: OnPinResultListener) {
        this.pinListener = listener
    }

    companion object {
        const val INTENT_PIN_ACTION = "action"
        const val ACTION_ENTER_PIN = "enter_pin"
        const val ACTION_DELETE_PIN = "delete_pin"
        const val ACTION_SET_NEW_PIN = "set_new_pin"
        const val ACTION_UPDATE_PIN = "update_pin"

        const val INTENT_PIN_SCREEN = "screen"
        const val SCREEN_FULL = "full"
    }

    private val dlg: AlertDialog by lazy { dialog as AlertDialog }
    private val btn0: Button by lazy { dlg.findViewById<Button>(R.id.btn0)!!}
    private val btn1: Button by lazy { dlg.findViewById<Button>(R.id.btn1)!!}
    private val btn2: Button by lazy { dlg.findViewById<Button>(R.id.btn2)!!}
    private val btn3: Button by lazy { dlg.findViewById<Button>(R.id.btn3)!!}
    private val btn4: Button by lazy { dlg.findViewById<Button>(R.id.btn4)!!}
    private val btn5: Button by lazy { dlg.findViewById<Button>(R.id.btn5)!!}
    private val btn6: Button by lazy { dlg.findViewById<Button>(R.id.btn6)!!}
    private val btn7: Button by lazy { dlg.findViewById<Button>(R.id.btn7)!!}
    private val btn8: Button by lazy { dlg.findViewById<Button>(R.id.btn8)!!}
    private val btn9: Button by lazy { dlg.findViewById<Button>(R.id.btn9)!!}
    private val btnBack: Button by lazy { dlg.findViewById<Button>(R.id.btnBack)!!}
    private val btnCancel: Button by lazy { dlg.findViewById<Button>(R.id.btnCancel)!!}
    private val pin1: ImageView by lazy { dlg.findViewById<ImageView>(R.id.pin1)!!}
    private val pin2: ImageView by lazy { dlg.findViewById<ImageView>(R.id.pin2)!!}
    private val pin3: ImageView by lazy { dlg.findViewById<ImageView>(R.id.pin3)!!}
    private val pin4: ImageView by lazy { dlg.findViewById<ImageView>(R.id.pin4)!!}
    private val pin5: ImageView by lazy { dlg.findViewById<ImageView>(R.id.pin5)!!}
    private val pin6: ImageView by lazy { dlg.findViewById<ImageView>(R.id.pin6)!!}
    private val errorMessage: TextView by lazy {dlg.findViewById<TextView>(R.id.errorMessage)!!}


    private val _action: String by lazy { requireArguments().getString(INTENT_PIN_ACTION,"") }
    private val _screen: String by lazy { requireArguments().getString(INTENT_PIN_SCREEN,"") }

    private var _pinCode = ""
    private var _pinCode1 = ""
    private var _variant = 1
    private var _step = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = if (_screen == SCREEN_FULL) {
            AlertDialog.Builder(
                requireActivity(),
                // Theme_Material_NoActionBar_Fullscreen
                // Theme_Material_Light_NoActionBar_Fullscreen
                android.R.style.Theme_Material_Light_NoActionBar_Fullscreen
            )
        } else {
            AlertDialog.Builder(requireActivity() )
        }
        builder.setView(R.layout.frame_dialog_pin)
        builder.setCancelable(false)
        when (_action) {
            ACTION_ENTER_PIN, ACTION_DELETE_PIN -> {
                // Проверить PIN и вернуть результат проверки.
                builder.setTitle(getString(R.string.enter_PIN))
                _variant = 0
            }

            ACTION_SET_NEW_PIN -> {
                // Ввод два раза нового PIN.
                builder.setTitle(getString(R.string.enter_PIN1))
                _variant = 2
            }

            ACTION_UPDATE_PIN -> {
                if (appPinCode.isBlank()) {
                    // Ввод два раза нового PIN, если старый - пустой.
                    builder.setTitle(getString(R.string.enter_PIN1))
                    _variant = 2
                } else {
                    // Проверка старого PIN, ввод два раза нового PIN.
                    builder.setTitle(getString(R.string.enter_old_PIN))
                    _variant = 3
                }

            }

        }
        return builder.create()
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
            pinListener.onPinResult(_action, false,
                getString(R.string.PIN_cancel), "")
            dismiss()
        }
    }

    fun addPin(symbol: String) {
        errorMessage.text = ""
        if (_pinCode.length < 6) {
            _pinCode += symbol
            pinsAdd(_pinCode.length)
        }
        if (_pinCode.length == 6) {
            check()
        }
    }

    fun deletePin() {
        errorMessage.text = ""
        if (_pinCode.isNotEmpty()) {
            _pinCode = _pinCode.dropLast(1)
            pinsDel(_pinCode.length)
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
        when (_variant) {
            -1 -> {
                // Вариант. Вернуть PIN.
                pinListener.onPinResult(_action, true, "",  _pinCode )
                dismiss()
            }

            0 -> {
                // Вариант. Проверить PIN и вернуть результат проверки.
                if (appPinCode == _pinCode) {
                    pinListener.onPinResult(_action, true, "ok", _pinCode)
                } else {
                    pinListener.onPinResult(_action, false,
                        getString(R.string.PIN_bad), "")
                }
                dismiss()
            }

            1 -> {
                // Вариант. Проверять PIN, пока не введётся правильный.
                if (appPinCode == _pinCode) {
                    pinListener.onPinResult(_action, true, "ok", _pinCode)
                    dismiss()
                } else {
                    errorMessage.setText(R.string.error_PIN)
                    _pinCode = ""
                    pinsClear()
                }
            }

            2 -> {
                // Вариант. Ввод два раза нового PIN. Смена PIN.
                when (_step) {
                    0 -> {
                        _pinCode1 = _pinCode
                        _step += 1
                        _pinCode = ""
                        pinsClear()
                        dlg.setTitle(getString(R.string.enter_PIN2))
                    }

                    1 -> {
                        if (_pinCode1 == _pinCode) {
                            appPinCode = _pinCode
                            pinListener.onPinResult(_action, true,
                                getString(R.string.PIN_changed), _pinCode)
                            dismiss()
                        } else {
                            pinListener.onPinResult(_action, false,
                                getString(R.string.PIN_new_bad), "")
                            dismiss()
                        }

                    }
                }
            }

            3 -> {
                // Вариант. Проверка старого PIN, ввод два раза нового PIN. Смена PIN.
                when (_step) {
                    0 -> {
                        if (appPinCode == _pinCode) {
                            _step += 1
                            _pinCode = ""
                            pinsClear()
                            dlg.setTitle(getString(R.string.enter_PIN1))
                        } else {
                            pinListener.onPinResult(_action, false,
                                getString(R.string.PIN_bad), "")
                            dismiss()
                        }
                    }

                    1 -> {
                        _pinCode1 = _pinCode
                        _step += 1
                        _pinCode = ""
                        pinsClear()
                        dlg.setTitle(getString(R.string.enter_PIN2))
                    }

                    2 -> {
                        if (_pinCode1 == _pinCode) {
                            appPinCode = _pinCode
                            pinListener.onPinResult(_action, true,
                                getString(R.string.PIN_changed), _pinCode)
                            dismiss()
                        } else {
                            pinListener.onPinResult(_action, false,
                                getString(R.string.PIN_new_bad), "")
                            dismiss()
                        }

                    }
                }
            }
        }
    }

}