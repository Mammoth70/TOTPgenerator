package ru.mammoth70.totpgenerator

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.ListView
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.lazy
import ru.mammoth70.totpgenerator.App.Companion.appSecrets
import ru.mammoth70.totpgenerator.App.Companion.appTokens
import ru.mammoth70.totpgenerator.App.Companion.unLocked

class MainActivity : AppActivity(),
    SecretBox.OnAddResultListener, SecretBox.OnDeleteResultListener, PinBox.OnPinResultListener {
    // Главная activity приложения.
    // Выводит список токенов.
    companion object {
        lateinit var mainContext : FragmentActivity
    }

    override val idLayout = R.layout.activity_main
    override val idActivity = R.id.frameMainActivity
    override val isSecure = true

    private val floatingActionButtonQR: FloatingActionButton by lazy { findViewById(R.id.floatingActionButtonQR) }
    private val navView: BottomNavigationView by lazy { findViewById(R.id.bottom_navigation) }
    private val tokensList: ListView by lazy { findViewById(R.id.tokensList) }
    private val adapter : TokensAdapter by
        lazy { TokensAdapter(this, R.layout.item_list, appTokens) }
    private val viewModel: TokensViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainContext = this as FragmentActivity
        topAppBar.setTitle(R.string.app_name)
        topAppBar.setNavigationOnClickListener {
            // Обработчик кнопки "назад".
            finish()
        }
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // Обработчик меню "+".
                R.id.item_add_token -> {
                    addSecret()
                    true
                }

                // Обработчик меню "QR".
                R.id.item_qr -> {
                    addQRsecret()
                    true
                }

                else -> false
            }
        }
        // Настройка адаптера.
        adapter.setOnBtnMenuClick(::showPopupMenu)
        adapter.setOnItemViewClick(::itemClick)
        adapter.setOnItemViewLongClick(::itemLongClick)
        tokensList.setAdapter(adapter)
        tokensList.isClickable = true

        // Настройка ViewModel.
        ViewModelProvider(this)[TokensViewModel::class.java].liveData.observe(this)
        { token ->
            // Вызывается при получении нового токена из потока.
            // Меняет список токенов и вызывает перестроение адаптера.
            val num = token.num
            // Проверка на то, что массивы секретов и токенов синхронизированы.
            if ((appTokens.size > num) && appTokens[num].id == appSecrets[num].id) {
                appTokens[num] = token
                adapter.notifyDataSetChanged()
            }
        }

        if (!unLocked && appPinCode.isNotBlank()) {
            // Ввод и проверка PIN-кода перед входом.
            enterPin()
        } else {
            unLocked = true
        }

        floatingActionButtonQR.setOnClickListener { _ ->
            // Обработчик кнопки "QR".
            addQRsecret()
        }

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }

                R.id.item_about -> {
                    val bundle = Bundle()
                    bundle.putString(AboutBox.ABOUT_TITLE, getString(R.string.app_name))
                    val text =
                        getString(R.string.description) + "\n\n" +
                                getString(R.string.version) + " " +
                                BuildConfig.VERSION_NAME
                    bundle.putString(AboutBox.ABOUT_MESSAGE, text)
                    val aboutBox = AboutBox()
                    aboutBox.setArguments(bundle)
                    aboutBox.show(this.supportFragmentManager, "ABOUT_DIALOG")
                }

            }
            false
        }
    }

    fun refreshSecrets() {
        // Функция перечитывает списки OTPauth и токенов из БД,
        // и заставляет переинициализироваться TokensViewModel.
        DBhelper.dbHelper.readAllSecrets()
        adapter.notifyDataSetChanged()
        viewModel.sendCommandUpdate()
    }

    fun showSnackbar(message: String) {
        // Функция выводит Snackbar со строкой message.
        Snackbar.make(tokensList, message, Snackbar.LENGTH_SHORT).show()
    }

    fun showSnackbar(resId: Int) {
        // Функция выводит Snackbar со строкой, хранимой в ресурсе resId.
        showSnackbar(getString(resId))
    }

    fun addQRsecret() {
        // Функция читает QR, парсит его, добавляет распарсенный OTPauth в БД и вызывает refreshSecrets().
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        val scanner = GmsBarcodeScanning.getClient(this,options)
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue: String? = barcode.rawValue
                val secretsNew = parseQR(rawValue)
                if (secretsNew.isNotEmpty()) {
                    secretsNew.forEach {
                        if (DBhelper.dbHelper.addSecret(it) == 0) {
                            showSnackbar(R.string.key_added)
                        } else {
                            showSnackbar(R.string.key_add_error)
                        }
                    }
                    refreshSecrets()
                } else {
                    showSnackbar(R.string.qr_code_error)
                }
            }
            .addOnCanceledListener { }
            .addOnFailureListener { _ -> showSnackbar(R.string.qr_error) }
    }

    fun addSecret() {
        // Функция вызывает Dialog для добавления OTPauth.
        // Dialog возвращает результат через листенер.
        val bundle = Bundle()
        bundle.putString(SecretBox.INTENT_TOTP_ACTION, SecretBox.ACTION_TOTP_ADD)
        val secretDialog = SecretBox()
        secretDialog.setArguments(bundle)
        secretDialog.isCancelable = false
        secretDialog.setOnAddResultListener(this)
        secretDialog.show(this.supportFragmentManager, "SECRET_DIALOG")
    }

    fun viewSecret(num: Int) {
        // Функция вызывает Dialog для чтения OTPauth.
        val bundle = Bundle()
        bundle.putString(SecretBox.INTENT_TOTP_ACTION, SecretBox.ACTION_TOTP_VIEW)
        bundle.putInt(SecretBox.INTENT_TOTP_NUM, num)
        val secretDialog = SecretBox()
        secretDialog.setArguments(bundle)
        secretDialog.isCancelable = false
        secretDialog.show(this.supportFragmentManager, "SECRET_DIALOG")
    }

    fun deleteSecret(num: Int) {
        // Функция вызывает Dialog для удаления OTPauth.
        // Dialog возвращает результат через листенер.
        val bundle = Bundle()
        bundle.putString(SecretBox.INTENT_TOTP_ACTION, SecretBox.ACTION_TOTP_DELETE)
        bundle.putInt(SecretBox.INTENT_TOTP_NUM, num)
        val secretDialog = SecretBox()
        secretDialog.setArguments(bundle)
        secretDialog.isCancelable = false
        secretDialog.setOnDeleteResultListener(this)
        secretDialog.show(this.supportFragmentManager, "SECRET_DIALOG")
    }

    private fun itemLongClick(view: View) : Boolean {
        // Обработчик двойного клика по токену.
        // Вызывает функцию копирования токена в clipboard.
        val position = view.tag as? Int ?: return false
        if (position >= 0 && position < adapter.count) {
            tokenToClipBoard(position)
            return true
        }
        return false
    }

    private fun itemClick(view: View) {
        // Обработчик клика по токену.
        // Вызывает функцию копирования токена в clipboard.
        val position = view.tag as? Int ?: return
        if (position >= 0 && position < adapter.count) {
            tokenToClipBoard(position)
        }
    }

    private fun showPopupMenu(view: View) {
        // Функция вызывается по клику на кнопку меню.
        val position = view.tag as? Int ?: return
        if (position >= 0 && position < adapter.count) {
            val popupMenu = PopupMenu(this, view)
            popupMenu.inflate(R.menu.token_menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.item_view_token -> {
                        viewSecret(position)
                        true
                    }

                    R.id.item_delete_token -> {
                        deleteSecret(position)
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    fun tokenToClipBoard(num: Int) {
        // Функция копирует токен в clipboard.
        val tokenValue = appTokens.getOrNull(num)?.totp ?: return
        if (tokenValue.isEmpty()) return

        val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("OTP Code", tokenValue)

        // Скрываем чувствительные данные (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
            clip.description.extras = extras
        }

        clipboard.setPrimaryClip(clip)

        // Уведомление пользователя только для старых версий
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            showSnackbar(R.string.token_to_clipboard)
        }

        // Автоматическая очистка через 30 секунд
        lifecycleScope.launch {
            delay(30000)
            val currentClip = clipboard.primaryClip
            if (currentClip != null && currentClip.itemCount > 0) {
                val currentText = currentClip.getItemAt(0).text?.toString()
                if (currentText == tokenValue) {
                    clipboard.clearPrimaryClip()
                }
            }
        }

    }

    override fun onAddResult(auth: OTPauth) {
        // Обработчик возврата из SecretDialog Add после нажатия кнопки PositiveButton.
        if (DBhelper.dbHelper.addSecret(auth) == 0) {
            refreshSecrets()
            showSnackbar(R.string.key_added)
        } else {
            showSnackbar(R.string.key_add_error)
        }
    }

    override fun onDeleteResult(num: Int) {
        // Обработчик возврата из SecretDialog Delete после нажатия кнопки PositiveButton.
        if (DBhelper.dbHelper.deleteSecret(num) == 0) {
            refreshSecrets()
            showSnackbar(R.string.key_deleted)
        } else {
            showSnackbar(R.string.key_delete_error)
        }
    }

    fun enterPin() {
        // Вызов окна ввода PIN
        val bundle = Bundle()
        bundle.putString(PinBox.INTENT_PIN_ACTION, PinBox.ACTION_ENTER_PIN)
        bundle.putString(PinBox.INTENT_PIN_SCREEN, PinBox.SCREEN_FULL)
        val pinBox = PinBox()
        pinBox.setArguments(bundle)
        pinBox.isCancelable = false
        pinBox.setOnPinResultListener(this)
        pinBox.show(this.supportFragmentManager, "PIN_DIALOG")
    }

    override fun onPinResult(action: String, result: Boolean, message: String, pin: String) {
        // Обработчик возврата из PinDialog.
        if ((action == PinBox.ACTION_ENTER_PIN) && (!result)) {
            finish()
        } else {
            unLocked = true
        }
    }

}