package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ServerHealth
import com.example.ui.components.VodafoneLogo
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.VfRed

@Composable
fun LoginScreen(
    serverHealth: ServerHealth,
    loginStatus: String,
    isBusy: Boolean,
    isDarkMode: Boolean,
    isWaitingApproval: Boolean,
    approvalElapsedSec: Int,
    onLoginClick: (String?) -> Unit,
    onToggleTheme: () -> Unit,
    onOpenServerConfig: () -> Unit
) {
    var manualPhone by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenServerConfig,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("change_server_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "Server settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            VodafoneLogo(size = 68.dp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Vodafone Cash",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Secure mobile payments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isWaitingApproval) {
                        // Approval Waiting View
                        CircularProgressIndicator(
                            color = StatusAmber,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Waiting for Approval",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "بانتظار موافقة الإدارة على تسجيل الدخول\n($approvalElapsedSec s elapsed)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                    } else {
                        Text(
                            text = "Welcome",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Your phone number will be detected automatically from your Vodafone network.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        // Server pill
                        val isOnline = serverHealth.online
                        val pillBg = if (isOnline) Color(0xFF0F2A1A) else Color(0xFF2A0F0F)
                        val pillFg = if (isOnline) Color(0xFF4ADE80) else Color(0xFFF87171)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = pillBg,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = if (isOnline) "● Connected (${serverHealth.latencyMs ?: 0} ms)" else "● Server Offline",
                                color = pillFg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Manual Phone Toggle
                        if (showManualInput) {
                            OutlinedTextField(
                                value = manualPhone,
                                onValueChange = { manualPhone = it.filter { ch -> ch.isDigit() }.take(11) },
                                label = { Text("Vodafone Phone Number") },
                                placeholder = { Text("010xxxxxxxx") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("manual_phone_input")
                            )
                        } else {
                            TextButton(
                                onClick = { showManualInput = true },
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text("Or enter phone number manually", fontSize = 12.sp, color = VfRed)
                            }
                        }

                        if (loginStatus.isNotBlank()) {
                            Text(
                                text = loginStatus,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = StatusAmber,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }

                        Button(
                            onClick = {
                                val num = if (showManualInput && manualPhone.isNotBlank()) manualPhone else null
                                onLoginClick(num)
                            },
                            enabled = !isBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = VfRed),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_continue_button")
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting...", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Encrypted connection • Vodafone Cash Client",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
