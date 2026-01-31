package ru.mammoth70.totpgenerator

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dev.robinohs.totpkt.otp.HashAlgorithm
import dev.robinohs.totpkt.otp.totp.TotpGenerator
import dev.robinohs.totpkt.otp.totp.timesupport.generateCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import java.util.Date
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList

val appSecrets: CopyOnWriteArrayList<OTPauth> =
    CopyOnWriteArrayList() // Список OTPauth, считывается из БД

class TokensViewModel : ViewModel() {
    // Класс в бесконечном цикле каждую секунду вычисляет токены и выдаёт их список в поток.
    // Можно заставить перечитать список OTPauth и обновиться.

    // Триггер для обновления. Вызываем sendCommandUpdate(), когда appSecrets изменился.
    private val _updateTrigger = MutableStateFlow(System.currentTimeMillis())

    @OptIn(ExperimentalCoroutinesApi::class)
    val tokensLiveData: LiveData<List<Token>> = _updateTrigger.flatMapLatest {
        flow {
            // При каждом обновлении триггера считываем актуальный глобальный список секретов.
            val currentSecrets = appSecrets.toList()
            val generators = currentSecrets.map { createGenerator(it) }

            // Цикл генерации токенов для этого набора секретов.
            while (true) {
                val now = System.currentTimeMillis()
                val sec = (now / 1000).toInt()

                // Генерируем список токенов на текущую секунду.
                val tokenList = currentSecrets.mapIndexed { index, auth ->
                    val remain = auth.period - (sec % auth.period)
                    val progress = (auth.period - remain) * 100 / auth.period

                    Token(
                        id = auth.id,
                        label = auth.label,
                        issuer = auth.issuer,
                        remain = remain,
                        progress = progress,
                        totp = generators[index].generateCode(auth.secret.toByteArray(), Date(now))
                    )
                }

                emit(tokenList) // Эмитим весь список один раз в секунду.

                // Ждем начала следующей секунды (с высокой точностью).
                delay(1000L - (System.currentTimeMillis() % 1000L))
            }
        }
    }.flowOn(Dispatchers.Default).asLiveData()

    fun sendCommandUpdate() {
        // Эта функция должна вызываться при добавлении или удалении секрета в appSecrets.
        DBhelper.dbHelper.readAllSecrets()
        _updateTrigger.value = System.currentTimeMillis()
    }

    private fun createGenerator(it: OTPauth): TotpGenerator {
        // Функция создаёт генератор токенов по заданным параметрам.
        val algorithm = when (it.hash) {
            "SHA256" -> HashAlgorithm.SHA256
            "SHA512" -> HashAlgorithm.SHA512
            else -> HashAlgorithm.SHA1
        }
        return TotpGenerator(
            algorithm = algorithm,
            codeLength = it.digits,
            timePeriod = Duration.ofSeconds(it.period.toLong())
        )
    }
}
