package ru.mammoth70.totpgenerator

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class) // Указываем, что это Android-тест
class PinSecurityInstrumentedTest {

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Поиск переменнной appContext.
        try {
            // Lateinit переменные из companion object компилируются в static поля основного класса.
            val field = App::class.java.getDeclaredField("appContext")
            field.isAccessible = true
            field.set(null, context) // null, так как поле статическое
        } catch (_: Exception) {
            // Если поле в основном классе не найдено, пробуем в Companion.
            val companionField = App.Companion::class.java.getDeclaredField("appContext")
            companionField.isAccessible = true
            companionField.set(App.Companion, context)
        }
    }

    @Test
    fun fullPinLifecycleTest() {
        val myPin = charArrayOf('1', '2', '3', '4', '5', '6')
        val wrongPin = charArrayOf('4', '3', '2', '1', '5', '6')

        // Устанавливаем новый PIN-код (хеширование с солью, шифрование и запись).
        setHashPin(myPin)

        // Проверяем, что система видит наличие PIN-кода в SharedPreferens.
        val settings = InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("hashpin", 0)
        assertTrue("Хеш должен быть сохранен в Prefs", settings.contains("pin"))

        // Проверка корректного PIN-кода (чтение, расшифровка, PBKDF2, сравнение).
        val isCorrectMatch = verifyPin(myPin)
        assertTrue("Верификация должна пройти успешно для верного PIN-кода", isCorrectMatch)

        // Проверка неверного ПИН-кода (чтение, расшифровка, PBKDF2, сравнение).
        val isWrongMatch = verifyPin(wrongPin)
        assertFalse("Верификация должна провалиться для неверного PIN-кода", isWrongMatch)
    }

    @Test
    fun deletePinTest() {
        val myPin = charArrayOf('1', '2', '3', '4', '5', '6')

        setHashPin(myPin)
        deleteHashPin()

        val result = verifyPin(myPin)
        assertFalse("Верификация должна провалиться для отсутствующего PIN-кода", result)
    }
}