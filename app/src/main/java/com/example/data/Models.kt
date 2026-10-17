package com.example.data

data class VipTier(
    val id: String,
    val name: String,
    val dailyTasks: Int,
    val perTaskReward: Double,
    val dailyIncome: Double,
    val withdrawalFee: Double,
    val investmentPrice: Double,
    val badge: String = "VIP"
)

val VIP_TIERS = listOf(
    VipTier(
        id = "U1",
        name = "UMG Level U1",
        dailyTasks = 3,
        perTaskReward = 18.0,
        dailyIncome = 54.0,
        withdrawalFee = 250.0,
        investmentPrice = 1000.0
    ),
    VipTier(
        id = "U2",
        name = "UMG Level U2",
        dailyTasks = 5,
        perTaskReward = 35.0,
        dailyIncome = 175.0,
        withdrawalFee = 500.0,
        investmentPrice = 6000.0
    ),
    VipTier(
        id = "U3",
        name = "UMG Level U3",
        dailyTasks = 8,
        perTaskReward = 55.0,
        dailyIncome = 440.0,
        withdrawalFee = 800.0,
        investmentPrice = 21000.0
    ),
    VipTier(
        id = "U4",
        name = "UMG Level U4",
        dailyTasks = 12,
        perTaskReward = 85.0,
        dailyIncome = 1020.0,
        withdrawalFee = 1200.0,
        investmentPrice = 45000.0
    ),
    VipTier(
        id = "U5",
        name = "UMG Level U5",
        dailyTasks = 18,
        perTaskReward = 120.0,
        dailyIncome = 2160.0,
        withdrawalFee = 1900.0,
        investmentPrice = 90000.0
    )
)

fun getVipLevelRank(id: String): Int {
    return when (id.uppercase().trim()) {
        "U1" -> 1
        "U2" -> 2
        "U3" -> 3
        "U4" -> 4
        "U5" -> 5
        else -> 1
    }
}

fun getVipTier(id: String): VipTier {
    return VIP_TIERS.find { it.id.equals(id, ignoreCase = true) } ?: VIP_TIERS.first()
}

data class TaskItem(
    val id: String,
    val title: String,
    val category: String,
    val reward: Double,
    val status: String = "AVAILABLE", // "AVAILABLE", "TAKEN", "COMPLETED"
    val isCompleted: Boolean = (status == "COMPLETED")
) {
    val isTaken: Boolean get() = status == "TAKEN"
}

data class TransactionRecord(
    val id: String,
    val userId: String = "",
    val type: String, // "Recharge", "Withdrawal", "Gift Code", "Task Earning", "Balance Adjustment"
    val amount: Double,
    val status: String, // "Pending", "Approved", "Successful", "Rejected"
    val method: String, // "CBE", "Telebirr", "Bank Transfer", "System"
    val ftNumber: String = "",
    val bankName: String? = null,
    val accountHolderName: String? = null,
    val accountNumber: String? = null,
    val screenshotUri: String? = null,
    val adminId: String? = null,
    val adminReason: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val completedTimestamp: Long? = null,
    val details: String = ""
)

data class UserSession(
    val userId: String = "USR-0912048137",
    val phoneNumber: String = "0912048137",
    val password: String = "123456",
    val level: String = "U1",
    val balance: Double = 1250.0,
    val todayTasksCompleted: Int = 0,
    val todayEarnings: Double = 0.0,
    val totalEarnings: Double = 840.0,
    val giftCodeClaimed: Boolean = false,
    val invitationCode: String = "UMG84920",
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE"
    val registeredTimestamp: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true,
    val isLoggedIn: Boolean = true
)

data class AdminStats(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val inactiveUsers: Int = 0,
    val onlineUsers: Int = 0,
    val registeredToday: Int = 0,
    val lastUpdatedTime: Long = System.currentTimeMillis()
)

data class BalanceAdjustmentRecord(
    val id: String,
    val adminId: String,
    val userId: String,
    val adjustmentType: String, // "INCREASE", "DECREASE"
    val amount: Double,
    val reason: String,
    val previousBalance: Double,
    val newBalance: Double,
    val timestamp: Long = System.currentTimeMillis()
)
