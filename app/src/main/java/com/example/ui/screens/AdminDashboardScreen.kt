package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AdminStats
import com.example.data.BalanceAdjustmentRecord
import com.example.data.TransactionRecord
import com.example.data.db.UserEntity
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DarkSlateCard
import com.example.ui.theme.DarkSlateElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    stats: AdminStats,
    users: List<UserEntity>,
    recharges: List<TransactionRecord>,
    withdrawals: List<TransactionRecord>,
    adjustments: List<BalanceAdjustmentRecord>,
    onClose: () -> Unit,
    onApproveRecharge: (txId: String) -> Result<Unit>,
    onRejectRecharge: (txId: String, reason: String) -> Result<Unit>,
    onApproveWithdrawal: (txId: String, paymentRef: String) -> Result<Unit>,
    onRejectWithdrawal: (txId: String, reason: String) -> Result<Unit>,
    onAdjustBalance: (userId: String, amount: Double, isIncrease: Boolean, reason: String) -> Result<Unit>,
    onToggleUserStatus: (userId: String, isActive: Boolean) -> Result<Unit>,
    onChangeAdminCode: (oldCode: String, newCode: String) -> Result<Unit>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "Users", "Recharges", "Withdrawals", "Audit")

    var selectedUserForDetails by remember { mutableStateOf<UserEntity?>(null) }
    var userForBalanceAdjustment by remember { mutableStateOf<UserEntity?>(null) }
    var showChangeCodeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .testTag("admin_dashboard_screen")
    ) {
        // Admin Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSlate)
                .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp).testTag("admin_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldLight
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Admin Control Hub",
                                color = GoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Live Database Session • Real-Time Stats",
                            color = EmeraldGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showChangeCodeDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Change Code",
                            tint = GoldLight
                        )
                    }
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSlateCard,
            contentColor = GoldLight,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GoldPrimary,
                    height = 2.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == index) GoldPrimary else TextSecondary
                        )
                    }
                )
            }
        }

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> AdminOverviewTab(stats = stats, users = users, recharges = recharges, withdrawals = withdrawals)
                1 -> AdminUsersTab(
                    users = users,
                    onOpenProfile = { selectedUserForDetails = it },
                    onAdjustBalance = { userForBalanceAdjustment = it },
                    onToggleStatus = onToggleUserStatus
                )
                2 -> AdminRechargesTab(
                    recharges = recharges,
                    onApprove = onApproveRecharge,
                    onReject = onRejectRecharge
                )
                3 -> AdminWithdrawalsTab(
                    withdrawals = withdrawals,
                    onApprove = onApproveWithdrawal,
                    onReject = onRejectWithdrawal
                )
                4 -> AdminAuditTab(adjustments = adjustments)
            }
        }
    }

    // Modal: User Details & Profile
    if (selectedUserForDetails != null) {
        val user = selectedUserForDetails!!
        AdminUserProfileDialog(
            user = user,
            onDismiss = { selectedUserForDetails = null },
            onAdjustBalance = {
                selectedUserForDetails = null
                userForBalanceAdjustment = user
            },
            onToggleStatus = { newStatus ->
                onToggleUserStatus(user.userId, newStatus)
                selectedUserForDetails = user.copy(status = if (newStatus) "ACTIVE" else "INACTIVE")
            }
        )
    }

    // Modal: Balance Adjustment with Confirmation
    if (userForBalanceAdjustment != null) {
        AdminBalanceAdjustmentDialog(
            user = userForBalanceAdjustment!!,
            onDismiss = { userForBalanceAdjustment = null },
            onConfirmAdjustment = { amount, isIncrease, reason ->
                val res = onAdjustBalance(userForBalanceAdjustment!!.userId, amount, isIncrease, reason)
                if (res.isSuccess) {
                    userForBalanceAdjustment = null
                }
                res
            }
        )
    }

    // Modal: Change Admin Code
    if (showChangeCodeDialog) {
        AdminChangeCodeDialog(
            onDismiss = { showChangeCodeDialog = false },
            onChangeCode = onChangeAdminCode
        )
    }
}

// ==========================================
// TAB 1: OVERVIEW & REAL-TIME STATISTICS
// ==========================================

