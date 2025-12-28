package ru.mammoth70.totpgenerator

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dev.robinohs.totpkt.otp.HashAlgorithm
import dev.robinohs.totpkt.otp.totp.TotpGenerator
import dev.robinohs.totpkt.otp.totp.timesupport.generateCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.yield
import java.util.Date
import java.time.Duration
import ru.mammoth70.totpgenerator.App.Companion.appSecrets
import java.time.LocalDateTime

class TokensViewModel: ViewModel() {
    // Класс в бесконечном цикле вычисляет токены и выдаёт их в поток.
    // Можно заставить поток перечитать список OTPauth.

    private var updated = false
    private val secrets = ArrayList<OTPauth>(ArrayList()) // Считывается с глобального списка OTPauth.
    private val totpGenerators: ArrayList<TotpGenerator> = ArrayList() // Список totpGenerators для генерации токенов.

    private val flow: Flow<Token> = flow {
        var sec1 = LocalDateTime.now().second
        while (true) {
            yield()
            if (! updated) {
                update()
            }
            val sec = LocalDateTime.now().second
            if (sec1 != sec) {
                sec1 = sec
                secrets.forEach {
                    val remain = it.period - (sec % it.period)
                    var progress = (it.period - remain) * 100 / it.period
                    emit(
                        Token(
                            it.num, secrets[it.num].id, it.label, it.issuer, remain, progress,
                            totpGenerators[it.num].generateCode(it.secret.toByteArray(), Date())
                        )
                    )
                }
            }
        }
    }.flowOn(Dispatchers.Default) // Весь цикл работает в дефолтовом потоке

    val liveData: LiveData<Token> = flow.asLiveData()

    private fun update() {
        // Функция перечитывает глобальный список OTPauth.
        // После чего пересоздаёт список totpGenerators.
        secrets.clear()
        totpGenerators.clear()
        secrets.addAll(appSecrets)
        secrets.forEach {
            val algorithm = when (it.hash) {
                SHA256 -> HashAlgorithm.SHA256
                SHA512 -> HashAlgorithm.SHA512
                else  -> HashAlgorithm.SHA1
            }
            totpGenerators.add(TotpGenerator(
                algorithm = algorithm,
                codeLength = it.digits,
                timePeriod = Duration.ofSeconds(it.period.toLong())))
        }
        updated = true
    }

    fun sendCommandUpdate() {
        updated = false
    }

}