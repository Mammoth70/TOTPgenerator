package ru.mammoth70.totpgenerator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource

class Base32Test {

    @ParameterizedTest(name = "{index} => {2}")
    @DisplayName("Тестирование валидатора Base32")
    @CsvFileSource(resources = ["/base32_data.csv"], numLinesToSkip = 1, delimiter = ';')
    fun `isValidBase32 validation test`(
        input: String?,
        isValid: Boolean,
        description: String?,
    ) {

        // Обрабатываем пустые строки из CSV.
        val actualInput = input ?: ""

        assertEquals(
            isValid,
            isValidBase32(actualInput),
            "Ошибка в варианте $description (строка: $actualInput)"
        )
    }
}