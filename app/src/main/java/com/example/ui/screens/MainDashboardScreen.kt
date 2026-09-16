package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.*
import com.example.ui.components.VodafoneLogo
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.VfRed
import com.example.viewmodel.MainViewModel

@Composable
fun MainDashboardScreen(
    viewModel: MainViewModel
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val serverHealth by viewModel.serverHealth.collectAsState()
    val localNumber by viewModel.localNumber.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Vodafone Cash",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Online chip
                        val isOnline = serverHealth.online
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isOnline) Color(0xFF0F2A1A) else Color(0xFF2A0F0F),
                            modifier = Modifier
                                .clickable { viewModel.openServerConfig() }
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (isOnline) "● ${serverHealth.latencyMs ?: 0} ms" else "● Offline",
                                color = if (isOnline) Color(0xFF4ADE80) else Color(0xFFF87171),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Theme toggle
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTab == "home",
                    onClick = { viewModel.selectTab("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = VfRed,
                        indicatorColor = VfRed
                    ),
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = selectedTab == "cards",
                    onClick = { viewModel.selectTab("cards") },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "Cards") },
                    label = { Text("Cards", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = VfRed,
                        indicatorColor = VfRed
                    ),
                    modifier = Modifier.testTag("nav_cards")
                )
                NavigationBarItem(
                    selected = selectedTab == "recharge",
                    onClick = { viewModel.selectTab("recharge") },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Recharge") },
                    label = { Text("Recharge", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = VfRed,
                        indicatorColor = VfRed
                    ),
                    modifier = Modifier.testTag("nav_recharge")
                )
                NavigationBarItem(
                    selected = selectedTab == "settings",
                    onClick = { viewModel.selectTab("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = VfRed,
                        indicatorColor = VfRed
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "home" -> HomeTabScreen(viewModel)
                "cards" -> CardsTabScreen(viewModel)
                "recharge" -> RechargeTabScreen(viewModel)
                "settings" -> SettingsTabScreen(viewModel)
            }
        }
    }
}

