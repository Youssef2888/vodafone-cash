package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.OperationHistoryItem
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vf_cash_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_SERVER_URL = "https://f3bef7d06eb43c.lhr.life"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_THEME_DARK = "theme_dark"
        private const val KEY_LAST_MSISDN = "last_msisdn"
    }

    var serverUrl: String
        get() = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
        set(value) {
            val sanitized = value.trim().removeSuffix("/")
            prefs.edit().putString(KEY_SERVER_URL, sanitized).apply()
        }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_THEME_DARK, true)
        set(value) = prefs.edit().putBoolean(KEY_THEME_DARK, value).apply()

    var lastMsisdn: String?
        get() = prefs.getString(KEY_LAST_MSISDN, null)
        set(value) = prefs.edit().putString(KEY_LAST_MSISDN, value).apply()

    private fun hashMsisdn(msisdn: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(msisdn.trim().toByteArray())
            bytes.joinToString("") { "%02x".format(it) }.take(24)
        } catch (e: Exception) {
            msisdn.filter { it.isLetterOrDigit() }
        }
    }

    fun getHistory(msisdn: String): List<OperationHistoryItem> {
        val key = "hist_" + hashMsisdn(msisdn)
        val jsonStr = prefs.getString(key, null) ?: return emptyList()
        val list = mutableListOf<OperationHistoryItem>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    OperationHistoryItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        timestamp = obj.optString("timestamp", ""),
                        title = obj.optString("title", ""),
                        subtitle = obj.optString("subtitle", ""),
                        success = obj.optBoolean("success", true),
                        opType = obj.optString("opType", null),
                        amount = obj.optString("amount", null),
                        recipient = obj.optString("recipient", null),
                        errorCode = obj.optString("errorCode", null),
                        errorMessage = obj.optString("errorMessage", null)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun addHistoryItem(
        msisdn: String,
        title: String,
        subtitle: String,
        success: Boolean,
        opType: String? = null,
        amount: String? = null,
        recipient: String? = null,
        errorCode: String? = null,
        errorMessage: String? = null
    ): OperationHistoryItem {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val item = OperationHistoryItem(
            id = UUID.randomUUID().toString(),
            timestamp = now,
            title = title,
            subtitle = subtitle,
            success = success,
            opType = opType,
            amount = amount,
            recipient = recipient,
            errorCode = errorCode,
            errorMessage = errorMessage
        )
        val current = getHistory(msisdn).toMutableList()
        current.add(0, item)
        if (current.size > 200) {
            current.subList(200, current.size).clear()
        }
        saveHistory(msisdn, current)
        return item
    }

    fun clearHistory(msisdn: String) {
        val key = "hist_" + hashMsisdn(msisdn)
        prefs.edit().remove(key).apply()
    }

    private fun saveHistory(msisdn: String, list: List<OperationHistoryItem>) {
        val key = "hist_" + hashMsisdn(msisdn)
        val arr = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("timestamp", item.timestamp)
            obj.put("title", item.title)
            obj.put("subtitle", item.subtitle)
            obj.put("success", item.success)
            item.opType?.let { obj.put("opType", it) }
            item.amount?.let { obj.put("amount", it) }
            item.recipient?.let { obj.put("recipient", it) }
            item.errorCode?.let { obj.put("errorCode", it) }
            item.errorMessage?.let { obj.put("errorMessage", it) }
            arr.put(obj)
        }
        prefs.edit().putString(key, arr.toString()).apply()
    }
}
