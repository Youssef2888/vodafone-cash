package com.example.network

import android.util.Base64
import com.example.model.AccountInfo
import com.example.model.ERROR_MAP
import com.example.model.ServerHealth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class ServerClient(var baseUrl: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    suspend fun checkHealth(): ServerHealth = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trim().removeSuffix("/")
        val t0 = System.currentTimeMillis()
        try {
            val req = Request.Builder()
                .url("$cleanUrl/api/health")
                .get()
                .build()
            val resp = client.newCall(req).execute()
            val latency = System.currentTimeMillis() - t0
            val success = resp.isSuccessful
            val error = if (!success) "HTTP ${resp.code}" else null
            ServerHealth(online = success, latencyMs = latency, error = error)
        } catch (e: Exception) {
            ServerHealth(online = false, latencyMs = null, error = e.message ?: "Connection error")
        }
    }

    suspend fun isApproved(msisdn: String): Boolean = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trim().removeSuffix("/")
        try {
            val req = Request.Builder()
                .url("$cleanUrl/api/is_approved/$msisdn")
                .get()
                .build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: ""
                val json = JSONObject(body)
                return@withContext json.optBoolean("approved", false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        false
    }

    suspend fun requestApproval(msisdn: String, extra: Map<String, String>): Boolean = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trim().removeSuffix("/")
        try {
            val json = JSONObject().apply {
                put("msisdn", msisdn)
                val extraObj = JSONObject()
                extra.forEach { (k, v) -> extraObj.put(k, v) }
                put("extra", extraObj)
            }
            val req = Request.Builder()
                .url("$cleanUrl/api/request_approval")
                .post(json.toString().toRequestBody(jsonMedia))
                .build()
            val resp = client.newCall(req).execute()
            resp.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkPending(msisdn: String): String = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trim().removeSuffix("/")
        try {
            val req = Request.Builder()
                .url("$cleanUrl/api/check_pending/$msisdn")
                .get()
                .build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: ""
                val json = JSONObject(body)
                return@withContext json.optString("status", "none")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        "error"
    }

    suspend fun postReport(payload: JSONObject): JSONObject? = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trim().removeSuffix("/")
        try {
            val req = Request.Builder()
                .url("$cleanUrl/api/report")
                .post(payload.toString().toRequestBody(jsonMedia))
                .build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: "{}"
                return@withContext JSONObject(body)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    suspend fun updateOperationStatus(
        msisdn: String,
        opId: String,
        status: String,
        httpCode: Int? = null,
        errorCode: String? = null,
        errorMessage: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanUrl = baseUrl.trim().removeSuffix("/")
        try {
            val json = JSONObject().apply {
                put("msisdn", msisdn)
                put("op_id", opId)
                put("status", status)
                httpCode?.let { put("http_code", it) }
                errorCode?.let { put("error_code", it) }
                errorMessage?.let { put("error_message", it) }
            }
            val req = Request.Builder()
                .url("$cleanUrl/api/op_status")
                .post(json.toString().toRequestBody(jsonMedia))
                .build()
            val resp = client.newCall(req).execute()
            resp.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun pollReply(msisdn: String, opId: String, onReply: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            val cleanUrl = baseUrl.trim().removeSuffix("/")
            val deadline = System.currentTimeMillis() + 600_000 // 10 minutes
            while (System.currentTimeMillis() < deadline) {
                try {
                    val req = Request.Builder()
                        .url("$cleanUrl/api/op/$msisdn/$opId")
                        .get()
                        .build()
                    val resp = client.newCall(req).execute()
                    if (resp.isSuccessful) {
                        val body = resp.body?.string() ?: ""
                        val json = JSONObject(body)
                        if (json.optString("reply_status") == "replied" && json.has("reply")) {
                            val reply = json.getString("reply")
                            withContext(Dispatchers.Main) {
                                onReply(reply)
                            }
                            return@withContext
                        }
                    }
                } catch (e: Exception) {
                    // continue polling
                }
                delay(6000)
            }
        }
    }
}
