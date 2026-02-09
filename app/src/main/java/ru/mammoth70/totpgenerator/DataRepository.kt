package ru.mammoth70.totpgenerator

import java.util.concurrent.CopyOnWriteArrayList

object DataRepository {
    // Основной список объектов (единственный источник истины).

    val secrets: List<OTPauth> get() = synchronized(_secrets) { _secrets.toList() }

    private val _secrets= CopyOnWriteArrayList<OTPauth>()  // Список OTPauth, считывается из БД.


    fun readAllSecrets() {
        // Функция считывает все секреты из в БД в DataRepository.

        val fromDb = DBhelper.dbHelper.readAllDbSecrets()
        if (fromDb.isEmpty() && _secrets.isEmpty()) return
        synchronized(_secrets) {
            _secrets.clear()
            _secrets.addAll(fromDb)
        }
    }


    fun addSecret(otpauth: OTPauth): Boolean {
        // Функция добавляет секрет в DataRepository и в БД.
        // Возвращает true, если успешно и false, если нет.

        synchronized(_secrets) {
            if (_secrets.any { it.label == otpauth.label }) return false
        }

        val newId = DBhelper.dbHelper.addDbSecret(otpauth)

    	if (newId > 0) {
            val newSecret = otpauth.copy(id = newId)
	        synchronized(_secrets) {
                if (_secrets.any { it.label == otpauth.label }) {
                    DBhelper.dbHelper.deleteDbSecret(newId) // Откатываем БД
                    return false
                }
                _secrets.add(newSecret)
                return true
            }
        }
        return false
    }

    @Suppress("unused")
    fun editSecret(otpauth: OTPauth): Boolean {
        // Функция изменяет секрет в DataRepository и в БД.
        // Возвращает true, если успешно и false, если нет.

        synchronized(_secrets) {
            val exists = _secrets.any { it.id == otpauth.id }
            if (!exists) return false

            if (_secrets.any { it.label == otpauth.label && it.id != otpauth.id }) {
                return false
            }
        }

        val result = DBhelper.dbHelper.editDbSecret(otpauth)

        if (result == 1L) {
            synchronized(_secrets) {
                val index = _secrets.indexOfFirst { it.id == otpauth.id }
                if (index != -1) {
                    _secrets[index] = otpauth
                    return true
                }
            }
        }

        return false
    }

    fun deleteSecret(id: Long): Boolean {
        // Функция удаляет секрет из DataRepository и из БД.
        // Возвращает true, если успешно и false, если нет.

        val secretToDelete = synchronized(_secrets) {
            _secrets.find { it.id == id }
        } ?: return false

        val isDeletedFromDb = DBhelper.dbHelper.deleteDbSecret(id) == 1L

        if (isDeletedFromDb) {
            synchronized(_secrets) {
                _secrets.remove(secretToDelete)
                return true
            }
        }

        return false
    }

}