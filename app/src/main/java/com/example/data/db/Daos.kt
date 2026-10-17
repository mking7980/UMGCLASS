package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY registeredTimestamp DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    fun getTotalUsersCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'")
    fun getActiveUsersCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE status = 'INACTIVE'")
    fun getInactiveUsersCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE isOnline = 1")
    fun getOnlineUsersCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE registeredTimestamp >= :startOfDayTimestamp")
    fun getRegisteredTodayCountFlow(startOfDayTimestamp: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET balance = :newBalance WHERE userId = :userId")
    suspend fun updateBalance(userId: String, newBalance: Double)

    @Query("UPDATE users SET status = :status WHERE userId = :userId")
    suspend fun updateStatus(userId: String, status: String)

    @Query("UPDATE users SET isOnline = :isOnline, lastActiveTimestamp = :timestamp WHERE userId = :userId")
    suspend fun updateOnlineStatus(userId: String, isOnline: Boolean, timestamp: Long)

    @Query("UPDATE users SET level = :level WHERE userId = :userId")
    suspend fun updateLevel(userId: String, level: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUserFlow(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'Recharge' ORDER BY timestamp DESC")
    fun getRechargeTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'Withdrawal' ORDER BY timestamp DESC")
    fun getWithdrawalTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET status = :status, adminId = :adminId, adminReason = :reason, completedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, adminId: String?, reason: String?, timestamp: Long?)
}

@Dao
interface BalanceAdjustmentDao {
    @Query("SELECT * FROM balance_adjustments ORDER BY timestamp DESC")
    fun getAllAdjustmentsFlow(): Flow<List<BalanceAdjustmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: BalanceAdjustmentEntity)
}

@Dao
interface AdminConfigDao {
    @Query("SELECT * FROM admin_config WHERE id = 1 LIMIT 1")
    suspend fun getAdminConfig(): AdminConfigEntity?

    @Query("SELECT * FROM admin_config WHERE id = 1 LIMIT 1")
    fun getAdminConfigFlow(): Flow<AdminConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: AdminConfigEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId")
    fun getTasksForUserFlow(userId: String): Flow<List<TaskRecordEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND taskId = :taskId LIMIT 1")
    suspend fun getTask(userId: String, taskId: String): TaskRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTask(task: TaskRecordEntity)

    @Query("UPDATE tasks SET status = :status, takenTimestamp = :timestamp WHERE userId = :userId AND taskId = :taskId")
    suspend fun updateTaskStatus(userId: String, taskId: String, status: String, timestamp: Long)

    @Query("UPDATE tasks SET status = :status, completedTimestamp = :timestamp WHERE userId = :userId AND taskId = :taskId")
    suspend fun completeTask(userId: String, taskId: String, status: String, timestamp: Long)
}