@Composable
fun HomeTabScreen(viewModel: MainViewModel) {
    val localNumber by viewModel.localNumber.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val walletDesc by viewModel.walletDesc.collectAsState()
    val isBalanceLoading by viewModel.isBalanceLoading.collectAsState()
    val historyList by viewModel.historyList.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Hero
        Text(
            text = "Hello,",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Everything you need, in one place",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Number Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = VfRed,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mobile Number",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = localNumber,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F2A1A)
                ) {
                    Text(
                        text = "✓ Verified",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Wallet Balance Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = VfRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Wallet Balance",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(
                        onClick = { viewModel.refreshBalance() },
                        enabled = !isBalanceLoading,
                        modifier = Modifier.testTag("refresh_balance_btn")
                    ) {
                        if (isBalanceLoading) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = VfRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Refresh", color = VfRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Text(
                    text = walletBalance ?: "---.-- EGP",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = walletDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.selectTab("cards") }
                    .testTag("quick_buy_fakka")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = VfRed, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Buy Fakka", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Choose a bundle", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.selectTab("recharge") }
                    .testTag("quick_recharge")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.AttachMoney, contentDescription = null, tint = VfRed, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Recharge", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Add balance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activity Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "See all ›",
                color = VfRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .clickable { viewModel.openHistoryModal() }
                    .testTag("see_all_activity")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (historyList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No activity yet", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Your transactions will appear here", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(modifier = Modifier.padding(12.dp)) {
                    historyList.take(5).forEachIndexed { index, item ->
                        if (index > 0) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val ok = item.success
                            val ibg = if (ok) Color(0xFF0F2A1A) else Color(0xFF2A0F0F)
                            val ifg = if (ok) Color(0xFF4ADE80) else Color(0xFFF87171)

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(ibg)
                            ) {
                                Icon(
                                    imageVector = if (ok) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = null,
                                    tint = ifg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(item.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Text(
                                text = item.timestamp.takeLast(8),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun CardsTabScreen(viewModel: MainViewModel) {
    val cardCategory by viewModel.cardCategory.collectAsState()
    val products = if (cardCategory == "fakka") FAKKA_PRODUCTS else MARED_PRODUCTS

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Buy a Card",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Pick a bundle that fits what you need",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // Category Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            Button(
                onClick = { viewModel.setCardCategory("fakka") },
                colors = if (cardCategory == "fakka") ButtonDefaults.buttonColors(containerColor = VfRed)
                else ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("cat_fakka")
            ) {
                Text("Fakka (فكة)", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { viewModel.setCardCategory("mared") },
                colors = if (cardCategory == "mared") ButtonDefaults.buttonColors(containerColor = VfRed)
                else ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("cat_mared")
            ) {
                Text("Mared (مارد)", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.initiateBuyCard(product) }
                        .testTag("card_${product.pid}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = VfRed, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "${product.units} units • ${product.validity}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun RechargeTabScreen(viewModel: MainViewModel) {
    var amountText by remember { mutableStateOf("") }
    val presets = listOf(10, 20, 50, 100, 200)

    val currentAmount = amountText.toDoubleOrNull() ?: 0.0
    val deductedAmount = Math.round(currentAmount * 1.428 * 100.0) / 100.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Recharge Balance",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Top up your Vodafone balance with cash wallet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Amount (EGP)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recharge_amount_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { a ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { amountText = a.toString() }
                        ) {
                            Text(
                                text = "$a",
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                if (currentAmount >= 2.0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Recharge Balance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$currentAmount EGP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Wallet Deduction (incl. tax)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$deductedAmount EGP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VfRed)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (currentAmount >= 2.0) {
                            viewModel.initiateRecharge(currentAmount)
                        }
                    },
                    enabled = currentAmount >= 2.0,
                    colors = ButtonDefaults.buttonColors(containerColor = VfRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("recharge_continue_btn")
                ) {
                    Text("Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun SettingsTabScreen(viewModel: MainViewModel) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val serverHealth by viewModel.serverHealth.collectAsState()
    val serverUrl = viewModel.prefs.serverUrl
    val localNumber by viewModel.localNumber.collectAsState()
    val accountInfo by viewModel.accountInfo.collectAsState()
    val sessionId by viewModel.sessionId.collectAsState()
    val sessionStartTime by viewModel.sessionStartTime.collectAsState()
    val historyList by viewModel.historyList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Manage appearance, server and account",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Appearance section
        Text("APPEARANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleDarkMode() }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = VfRed
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Dark Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(if (isDarkMode) "Dark theme enabled" else "Light theme enabled", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(checked = isDarkMode, onCheckedChange = { viewModel.toggleDarkMode() })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Server Connection Section
        Text("SERVER CONNECTION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Server URL", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(serverUrl, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Backend Status", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (serverHealth.online) "Connected" else "Offline", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (serverHealth.online) Color(0xFF22C55E) else MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Latency", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${serverHealth.latencyMs ?: 0} ms", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.checkServerHealth(serverUrl) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Test", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { viewModel.openServerConfig() },
                        colors = ButtonDefaults.buttonColors(containerColor = VfRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Change URL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Details Section
        Text("ACCOUNT DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsRow("Phone Number", localNumber)
                accountInfo?.fullName?.let { SettingsRow("Full Name", it) }
                accountInfo?.customerId?.let { SettingsRow("Customer ID", it) }
                accountInfo?.contractType?.let { SettingsRow("Contract Type", it) }
                accountInfo?.contractStatus?.let { SettingsRow("Contract Status", it) }
                accountInfo?.lineType?.let { SettingsRow("Line Type", it) }
                accountInfo?.sim?.let { SettingsRow("SIM", it) }
                accountInfo?.loginCode?.let { SettingsRow("Login Code", it) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Session Section
        Text("SESSION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsRow("Session ID", sessionId)
                SettingsRow("Started", sessionStartTime)
                SettingsRow("History Entries", "${historyList.size}")

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.clearAllHistory() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear All History", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsRow(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(key, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
