package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.model.ServerHealth
import com.example.ui.components.VodafoneLogo
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.VfRed

@Composable
fun ServerSetupScreen(
    currentUrl: String,
    serverHealth: ServerHealth,
    isChecking: Boolean,
    isDarkMode: Boolean,
    onUrlChange: (String) -> Unit,
    onTestConnection: (String) -> Unit,
    onContinue: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val scrollState = rememberScrollState()

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VodafoneLogo(size = 32.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vodafone Cash",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
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
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Big Icon / Logo Header
            VodafoneLogo(size = 72.dp)

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Server Configuration",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "يرجى تحديد رابط خادم الإدارة والمزامنة للمتابعة\nEnter your backend server URL to proceed",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
            )

            // Card with Input & Controls
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Server URL / رابط السيرفر",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = currentUrl,
                        onValueChange = onUrlChange,
                        placeholder = { Text("https://example.com") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = VfRed
                            )
                        },
                        trailingIcon = {
                            if (currentUrl.isNotEmpty()) {
                                IconButton(onClick = { onUrlChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_url_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick presets
                    Text(
                        text = "Quick Presets:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = currentUrl == PreferencesManager.DEFAULT_SERVER_URL,
                            onClick = { onUrlChange(PreferencesManager.DEFAULT_SERVER_URL) },
                            label = { Text("Cloud Default", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FilterChip(
                            selected = currentUrl == "http://10.0.2.2:5000",
                            onClick = { onUrlChange("http://10.0.2.2:5000") },
                            label = { Text("Localhost", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Test connection button
                    OutlinedButton(
                        onClick = { onTestConnection(currentUrl) },
                        enabled = !isChecking && currentUrl.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("test_server_button")
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing Connection...")
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Connection (فحص الاتصال)", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Health Indicator
                    AnimatedVisibility(visible = serverHealth.online || serverHealth.error != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val isOnline = serverHealth.online
                        val statusBg = if (isOnline) Color(0xFF0F2A1A) else Color(0xFF2A0F0F)
                        val statusFg = if (isOnline) Color(0xFF4ADE80) else Color(0xFFF87171)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = statusBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isOnline) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = statusFg,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isOnline) "Connected (${serverHealth.latencyMs} ms)" else "Failed: ${serverHealth.error}",
                                    color = statusFg,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue button
            Button(
                onClick = onContinue,
                enabled = currentUrl.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = VfRed),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("continue_server_button")
            ) {
                Text(
                    text = "Continue to Application  ›",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "v1.1 • Encrypted TLS connection",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
