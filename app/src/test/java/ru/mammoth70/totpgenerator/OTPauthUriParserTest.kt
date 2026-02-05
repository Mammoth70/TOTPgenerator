package ru.mammoth70.totpgenerator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.provider.CsvFileSource

class Base32Test {
    @ParameterizedTest(name = "[{index}] Проверка: {0}")
    @DisplayName("Проверка валидатора Base32")
    @CsvFileSource(resources = ["/base32_data.csv"], numLinesToSkip = 1, delimiter = ';')

    fun `isValidBase32 validation test`(input: String?, expected: Boolean, comment: String?) {

        val actualInput = input ?: "" // Используем элвис-оператор для обработки пустых строк из CSV как "".
        assertEquals(
            expected,
            isValidBase32(actualInput),
            "Ошибка: $comment (вход: '$actualInput')"
        )
    }
}

class OTPParserTest {
    @ParameterizedTest(name = "{index} => URL: {0}")
    @DisplayName("Проверка парсера схемы otpauth://")
    @CsvFileSource(resources = ["/otp_data.csv"], numLinesToSkip = 1, delimiter = ';')

    fun `parseOTPauth validation test`(
        url: String,
        isValid: Boolean,
        period: Int,
        hash: String,
        digits: Int,
        label:  String,
        issuer: String,
        secret: String,
    ) {
        val result = parseOTPauth(url)

        if (!isValid) {
            assertNull(result, "Ожидался null для невалидного URL: $url")
        } else {
            // Сначала проверяем, что объект вообще создался
            assertNotNull(result, "Объект не должен быть null для: $url")

            // Теперь проверяем поля
            checkNotNull(result)
            assertAll(
                "Проверка полей OTP для URL: $url",
                { assertEquals(period, result.period, "Неверный period для URL: $url") },
                { assertEquals(hash, result.hash, "Неверный алгоритм (hash) для URL: $url") },
                { assertEquals(digits, result.digits, "Неверное количество цифр (digits) для URL: $url") },
                { assertEquals(label, result.label, "Ошибка в поле label для URL: $url") },
                { assertEquals(issuer, result.issuer, "Ошибка в поле issuer для URL: $url") },
                { assertEquals(secret, result.secret, "Ошибка в секретном ключе для URL: $url") })
        }
    }
}

class GoogleMigrationTest {
    @ParameterizedTest(name = "{index} => {0}")
    @DisplayName("Проверка парсера схемы otpauth-migration://")
    @CsvFileSource(resources = ["/migration_data.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `test google migration parsing`(
        description: String,
        url: String,
        expectedCount: Int,
        expectedLabel: String?,
        expectedSecret: String?
    ) {
        val result = parseGoogleMigration(url)

        assertEquals(expectedCount, result.size, "Неверное количество аккаунтов: $description")

        if (expectedCount > 0) {
            val firstAccount = result[0]
            assertAll("Проверка полей первого аккаунта ($description)",
                { assertEquals(expectedLabel, firstAccount.label, "Label mismatch") },
                { assertEquals(expectedSecret, firstAccount.secret, "Secret mismatch") }
            )
        }
    }
}