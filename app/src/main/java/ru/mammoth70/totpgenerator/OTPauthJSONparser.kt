package ru.mammoth70.totpgenerator

import org.json.JSONArray
import org.json.JSONObject

// Конверторы списка OTPauth в JSON и обратно.

fun secretsToJson(list: List<OTPauth>): String {
    // Функция конверирует список секретов в JSON.

    val jsonArray = JSONArray()
    for (item in list) {
        val jsonObject = JSONObject().apply {
            put("label", item.label)
            put("issuer", item.issuer)
            put("secret", item.secret)
            put("period", item.period)
            put("hash", item.hash)
            put("digits", item.digits)
        }
        jsonArray.put(jsonObject)
    }
    return jsonArray.toString(2)
}


fun secretsFromJson(jsonString: String): List<OTPauth> {
    // Функция конверирует JSON в список секретов.

    val list = mutableListOf<OTPauth>()
    val jsonArray = JSONArray(jsonString)

    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        list.add(
            OTPauth(
                id = EMPTY_OTP,
                label = obj.getString("label"),
                issuer = obj.optString("issuer", ""),
                secret = obj.getString("secret"),
                period = obj.optInt("period", DEFAULT_PERIOD),
                hash = obj.optString("hash", SHA1),
                digits = obj.optInt("digits", DEFAULT_DIGITS),
            )
        )
    }
    return list
}