package ru.mammoth70.totpgenerator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import ru.mammoth70.totpgenerator.App.Companion.secrets
import ru.mammoth70.totpgenerator.App.Companion.tokens

class MainActivity : AppActivity(),
    SecretBox.OnAddResultListener, SecretBox.OnDeleteResultListener, PinBox.OnPinResultListener {
    // Главная activity приложения.
    // Выводит список токенов.

    override val idLayout = R.layout.activity_main
    override val idActivity = R.id.frameMainActivity
    private val tokensList: ListView by lazy { findViewById(R.id.tokensList) }
    private val adapter : TokensAdapter by
    lazy { TokensAdapter(this, R.layout.list_item, tokens) }
    private val viewModel: TokensViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topAppBar.setTitle(R.string.app_name)
        topAppBar.setNavigationOnClickListener {
            // Обработчик кнопки "назад".
            finish()
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
                if ((tokens.size > num) && tokens[num].id == secrets[num].id) {
                    tokens[num] = token
                    adapter.notifyDataSetChanged()
                }
        }

        if (appPinCode.isNotBlank()) {
            // Ввод и проверка PIN-кода перед входом.
            enterPin()
        }
    }

    fun refreshSecrets() {
        // Функция перечитывает списки OTPauth и токенов из БД,
        // и заставляет переинициализироваться TokensViewModel.
        DBhelper.dbHelper.readAllSecrets()
        if (tokens.isEmpty()) {
            adapter.notifyDataSetChanged()
        }
        viewModel.sendCommandUpdate()
    }

    fun onAddSecretClicked(@Suppress("UNUSED_PARAMETER")ignored: View?) {
        // Функция обработчик клика FAB "add".
        // Вызывает функцию добавления OTPauth.
        addSecret()
    }

    fun onQRSecretClicked(@Suppress("UNUSED_PARAMETER")ignored: View?) {
        // Функция обработчик клика FAB "qr".
        // Вызывает функцию чтения QR с OTPauth.
        addQRsecret()
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
        tokenToClipBoard(view.tag.toString().toInt())
        return true
    }

    private fun itemClick(view: View) {
        // Обработчик клика по токену.
        // Вызывает функцию копирования токена в clipboard.
        tokenToClipBoard(view.tag.toString().toInt())
    }

    private fun showPopupMenu(view: View) {
        // Функция вызывается по клику на кнопку меню.
        val position: Int? = view.tag as Int?
        position?.let {
            val popupMenu = PopupMenu(this, view)
            popupMenu.inflate(R.menu.token_menu)
            popupMenu.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem): Boolean {
                    when (item.itemId) {
                        R.id.item_view_token -> {
                            viewSecret(position)
                            return true
                        }

                        R.id.item_delete_token -> {
                            deleteSecret(position)
                            return true
                        }

                        else -> return false
                    }
                }
            })
            popupMenu.show()
        }
    }

    fun tokenToClipBoard(num: Int) {
        // Функция копирует токен в clipboard.
        val clipboardManager = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text_label", tokens[num].totp)
        clipboardManager.setPrimaryClip(clipData)
        showSnackbar(R.string.token_to_clipboard)
    }

    fun onAboutClicked(@Suppress("UNUSED_PARAMETER")ignored: MenuItem?) {
        // Обработчик кнопки меню "about".
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

    fun onSettingsClicked(@Suppress("UNUSED_PARAMETER")ignored: MenuItem?) {
        // Обработчик кнопки меню "settings".
        // Функция - обработчик кнопки меню "настройки".
        // Вызывает соответствующую Activity.
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onAddResult(result: OTPauth) {
        // Обработчик возврата из SecretDialog Add после нажатия кнопки PositiveButton.
        if (DBhelper.dbHelper.addSecret(result) == 0) {
            refreshSecrets()
            showSnackbar(R.string.key_added)
        } else {
            showSnackbar(R.string.key_add_error)
        }
    }

    override fun onDeleteResult(result: Int) {
        // Обработчик возврата из SecretDialog Delete после нажатия кнопки PositiveButton.
        if (DBhelper.dbHelper.deleteSecret(result) == 0) {
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
        }
    }

}