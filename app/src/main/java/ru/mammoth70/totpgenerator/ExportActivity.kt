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
    private val btnExport: MaterialButton by lazy { findViewById(R.id.btnExport) }
    private val secretsAdapter: OTPauthAdapter by lazy { OTPauthAdapter(OTPauthDataRepo.secrets)
        { hasSelection -> btnExport.isEnabled = hasSelection }}

    private var pendingPassword: String? = null

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream"))
        // Launcher вызывается после получения пароля.

    { uri ->
        val password = pendingPassword
        uri?.let { fileUri ->
            val secrets = secretsAdapter.getSelectedItems()
            if (password != null && !secrets.isEmpty()) {
                startExport(secrets, fileUri, password)
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

        btnExport.isEnabled = false
        btnExport.setOnClickListener { _ ->
            // Обработчик кнопки "Экспорт".
            startDialogExport()
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

            btnExport.icon = progressDrawable
            btnExport.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            btnExport.isEnabled = false
            btnExport.setText(R.string.encrypting)
        } else {
            btnExport.icon = null
            secretsAdapter.deselectAll()
            btnExport.setText(R.string.title_export)
        }
    }


    private fun startDialogExport() {
        // Вызов диалогового окна для ввода пароля шифрования экспортируемого файла.

        if (secretsAdapter.getSelectedItems().isEmpty()) {
            showSnackbar(R.string.select_item)
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


    private fun startExport(secrets: List<OTPauth>, uri: Uri, password: String) {
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