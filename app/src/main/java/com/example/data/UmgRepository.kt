package com.example.data

import android.content.Context
import com.example.data.db.AdminConfigEntity
import com.example.data.db.AppDatabase
import com.example.data.db.BalanceAdjustmentEntity
import com.example.data.db.TaskRecordEntity
import com.example.data.db.TransactionEntity
import com.example.data.db.UserEntity
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Calendar

class UmgRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val transactionDao = db.transactionDao()
    private val balanceAdjustmentDao = db.balanceAdjustmentDao()
    private val adminConfigDao = db.adminConfigDao()
    private val taskDao = db.taskDao()

    private val scope = CoroutineScope(Dispatchers.IO)

    // Current active logged-in user ID
    private val currentUserId = MutableStateFlow("USR-0912048137")

    private val _userSession = MutableStateFlow(
        UserSession(
            userId = "USR-0912048137",
            phoneNumber = "0912048137",
            password = "••••••",
            level = "U1",
            balance = 1250.0,
            todayTasksCompleted = 0,
            todayEarnings = 0.0,
            totalEarnings = 840.0,
            giftCodeClaimed = false,
            invitationCode = "UMG84920",
            status = "ACTIVE",
            isOnline = true,
            isLoggedIn = true
        )
    )
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionRecord>>(emptyList())
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    // Real-Time Admin StateFlows backed directly by Database
    val allUsers: StateFlow<List<UserEntity>> = userDao.getAllUsersFlow()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val allTransactions: StateFlow<List<TransactionRecord>> = transactionDao.getAllTransactionsFlow()
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.map { it.toModel() }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val allRecharges: StateFlow<List<TransactionRecord>> = transactionDao.getRechargeTransactionsFlow()
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.map { it.toModel() }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val allWithdrawals: StateFlow<List<TransactionRecord>> = transactionDao.getWithdrawalTransactionsFlow()
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.map { it.toModel() }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val balanceAdjustments: StateFlow<List<BalanceAdjustmentRecord>> = balanceAdjustmentDao.getAllAdjustmentsFlow()
        .combine(MutableStateFlow(Unit)) { list, _ ->
            list.map {
                BalanceAdjustmentRecord(
                    id = it.id,
                    adminId = it.adminId,
                    userId = it.userId,
                    adjustmentType = it.adjustmentType,
                    amount = it.amount,
                    reason = it.reason,
                    previousBalance = it.previousBalance,
                    newBalance = it.newBalance,
                    timestamp = it.timestamp
                )
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // Admin Real-Time Database Statistics
    val adminStats: StateFlow<AdminStats> = combine(
        userDao.getTotalUsersCountFlow(),
        userDao.getActiveUsersCountFlow(),
        userDao.getInactiveUsersCountFlow(),
        userDao.getOnlineUsersCountFlow(),
        userDao.getRegisteredTodayCountFlow(getStartOfDayTimestamp())
    ) { total, active, inactive, online, today ->
        AdminStats(
            totalUsers = total,
            activeUsers = active,
            inactiveUsers = inactive,
            onlineUsers = online,
            registeredToday = today,
            lastUpdatedTime = System.currentTimeMillis()
        )
    }.stateIn(scope, SharingStarted.Eagerly, AdminStats())

    init {
        runBlocking(Dispatchers.IO) {
            seedDatabaseIfEmpty()
        }
        scope.launch {
            observeCurrentUser()
            observeUserTransactions()
            observeUserTasks()
        }
    }

    private suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        // 1. Seed Admin Config if not set
        val admin = adminConfigDao.getAdminConfig()
        if (admin == null) {
            val salt = SecurityUtils.generateSalt()
            val initialHash = SecurityUtils.hashWithSalt("1954", salt)
            adminConfigDao.insertOrUpdate(
                AdminConfigEntity(
                    id = 1,
                    codeHash = initialHash,
                    codeSalt = salt,
                    failedAttempts = 0,
                    lockoutUntil = 0L,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

        // 2. Seed Default User if not present
        val existingUser = userDao.getUserById("USR-0912048137")
        if (existingUser == null) {
            val userSalt = SecurityUtils.generateSalt()
            val userPassHash = SecurityUtils.hashWithSalt("123456", userSalt)
            val defaultUser = UserEntity(
                userId = "USR-0912048137",
                phoneNumber = "0912048137",
                passwordHash = userPassHash,
                passwordSalt = userSalt,
                level = "U1",
                balance = 1250.0,
                todayTasksCompleted = 0,
                todayEarnings = 0.0,
                totalEarnings = 840.0,
                giftCodeClaimed = false,
                invitationCode = "UMG84920",
                status = "ACTIVE",
                registeredTimestamp = System.currentTimeMillis() - 86400000L * 7,
                lastActiveTimestamp = System.currentTimeMillis(),
                isOnline = true
            )
            userDao.insertUser(defaultUser)

            // Seed initial transactions
            transactionDao.insertTransaction(
                TransactionEntity(
                    id = "TX-89210",
                    userId = defaultUser.userId,
                    type = "Task Earning",
                    amount = 54.0,
                    status = "Successful",
                    method = "Daily Tasks",
                    ftNumber = "UMG-TASK-8921",
                    timestamp = System.currentTimeMillis() - 86400000L,
                    completedTimestamp = System.currentTimeMillis() - 86400000L,
                    details = "3 Tax Tasks Completed"
                )
            )
            transactionDao.insertTransaction(
                TransactionEntity(
                    id = "TX-78144",
                    userId = defaultUser.userId,
                    type = "Recharge",
                    amount = 500.0,
                    status = "Approved",
                    method = "Telebirr",
                    ftNumber = "FT240914881",
                    adminId = "ADMIN-1954",
                    timestamp = System.currentTimeMillis() - 172800000L,
                    completedTimestamp = System.currentTimeMillis() - 172800000L,
                    details = "Account VIP Activation"
                )
            )
            transactionDao.insertTransaction(
                TransactionEntity(
                    id = "TX-66321",
                    userId = defaultUser.userId,
                    type = "Withdrawal",
                    amount = 2500.0,
                    status = "Successful",
                    method = "CBE",
                    ftNumber = "FT240910129",
                    bankName = "Commercial Bank of Ethiopia",
                    accountHolderName = "Ms Hewot Eshetu",
                    accountNumber = "1000139470277",
                    adminId = "ADMIN-1954",
                    timestamp = System.currentTimeMillis() - 345600000L,
                    completedTimestamp = System.currentTimeMillis() - 345600000L,
                    details = "Bank Payout Dispatched"
                )
            )
        }

        // Seed initial tasks for user
        seedTasksForUser(currentUserId.value, "U1")
    }

    private suspend fun seedTasksForUser(userId: String, level: String) {
        val tier = getVipTier(level)
        val initialTaskTemplates = listOf(
            Triple("t1", "Federal Tax Declaration Audit #ET-9182", "Tax Audit"),
            Triple("t2", "Value Added Tax (VAT) Clearance Form #VAT-304", "VAT Clearance"),
            Triple("t3", "Customs Commercial Duty Verification #CD-881", "Customs Check"),
            Triple("t4", "Corporate Income Revenue Stamp #REV-221", "Revenue Stamp"),
            Triple("t5", "Withholding Tax Certificate Verification #WH-709", "Withholding Tax"),
            Triple("t6", "Ministry of Revenues Settlement Audit #ET-449", "Ministry Audit")
        )

        for ((id, title, cat) in initialTaskTemplates) {
            val existing = taskDao.getTask(userId, id)
            if (existing == null) {
                taskDao.insertOrUpdateTask(
                    TaskRecordEntity(
                        id = "$userId-$id",
                        userId = userId,
                        taskId = id,
                        title = title,
                        category = cat,
                        reward = tier.perTaskReward,
                        status = "AVAILABLE"
                    )
                )
            }
        }
    }

    private fun observeCurrentUser() {
        scope.launch {
            currentUserId.collect { uid ->
                userDao.getUserByIdFlow(uid).collect { user ->
                    if (user != null) {
                        _userSession.value = UserSession(
                            userId = user.userId,
                            phoneNumber = user.phoneNumber,
                            password = "••••••",
                            level = user.level,
                            balance = user.balance,
                            todayTasksCompleted = user.todayTasksCompleted,
                            todayEarnings = user.todayEarnings,
                            totalEarnings = user.totalEarnings,
                            giftCodeClaimed = user.giftCodeClaimed,
                            invitationCode = user.invitationCode,
                            status = user.status,
                            registeredTimestamp = user.registeredTimestamp,
                            isOnline = user.isOnline,
                            isLoggedIn = true
                        )
                    }
                }
            }
        }
    }

    private fun observeUserTransactions() {
        scope.launch {
            currentUserId.collect { uid ->
                transactionDao.getTransactionsForUserFlow(uid).collect { list ->
                    _transactions.value = list.map { it.toModel() }
                }
            }
        }
    }

    private fun observeUserTasks() {
        scope.launch {
            currentUserId.collect { uid ->
                taskDao.getTasksForUserFlow(uid).collect { list ->
                    _tasks.value = list.map {
                        TaskItem(
                            id = it.taskId,
                            title = it.title,
                            category = it.category,
                            reward = it.reward,
                            status = it.status
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // USER AUTH & SESSION
    // ==========================================

    fun login(phone: String, pass: String, selectedLevel: String): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            val user = userDao.getUserByPhone(phone.trim())
            if (user != null) {
                if (!SecurityUtils.verify(pass, user.passwordSalt, user.passwordHash)) {
                    return@withContext Result.failure(Exception("Incorrect password for phone number $phone."))
                }

                if (user.status == "INACTIVE") {
                    return@withContext Result.failure(Exception("This account has been deactivated by administrator."))
                }

                val currentRank = getVipLevelRank(user.level)
                val selectedRank = getVipLevelRank(selectedLevel)
                if (selectedRank < currentRank) {
                    return@withContext Result.failure(
                        Exception("Downgrading to a lower level is not allowed. Active level is ${user.level}.")
                    )
                }

                userDao.updateOnlineStatus(user.userId, true, System.currentTimeMillis())
                if (selectedRank > currentRank) {
                    userDao.updateLevel(user.userId, selectedLevel)
                }
                currentUserId.value = user.userId
            } else {
                // Register new user on the fly if not existing
                val salt = SecurityUtils.generateSalt()
                val hash = SecurityUtils.hashWithSalt(pass, salt)
                val newUser = UserEntity(
                    userId = "USR-${phone.trim()}",
                    phoneNumber = phone.trim(),
                    passwordHash = hash,
                    passwordSalt = salt,
                    level = selectedLevel,
                    balance = 1250.0,
                    todayTasksCompleted = 0,
                    todayEarnings = 0.0,
                    totalEarnings = 0.0,
                    giftCodeClaimed = false,
                    invitationCode = "UMG${(10000..99999).random()}",
                    status = "ACTIVE",
                    registeredTimestamp = System.currentTimeMillis(),
                    lastActiveTimestamp = System.currentTimeMillis(),
                    isOnline = true
                )
                userDao.insertUser(newUser)
                seedTasksForUser(newUser.userId, selectedLevel)
                currentUserId.value = newUser.userId
            }
            Result.success(Unit)
        }
    }

    fun upgradeVipLevel(targetLevelId: String): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            val user = userDao.getUserById(currentUserId.value) ?: return@withContext Result.failure(Exception("User not found"))
            val currentRank = getVipLevelRank(user.level)
            val targetRank = getVipLevelRank(targetLevelId)

            if (targetRank <= currentRank) {
                return@withContext Result.failure(Exception("Downgrading is not allowed. Currently at ${user.level}."))
            }

            val targetTier = getVipTier(targetLevelId)
            if (user.balance < targetTier.investmentPrice) {
                return@withContext Result.failure(
                    Exception("Insufficient balance. You need ${targetTier.investmentPrice.toInt()} ETB. Available: ${user.balance.toInt()} ETB.")
                )
            }

            userDao.updateBalance(user.userId, user.balance - targetTier.investmentPrice)
            userDao.updateLevel(user.userId, targetTier.id)

            transactionDao.insertTransaction(
                TransactionEntity(
                    id = "TX-${System.currentTimeMillis() % 100000}",
                    userId = user.userId,
                    type = "Level Upgrade",
                    amount = targetTier.investmentPrice,
                    status = "Successful",
                    method = "Account Balance",
                    ftNumber = "UPGRADE-${targetTier.id}",
                    timestamp = System.currentTimeMillis(),
                    completedTimestamp = System.currentTimeMillis(),
                    details = "Upgraded to ${targetTier.name}"
                )
            )

            seedTasksForUser(user.userId, targetTier.id)
            Result.success(Unit)
        }
    }

    // ==========================================
    // RECHARGE RULES & SUBMISSION
    // ==========================================

    fun submitRecharge(
        amount: Double,
        method: String,
        ftNumber: String,
        screenshotUri: String?
    ): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            // Requirement 3: Minimum 100 ETB, Maximum 10,000 ETB
            if (amount < 100.0) {
                return@withContext Result.failure(Exception("Minimum recharge amount is 100 ETB."))
            }
            if (amount > 10000.0) {
                return@withContext Result.failure(Exception("Maximum recharge amount is 10,000 ETB."))
            }
            if (ftNumber.isBlank()) {
                return@withContext Result.failure(Exception("Please enter the transaction reference / FT Number."))
            }

            val uid = currentUserId.value
            val tx = TransactionEntity(
                id = "TX-${System.currentTimeMillis() % 1000000}",
                userId = uid,
                type = "Recharge",
                amount = amount,
                status = "Pending", // Must be marked as Pending
                method = method,
                ftNumber = ftNumber.trim(),
                screenshotUri = screenshotUri,
                timestamp = System.currentTimeMillis(),
                details = if (screenshotUri != null) "Screenshot attached • Verification in progress" else "Verification in progress"
            )

            // Persist to database without modifying user's balance!
            transactionDao.insertTransaction(tx)
            Result.success(Unit)
        }
    }

    // ==========================================
    // WITHDRAWAL RULES & SUBMISSION
    // ==========================================

    fun submitWithdrawal(
        amount: Double,
        bankName: String,
        accountHolderName: String,
        accountNumber: String
    ): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            // Requirement 4: Minimum 2,500 ETB, Maximum 50,000 ETB
            if (amount < 2500.0) {
                return@withContext Result.failure(Exception("Minimum withdrawal amount is 2,500 ETB."))
            }
            if (amount > 50000.0) {
                return@withContext Result.failure(Exception("Maximum withdrawal amount is 50,000 ETB."))
            }
            if (bankName.isBlank()) {
                return@withContext Result.failure(Exception("Please select or specify your bank name."))
            }
            if (accountHolderName.isBlank()) {
                return@withContext Result.failure(Exception("Please enter the bank account holder's full name."))
            }
            if (accountNumber.isBlank()) {
                return@withContext Result.failure(Exception("Please enter the bank account number."))
            }

            val uid = currentUserId.value
            val user = userDao.getUserById(uid) ?: return@withContext Result.failure(Exception("User not found"))

            if (user.balance < amount) {
                return@withContext Result.failure(
                    Exception("Insufficient available balance. Available: ${user.balance.toInt()} ETB, requested: ${amount.toInt()} ETB.")
                )
            }

            // Transactionally reserve the funds from user balance to prevent duplicate withdrawals of same funds
            val newBal = user.balance - amount
            userDao.updateBalance(uid, newBal)
            if (uid == currentUserId.value) {
                _userSession.value = _userSession.value.copy(balance = newBal)
            }

            val tx = TransactionEntity(
                id = "TX-${System.currentTimeMillis() % 1000000}",
                userId = uid,
                type = "Withdrawal",
                amount = amount,
                status = "Pending", // Status set to Pending
                method = bankName.trim(),
                ftNumber = "PENDING-PAYOUT",
                bankName = bankName.trim(),
                accountHolderName = accountHolderName.trim(),
                accountNumber = accountNumber.trim(),
                timestamp = System.currentTimeMillis(),
                details = "Pending admin verification for $bankName account $accountNumber"
            )

            transactionDao.insertTransaction(tx)
            Result.success(Unit)
        }
    }

    // ==========================================
    // TASK EXECUTION (TAKE -> CONTINUE)
    // ==========================================

    fun canExecuteTask(): Pair<Boolean, String?> {
        val session = _userSession.value
        val tier = getVipTier(session.level)
        val maxDaily = if (session.level.equals("U1", ignoreCase = true)) 3 else tier.dailyTasks

        if (session.todayTasksCompleted >= maxDaily) {
            return Pair(false, "Today's daily task limit ($maxDaily) has been completed.")
        }
        return Pair(true, null)
    }

    fun takeTask(taskId: String): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            val uid = currentUserId.value
            val (canRun, reason) = canExecuteTask()
            if (!canRun) {
                return@withContext Result.failure(Exception(reason ?: "Task limit reached"))
            }

            taskDao.updateTaskStatus(uid, taskId, "TAKEN", System.currentTimeMillis())
            Result.success(Unit)
        }
    }

    fun completeTask(taskId: String): Result<Double> = runBlocking {
        withContext(Dispatchers.IO) {
            val uid = currentUserId.value
            val (canRun, reason) = canExecuteTask()
            if (!canRun) {
                return@withContext Result.failure(Exception(reason ?: "Task limit reached"))
            }

            val user = userDao.getUserById(uid) ?: return@withContext Result.failure(Exception("User not found"))
            val task = taskDao.getTask(uid, taskId) ?: return@withContext Result.failure(Exception("Task not found"))

            if (task.status == "COMPLETED") {
                return@withContext Result.failure(Exception("Task has already been completed."))
            }

            val earned = task.reward
            taskDao.completeTask(uid, taskId, "COMPLETED", System.currentTimeMillis())

            val updatedUser = user.copy(
                balance = user.balance + earned,
                todayTasksCompleted = user.todayTasksCompleted + 1,
                todayEarnings = user.todayEarnings + earned,
                totalEarnings = user.totalEarnings + earned
            )
            userDao.updateUser(updatedUser)

            transactionDao.insertTransaction(
                TransactionEntity(
                    id = "TX-${System.currentTimeMillis() % 1000000}",
                    userId = uid,
                    type = "Task Earning",
                    amount = earned,
                    status = "Successful",
                    method = "Music Task",
                    ftNumber = "TASK-${task.taskId}",
                    timestamp = System.currentTimeMillis(),
                    completedTimestamp = System.currentTimeMillis(),
                    details = task.title
                )
            )

            Result.success(earned)
        }
    }

    fun claimGiftCode(code: String): Result<Double> = runBlocking {
        withContext(Dispatchers.IO) {
            val uid = currentUserId.value
            val user = userDao.getUserById(uid) ?: return@withContext Result.failure(Exception("User not found"))

            if (user.giftCodeClaimed) {
                return@withContext Result.failure(Exception("Gift code has already been redeemed on this account."))
            }

            if (code.trim().uppercase() != "UMG2026") {
                return@withContext Result.failure(Exception("Invalid gift code. Please enter a valid redemption code."))
            }

            val bonus = 500.0
            val updatedUser = user.copy(
                balance = user.balance + bonus,
                giftCodeClaimed = true,
                totalEarnings = user.totalEarnings + bonus
            )
            userDao.updateUser(updatedUser)

            transactionDao.insertTransaction(
                TransactionEntity(
                    id = "TX-${System.currentTimeMillis() % 1000000}",
                    userId = uid,
                    type = "Gift Code",
                    amount = bonus,
                    status = "Successful",
                    method = "Promo Code",
                    ftNumber = "GIFT-UMG2026",
                    timestamp = System.currentTimeMillis(),
                    completedTimestamp = System.currentTimeMillis(),
                    details = "Exclusive 500 ETB Bonus Redeemed"
                )
            )

            Result.success(bonus)
        }
    }

    // ==========================================
    // ADMIN AUTHENTICATION & OPERATIONS
    // ==========================================

    fun verifyAdminCode(enteredCode: String): Result<Boolean> = runBlocking {
        withContext(Dispatchers.IO) {
            val config = adminConfigDao.getAdminConfig() ?: return@withContext Result.failure(Exception("Admin config uninitialized"))

            val now = System.currentTimeMillis()
            if (now < config.lockoutUntil) {
                val remainingSec = ((config.lockoutUntil - now) / 1000).coerceAtLeast(1)
                return@withContext Result.failure(
                    Exception("Too many failed attempts. Admin access locked. Try again in $remainingSec seconds.")
                )
            }

            val isValid = SecurityUtils.verify(enteredCode.trim(), config.codeSalt, config.codeHash)
            if (!isValid) {
                val newFailed = config.failedAttempts + 1
                val newLockout = if (newFailed >= 5) now + (5 * 60 * 1000L) else 0L
                adminConfigDao.insertOrUpdate(
                    config.copy(
                        failedAttempts = newFailed,
                        lockoutUntil = newLockout,
                        updatedAt = now
                    )
                )
                val remainingAttempts = (5 - newFailed).coerceAtLeast(0)
                val msg = if (newLockout > 0) {
                    "Security Lockout: 5 failed attempts reached. Locked for 5 minutes."
                } else {
                    "Invalid administrator access code. $remainingAttempts attempt(s) remaining."
                }
                return@withContext Result.failure(Exception(msg))
            }

            // Success: reset failed attempts
            adminConfigDao.insertOrUpdate(
                config.copy(failedAttempts = 0, lockoutUntil = 0L, updatedAt = now)
            )
            Result.success(true)
        }
    }

    fun changeAdminCode(currentCode: String, newCode: String): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            if (newCode.length < 4) {
                return@withContext Result.failure(Exception("New access code must be at least 4 digits."))
            }
            val verifyRes = verifyAdminCode(currentCode)
            if (verifyRes.isFailure) {
                return@withContext Result.failure(verifyRes.exceptionOrNull() ?: Exception("Verification failed"))
            }

            val salt = SecurityUtils.generateSalt()
            val newHash = SecurityUtils.hashWithSalt(newCode.trim(), salt)
            adminConfigDao.insertOrUpdate(
                AdminConfigEntity(
                    id = 1,
                    codeHash = newHash,
                    codeSalt = salt,
                    failedAttempts = 0,
                    lockoutUntil = 0L,
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        }
    }

    fun approveRecharge(txId: String, adminId: String = "ADMIN-1954"): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            val tx = transactionDao.getTransactionById(txId) ?: return@withContext Result.failure(Exception("Transaction not found"))
            if (tx.status != "Pending") {
                return@withContext Result.failure(Exception("Transaction is already in status '${tx.status}'."))
            }

            val user = userDao.getUserById(tx.userId) ?: return@withContext Result.failure(Exception("User not found"))

            // Idempotent balance credit
            val newBal = user.balance + tx.amount
            userDao.updateBalance(user.userId, newBal)
            if (user.userId == currentUserId.value) {
                _userSession.value = _userSession.value.copy(balance = newBal)
            }
            transactionDao.updateStatus(
                id = tx.id,
                status = "Approved",
                adminId = adminId,
                reason = "Verified & Approved by Admin",
                timestamp = System.currentTimeMillis()
            )

            Result.success(Unit)
        }
    }

    fun rejectRecharge(txId: String, adminId: String = "ADMIN-1954", reason: String): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            val tx = transactionDao.getTransactionById(txId) ?: return@withContext Result.failure(Exception("Transaction not found"))
            if (tx.status != "Pending") {
                return@withContext Result.failure(Exception("Transaction is already in status '${tx.status}'."))
            }

            transactionDao.updateStatus(
                id = tx.id,
                status = "Rejected",
                adminId = adminId,
                reason = reason.ifBlank { "Recharge rejected by admin" },
                timestamp = System.currentTimeMillis()
            )
            Result.success(Unit)
        }
    }

    fun approveWithdrawal(txId: String, adminId: String = "ADMIN-1954", paymentRef: String): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            val tx = transactionDao.getTransactionById(txId) ?: return@withContext Result.failure(Exception("Transaction not found"))
            if (tx.status != "Pending") {
                return@withContext Result.failure(Exception("Withdrawal is already in status '${tx.status}'."))
            }
            if (paymentRef.isBlank()) {
                return@withContext Result.failure(Exception("Please enter the bank payment confirmation reference."))
            }

            transactionDao.updateStatus(
                id = tx.id,
                status = "Successful",
                adminId = adminId,
                reason = "Bank payout executed • Ref: $paymentRef",
                timestamp = System.currentTimeMillis()
            )
            Result.success(Unit)
        }
    }

    fun rejectWithdrawal(txId: String, adminId: String = "ADMIN-1954", reason: String): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            val tx = transactionDao.getTransactionById(txId) ?: return@withContext Result.failure(Exception("Transaction not found"))
            if (tx.status != "Pending") {
                return@withContext Result.failure(Exception("Withdrawal is already in status '${tx.status}'."))
            }

            val user = userDao.getUserById(tx.userId) ?: return@withContext Result.failure(Exception("User not found"))

            // Refund reserved funds back to user
            val newBal = user.balance + tx.amount
            userDao.updateBalance(user.userId, newBal)
            if (user.userId == currentUserId.value) {
                _userSession.value = _userSession.value.copy(balance = newBal)
            }

            transactionDao.updateStatus(
                id = tx.id,
                status = "Rejected",
                adminId = adminId,
                reason = reason.ifBlank { "Withdrawal rejected by administrator. Funds refunded." },
                timestamp = System.currentTimeMillis()
            )
            Result.success(Unit)
        }
    }

    fun adjustUserBalance(
        adminId: String = "ADMIN-1954",
        targetUserId: String,
        amount: Double,
        isIncrease: Boolean,
        reason: String
    ): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            if (amount <= 0) {
                return@withContext Result.failure(Exception("Adjustment amount must be greater than 0."))
            }
            if (reason.isBlank()) {
                return@withContext Result.failure(Exception("A valid reason is required for balance adjustment."))
            }

            val user = userDao.getUserById(targetUserId) ?: return@withContext Result.failure(Exception("User $targetUserId not found."))

            val prevBalance = user.balance
            val newBalance = if (isIncrease) prevBalance + amount else prevBalance - amount
            if (newBalance < 0) {
                return@withContext Result.failure(
                    Exception("Cannot decrease balance below 0. Current balance is ${prevBalance.toInt()} ETB.")
                )
            }

            // Update user balance
            userDao.updateBalance(targetUserId, newBalance)
            if (targetUserId == currentUserId.value) {
                _userSession.value = _userSession.value.copy(balance = newBalance)
            }

            // Record audit log in balance_adjustments
            val adjId = "ADJ-${System.currentTimeMillis() % 1000000}"
            balanceAdjustmentDao.insertAdjustment(
                BalanceAdjustmentEntity(
                    id = adjId,
                    adminId = adminId,
                    userId = targetUserId,
                    adjustmentType = if (isIncrease) "INCREASE" else "DECREASE",
                    amount = amount,
                    reason = reason.trim(),
                    previousBalance = prevBalance,
                    newBalance = newBalance,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Also record in transactions
            transactionDao.insertTransaction(
                TransactionEntity(
                    id = "TX-${System.currentTimeMillis() % 1000000}",
                    userId = targetUserId,
                    type = "Balance Adjustment",
                    amount = amount,
                    status = "Successful",
                    method = "Admin Adjustment",
                    ftNumber = adjId,
                    adminId = adminId,
                    adminReason = reason.trim(),
                    timestamp = System.currentTimeMillis(),
                    completedTimestamp = System.currentTimeMillis(),
                    details = "Admin ${if (isIncrease) "Credited" else "Debited"} ${amount.toInt()} ETB • Reason: $reason"
                )
            )

            Result.success(Unit)
        }
    }

    fun setUserStatus(adminId: String = "ADMIN-1954", targetUserId: String, isActive: Boolean): Result<Unit> = runBlocking {
        withContext(Dispatchers.IO) {
            val user = userDao.getUserById(targetUserId) ?: return@withContext Result.failure(Exception("User not found"))
            val newStatus = if (isActive) "ACTIVE" else "INACTIVE"
            userDao.updateStatus(targetUserId, newStatus)
            Result.success(Unit)
        }
    }

    private fun getStartOfDayTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun TransactionEntity.toModel(): TransactionRecord {
        return TransactionRecord(
            id = id,
            userId = userId,
            type = type,
            amount = amount,
            status = status,
            method = method,
            ftNumber = ftNumber,
            bankName = bankName,
            accountHolderName = accountHolderName,
            accountNumber = accountNumber,
            screenshotUri = screenshotUri,
            adminId = adminId,
            adminReason = adminReason,
            timestamp = timestamp,
            completedTimestamp = completedTimestamp,
            details = details
        )
    }
}
