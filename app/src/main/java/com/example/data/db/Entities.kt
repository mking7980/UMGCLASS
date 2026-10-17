package com.example.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["phoneNumber"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val userId: String,
    val phoneNumber: String,
    val passwordHash: String,
    val passwordSalt: String,
    val level: String = "U1",
    val balance: Double = 1250.0,
    val todayTasksCompleted: Int = 0,
    val todayEarnings: Double = 0.0,
    val totalEarnings: Double = 840.0,
    val giftCodeClaimed: Boolean = false,
    val invitationCode: String = "UMG84920",
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE"
    val registeredTimestamp: Long = System.currentTimeMillis(),
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val isOnline: Boolean = true
)

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["userId"]), Index(value = ["status"])]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // "Recharge", "Withdrawal", "Task Earning", "Level Upgrade", "Gift Code", "Balance Adjustment"
    val amount: Double,
    val status: String, // "Pending", "Approved", "Successful", "Rejected"
    val method: String, // "CBE", "Telebirr", "Bank Transfer", "System"
    val ftNumber: String,
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

@Entity(
    tableName = "balance_adjustments",
    indices = [Index(value = ["userId"]), Index(value = ["timestamp"])]
)
data class BalanceAdjustmentEntity(
    @PrimaryKey val id: String,
    val adminId: String,
    val userId: String,
    val adjustmentType: String, // "INCREASE", "DECREASE"
    val amount: Double,
    val reason: String,
    val previousBalance: Double,
    val newBalance: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_config")
data class AdminConfigEntity(
    @PrimaryKey val id: Int = 1,
    val codeHash: String,
    val codeSalt: String,
    val failedAttempts: Int = 0,
    val lockoutUntil: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["userId", "taskId"], unique = true)]
)
data class TaskRecordEntity(
    @PrimaryKey val id: String, // "$userId-$taskId"
    val userId: String,
    val taskId: String,
    val title: String,
    val category: String,
    val reward: Double,
    val status: String, // "AVAILABLE", "TAKEN", "COMPLETED"
    val takenTimestamp: Long? = null,
    val completedTimestamp: Long? = null
)
