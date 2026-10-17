package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.UserSession
import com.example.data.VIP_TIERS
import com.example.data.VipTier
import com.example.data.getVipLevelRank
import com.example.ui.components.NavScreen
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DarkSlateCard
import com.example.ui.theme.DarkSlateElevated
import com.example.ui.theme.DeepBlueGradient
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldGradient
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldSubtle
import com.example.ui.theme.PureBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

data class FeatureItem(
    val title: String,
    val icon: ImageVector,
    val actionTag: String
)

@Composable
fun HomeScreen(
    userSession: UserSession,
    onNavigate: (NavScreen) -> Unit,
    onRechargeClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onInviteClick: () -> Unit,
    onGiftCodeClick: () -> Unit,
    onServiceClick: () -> Unit,
    onInvestmentClick: () -> Unit,
    onUpgradeLevel: (String) -> Result<Unit> = { Result.success(Unit) },
    modifier: Modifier = Modifier
) {
    var selectedTierForUpgrade by remember { mutableStateOf<VipTier?>(null) }
    var upgradeError by remember { mutableStateOf<String?>(null) }

    val livePayouts = remember {
        listOf(
            "Member 09****4813 received 1850 ETB",
            "Member 09****1290 received 2400 ETB",
            "Member 07****8821 received 950 ETB",
            "Member 09****3342 received 3100 ETB",
            "Member 09****7601 received 1200 ETB",
            "Member 09****9518 received 4500 ETB"
        )
    }

    var tickerIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            tickerIndex = (tickerIndex + 1) % livePayouts.size
        }
    }

    val pagerState = rememberPagerState(pageCount = { 3 })
    LaunchedEffect(Unit) {
        while (true) {
            delay(4500)
            val next = (pagerState.currentPage + 1) % 3
            pagerState.animateScrollToPage(next)
        }
    }

    // 8-item feature grid with gold icons:
    // Recharge, Withdraw, Invitation, Investment, Gift Code, Message, Service, Task
    val features = remember {
        listOf(
            FeatureItem("Recharge", Icons.Default.Payments, "feature_recharge"),
            FeatureItem("Withdraw", Icons.Default.AccountBalanceWallet, "feature_withdraw"),
            FeatureItem("Invitation", Icons.Default.GroupAdd, "feature_invitation"),
            FeatureItem("Investment", Icons.Default.TrendingUp, "feature_investment"),
            FeatureItem("Gift Code", Icons.Default.CardGiftcard, "feature_gift_code"),
            FeatureItem("Message", Icons.Default.Message, "feature_message"),
            FeatureItem("Service", Icons.Default.HeadsetMic, "feature_service"),
            FeatureItem("Task", Icons.Default.Star, "feature_task")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Image Slider / Carousel
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> {
                                Image(
                                    painter = painterResource(id = R.drawable.umg_banner_invest_1789538545815),
                                    contentDescription = "Investment Banner",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            1 -> {
                                Image(
                                    painter = painterResource(id = R.drawable.umg_banner_vip_1789538564328),
                                    contentDescription = "VIP Tiers Banner",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(DeepBlueGradient)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Column {
                                        Text(
                                            text = "UMG Global Capital",
                                            color = GoldLight,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Daily automated revenue sharing platform with guaranteed ETB payouts",
                                            color = TextPrimary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Pager Indicators
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(3) { i ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == i) 18.dp else 6.dp, 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (pagerState.currentPage == i) GoldPrimary else Color.White.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }

        // 2. Animated Ticker Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSlateCard)
                    .border(1.dp, GoldPrimary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Payout Notice",
                        tint = GoldLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = livePayouts[tickerIndex],
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 3. 8-Item Feature Grid with Gold Icons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Quick Services",
                    color = GoldLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // 2 rows of 4 items
                for (rowIndex in 0..1) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (colIndex in 0..3) {
                            val index = rowIndex * 4 + colIndex
                            if (index < features.size) {
                                val feat = features[index]
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            when (feat.actionTag) {
                                                "feature_recharge" -> onRechargeClick()
                                                "feature_withdraw" -> onWithdrawClick()
                                                "feature_invitation" -> onInviteClick()
                                                "feature_investment" -> onInvestmentClick()
                                                "feature_task" -> onNavigate(NavScreen.TASK)
                                                "feature_gift_code" -> onGiftCodeClick()
                                                "feature_message" -> onNavigate(NavScreen.MESSAGE)
                                                "feature_service" -> onServiceClick()
                                            }
                                        }
                                        .padding(vertical = 8.dp)
                                        .testTag(feat.actionTag)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(DarkSlateElevated)
                                            .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = feat.icon,
                                            contentDescription = feat.title,
                                            tint = GoldLight,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = feat.title,
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 4. VIP Level Membership Cards Section (U1 to U5)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VIP Membership Tiers",
                        color = GoldLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Active: ${userSession.level}",
                        color = EmeraldGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        items(VIP_TIERS) { tier ->
            VipTierCard(
                tier = tier,
                currentLevel = userSession.level,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }

    // Level Upgrade Dialog
    val targetTier = selectedTierForUpgrade
    if (targetTier != null) {
        val hasEnoughBalance = userSession.balance >= targetTier.investmentPrice

        Dialog(onDismissRequest = { selectedTierForUpgrade = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("upgrade_dialog"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSlate),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VIP Level Upgrade",
                        color = GoldLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Upgrade to ${targetTier.name}",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSlateCard)
                            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Required Investment:", color = TextSecondary, fontSize = 13.sp)
                                Text("${targetTier.investmentPrice.toInt()} ETB", color = GoldPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Your Balance:", color = TextSecondary, fontSize = 13.sp)
                                Text(
                                    "${userSession.balance.toInt()} ETB",
                                    color = if (hasEnoughBalance) EmeraldGreen else Color(0xFFFF5252),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Daily Earnings:", color = TextSecondary, fontSize = 13.sp)
                                Text("${targetTier.dailyIncome.toInt()} ETB/day", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "⚠️ Notice: Once upgraded to ${targetTier.id}, downgrading to a lower level is strictly not allowed.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    val currentError = upgradeError
                    if (currentError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentError,
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (hasEnoughBalance) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { selectedTierForUpgrade = null },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                            ) {
                                Text("Cancel", fontSize = 13.sp)
                            }
                            Button(
                                onClick = {
                                    val res = onUpgradeLevel(targetTier.id)
                                    if (res.isSuccess) {
                                        selectedTierForUpgrade = null
                                    } else {
                                        upgradeError = res.exceptionOrNull()?.message ?: "Upgrade failed"
                                    }
                                },
                                modifier = Modifier.weight(1f).height(44.dp).testTag("confirm_upgrade_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                            ) {
                                Text("Confirm Upgrade", color = PureBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Insufficient balance. You need ${targetTier.investmentPrice.toInt()} ETB to upgrade.",
                                color = Color(0xFFFF5252),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedTierForUpgrade = null },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                                ) {
                                    Text("Cancel", fontSize = 13.sp)
                                }
                                Button(
                                    onClick = {
                                        selectedTierForUpgrade = null
                                        onRechargeClick()
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp).testTag("upgrade_recharge_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                                ) {
                                    Text("Recharge Now", color = PureBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VipTierCard(
    tier: VipTier,
    currentLevel: String,
    onUpgradeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentRank = getVipLevelRank(currentLevel)
    val tierRank = getVipLevelRank(tier.id)
    val isActive = currentRank == tierRank

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vip_card_${tier.id.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) DarkSlateElevated else DarkSlate
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) GoldPrimary else DarkBorder
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) GoldPrimary else DarkSlateCard)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tier.id,
                            color = if (isActive) PureBlack else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = tier.name,
                        color = if (isActive) GoldLight else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(EmeraldGreen.copy(alpha = 0.2f))
                            .border(1.dp, EmeraldGreen, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            color = EmeraldGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSlateCard)
                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("tier_locked_${tier.id.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Tier Locked",
                            tint = TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Locked",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Daily Tasks", color = TextSecondary, fontSize = 11.sp)
                    Text("${tier.dailyTasks} Tasks/Day", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Per Task Yield", color = TextSecondary, fontSize = 11.sp)
                    Text("${tier.perTaskReward.toInt()} ETB", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Daily Returns", color = TextSecondary, fontSize = 11.sp)
                    Text("${tier.dailyIncome.toInt()} ETB", color = EmeraldGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Withdrawal Fee: ${tier.withdrawalFee.toInt()} ETB",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "Price: ${tier.investmentPrice.toInt()} ETB",
                    color = GoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
