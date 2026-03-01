package ru.mammoth70.totpgenerator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.provider.CsvFileSource
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class OTPauthTest {

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
                { assertEquals(expectedSecret, result.secret, "Неверный поле secret") },
            )
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
                { assertEquals(expectedSecret, firstAccount.secret, "Неверное поле secret") },
            )
        }
    }


    @ParameterizedTest(name = "{index} => {0}")
    @DisplayName("Интеграционный тест: парсинг -> генерация -> парсинг схемы otpauth-migration://")
    @CsvFileSource(resources = ["/migration_data.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `migration round-trip 1 integration test`(
        description: String,
        originalUrl: String,
        expectedCount: Int,
    ) {

        val parsedAuths = parseGoogleMigration(originalUrl)
        if (expectedCount == 0 || parsedAuths.isEmpty()) return // пропускаем невалидные кейсы из CSV

        val generatedUrl = generateMigrationUrl(parsedAuths)

        val finalAuths = parseGoogleMigration(generatedUrl)

        assertEquals(parsedAuths.size, finalAuths.size, "Количество аккаунтов изменилось после перекодирования")

        for (i in parsedAuths.indices) {
            val original = parsedAuths[i]
            val final = finalAuths[i]

            assertAll("Элемент $description",
                { assertEquals(original.label, final.label, "Label не совпадает") },
                { assertEquals(original.secret, final.secret, "Secret не совпадает") },
                { assertEquals(original.issuer, final.issuer, "Issuer не совпадает") },
                { assertEquals(original.digits, final.digits, "Digits не совпадают") },
            )
        }
    }


    @ParameterizedTest(name = "{index} => {0}")
    @DisplayName("Интеграционный тест: генерация -> парсинг схемы otpauth-migration://")
    @CsvFileSource(resources = ["/migration_data2.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `migration round-trip 2 integration test`(
        description: String,
        label: String,
        issuer: String,
        secret: String,
        hash: String,
        digits: Int
    ) {

        val originalAuth = OTPauth(
            label = label,
            issuer = issuer,
            secret = secret,
            hash = hash,
            digits = digits
        )

        val migrationUrl = generateMigrationUrl(listOf(originalAuth))

        val parsedList = parseGoogleMigration(migrationUrl)

        assertEquals(1, parsedList.size, "Список после парсинга должен содержать 1 элемент ($description)")

        val result = parsedList[0]
        assertAll("Сверка полей для: $description",
            { assertEquals(label, result.label, "Label не совпадает") },
            { assertEquals(issuer, result.issuer, "Issuer не совпадает") },
            { assertEquals(secret, result.secret, "Secret не совпадает") },
            { assertEquals(hash, result.hash, "Hash не совпадает") },
            { assertEquals(digits, result.digits, "Digits не совпадают") },
        )
    }


    companion object {
        @JvmStatic
        fun provideOtpAuthList(): List<List<OTPauth>> {
            val csvFile = File("src/test/resources/migration_data2.csv")
            val auths = csvFile.readLines().drop(1).map { line ->
                val parts = line.split(";").map { it.trim() }
                OTPauth(
                    label = parts[1],
                    issuer = parts[2],
                    secret = parts[3],
                    hash = parts[4],
                    digits = parts[5].toInt()
                )
            }
            return listOf(auths)
        }
    }


    @ParameterizedTest
    @MethodSource("provideOtpAuthList")
    @DisplayName("Интеграционный тест: генерация -> парсинг схемы otpauth-migration:// 10 аккаунтов в одном URL")
    fun `migration round-trip 3 integration test`(expectedList: List<OTPauth>) {

        val migrationUrl = generateMigrationUrl(expectedList)

        val actualList = parseGoogleMigration(migrationUrl)

        assertEquals(expectedList.size, actualList.size, "Количество аккаунтов не совпадает")

        expectedList.indices.forEach { i ->
            val expected = expectedList[i]
            val actual = actualList[i]

            assertAll("Проверка аккаунта #$i (${expected.label})",
                { assertEquals(expected.label, actual.label, "Label не совпадает") },
                { assertEquals(expected.issuer, actual.issuer, "Issuer не совпадает") },
                { assertEquals(expected.secret, actual.secret, "Secret не совпадает") },
                { assertEquals(expected.hash, actual.hash, "Hash не совпадает") },
                { assertEquals(expected.digits, actual.digits, "Digits не совпадают") },
            )
        }
    }

}