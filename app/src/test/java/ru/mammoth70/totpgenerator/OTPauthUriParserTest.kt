package ru.mammoth70.totpgenerator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.provider.CsvFileSource

class OTPParserTest {

    @ParameterizedTest(name = "{index} => URL: {0}")
    @DisplayName("Тестирование парсера схемы otpauth://")
    @CsvFileSource(resources = ["/otp_data.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `parseOTPauth validation test`(
        url: String,
        isValid: Boolean,
        expectedPeriod: Int,
        expectedHash: String,
        expectedDigits: Int,
        expectedLabel:  String,
        expectedIssuer: String,
        expectedSecret: String,
    ) {
        val result = parseOTPauth(url)

        if (isValid) {
            // Сначала проверяем, что объект вообще создался.
            assertNotNull(result, "Объект не должен быть null для URL $url")

            // Теперь проверяем поля.
            checkNotNull(result)
            assertAll(
                "Проверка полей OTP для URL $url",
                { assertEquals(expectedPeriod, result.period, "Неверное поле period") },
                { assertEquals(expectedHash, result.hash, "Неверный поле hash") },
                { assertEquals(expectedDigits, result.digits, "Неверное поле digits") },
                { assertEquals(expectedLabel, result.label, "Неверное поле label") },
                { assertEquals(expectedIssuer, result.issuer, "Неверное поле issuer") },
                { assertEquals(expectedSecret, result.secret, "Неверный поле secret") })
        } else {
            assertNull(result, "Ожидался null для невалидного URL $url")
        }
    }
}

class GoogleMigrationTest {
    // Для прохождения 10-го варианта теста нужно временно закомментировать вызов LogSmart в функции parseGoogleMigration.
    @ParameterizedTest(name = "{index} => {0}")
    @DisplayName("Тестирование парсера схемы otpauth-migration://")
    @CsvFileSource(resources = ["/migration_data.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `parseGoogleMigration validation test`(
        description: String,
        url: String,
        expectedCount: Int,
        expectedLabel: String?,
        expectedSecret: String?
    ) {
        val result = parseGoogleMigration(url)

        assertEquals(expectedCount, result.size, "Неверное количество аккаунтов для варианта $description")

        if (expectedCount > 0) {
            val firstAccount = result[0]
            assertAll("Проверка полей первого аккаунта для варианта $description ",
                { assertEquals(expectedLabel, firstAccount.label, "Неверное поле label") },
                { assertEquals(expectedSecret, firstAccount.secret, "Неверное поле secret") }
            )
        }
    }
}