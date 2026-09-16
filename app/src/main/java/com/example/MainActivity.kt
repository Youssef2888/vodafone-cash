package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.VodafoneCashTheme
import com.example.viewmodel.AppStep
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentStep by viewModel.currentStep.collectAsState()
            val serverHealth by viewModel.serverHealth.collectAsState()
            val serverUrlInput by viewModel.serverUrlInput.collectAsState()
            val isServerChecking by viewModel.isServerChecking.collectAsState()
            val loginStatus by viewModel.loginStatus.collectAsState()
            val isLoginBusy by viewModel.isLoginBusy.collectAsState()
            val approvalElapsedSec by viewModel.approvalElapsedSec.collectAsState()
            val localNumber by viewModel.localNumber.collectAsState()

            // Modals
            val step1Data by viewModel.step1Modal.collectAsState()
            val step2Data by viewModel.step2Modal.collectAsState()
            val resultData by viewModel.resultModal.collectAsState()
            val adminReplyData by viewModel.adminReplyModal.collectAsState()
            val isHistoryOpen by viewModel.isHistoryModalOpen.collectAsState()
            val isServerConfigOpen by viewModel.isServerConfigOpen.collectAsState()
            val recipientVerification by viewModel.recipientVerification.collectAsState()
            val historyList by viewModel.historyList.collectAsState()

            VodafoneCashTheme(darkTheme = isDarkMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentStep) {
                        is AppStep.ServerSetup -> {
                            ServerSetupScreen(
                                currentUrl = serverUrlInput,
                                serverHealth = serverHealth,
                                isChecking = isServerChecking,
                                isDarkMode = isDarkMode,
                                onUrlChange = { viewModel.setServerUrlInput(it) },
                                onTestConnection = { viewModel.checkServerHealth(it) },
                                onContinue = { viewModel.confirmServerUrlAndProceed() },
                                onToggleTheme = { viewModel.toggleDarkMode() }
                            )
                        }
                        is AppStep.Splash -> {
                            SplashScreen(serverHealth = serverHealth)
                        }
                        is AppStep.Login -> {
                            LoginScreen(
                                serverHealth = serverHealth,
                                loginStatus = loginStatus,
                                isBusy = isLoginBusy,
                                isDarkMode = isDarkMode,
                                isWaitingApproval = false,
                                approvalElapsedSec = 0,
                                onLoginClick = { manualNum -> viewModel.startLoginFlow(manualNum) },
                                onToggleTheme = { viewModel.toggleDarkMode() },
                                onOpenServerConfig = { viewModel.openServerConfig() }
                            )
                        }
                        is AppStep.ApprovalWait -> {
                            LoginScreen(
                                serverHealth = serverHealth,
                                loginStatus = loginStatus,
                                isBusy = true,
                                isDarkMode = isDarkMode,
                                isWaitingApproval = true,
                                approvalElapsedSec = approvalElapsedSec,
                                onLoginClick = {},
                                onToggleTheme = { viewModel.toggleDarkMode() },
                                onOpenServerConfig = { viewModel.openServerConfig() }
                            )
                        }
                        is AppStep.Main -> {
                            MainDashboardScreen(viewModel = viewModel)
                        }
                    }

                    // Dialogs
                    step1Data?.let { data ->
                        Step1RecipientDialog(
                            data = data,
                            localNumber = localNumber,
                            verificationStatus = recipientVerification,
                            onVerify = { viewModel.verifyRecipient(it) },
                            onDismiss = { viewModel.closeStep1() }
                        )
                    }

                    step2Data?.let { data ->
                        Step2PinDialog(
                            data = data,
                            onDismiss = { viewModel.closeStep2() }
                        )
                    }

                    resultData?.let { res ->
                        ResultNotificationDialog(
                            result = res,
                            onDismiss = { viewModel.closeResultModal() }
                        )
                    }

                    adminReplyData?.let { reply ->
                        AdminReplyDialog(
                            reply = reply,
                            onDismiss = { viewModel.closeAdminReplyModal() }
                        )
                    }

                    if (isHistoryOpen) {
                        FullHistoryDialog(
                            history = historyList,
                            onClear = { viewModel.clearAllHistory() },
                            onDismiss = { viewModel.closeHistoryModal() }
                        )
                    }

                    if (isServerConfigOpen) {
                        ServerConfigDialog(
                            initialUrl = viewModel.prefs.serverUrl,
                            onApply = { newUrl -> viewModel.applyServerUrl(newUrl) },
                            onDismiss = { viewModel.closeServerConfig() }
                        )
                    }
                }
            }
        }
    }
}
