package ru.mammoth70.totpgenerator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

object OTPauthDataRepo {
    // Основной список объектов (единственный источник истины).


    private val dao by lazy {
        OTPauthDataBase.getInstance(App.appContext).otpDao()
    }

    val secrets: List<OTPauth> get() = synchronized(_secrets) { _secrets.toList() }
    private val _secrets= CopyOnWriteArrayList<OTPauth>()  // Список OTPauth, считывается из БД.

    @Volatile
    var isDatabaseReady = false
        private set
    @Volatile
    var databaseError: String? = null
        private set


    suspend fun readAllSecrets() = withContext(Dispatchers.IO) {
        // Функция считывает все секреты из БД в DataRepository.

        try {
            val entities = dao.getAll()

            val fromDb = entities.map { entity ->
                OTPauth(
                    id = entity.id,
                    label = entity.label,
                    issuer = entity.issuer ?: "",
                    secret = decryptString(entity.encryptedSecret),
                    period = entity.period ?: DEFAULT_PERIOD,
                    hash = entity.hash ?: SHA1,
                    digits = entity.digits ?: DEFAULT_DIGITS,
                )
            }

            synchronized(_secrets) {
                _secrets.clear()
                _secrets.addAll(fromDb)
                isDatabaseReady = true
                databaseError = null
            }

        } catch (e: Exception) {
            synchronized(this@OTPauthDataRepo) {
                isDatabaseReady = false
                databaseError = e.message
            }
            LogSmart.e("OTPauthDataRepo", "Ошибка базы", e)
        }
    }


    suspend fun addSecret(otpauth: OTPauth): Boolean = withContext(Dispatchers.IO) {
        // Функция добавляет секрет в DataRepository и в БД.
        // Возвращает true, если успешно и false, если нет.

        synchronized(_secrets) {
            if (_secrets.any { it.label == otpauth.label }) return@withContext false
        }

        val pair = encryptString(otpauth.secret)
        if (pair.encodedText.isEmpty() || pair.iv.isEmpty()) return@withContext false

        val entity = OTPauthEntity(
            label = otpauth.label,
            issuer = otpauth.issuer,
            encryptedSecret = pair,
            period = otpauth.period,
            hash = otpauth.hash,
            digits = otpauth.digits,
        )

        try {
            val newId = dao.insert(entity)

            if (newId > 0) {
                val newSecret = otpauth.copy(id = newId)

                if (_secrets.none { it.label == otpauth.label }) {
                    _secrets.add(newSecret)
                    return@withContext true
                } else {
                    dao.deleteById(newId)
                }
            }

        } catch (e: Exception) {
            LogSmart.e("OTPauthDataRepo", "Ошибка при добавлении в БД", e)
        }

        false
    }


    @Suppress("unused")
    suspend fun editSecret(otpauth: OTPauth): Boolean = withContext(Dispatchers.IO) {
        // Функция изменяет секрет в DataRepository и в БД.
        // Возвращает true, если успешно и false, если нет.

        synchronized(_secrets) {
            val exists = _secrets.any { it.id == otpauth.id }
            if (!exists) return@withContext false

            if (_secrets.any { it.label == otpauth.label && it.id != otpauth.id }) {
                return@withContext false
            }
        }

        val pair = encryptString(otpauth.secret)
        if (pair.encodedText.isEmpty()) return@withContext false

        val entity = OTPauthEntity(
            id = otpauth.id,
            label = otpauth.label,
            issuer = otpauth.issuer,
            encryptedSecret = pair,
            period = otpauth.period,
            hash = otpauth.hash,
            digits = otpauth.digits,
        )

        return@withContext try {
            val rowsAffected = dao.update(entity)

            if (rowsAffected == 1) {
                synchronized(_secrets) {
                    val index = _secrets.indexOfFirst { it.id == otpauth.id }
                    if (index != -1) {
                        _secrets[index] = otpauth
                        true
                    } else false
                }
            } else false

        } catch (e: Exception) {
            LogSmart.e("OTPauthDataRepo", "Ошибка при обновлении в БД: ${otpauth.label}", e)
            false
        }
    }



    suspend fun deleteSecret(id: Long): Boolean = withContext(Dispatchers.IO) {
        // Функция удаляет секрет из DataRepository и из БД.
        // Возвращает true, если успешно и false, если нет.

        val secretToDelete = synchronized(_secrets) {
            _secrets.find { it.id == id }
        } ?: return@withContext false

        try {
            val rowsDeleted = dao.deleteById(id)

            if (rowsDeleted == 1) {
                synchronized(_secrets) {
                    _secrets.remove(secretToDelete)
                }
                return@withContext true
            }

        } catch (e: Exception) {
            LogSmart.e("OTPauthDataRepo", "Ошибка при удалении ID: $id", e)
        }

        false
    }


    @Suppress("unused")
    fun clearDatabaseError()  = synchronized(this) {
        databaseError = null
    }

}