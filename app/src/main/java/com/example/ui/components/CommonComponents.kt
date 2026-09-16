package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.OperationHistoryItem
import com.example.model.OperationResult
import com.example.ui.theme.*
import com.example.viewmodel.ModalStep1Data
import com.example.viewmodel.ModalStep2Data

@Composable
fun VodafoneLogo(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size.minDimension
        // Red circle background
        drawCircle(
            color = Color(0xFFE60000),
            radius = s / 2,
            center = Offset(s / 2, s / 2)
        )
        // Stylized speech mark / droplet in white
        drawCircle(
            color = Color.White,
            radius = s * 0.22f,
            center = Offset(s * 0.5f, s * 0.4f)
        )
        val path = Path().apply {
            moveTo(s * 0.28f, s * 0.48f)
            lineTo(s * 0.62f, s * 0.48f)
            lineTo(s * 0.28f, s * 0.82f)
            close()
        }
        drawPath(path, Color.White)
    }
}

@Composable
fun Step1RecipientDialog(
    data: ModalStep1Data,
    localNumber: String,
    verificationStatus: String?,
    onVerify: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var recipientText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("step1_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (data.icon == "card") Icons.Default.CreditCard else Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = VfRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(VfRed)
                    ) {
                        Text("1", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(" Details", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = VfRed)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(28.dp)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("2", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(" PIN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Details list
                data.details.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recipient Input
                Text(
                    text = "Recipient Number (Optional)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = recipientText,
                        onValueChange = { recipientText = it },
                        placeholder = { Text("010xxxxxxx") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (recipientText.length >= 10) onVerify(recipientText)
                        }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("recipient_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (recipientText.length >= 10) onVerify(recipientText)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("verify_recipient_button")
                    ) {
                        Text("Verify", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                // Verification status text
                if (!verificationStatus.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val isSuccess = verificationStatus != "Verifying..." && !verificationStatus.contains("not", ignoreCase = true)
                    val statusColor = when {
                        verificationStatus == "Verifying..." -> StatusAmber
                        isSuccess -> Color(0xFF22C55E)
                        else -> MaterialTheme.colorScheme.error
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                verificationStatus == "Verifying..." -> Icons.Default.HourglassEmpty
                                isSuccess -> Icons.Default.CheckCircle
                                else -> Icons.Default.Cancel
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = verificationStatus,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "Leave empty to send to yourself ($localNumber)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("step1_cancel"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val rec = if (recipientText.isBlank()) localNumber else recipientText.trim()
                            data.onContinue(rec)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VfRed),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("step1_continue"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun Step2PinDialog(
    data: ModalStep2Data,
    onDismiss: () -> Unit
) {
    var pinText by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("step2_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = VfRed,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Step indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("1", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(" Details", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .width(28.dp)
                            .height(2.dp)
                            .background(VfRed)
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(VfRed)
                    ) {
                        Text("2", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(" PIN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = VfRed)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Details summary
                data.details.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Enter your 4-8 digit wallet PIN",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // PIN Display Box
                OutlinedTextField(
                    value = pinText,
                    onValueChange = {
                        val filtered = it.filter { ch -> ch.isDigit() }.take(8)
                        pinText = filtered
                    },
                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation('•'),
                    trailingIcon = {
                        IconButton(onClick = { pinVisible = !pinVisible }) {
                            Icon(
                                imageVector = if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle PIN",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 6.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pin_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mobile numeric keypad
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("⌫", "0", "OK")
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    keys.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { k ->
                                Button(
                                    onClick = {
                                        when (k) {
                                            "⌫" -> if (pinText.isNotEmpty()) pinText = pinText.dropLast(1)
                                            "OK" -> if (pinText.length >= 4) data.onConfirm(pinText)
                                            else -> if (pinText.length < 8) pinText += k
                                        }
                                    },
                                    colors = if (k == "OK") ButtonDefaults.buttonColors(containerColor = VfRed)
                                    else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("keypad_$k")
                                ) {
                                    Text(
                                        text = k,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (k == "OK") Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("step2_cancel"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            if (pinText.length >= 4) {
                                data.onConfirm(pinText)
                            }
                        },
                        enabled = pinText.length >= 4,
                        colors = ButtonDefaults.buttonColors(containerColor = VfRed),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("step2_confirm"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Confirm", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ResultNotificationDialog(
    result: OperationResult,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("result_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success / Failure Icon
                val iconBg = if (result.success) Color(0xFF0F2A1A) else Color(0xFF2A0F0F)
                val iconColor = if (result.success) Color(0xFF4ADE80) else Color(0xFFF87171)
                val iconVector = if (result.success) Icons.Default.Check else Icons.Default.Close

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(iconBg)
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = result.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (!result.code.isNullOrBlank() || !result.message.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            result.code?.let {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Response Code", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(it, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            result.message?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = it,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = VfRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("result_done_button")
                ) {
                    Text(if (result.success) "Done" else "Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminReplyDialog(
    reply: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("admin_reply_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = VfRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "رد من الإدارة (Admin Reply)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reply,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = VfRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("حسناً (OK)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FullHistoryDialog(
    history: List<OperationHistoryItem>,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp)
                .testTag("full_history_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = VfRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Operation History",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "${history.size} entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (history.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No history recorded yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(history) { item ->
                            HistoryRow(item)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onClear,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Clear", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = VfRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRow(item: OperationHistoryItem) {
    val ok = item.success
    val ibg = if (ok) Color(0xFF0F2A1A) else Color(0xFF2A0F0F)
    val ifg = if (ok) Color(0xFF4ADE80) else Color(0xFFF87171)

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(ibg)
            ) {
                Icon(
                    imageVector = if (ok) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = ifg,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.timestamp.takeLast(8),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!item.errorCode.isNullOrBlank()) {
                    Text(
                        text = "Code: ${item.errorCode}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ServerConfigDialog(
    initialUrl: String,
    onApply: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var urlText by remember { mutableStateOf(initialUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("server_config_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = VfRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Configure Server URL",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("Server URL") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = urlText == com.example.data.PreferencesManager.DEFAULT_SERVER_URL,
                        onClick = { urlText = com.example.data.PreferencesManager.DEFAULT_SERVER_URL },
                        label = { Text("Cloud Server", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = urlText == "http://10.0.2.2:5000",
                        onClick = { urlText = "http://10.0.2.2:5000" },
                        label = { Text("Localhost", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onApply(urlText) },
                        colors = ButtonDefaults.buttonColors(containerColor = VfRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
