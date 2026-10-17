package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.data.UmgRepository
import com.example.ui.components.AppHeader
import com.example.ui.components.BottomNavBar
import com.example.ui.components.NavScreen
import com.example.ui.modals.GiftCodeDialog
import com.example.ui.modals.InvestmentDialog
import com.example.ui.modals.InvitationDialog
import com.example.ui.modals.LoginDialog
import com.example.ui.modals.RechargeDialog
import com.example.ui.modals.ServiceDialog
import com.example.ui.modals.WithdrawDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MessageScreen
import com.example.ui.screens.TaskScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PureBlack

class MainActivity : ComponentActivity() {

    private lateinit var repository: UmgRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = UmgRepository(applicationContext)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                UmgApp(repository = repository)
            }
        }
    }
}

@Composable
fun UmgApp(repository: UmgRepository) {
    val userSession by repository.userSession.collectAsState()
    val transactions by repository.transactions.collectAsState()
    val tasks by repository.tasks.collectAsState()

    // Admin Real-Time Database State
    val adminStats by repository.adminStats.collectAsState()
    val allUsers by repository.allUsers.collectAsState()
    val allRecharges by repository.allRecharges.collectAsState()
    val allWithdrawals by repository.allWithdrawals.collectAsState()
    val balanceAdjustments by repository.balanceAdjustments.collectAsState()

    var currentScreen by remember { mutableStateOf(NavScreen.HOME) }
    var showWalletScreen by remember { mutableStateOf(false) }
    var showAdminDashboard by remember { mutableStateOf(false) }

    // Dialogs state
    var showRechargeDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showInvitationDialog by remember { mutableStateOf(false) }
    var showGiftCodeDialog by remember { mutableStateOf(false) }
    var showServiceDialog by remember { mutableStateOf(false) }
    var showInvestmentDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    // Outer container: pure black, center-aligned, mobile-first container capped at 414dp max-width
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
            .testTag("app_root_container"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 414.dp)
                .fillMaxWidth()
                .background(PureBlack)
        ) {
            if (showAdminDashboard) {
                // Real-Time Administrator Control Hub
                AdminDashboardScreen(
                    stats = adminStats,
                    users = allUsers,
                    recharges = allRecharges,
                    withdrawals = allWithdrawals,
                    adjustments = balanceAdjustments,
                    onClose = { showAdminDashboard = false },
                    onApproveRecharge = { txId -> repository.approveRecharge(txId) },
                    onRejectRecharge = { txId, reason -> repository.rejectRecharge(txId, reason = reason) },
                    onApproveWithdrawal = { txId, ref -> repository.approveWithdrawal(txId, paymentRef = ref) },
                    onRejectWithdrawal = { txId, reason -> repository.rejectWithdrawal(txId, reason = reason) },
                    onAdjustBalance = { userId, amount, isInc, reason ->
                        repository.adjustUserBalance(
                            targetUserId = userId,
                            amount = amount,
                            isIncrease = isInc,
                            reason = reason
                        )
                    },
                    onToggleUserStatus = { userId, isActive -> repository.setUserStatus(targetUserId = userId, isActive = isActive) },
                    onChangeAdminCode = { oldCode, newCode -> repository.changeAdminCode(oldCode, newCode) },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (showWalletScreen) {
                // Wallet Page / Screen
                WalletScreen(
                    userSession = userSession,
                    transactions = transactions,
                    onBack = { showWalletScreen = false },
                    onRechargeClick = { showRechargeDialog = true },
                    onWithdrawClick = { showWithdrawDialog = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Top Header: Branding UMG Level: U1 with gold globe icon & top-right 'My Wallet' button
                AppHeader(
                    currentLevel = userSession.level,
                    onWalletClick = { showWalletScreen = true },
                    modifier = Modifier.fillMaxWidth()
                )

                // Main Content Screen Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            NavScreen.HOME -> {
                                HomeScreen(
                                    userSession = userSession,
                                    onNavigate = { currentScreen = it },
                                    onRechargeClick = { showRechargeDialog = true },
                                    onWithdrawClick = { showWithdrawDialog = true },
                                    onInviteClick = { showInvitationDialog = true },
                                    onGiftCodeClick = { showGiftCodeDialog = true },
                                    onServiceClick = { showServiceDialog = true },
                                    onInvestmentClick = { showInvestmentDialog = true },
                                    onUpgradeLevel = { repository.upgradeVipLevel(it) }
                                )
                            }
                            NavScreen.TASK -> {
                                TaskScreen(
                                    userSession = userSession,
                                    tasks = tasks,
                                    onTakeTask = { repository.takeTask(it) },
                                    onCompleteTask = { repository.completeTask(it) },
                                    canExecuteTask = { repository.canExecuteTask() }
                                )
                            }
                            NavScreen.MESSAGE -> {
                                MessageScreen(
                                    userSession = userSession,
                                    onServiceClick = { showServiceDialog = true }
                                )
                            }
                        }
                    }
                }

                // Sticky Bottom Navigation Bar (Home, Task, Message - Schedule removed)
                BottomNavBar(
                    currentScreen = currentScreen,
                    onSelectScreen = { currentScreen = it }
                )
            }
        }
    }

    // Modals
    if (showRechargeDialog) {
        RechargeDialog(
            userSession = userSession,
            onDismiss = { showRechargeDialog = false },
            onSubmitRecharge = { amount, method, ft, uri ->
                repository.submitRecharge(amount, method, ft, uri)
            }
        )
    }

    if (showWithdrawDialog) {
        WithdrawDialog(
            userSession = userSession,
            onDismiss = { showWithdrawDialog = false },
            onSubmitWithdrawal = { amount, bankName, holder, account ->
                repository.submitWithdrawal(amount, bankName, holder, account)
            }
        )
    }

    if (showInvitationDialog) {
        InvitationDialog(
            invitationCode = userSession.invitationCode,
            onDismiss = { showInvitationDialog = false }
        )
    }

    if (showGiftCodeDialog) {
        GiftCodeDialog(
            onDismiss = { showGiftCodeDialog = false },
            onClaimGiftCode = { repository.claimGiftCode(it) },
            onVerifyAdminCode = { repository.verifyAdminCode(it) },
            onOpenAdminDashboard = { showAdminDashboard = true }
        )
    }

    if (showServiceDialog) {
        ServiceDialog(
            onDismiss = { showServiceDialog = false }
        )
    }

    if (showInvestmentDialog) {
        InvestmentDialog(
            currentLevel = userSession.level,
            onDismiss = { showInvestmentDialog = false }
        )
    }

    if (showLoginDialog || !userSession.isLoggedIn) {
        LoginDialog(
            initialPhone = userSession.phoneNumber,
            currentLevel = userSession.level,
            onDismiss = if (userSession.isLoggedIn) { { showLoginDialog = false } } else null,
            onLogin = { phone, pass, level ->
                val res = repository.login(phone, pass, level)
                if (res.isSuccess) {
                    showLoginDialog = false
                }
                res
            }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("UMG")
    }
}
