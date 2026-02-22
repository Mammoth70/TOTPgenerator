package ru.mammoth70.totpgenerator

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TokensTrigger {
    // Глобальный триггер обновления для TokensViewModel.

    private val _update = MutableStateFlow(System.currentTimeMillis())
    val update: StateFlow<Long> = _update.asStateFlow()

    fun sendCommandUpdate() {
        // Эта функция должна вызываться при добавлении, изменении или удалении секрета.
        _update.value = System.currentTimeMillis()
    }
}