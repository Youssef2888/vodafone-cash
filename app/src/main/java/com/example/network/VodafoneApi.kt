package com.example.network

import android.util.Base64
import com.example.model.AccountInfo
import com.example.model.ERROR_MAP
import com.example.model.OperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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

object VodafoneApi {
    private const val SESSION_DIGITAL_ID = "25VT5Q5QUDYYV0"
    private const val DEVICE_ID = "sa"
    private const val USER_AGENT = "okhttp/4.12.0"
    private const val CLIENT_ID_HEADER = "AnaVodafoneAndroid"
    private const val AGENT_DEVICE = "Samsung"
    private const val AGENT_VERSION = "2026.4.1"
    private const val AGENT_BUILD = "1139"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=UTF-8".toMediaType()

    fun buildBaseHeaders(token: String?, msisdn: String?): Map<String, String> {
        val map = mutableMapOf(
            "User-Agent" to USER_AGENT,
            "Accept" to "application/json",
            "Accept-Encoding" to "gzip",
            "X-Request-ID" to UUID.randomUUID().toString(),
            "device-id" to DEVICE_ID,
            "Content-Type" to "application/json; charset=UTF-8",
            "api-version" to "v2",
            "Accept-Language" to "ar",
            "x-agent-operatingsystem" to "13",
            "x-agent-device" to AGENT_DEVICE,
            "x-agent-version" to AGENT_VERSION,
            "x-agent-build" to AGENT_BUILD,
            "digitalId" to SESSION_DIGITAL_ID,
            "clientId" to CLIENT_ID_HEADER
        )
        if (!msisdn.isNullOrBlank()) {
            map["msisdn"] = msisdn
        }
        if (!token.isNullOrBlank()) {
            map["Authorization"] = "Bearer $token"
        }
        return map
    }