@Composable
fun AdminOverviewTab(
    stats: AdminStats,
    users: List<UserEntity>,
    recharges: List<TransactionRecord>,
    withdrawals: List<TransactionRecord>
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }
    val pendingRechargeCount = recharges.count { it.status == "Pending" }
    val pendingWithdrawalCount = withdrawals.count { it.status == "Pending" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Live Update Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldGreen))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Database Live Sync", color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Updated: ${dateFormat.format(Date(stats.lastUpdatedTime))}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        item {
            // 2x2 Grid of User Statistics (Accurate database counts)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Total Registered",
                        value = "${stats.totalUsers}",
                        color = GoldLight,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Active Accounts",
                        value = "${stats.activeUsers}",
                        color = EmeraldGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Inactive Accounts",
                        value = "${stats.inactiveUsers}",
                        color = if (stats.inactiveUsers > 0) Color(0xFFFF5252) else TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Online Users Now",
                        value = "${stats.onlineUsers}",
                        color = EmeraldGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Registered Today",
                        value = "${stats.registeredToday}",
                        color = GoldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total DB Balance",
                        value = "${users.sumOf { it.balance }.toInt()} ETB",
                        color = GoldLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Text("Pending Requests Queue", color = GoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (pendingRechargeCount > 0) GoldPrimary else DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pending Recharges", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$pendingRechargeCount",
                            color = if (pendingRechargeCount > 0) GoldPrimary else TextSecondary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (pendingWithdrawalCount > 0) EmeraldGreen else DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Pending Withdrawals", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$pendingWithdrawalCount",
                            color = if (pendingWithdrawalCount > 0) EmeraldGreen else TextSecondary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// TAB 2: USER MANAGEMENT
// ==========================================

@Composable
fun AdminUsersTab(
    users: List<UserEntity>,
    onOpenProfile: (UserEntity) -> Unit,
    onAdjustBalance: (UserEntity) -> Unit,
    onToggleStatus: (userId: String, isActive: Boolean) -> Result<Unit>
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users else users.filter {
            it.phoneNumber.contains(searchQuery.trim(), ignoreCase = true) ||
            it.userId.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by phone number or ID...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldLight) },
            modifier = Modifier.fillMaxWidth().testTag("admin_user_search"),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkSlateCard,
                unfocusedContainerColor = DarkSlateCard
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Total Registered Users (${filteredUsers.size})",
            color = GoldLight,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredUsers) { user ->
                UserRowCard(
                    user = user,
                    onOpenProfile = { onOpenProfile(user) },
                    onAdjustBalance = { onAdjustBalance(user) }
                )
            }
        }
    }
}

