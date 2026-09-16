package com.example.model

data class CardProduct(
    val name: String,
    val pid: String,
    val units: Int,
    val validity: String,
    val category: String // "fakka" or "mared"
)

val FAKKA_PRODUCTS = listOf(
    CardProduct("Fakka 2.5 EGP", "Fakka_2.5_Unite", 45, "1 day", "fakka"),
    CardProduct("Fakka 4.25 EGP", "Fakka_4.25_Unite", 190, "1 day", "fakka"),
    CardProduct("Fakka 5 EGP", "Fakka_5_Unite", 225, "1 day", "fakka"),
    CardProduct("Fakka 7 EGP", "Fakka_7_Unite", 340, "3 days", "fakka"),
    CardProduct("Fakka 9 EGP", "Fakka_9_Unite", 400, "4 days", "fakka"),
    CardProduct("Fakka 11.5 EGP", "Fakka_11.5_Unite", 450, "7 days", "fakka"),
    CardProduct("Fakka 13.5 EGP", "Fakka_13.5_Unite", 625, "7 days", "fakka"),
    CardProduct("Fakka 17.5 EGP", "Fakka_17.5_Unite", 650, "10 days", "fakka"),
    CardProduct("Fakka 20 EGP", "Fakka_20_Unite", 750, "10 days", "fakka")
)

val MARED_PRODUCTS = listOf(
    CardProduct("Mared 10 Minutes", "Mared_10_Minuts", 450, "7 days", "mared"),
    CardProduct("Mared 10 Flex", "Mared_10_Flexs", 450, "7 days", "mared"),
    CardProduct("Mared 10 Social", "Mared_10_Social", 900, "7 days", "mared")
)

val ALL_PRODUCTS = FAKKA_PRODUCTS + MARED_PRODUCTS

data class AccountInfo(
    val msisdn: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val fullName: String? = null,
    val customerId: String? = null,
    val sim: String? = null,
    val customerType: String? = null,
    val contractType: String? = null,
    val contractStatus: String? = null,
    val lineType: String? = null,
    val jti: String? = null,
    val loginCode: String? = null,
    val iatReadable: String? = null,
    val expReadable: String? = null
)

data class OperationHistoryItem(
    val id: String,
    val timestamp: String,
    val title: String,
    val subtitle: String,
    val success: Boolean,
    val opType: String? = null,
    val amount: String? = null,
    val recipient: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null
)

data class ServerHealth(
    val online: Boolean = false,
    val latencyMs: Long? = null,
    val error: String? = null
)

data class OperationResult(
    val success: Boolean,
    val title: String,
    val subtitle: String,
    val code: String? = null,
    val message: String? = null
)

val ERROR_MAP = mapOf(
    "0000" to "Operation completed successfully",
    "0" to "Operation completed successfully",
    "200" to "Operation completed successfully",
    "1056" to "Incorrect wallet PIN",
    "2123" to "Wallet locked",
    "2124" to "Code expired",
    "1058" to "Set a wallet PIN first",
    "1118" to "Account is locked",
    "6051" to "Insufficient wallet balance",
    "6114" to "Insufficient balance",
    "2252" to "Insufficient balance",
    "584" to "Minimum 5, maximum 30,000",
    "583" to "Maximum recharge is 6,000",
    "2001" to "Silent login required",
    "2256" to "Service unavailable",
    "2009" to "Number is not active",
    "1006" to "No data available",
    "6104" to "Service unavailable for this number",
    "2012" to "Maximum limit reached",
    "2076" to "Daily limit reached",
    "2043" to "Monthly limit reached",
    "1043" to "Invalid request format",
    "202" to "A pending request exists",
    "6131" to "A pending request exists",
    "5005" to "A pending request exists",
    "3998" to "Transaction error",
    "3800" to "Service unavailable",
    "3999" to "General error",
    "1001" to "Card is unavailable",
    "301" to "Card cannot be used",
    "6129" to "Card cannot be used",
    "6124" to "Card already used",
    "1051" to "No wallet found",
    "194" to "Wallet suspended",
    "203" to "Insufficient wallet balance",
    "2251" to "Active bundle exists",
    "2125" to "Attempt limit exceeded",
    "1022" to "Transaction limit exceeded",
    "429" to "Temporary block",
    "-500" to "Connection timed out",
    "-50500" to "Check your internet connection",
    "-401030" to "Service under maintenance"
)
