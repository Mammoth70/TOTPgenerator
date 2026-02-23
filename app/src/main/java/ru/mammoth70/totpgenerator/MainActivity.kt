package ru.mammoth70.totpgenerator

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.lazy

class MainActivity : AppActivity(),
    OTPauthDialog.OnAddResultListener, OTPauthDialog.OnDeleteResultListener, PinDialog.OnPinResultListener {
    // Главная activity приложения.
    // Выводит список токенов.


    override val idLayout = R.layout.activity_main
    override val idActivity = R.id.frameMainActivity
    override val isSecure = true // Нельзя делать скриншоты.

    companion object {
        private var unLocked = false
    }

    private val floatingActionButtonQR: FloatingActionButton by lazy { findViewById(R.id.floatingActionButtonQR) }
    private val navView: BottomNavigationView by lazy { findViewById(R.id.bottom_navigation) }
    private val tokensList: RecyclerView by lazy { findViewById(R.id.tokensList) }
    private val adapter : TokensAdapter by lazy { TokensAdapter() }
    private val viewModel: TokensViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!unLocked && isHaveHashPin) {
            // Ввод и проверка PIN-кода перед входом.
            enterPin()
        } else {
            unLocked = true
        }

        topAppBar.setTitle(R.string.app_name)
        topAppBar.setNavigationOnClickListener {
            // Обработчик кнопки "назад".
            finish()
        }

        lifecycleScope.launch {
            // Обработчик "битой" базы.
            OTPauthDataRepo.databaseError.collect { errorMessage ->
                if (errorMessage != null) {
                    showErrorDialog(errorMessage)
                    OTPauthDataRepo.clearDatabaseError()
                }
            }
        }

        // Настройка верхнего меню.
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
        tokensList.adapter = adapter
        tokensList.layoutManager = LinearLayoutManager(this)
        tokensList.isClickable = true
        (tokensList.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        // Подписка на ViewModel.
        viewModel.tokensLiveData.observe(this) { adapter.submitList(it) }

        floatingActionButtonQR.setOnClickListener { _ ->
            // Обработчик кнопки "QR".
            addQRsecret()
        }

        // Настройка нижнего меню.
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_settings -> {
                    // Обработчик кнопки "Настройка".
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }

                R.id.item_about -> {
                    // Обработчик кнопки "Инфо".
                    AboutDialog().show(supportFragmentManager, "ABOUT_DIALOG")
                }

            }
            false
        }
    }


    private fun showSnackbar(message: String) {
        // Функция выводит Snackbar со строкой message.

        Snackbar.make(floatingActionButtonQR, message, Snackbar.LENGTH_SHORT).show()
    }


    private fun showSnackbar(@StringRes resId: Int) {
        // Функция выводит Snackbar со строкой, хранимой в ресурсе resId.

        showSnackbar(getString(resId))
    }


    private fun addQRsecret() {
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
                    lifecycleScope.launch {
                        secretsNew.forEach { secret ->
                            val isAdded = OTPauthDataRepo.addSecret(secret)

                            if (isAdded) {
                                showSnackbar(R.string.secret_added)
                            } else {
                                showSnackbar(R.string.secret_add_error)
                            }
                        }
                        TokensTrigger.sendCommandUpdate()
                    }
                } else {
                    showSnackbar(R.string.qr_code_error)
                }
            }
            .addOnCanceledListener { }
            .addOnFailureListener { _ -> showSnackbar(R.string.qr_error) }
    }


    private fun addSecret() {
        // Функция вызывает Dialog для добавления OTPauth.
        // Dialog возвращает результат через листенер.

        val bundle = Bundle()
        bundle.putString(OTPauthDialog.INTENT_TOTP_ACTION, OTPauthDialog.ACTION_TOTP_ADD)
        val otpAuthDialog = OTPauthDialog()
        otpAuthDialog.setArguments(bundle)
        otpAuthDialog.isCancelable = false
        otpAuthDialog.setOnAddResultListener(this)
        otpAuthDialog.show(this.supportFragmentManager, "SECRET_DIALOG")
    }


    private fun viewSecret(id: Long) {
        // Функция вызывает Dialog для чтения OTPauth.

        val bundle = Bundle()
        bundle.putString(OTPauthDialog.INTENT_TOTP_ACTION, OTPauthDialog.ACTION_TOTP_VIEW)
        bundle.putLong(OTPauthDialog.INTENT_TOTP_ID, id)
        val otpAuthDialog = OTPauthDialog()
        otpAuthDialog.setArguments(bundle)
        otpAuthDialog.isCancelable = false
        otpAuthDialog.show(this.supportFragmentManager, "SECRET_DIALOG")
    }


    private fun deleteSecret(id: Long) {
        // Функция вызывает Dialog для удаления OTPauth.
        // Dialog возвращает результат через листенер.

        val bundle = Bundle()
        bundle.putString(OTPauthDialog.INTENT_TOTP_ACTION, OTPauthDialog.ACTION_TOTP_DELETE)
        bundle.putLong(OTPauthDialog.INTENT_TOTP_ID, id)
        val otpAuthDialog = OTPauthDialog()
        otpAuthDialog.setArguments(bundle)
        otpAuthDialog.isCancelable = false
        otpAuthDialog.setOnDeleteResultListener(this)
        otpAuthDialog.show(this.supportFragmentManager, "SECRET_DIALOG")
    }


    private fun itemLongClick(totp: String) : Boolean {
        // Обработчик двойного клика по токену.
        // Вызывает функцию копирования токена в clipboard.

        tokenToClipBoard(totp)
        return true
    }


    private fun itemClick(totp: String) {
        // Обработчик клика по токену.
        // Вызывает функцию копирования токена в clipboard.

        tokenToClipBoard(totp)
    }


    private fun showPopupMenu(view: View, id: Long) {
        // Обработчик клика на кнопку меню.

        val popupMenu = PopupMenu(this, view)
        popupMenu.gravity = Gravity.END
        popupMenu.inflate(R.menu.token_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_view_token -> {
                    // Обработчик меню "Просмотр".
                    viewSecret(id)
                    true
                }

                R.id.item_delete_token -> {
                    // Обработчик меню "Удалить".
                    deleteSecret(id)
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }


    private fun tokenToClipBoard(totp: String) {
        // Функция копирует токен в clipboard.

        if (totp.isEmpty()) return

        val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("OTP Code", totp)

        // Скрываем чувствительные данные. (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
            clip.description.extras = extras
        }

        clipboard.setPrimaryClip(clip)

        // Уведомление пользователя только для старых версий.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            showSnackbar(R.string.token_to_clipboard)
        }

        // Автоматическая очистка через 30 секунд.
        lifecycleScope.launch {
            delay(30000)
            val currentClip = clipboard.primaryClip
            if (currentClip != null && currentClip.itemCount > 0) {
                val currentText = currentClip.getItemAt(0).text?.toString()
                if (currentText == totp) {
                    clipboard.clearPrimaryClip()
                }
            }
        }

    }


    override fun onAddResult(auth: OTPauth) {
        // Обработчик возврата из SecretDialog Add после нажатия кнопки PositiveButton.

        lifecycleScope.launch {
            val isAdded = OTPauthDataRepo.addSecret(auth)

            if (isAdded) {
                TokensTrigger.sendCommandUpdate()
                showSnackbar(R.string.secret_added)
            } else {
                showSnackbar(R.string.secret_add_error)
            }
        }
    }


    override fun onDeleteResult(id: Long) {
        // Обработчик возврата из SecretDialog Delete после нажатия кнопки PositiveButton.

        lifecycleScope.launch {
            val isDeleted = OTPauthDataRepo.deleteSecret(id)

            if (isDeleted) {
                TokensTrigger.sendCommandUpdate()
                showSnackbar(R.string.secret_deleted)
            } else {
                showSnackbar(R.string.secret_delete_error)
            }
        }
    }


    private fun enterPin() {
        // Вызов окна ввода PIN.

        val bundle = Bundle()
        bundle.putString(PinDialog.INTENT_PIN_ACTION, PinDialog.ACTION_ENTER_PIN)
        bundle.putString(PinDialog.INTENT_PIN_SCREEN, PinDialog.SCREEN_FULL)
        val pinDialog = PinDialog()
        pinDialog.setArguments(bundle)
        pinDialog.isCancelable = false
        pinDialog.setOnPinResultListener(this)
        pinDialog.show(this.supportFragmentManager, "PIN_DIALOG")
    }


    override fun onPinResult(action: String, result: Boolean, message: String) {
        // Обработчик возврата из PinDialog.

        if ((action == PinDialog.ACTION_ENTER_PIN) && (!result)) {
            finish()
        } else {
            unLocked = true
        }
    }


    private fun showErrorDialog(errorMessage: String) {
        // Вызывает диалоговое окно, если БД накрылась.

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.db_error_title)
            .setMessage(getString(R.string.db_fatal_error_message, errorMessage))
            .setIcon(R.drawable.ic_action_error)
            .setCancelable(false)
            .setPositiveButton(R.string.exit) { _, _ ->
                finishAffinity() // Закрываем всё приложение.
            }
            .show()
    }

}