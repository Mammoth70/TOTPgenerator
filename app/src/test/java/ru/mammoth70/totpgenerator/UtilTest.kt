package ru.mammoth70.totpgenerator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
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
