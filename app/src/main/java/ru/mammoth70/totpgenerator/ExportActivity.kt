package ru.mammoth70.totpgenerator

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportActivity : AppActivity() {
    //  Activity выводит список секретов для выгрузки.


    override val idLayout = R.layout.activity_export
    override val idActivity = R.id.frameExportActivity

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.secretsList) }
    private val btnExportJSON: MaterialButton by lazy { findViewById(R.id.btnExportJSON) }
    private val btnExportQR: MaterialButton by lazy { findViewById(R.id.btnExportQR) }
    private val secretsAdapter: OTPauthAdapter by lazy {
        OTPauthAdapter(
            OTPauthDataRepo.secrets,
            { btnExportJSON.isEnabled = it },
            { btnExportQR.isEnabled = it },
            { startDialogQR(it) }
        )
    }

    private var pendingPassword: String? = null

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"))
        // Launcher вызывается после получения пароля.

    { uri ->
        val password = pendingPassword
        uri?.let { fileUri ->
            val secrets = secretsAdapter.getSelectedItems()
            if (password != null && !secrets.isEmpty()) {
                startExportJSON(secrets, fileUri, password)
            }
        }
        pendingPassword = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        topAppBar.setTitle(R.string.title_export)
        topAppBar.setNavigationOnClickListener {
            // Обработчик кнопки "назад".
            finish()
        }

        // Настройка верхнего меню.
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                // Обработчик меню "Select all".
                R.id.item_selectAll -> {
                    secretsAdapter.selectAll()
                    true
                }

                // Обработчик меню "Deselect all".
                R.id.item_deselectAll -> {
                    secretsAdapter.deselectAll()
                    true
                }

                else -> false
            }
        }

        btnExportJSON.isEnabled = false
        btnExportJSON.setOnClickListener { _ ->
            // Обработчик кнопки "Экспорт в JSON".
            startDialogExportJSON()
        }

        btnExportQR.isEnabled = false
        btnExportQR.setOnClickListener { _ ->
            // Обработчик кнопки "Экспорт в QR".
            startDialogExportQR()
        }

        // Настройка адаптера.
        recyclerView.apply {
            adapter = secretsAdapter
            setHasFixedSize(true)
        }

    }


    private fun setButtonLoading(isLoading: Boolean) {
        // Функция выводит "крутилку" на кнопке "Экспорт".

        if (isLoading) {
            val spec = CircularProgressIndicatorSpec(
                this, null, 0,
                com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
            )
            val progressDrawable = IndeterminateDrawable.createCircularDrawable(this, spec)

            btnExportJSON.icon = progressDrawable
            btnExportJSON.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            btnExportJSON.isEnabled = false
            btnExportJSON.setText(R.string.encrypting)
        } else {
            btnExportJSON.icon = null
            secretsAdapter.deselectAll()
            btnExportJSON.setText(R.string.title_export_json)
        }
    }


    private fun startDialogExportJSON() {
        // Вызов диалогового окна для ввода пароля шифрования экспортируемого файла.

        if (secretsAdapter.getSelectedItems().isEmpty()) {
            showSnackbar(R.string.select_item_error)
            return
        }

        val passwordExportDialog = PasswordExportDialog { password ->
            pendingPassword = password
            val date = SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
            val fileName = "$date.totp"
            exportLauncher.launch(fileName)
        }
        passwordExportDialog.show(supportFragmentManager, "EXPORT_PASSWORD_DIALOG")
    }


    private fun startDialogExportQR() {
        // Вызов диалогового окна, показывающего QR-код с экспортируемыми секретами.

        val allSelectedSecrets = secretsAdapter.getSelectedItems()
        val standardSecrets = allSelectedSecrets.filter { it.digits != 7 && it.period == 30 }
        val nonStandardCount = allSelectedSecrets.size - standardSecrets.size

        if (allSelectedSecrets.isEmpty()) {
            showSnackbar(R.string.select_item_error)
            return
        }

        if (standardSecrets.isEmpty()) {
            showSnackbar(R.string.select_all_non_standard_items_error)
            return
        }

        if (standardSecrets.size > 10) {
            showSnackbar(R.string.select_standart_items_error)
            return
        }

        if (nonStandardCount > 0) {
            showSnackbar(getString(R.string.mixed_standard_items_warning, standardSecrets.size, nonStandardCount))
        }

        val migrationUri = generateMigrationUri(standardSecrets)
        QrExportDialog(migrationUri).show(supportFragmentManager, "QR_EXPORT_DIALOG")
        secretsAdapter.deselectAll()
    }


    private fun startDialogQR(secret:OTPauth ) {
        // Вызов диалогового окна, показывающего QR-код с секретом.

        val uri = generateOtpauthUri(secret)
        QrExportDialog(uri).show(supportFragmentManager, "QR_EXPORT_DIALOG")
    }


    private fun startExportJSON(secrets: List<OTPauth>, uri: Uri, password: String) {
        // Шифрование и выгрузка файла экспорта.

        setButtonLoading(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val json = secretsToJson(secrets)

                contentResolver.openOutputStream(uri)?.use { os ->
                    encryptAndWrite(json, password, os)

                    withContext(Dispatchers.Main) {
                        setButtonLoading(false)
                        showSnackbar(R.string.export_success)
                    }
                } ?: throw IOException(getString(R.string.error_io_stream))

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setButtonLoading(false)
                    showSnackbar(e.localizedMessage ?: getString(R.string.unknown_error))
                }
            }
        }
    }

}