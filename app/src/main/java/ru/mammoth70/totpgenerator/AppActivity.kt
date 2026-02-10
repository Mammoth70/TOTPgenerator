package ru.mammoth70.totpgenerator

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar

abstract class AppActivity : AppCompatActivity() {
    // Абстрактный класс для создания Activity приложения.

    protected abstract val idLayout : Int
    protected abstract val idActivity : Int
    protected open val isSecure: Boolean = false // Флаг запрещения скринштов.
    protected open val topAppBar: MaterialToolbar by lazy { findViewById(R.id.topAppBar) }

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

}