@Composable
fun UserRowCard(
    user: UserEntity,
    onOpenProfile: () -> Unit,
    onAdjustBalance: () -> Unit
) {
    val isActive = user.status == "ACTIVE"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onOpenProfile() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) DarkBorder else Color(0x66FF5252))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DarkSlateElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isActive) GoldLight else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(user.phoneNumber, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(user.userId, color = TextSecondary, fontSize = 10.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActive) EmeraldGreen.copy(alpha = 0.2f) else Color(0x33FF5252))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isActive) "ACTIVE" else "INACTIVE",
                        color = if (isActive) EmeraldGreen else Color(0xFFFF5252),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Balance: ${user.balance.toInt()} ETB", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Tier: ${user.level} • Tasks: ${user.todayTasksCompleted}", color = TextSecondary, fontSize = 11.sp)
                }

                Row {
                    Button(
                        onClick = onAdjustBalance,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSlateElevated),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("± Balance", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = onOpenProfile,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("View", color = PureBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: RECHARGE MANAGEMENT
// ==========================================

@Composable
fun AdminRechargesTab(
    recharges: List<TransactionRecord>,
    onApprove: (txId: String) -> Result<Unit>,
    onReject: (txId: String, reason: String) -> Result<Unit>
) {
    var statusFilter by remember { mutableStateOf("Pending") }
    val filtered = remember(recharges, statusFilter) {
        if (statusFilter == "All") recharges else recharges.filter { it.status == statusFilter }
    }

    var selectedRechargeToReject by remember { mutableStateOf<TransactionRecord?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Pending", "Approved", "Rejected", "All").forEach { filter ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (statusFilter == filter) GoldPrimary else DarkSlateCard)
                        .clickable { statusFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (statusFilter == filter) PureBlack else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No $statusFilter recharges found", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { tx ->
                    RechargeAdminCard(
                        tx = tx,
                        onApprove = { onApprove(tx.id) },
                        onReject = { selectedRechargeToReject = tx }
                    )
                }
            }
        }
    }

    if (selectedRechargeToReject != null) {
        val tx = selectedRechargeToReject!!
        Dialog(onDismissRequest = { selectedRechargeToReject = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlate)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Reject Recharge Request", color = Color(0xFFFF5252), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reason for Rejection:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        placeholder = { Text("e.g. Invalid FT transaction number", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF5252),
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSlateCard,
                            unfocusedContainerColor = DarkSlateCard
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { selectedRechargeToReject = null }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onReject(tx.id, rejectionReason)
                                selectedRechargeToReject = null
                                rejectionReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                        ) {
                            Text("Confirm Reject", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RechargeAdminCard(
    tx: TransactionRecord,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${tx.amount.toInt()} ETB", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (tx.status) {
                                "Approved", "Successful" -> EmeraldGreen.copy(alpha = 0.2f)
                                "Rejected" -> Color(0x33FF5252)
                                else -> Color(0x33FF9800)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tx.status,
                        color = when (tx.status) {
                            "Approved", "Successful" -> EmeraldGreen
                            "Rejected" -> Color(0xFFFF5252)
                            else -> Color(0xFFFF9800)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("User ID: ${tx.userId}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Method: ${tx.method} • Ref/FT: ${tx.ftNumber}", color = TextSecondary, fontSize = 12.sp)
            Text("Time: ${dateFormat.format(Date(tx.timestamp))}", color = TextSecondary, fontSize = 11.sp)
            if (tx.screenshotUri != null) {
                Text("Screenshot: Attached", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }

            if (tx.status == "Pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApprove,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = PureBlack, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve & Credit", color = PureBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 4: WITHDRAWAL MANAGEMENT
// ==========================================

@Composable
fun AdminWithdrawalsTab(
    withdrawals: List<TransactionRecord>,
    onApprove: (txId: String, paymentRef: String) -> Result<Unit>,
    onReject: (txId: String, reason: String) -> Result<Unit>
) {
    var statusFilter by remember { mutableStateOf("Pending") }
    val filtered = remember(withdrawals, statusFilter) {
        if (statusFilter == "All") withdrawals else withdrawals.filter { it.status == statusFilter }
    }

    var selectedWithdrawalToApprove by remember { mutableStateOf<TransactionRecord?>(null) }
    var paymentRefInput by remember { mutableStateOf("") }

    var selectedWithdrawalToReject by remember { mutableStateOf<TransactionRecord?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Pending", "Successful", "Rejected", "All").forEach { filter ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (statusFilter == filter) GoldPrimary else DarkSlateCard)
                        .clickable { statusFilter = filter }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        color = if (statusFilter == filter) PureBlack else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No $statusFilter withdrawals found", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered) { tx ->
                    WithdrawalAdminCard(
                        tx = tx,
                        onApproveClick = {
                            selectedWithdrawalToApprove = tx
                            paymentRefInput = "TX-${System.currentTimeMillis() % 1000000}"
                        },
                        onRejectClick = { selectedWithdrawalToReject = tx }
                    )
                }
            }
        }
    }

    // Approve Dialog
    if (selectedWithdrawalToApprove != null) {
        val tx = selectedWithdrawalToApprove!!
        Dialog(onDismissRequest = { selectedWithdrawalToApprove = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlate)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Confirm Bank Payout Execution", color = EmeraldGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Recipient: ${tx.accountHolderName ?: tx.userId}", color = TextPrimary, fontSize = 13.sp)
                    Text("Bank: ${tx.bankName ?: tx.method}", color = TextSecondary, fontSize = 12.sp)
                    Text("Account: ${tx.accountNumber ?: "N/A"}", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Payout Amount: ${tx.amount.toInt()} ETB", color = GoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Bank Confirmation / FT Reference:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = paymentRefInput,
                        onValueChange = { paymentRefInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSlateCard,
                            unfocusedContainerColor = DarkSlateCard
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { selectedWithdrawalToApprove = null }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onApprove(tx.id, paymentRefInput)
                                selectedWithdrawalToApprove = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                        ) {
                            Text("Mark Successful", color = PureBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Reject Dialog (Refunds Reserved Balance)
    if (selectedWithdrawalToReject != null) {
        val tx = selectedWithdrawalToReject!!
        Dialog(onDismissRequest = { selectedWithdrawalToReject = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlate)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Reject Withdrawal & Refund", color = Color(0xFFFF5252), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Rejecting will restore ${tx.amount.toInt()} ETB back to user's account balance.", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Reason for Rejection:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        placeholder = { Text("e.g. Account name mismatch", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF5252),
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSlateCard,
                            unfocusedContainerColor = DarkSlateCard
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { selectedWithdrawalToReject = null }) {
                            Text("Cancel", color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onReject(tx.id, rejectionReason)
                                selectedWithdrawalToReject = null
                                rejectionReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                        ) {
                            Text("Confirm & Refund", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawalAdminCard(
    tx: TransactionRecord,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${tx.amount.toInt()} ETB", color = GoldPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (tx.status) {
                                "Successful" -> EmeraldGreen.copy(alpha = 0.2f)
                                "Rejected" -> Color(0x33FF5252)
                                else -> Color(0x33FF9800)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tx.status,
                        color = when (tx.status) {
                            "Successful" -> EmeraldGreen
                            "Rejected" -> Color(0xFFFF5252)
                            else -> Color(0xFFFF9800)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("User ID: ${tx.userId}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Bank: ${tx.bankName ?: tx.method}", color = GoldLight, fontSize = 12.sp)
            Text("Holder: ${tx.accountHolderName ?: "N/A"}", color = TextSecondary, fontSize = 12.sp)
            Text("Account No: ${tx.accountNumber ?: "N/A"}", color = TextSecondary, fontSize = 12.sp)
            Text("Date: ${dateFormat.format(Date(tx.timestamp))}", color = TextSecondary, fontSize = 11.sp)

            if (tx.status == "Pending") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onRejectClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Reject & Refund", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApproveClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Approve Payout", color = PureBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 5: AUDIT LOGS
// ==========================================

@Composable
fun AdminAuditTab(adjustments: List<BalanceAdjustmentRecord>) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Balance Adjustment Audit Trail", color = GoldLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Immutable record of all admin financial adjustments", color = TextSecondary, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))

        if (adjustments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No manual balance adjustments recorded", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(adjustments) { adj ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${if (adj.adjustmentType == "INCREASE") "+" else "-"}${adj.amount.toInt()} ETB",
                                    color = if (adj.adjustmentType == "INCREASE") EmeraldGreen else Color(0xFFFF5252),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dateFormat.format(Date(adj.timestamp)),
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("User: ${adj.userId} • Admin: ${adj.adminId}", color = TextPrimary, fontSize = 12.sp)
                            Text("Balance: ${adj.previousBalance.toInt()} ETB ➔ ${adj.newBalance.toInt()} ETB", color = GoldLight, fontSize = 12.sp)
                            Text("Reason: ${adj.reason}", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// MODAL: USER PROFILE & ACTIONS
// ==========================================

@Composable
fun AdminUserProfileDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onAdjustBalance: () -> Unit,
    onToggleStatus: (Boolean) -> Unit
) {
    val isActive = user.status == "ACTIVE"
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlate),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("User Profile Details", color = GoldLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Phone: ${user.phoneNumber}", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("User ID: ${user.userId}", color = TextSecondary, fontSize = 12.sp)
                Text("VIP Level: ${user.level}", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Current Balance: ${user.balance.toInt()} ETB", color = EmeraldGreen, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Account Status: ${user.status}", color = if (isActive) EmeraldGreen else Color(0xFFFF5252), fontSize = 13.sp)
                Text("Registered: ${dateFormat.format(Date(user.registeredTimestamp))}", color = TextSecondary, fontSize = 11.sp)
                Text("Last Active: ${dateFormat.format(Date(user.lastActiveTimestamp))}", color = TextSecondary, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onToggleStatus(!isActive) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color(0x33FF5252) else EmeraldGreen
                        )
                    ) {
                        Text(
                            text = if (isActive) "Deactivate" else "Activate",
                            color = if (isActive) Color(0xFFFF5252) else PureBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onAdjustBalance,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("± Adjust Balance", color = PureBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// MODAL: BALANCE ADJUSTMENT WITH CONFIRMATION
// ==========================================

@Composable
fun AdminBalanceAdjustmentDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onConfirmAdjustment: (amount: Double, isIncrease: Boolean, reason: String) -> Result<Unit>
) {
    var isIncrease by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf("") }
    var reasonText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val targetBalance = if (isIncrease) user.balance + amount else user.balance - amount

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlate),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (showConfirmation) "Confirm Balance Change" else "Adjust User Balance",
                    color = GoldLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Target User: ${user.phoneNumber} (${user.userId})", color = TextSecondary, fontSize = 12.sp)
                Text("Current Balance: ${user.balance.toInt()} ETB", color = GoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(14.dp))

                if (!showConfirmation) {
                    // Type selector
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isIncrease) EmeraldGreen else DarkSlateCard)
                                .clickable { isIncrease = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Increase (+)", color = if (isIncrease) PureBlack else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isIncrease) Color(0xFFFF5252) else DarkSlateCard)
                                .clickable { isIncrease = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Decrease (-)", color = if (!isIncrease) Color.White else TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Adjustment Amount (ETB)", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        modifier = Modifier.fillMaxWidth().testTag("admin_adjust_amount_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSlateCard,
                            unfocusedContainerColor = DarkSlateCard
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Reason for Adjustment (Required for audit)", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        placeholder = { Text("e.g. Compensation / Manual deposit credit", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth().testTag("admin_adjust_reason_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSlateCard,
                            unfocusedContainerColor = DarkSlateCard
                        )
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage ?: "", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (amount <= 0) {
                                    errorMessage = "Please enter an amount greater than 0"
                                    return@Button
                                }
                                if (!isIncrease && targetBalance < 0) {
                                    errorMessage = "Cannot decrease balance below 0. Maximum deduction is ${user.balance.toInt()} ETB."
                                    return@Button
                                }
                                if (reasonText.isBlank()) {
                                    errorMessage = "Please enter a mandatory reason for this adjustment"
                                    return@Button
                                }
                                errorMessage = null
                                showConfirmation = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("Next", color = PureBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Confirmation View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSlateCard)
                            .padding(14.dp)
                    ) {
                        Column {
                            Text("Action: ${if (isIncrease) "CREDIT (+)" else "DEBIT (-)"} ${amount.toInt()} ETB", color = if (isIncrease) EmeraldGreen else Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Previous Balance: ${user.balance.toInt()} ETB", color = TextSecondary, fontSize = 12.sp)
                            Text("New Balance: ${targetBalance.toInt()} ETB", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Reason: $reasonText", color = TextPrimary, fontSize = 12.sp)
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMessage ?: "", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { showConfirmation = false }) { Text("Back", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val res = onConfirmAdjustment(amount, isIncrease, reasonText)
                                if (res.isFailure) {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Adjustment failed"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIncrease) EmeraldGreen else Color(0xFFFF5252)
                            )
                        ) {
                            Text("Confirm & Apply", color = PureBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// MODAL: CHANGE ADMIN CODE
// ==========================================

@Composable
fun AdminChangeCodeDialog(
    onDismiss: () -> Unit,
    onChangeCode: (oldCode: String, newCode: String) -> Result<Unit>
) {
    var oldCode by remember { mutableStateOf("") }
    var newCode by remember { mutableStateOf("") }
    var confirmNewCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlate),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Change Admin Access Code", color = GoldLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                if (successMessage != null) {
                    Text(successMessage ?: "", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)) {
                        Text("Done", color = PureBlack)
                    }
                } else {
                    OutlinedTextField(
                        value = oldCode,
                        onValueChange = { oldCode = it },
                        placeholder = { Text("Current Admin Code (default 1954)", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSlateCard,
                            unfocusedContainerColor = DarkSlateCard
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newCode,
                        onValueChange = { newCode = it },
                        placeholder = { Text("New Admin Code", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSlateCard,
                            unfocusedContainerColor = DarkSlateCard
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmNewCode,
                        onValueChange = { confirmNewCode = it },
                        placeholder = { Text("Confirm New Admin Code", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSlateCard,
                            unfocusedContainerColor = DarkSlateCard
                        )
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage ?: "", color = Color(0xFFFF5252), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newCode != confirmNewCode) {
                                    errorMessage = "New codes do not match."
                                    return@Button
                                }
                                val res = onChangeCode(oldCode, newCode)
                                if (res.isSuccess) {
                                    successMessage = "Admin access code updated successfully."
                                    errorMessage = null
                                } else {
                                    errorMessage = res.exceptionOrNull()?.message ?: "Failed to change code."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Text("Update Code", color = PureBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
