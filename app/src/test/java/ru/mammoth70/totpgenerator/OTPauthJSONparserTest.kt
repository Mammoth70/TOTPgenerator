package ru.mammoth70.totpgenerator

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource

class OTPauthJSONparserTest {

    @ParameterizedTest(name = "{index} => Label: {1}, Issuer: {2}")
    @DisplayName("Интеграционный тест сериализации JSON")
    @CsvFileSource(resources = ["/otp_data2.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `integration test to json from json`(
        id: Long,
        label: String,
        issuer: String?, // Может быть пустым в CSV.
        secret: String,
        period: Int,
        hash: String,
        digits: Int
    ) {

        // Создаем исходный объект из данных CSV.
        val original = OTPauth(
            id = id,
            label = label,
            issuer = issuer ?: "", // Обрабатываем "null" из CSV.
            secret = secret,
            period = period,
            hash = hash,
            digits = digits
        )

        // Кодируем помаленьку.
        val originalList = listOf(original)
        val jsonString = secretsToJson(originalList)

        // Декодируем.
        val restoredList = secretsFromJson(jsonString)
        val restored = restoredList[0]

        // Сначала проверяем, что объект вообще создался.
        assertNotNull(restored, "Объект не должен быть null для варианта {$original.id}")

        // Теперь проверяем поля.
        assertAll(
            "Проверка полей OTPauth для варианта {$original.id}",
            {assertEquals(EMPTY_OTP, restored.id, "Неправильный id")},
            {assertEquals(original.label, restored.label, "Не совпадает label")},
            {assertEquals(original.issuer, restored.issuer, "Не совпадает issuer")},
            {assertEquals(original.secret, restored.secret, "Не совпадает secret")},
            {assertEquals(original.period, restored.period, "Не совпадает period")},
            {assertEquals(original.hash, restored.hash, "Не совпадает hash")},
            {assertEquals(original.digits, restored.digits, "Не совпадает digits")},)
    }

}