    fun base36Encode(num: Long): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        if (num == 0L) return "0"
        var n = num
        var r = ""
        while (n > 0) {
            r = chars[(n % 36).toInt()] + r
            n /= 36
        }
        return r
    }

    fun generateSuffix(jti: String?): String {
        if (jti.isNullOrBlank()) return "0001"
        return try {
            val u = if (jti.contains(":")) jti.substringAfterLast(":") else jti
            val h = u.replace("-", "").take(8)
            val num = h.toLong(16)
            val encoded = base36Encode(num)
            encoded.takeLast(4).uppercase(Locale.US).padStart(4, '0')
        } catch (e: Exception) {
            "0001"
        }
    }

    fun decodeJwt(token: String): Pair<JSONObject?, JSONObject?> {
        return try {
            val parts = token.trim().split(".")
            if (parts.size != 3) return Pair(null, null)
            val header = String(Base64.decode(parts[0], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
            Pair(JSONObject(header), JSONObject(payload))
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    fun extractAccountInfo(token: String): AccountInfo {
        val (_, payload) = decodeJwt(token) ?: return AccountInfo()
        val p = payload ?: return AccountInfo()
        val ui = p.optJSONObject("userInfo") ?: JSONObject()
        val first = ui.optString("firstName", "")
        val last = ui.optString("lastName", "")
        val fullName = "$first $last".trim()
        val jti = p.optString("jti", null)
        val loginCode = jti?.let { generateSuffix(it) }

        var iatStr: String? = null
        var expStr: String? = null
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            if (p.has("iat")) {
                iatStr = sdf.format(Date(p.getLong("iat") * 1000))
            }
            if (p.has("exp")) {
                expStr = sdf.format(Date(p.getLong("exp") * 1000))
            }
        } catch (e: Exception) {
            // ignore date format error
        }

        return AccountInfo(
            msisdn = ui.optString("msisdn", null),
            firstName = first.ifEmpty { null },
            lastName = last.ifEmpty { null },
            fullName = fullName.ifEmpty { null },
            customerId = ui.optString("customerID", null),
            sim = if (ui.has("SIM")) ui.optString("SIM") else ui.optString("sim", null),
            customerType = ui.optString("customerType", null),
            contractType = ui.optString("contractType", null),
            contractStatus = ui.optString("contractStatus", null),
            lineType = ui.optString("lineType", null),
            jti = jti,
            loginCode = loginCode,
            iatReadable = iatStr,
            expReadable = expStr
        )
    }

    suspend fun performSeamlessAuth(): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val url = "http://mobile.vodafone.com.eg/checkSeamless/realms/vf-realm/protocol/openid-connect/auth?client_id=cash-app"
            val reqBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept-Encoding", "gzip")
                .header("x-agent-operatingsystem", "13")
                .header("clientId", CLIENT_ID_HEADER)
                .header("Accept-Language", "ar")
                .header("x-agent-device", AGENT_DEVICE)
                .header("x-agent-version", AGENT_VERSION)
                .header("x-agent-build", AGENT_BUILD)
                .header("digitalId", SESSION_DIGITAL_ID)
                .header("device-id", DEVICE_ID)
                .get()

            val resp = client.newCall(reqBuilder.build()).execute()
            if (!resp.isSuccessful) {
                return@withContext Result.failure(Exception("Seamless failed HTTP ${resp.code}"))
            }
            val body = resp.body?.string() ?: ""
            val json = JSONObject(body)
            val seamless = json.getString("seamlessToken")
            val msisdn = json.getString("msisdn")
            Result.success(Pair(seamless, msisdn))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exchangeToken(seamlessToken: String, msisdnShort: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://mobile.vodafone.com.eg/auth/realms/vf-realm/protocol/openid-connect/token"
            val form = FormBody.Builder()
                .add("client_id", "cash-app")
                .add("client_secret", "b86e30a8-ae29-467a-a71f-65c73f2ff5e3")
                .add("grant_type", "password")
                .build()

            val reqBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Encoding", "gzip")
                .header("silentLogin", "true")
                .header("msisdn", msisdnShort)
                .header("seamlessToken", seamlessToken)
                .header("firstTimeLogin", "true")
                .header("x-agent-operatingsystem", "13")
                .header("clientId", CLIENT_ID_HEADER)
                .header("Accept-Language", "ar")
                .header("x-agent-device", AGENT_DEVICE)
                .header("x-agent-version", AGENT_VERSION)
                .header("x-agent-build", AGENT_BUILD)
                .header("digitalId", "")
                .header("device-id", DEVICE_ID)
                .post(form)

            val resp = client.newCall(reqBuilder.build()).execute()
            if (!resp.isSuccessful) {
                return@withContext Result.failure(Exception("Token exchange failed HTTP ${resp.code}"))
            }
            val body = resp.body?.string() ?: ""
            val json = JSONObject(body)
            val token = json.getString("access_token")
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyRecipient(localNumber: String, recipient: String, token: String): Result<String?> = withContext(Dispatchers.IO) {
        try {
            val url = "https://mobile.vodafone.com.eg/services/dxl/paymentmng/payment".toHttpUrlOrNull()!!
                .newBuilder()
                .addQueryParameter("payer.id", localNumber)
                .addQueryParameter("$.paymentMethod.relatedParty.id", recipient)
                .addQueryParameter("$.amount.value", "5")
                .addQueryParameter("$.account.type", "Vodafone Consumer")
                .addQueryParameter("@type", "CashMandate")
                .build()

            val reqBuilder = Request.Builder().url(url)
            buildBaseHeaders(token, localNumber).forEach { (k, v) -> reqBuilder.header(k, v) }
            reqBuilder.header("X-App-StackTrace", "")
            reqBuilder.header("X-Network-StackTrace", "Proceeding with request")

            val resp = client.newCall(reqBuilder.build()).execute()
            if (resp.isSuccessful) {
                val body = resp.body?.string() ?: ""
                val arr = JSONArray(body)
                if (arr.length() > 0) {
                    val rp = arr.getJSONObject(0).optJSONObject("paymentMethod")?.optJSONObject("relatedParty")
                    val name = rp?.optString("name", null)
                    return@withContext Result.success(name)
                }
            }
            Result.failure(Exception("Could not verify recipient name"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchWalletBalance(localNumber: String, pin: String, token: String): Result<Pair<String?, String?>> = withContext(Dispatchers.IO) {
        try {
            val url = "https://mobile.vodafone.com.eg/services/dxl/pm/paymentMethod/$localNumber".toHttpUrlOrNull()!!
                .newBuilder()
                .addQueryParameter("@type", "DigitalWallet")
                .addQueryParameter("@referredType", "CashBalance")
                .build()

            val reqBuilder = Request.Builder().url(url)
            buildBaseHeaders(token, localNumber).forEach { (k, v) -> reqBuilder.header(k, v) }
            reqBuilder.header("pinCode", pin)
            reqBuilder.header("X-App-StackTrace", "")
            reqBuilder.header("X-Network-StackTrace", "Proceeding with request")

            val resp = client.newCall(reqBuilder.build()).execute()
            val body = resp.body?.string() ?: ""
            if (resp.isSuccessful) {
                val json = JSONObject(body)
                var balance: String? = null
                val chars = json.optJSONArray("characteristics")
                if (chars != null) {
                    for (i in 0 until chars.length()) {
                        val ch = chars.getJSONObject(i)
                        if (ch.optString("name") == "balance") {
                            balance = ch.optString("value")
                            break
                        }
                    }
                }
                val desc = json.optString("description", "")
                return@withContext Result.success(Pair(balance, desc))
            } else {
                val parsed = parseError(body, resp.code)
                return@withContext Result.failure(Exception(parsed.message ?: "HTTP ${resp.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buyCard(
        localNumber: String,
        msisdnShort: String,
        receiver: String,
        productId: String,
        pin: String,
        token: String
    ): OperationResult = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("channel", JSONObject().put("name", "MobileApp"))
                val orderItemArr = JSONArray().apply {
                    put(JSONObject().apply {
                        put("action", "insert")
                        put("id", productId)
                        put("product", JSONObject().apply {
                            put("characteristic", JSONArray().apply {
                                put(JSONObject().put("name", "PaymentMethod").put("value", "VFCash"))
                                put(JSONObject().put("name", "USE_EMONEY").put("value", "True"))
                                put(JSONObject().put("name", "MerchantCode").put("value", ""))
                            })
                            put("id", productId)
                            put("relatedParty", JSONArray().apply {
                                put(JSONObject().put("id", msisdnShort).put("name", "MSISDN").put("role", "Subscriber"))
                                put(JSONObject().put("id", receiver).put("name", "Receiver").put("role", "Receiver"))
                            })
                        })
                        put("@type", productId)
                        put("eCode", 0)
                    })
                }
                put("orderItem", orderItemArr)
                put("relatedParty", JSONArray().apply {
                    put(JSONObject().put("id", pin).put("name", "pin").put("role", "Requestor"))
                })
                put("@type", "CashFakkaAndMared")
            }

            val reqBuilder = Request.Builder()
                .url("https://mobile.vodafone.com.eg/services/dxl/pom/productOrder")
            buildBaseHeaders(token, localNumber).forEach { (k, v) -> reqBuilder.header(k, v) }
            reqBuilder.header("api-host", "ProductOrderingManagement")
            reqBuilder.header("useCase", "CashFakkaAndMared")
            reqBuilder.post(payload.toString().toRequestBody(jsonMedia))

            val resp = client.newCall(reqBuilder.build()).execute()
            val body = resp.body?.string() ?: ""
            return@withContext interpretResponse(body, resp.code, "Buy Card")
        } catch (e: Exception) {
            OperationResult(
                success = false,
                title = "Purchase Failed",
                subtitle = "Network or server error",
                code = "-50500",
                message = e.message ?: "Connection failure"
            )
        }
    }

    suspend fun recharge(
        localNumber: String,
        receiver: String,
        amount: Double,
        pin: String,
        jti: String?,
        token: String
    ): OperationResult = withContext(Dispatchers.IO) {
        try {
            val dtx = SESSION_DIGITAL_ID.take(9) + generateSuffix(jti)
            val deducted = Math.round(amount * 1.428 * 100.0) / 100.0

            val payload = JSONObject().apply {
                put("payment", JSONArray().apply {
                    put(JSONObject().apply {
                        put("characteristics", JSONArray().apply {
                            put(JSONObject().put("name", "authorizationCode").put("value", pin))
                            put(JSONObject().put("name", "digitalTransactionId").put("value", dtx))
                        })
                        put("@type", "digitalWallet")
                    })
                })
                put("productOrderItem", JSONArray().apply {
                    put(JSONObject().apply {
                        put("characteristics", JSONArray().apply {
                            put(JSONObject().put("name", "MSISDN").put("@type", "receiver").put("value", receiver))
                            put(JSONObject().put("name", "MSISDN").put("@type", "sender").put("value", localNumber))
                        })
                        put("itemTotalPrice", JSONArray().apply {
                            put(JSONObject().apply {
                                put("price", JSONObject().apply {
                                    put("taxIncludedAmount", JSONObject().apply {
                                        put("unit", "EGP")
                                        put("value", deducted)
                                    })
                                })
                            })
                        })
                    })
                })
                put("@type", "paymentRecharge")
            }

            val reqBuilder = Request.Builder()
                .url("https://mobile.vodafone.com.eg/services/dxl/orderor/productOrder")
            buildBaseHeaders(token, localNumber).forEach { (k, v) -> reqBuilder.header(k, v) }
            reqBuilder.post(payload.toString().toRequestBody(jsonMedia))

            val resp = client.newCall(reqBuilder.build()).execute()
            val body = resp.body?.string() ?: ""
            return@withContext interpretResponse(body, resp.code, "Recharge")
        } catch (e: Exception) {
            OperationResult(
                success = false,
                title = "Recharge Failed",
                subtitle = "Network or server error",
                code = "-50500",
                message = e.message ?: "Connection failure"
            )
        }
    }

    private fun findCode(json: Any?): String? {
        val keys = listOf("code", "statusCode", "errorCode", "status")
        when (json) {
            is JSONObject -> {
                for (k in keys) {
                    if (json.has(k)) return json.optString(k)
                }
                val it = json.keys()
                while (it.hasNext()) {
                    val found = findCode(json.opt(it.next()))
                    if (found != null) return found
                }
            }
            is JSONArray -> {
                for (i in 0 until json.length()) {
                    val found = findCode(json.opt(i))
                    if (found != null) return found
                }
            }
        }
        return null
    }

    private fun findMsg(json: Any?): String? {
        val keys = listOf("message", "reason", "description", "userMessage")
        when (json) {
            is JSONObject -> {
                for (k in keys) {
                    if (json.has(k) && json.optString(k).isNotBlank()) return json.optString(k)
                }
                val it = json.keys()
                while (it.hasNext()) {
                    val found = findMsg(json.opt(it.next()))
                    if (found != null) return found
                }
            }
            is JSONArray -> {
                for (i in 0 until json.length()) {
                    val found = findMsg(json.opt(i))
                    if (found != null) return found
                }
            }
        }
        return null
    }

    fun parseError(body: String, httpCode: Int): OperationResult {
        return interpretResponse(body, httpCode, "Error")
    }

    private fun interpretResponse(body: String, httpCode: Int, opName: String): OperationResult {
        var code: String? = null
        var msg: String? = null
        try {
            if (body.startsWith("{")) {
                val obj = JSONObject(body)
                code = findCode(obj)
                msg = findMsg(obj)
            } else if (body.startsWith("[")) {
                val arr = JSONArray(body)
                code = findCode(arr)
                msg = findMsg(arr)
            }
        } catch (e: Exception) {
            // ignore JSON parse error
        }

        if (code.isNullOrBlank()) {
            code = httpCode.toString()
        }

        val friendly = ERROR_MAP[code]
        val successCodes = setOf("0000", "0", "200", "SUCCESS", "success")
        val isSuccess = (code in successCodes || httpCode in 200..201) && (code != "1056" && code != "2123" && code != "6051")

        val title = if (isSuccess) "Success!" else "$opName Failed"
        val subtitle = friendly ?: (msg ?: "Operation code: $code")

        return OperationResult(
            success = isSuccess,
            title = title,
            subtitle = subtitle,
            code = code,
            message = msg ?: friendly
        )
    }
}
