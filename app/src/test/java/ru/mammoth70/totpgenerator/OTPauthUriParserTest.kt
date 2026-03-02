package ru.mammoth70.totpgenerator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.provider.CsvFileSource
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

class OTPauthTest {

    @ParameterizedTest(name = "{index} => URI: {0}")
    @DisplayName("Тестирование парсера схемы otpauth://")
    @CsvFileSource(resources = ["/otp_data.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `parseOTPauth validation test`(
        uri: String,
        isValid: Boolean,
        expectedPeriod: Int,
        expectedHash: String,
        expectedDigits: Int,
        expectedLabel:  String,
        expectedIssuer: String,
        expectedSecret: String,
    ) {
        val result = parseOTPauth(uri)

        if (isValid) {
            // Сначала проверяем, что объект вообще создался.
            assertNotNull(result, "Объект не должен быть null для URI $uri")

            // Теперь проверяем поля.
            checkNotNull(result)
            assertAll(
                "Проверка полей OTP для URI $uri",
                { assertEquals(expectedPeriod, result.period, "Неверное поле period") },
                { assertEquals(expectedHash, result.hash, "Неверный поле hash") },
                { assertEquals(expectedDigits, result.digits, "Неверное поле digits") },
                { assertEquals(expectedLabel, result.label, "Неверное поле label") },
                { assertEquals(expectedIssuer, result.issuer, "Неверное поле issuer") },
                { assertEquals(expectedSecret, result.secret, "Неверный поле secret") },
            )
        } else {
            assertNull(result, "Ожидался null для невалидного URI $uri")
        }
    }



    @ParameterizedTest(name = "{index} => Label: {1}, Issuer: {2}")
    @DisplayName("генерация -> парсинг схемы otpauth//")
    @CsvFileSource(resources = ["/otp_data2.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `integration test to OTPauth from OTPauth`(
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
        val uri = generateOtpauthUri(original)

        // Декодируем.
        val restored = parseOTPauth(uri)

        // Сначала проверяем, что объект вообще создался.
        assertNotNull(restored, "Объект не должен быть null для варианта {$original.id}")

        // Теперь проверяем поля.
        checkNotNull(restored)
        assertAll(
            "Проверка полей OTPauth для варианта {$original.id}",
            { assertEquals(EMPTY_OTP, restored.id, "Неправильный id") },
            { assertEquals(original.label, restored.label, "Не совпадает label") },
            { assertEquals(original.issuer, restored.issuer, "Не совпадает issuer") },
            { assertEquals(original.secret, restored.secret, "Не совпадает secret") },
            { assertEquals(original.period, restored.period, "Не совпадает period") },
            { assertEquals(original.hash, restored.hash, "Не совпадает hash") },
            { assertEquals(original.digits, restored.digits, "Не совпадает digits") },
        )
    }

}



class GoogleMigrationTest {

    @ParameterizedTest(name = "{index} => {0}")
    @DisplayName("Тестирование парсера схемы otpauth-migration://")
    @CsvFileSource(resources = ["/migration_data.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `parseGoogleMigration validation test`(
        description: String,
        uri: String,
        expectedCount: Int,
        expectedLabel: String?,
        expectedSecret: String?
    ) {
        val result = parseGoogleMigration(uri)

        assertEquals(expectedCount, result.size, "Неверное количество аккаунтов для варианта $description")

        if (expectedCount > 0) {
            val firstAccount = result[0]
            assertAll(
                "Проверка полей первого аккаунта для варианта $description ",
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
        originalUri: String,
        expectedCount: Int,
    ) {

        val parsedAuths = parseGoogleMigration(originalUri)
        if (expectedCount == 0 || parsedAuths.isEmpty()) return // пропускаем невалидные кейсы из CSV

        val generatedUri = generateMigrationUri(parsedAuths)

        val finalAuths = parseGoogleMigration(generatedUri)

        assertEquals(parsedAuths.size, finalAuths.size, "Количество аккаунтов изменилось после перекодирования")

        for (i in parsedAuths.indices) {
            val original = parsedAuths[i]
            val final = finalAuths[i]

            assertAll(
                "Элемент $description",
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

        val migrationUri = generateMigrationUri(listOf(originalAuth))

        val parsedList = parseGoogleMigration(migrationUri)

        assertEquals(1, parsedList.size, "Список после парсинга должен содержать 1 элемент ($description)")

        val result = parsedList[0]
        assertAll(
            "Сверка полей для: $description",
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
            val csvFile = File("src/test/resources/migration_data3.csv")
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
    @DisplayName("Интеграционный тест: генерация -> парсинг схемы otpauth-migration:// 10 аккаунтов в одном URI")
    fun `migration round-trip 3 integration test`(expectedList: List<OTPauth>) {

        val migrationUri = generateMigrationUri(expectedList)

        val actualList = parseGoogleMigration(migrationUri)

        assertEquals(expectedList.size, actualList.size, "Количество аккаунтов не совпадает")

        expectedList.indices.forEach { i ->
            val expected = expectedList[i]
            val actual = actualList[i]

            assertAll(
                "Проверка аккаунта #$i (${expected.label})",
                { assertEquals(expected.label, actual.label, "Label не совпадает") },
                { assertEquals(expected.issuer, actual.issuer, "Issuer не совпадает") },
                { assertEquals(expected.secret, actual.secret, "Secret не совпадает") },
                { assertEquals(expected.hash, actual.hash, "Hash не совпадает") },
                { assertEquals(expected.digits, actual.digits, "Digits не совпадают") },
            )
        }
    }

}