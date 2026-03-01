package ru.mammoth70.totpgenerator

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar

abstract class AppActivity : AppCompatActivity() {
    // Абстрактный класс для создания Activity приложения.


    @get:LayoutRes
    protected abstract val idLayout : Int
    @get:IdRes
    protected abstract val idActivity : Int
    protected open val isSecure: Boolean = false // Флаг запрещения скринштов.
    protected open val topAppBar: MaterialToolbar by lazy { findViewById(R.id.topAppBar) }
    private val parentLayout: View by lazy { findViewById(android.R.id.content) }


    override fun onCreate(savedInstanceState: Bundle?) {
        // Функция вызывается при создании Activity.
        // Может (и даже должна) быть переопределена.

        super.onCreate(savedInstanceState)

        if (isSecure) {
            // Если в Activity-потомке будет включён флаг isSecure,
            // то в ней будет запрещено создание скриншотов.
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        enableEdgeToEdge()
        setContentView(idLayout)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(idActivity))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top,
                systemBars.right, systemBars.bottom)
            insets
        }
    }


    fun showSnackbar(message: String) {
        // Функция выводит Snackbar со строкой message.

        Snackbar.make(parentLayout, message, Snackbar.LENGTH_SHORT).show()
    }


    fun showSnackbar(@StringRes resId: Int) {
        // Функция выводит Snackbar со строкой, хранимой в ресурсе resId.

        showSnackbar(getString(resId))
    }


    fun showToast(message: String) {
        // Функция выводит Toast со строкой message.

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    @Suppress("unused")
    fun showToast(@StringRes resId: Int) {
        // Функция выводит Toast со строкой, хранимой в ресурсе resId.

        showToast(getString(resId))
    }

}