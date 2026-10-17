package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.TaskItem
import com.example.data.UserSession
import com.example.data.getVipTier
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
import com.example.util.UmgMusicPlayer
import kotlinx.coroutines.delay

@Composable
fun TaskScreen(
    userSession: UserSession,
    tasks: List<TaskItem>,
    onTakeTask: (taskId: String) -> Result<Unit>,
    onCompleteTask: (taskId: String) -> Result<Double>,
    canExecuteTask: () -> Pair<Boolean, String?>,
    modifier: Modifier = Modifier
) {
    val tier = getVipTier(userSession.level)
    val maxDaily = if (userSession.level.equals("U1", ignoreCase = true)) 3 else tier.dailyTasks
    val completedCount = userSession.todayTasksCompleted
    val remainingTasks = (maxDaily - completedCount).coerceAtLeast(0)

    var activeTaskId by remember { mutableStateOf<String?>(null) }
    var taskNoticeMessage by remember { mutableStateOf<String?>(null) }

    val isLimitReached = completedCount >= maxDaily
    val isMusicPlaying by UmgMusicPlayer.isPlaying.collectAsState()

    // Stop music if screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            UmgMusicPlayer.stop()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Top Task Metrics Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSlate)
                .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music Tasks",
                        tint = GoldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Music Task Center",
                        color = GoldLight,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Tier: ${userSession.level}",
                    color = EmeraldGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Grid: 3 columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Remaining Tasks
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSlateCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Remaining", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$remainingTasks",
                        color = GoldLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("remaining_tasks_metric")
                    )
                }

                // Completed Tasks
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSlateCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Completed", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completedCount/$maxDaily",
                        color = EmeraldGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("completed_tasks_metric")
                    )
                }

                // Earned Amount per task
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSlateCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Per Task", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${tier.perTaskReward.toInt()} ETB",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("earned_per_task_metric")
                    )
                }
            }
        }

        // Live Audio Playback Banner if music is running
        if (isMusicPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSlateElevated)
                    .border(1.dp, EmeraldGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("active_music_status_banner")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedSoundWave(isPlaying = true)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Music is currently playing inside the application.",
                                color = EmeraldGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "432Hz Ambient Focus Track",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { UmgMusicPlayer.toggle() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isMusicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Music",
                            tint = GoldLight
                        )
                    }
                }
            }
        }

        // Daily Limit Banner if reached
        if (isLimitReached || taskNoticeMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FF9800))
                    .border(1.dp, Color(0xFFFF9800), RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag("task_completed_notice_banner")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Notice",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = taskNoticeMessage ?: "Today's daily task limit reached ($maxDaily tasks completed).",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Task List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp)
        ) {
            items(tasks) { task ->
                TaskCardItem(
                    task = task,
                    canRun = !isLimitReached,
                    onTakeTask = {
                        val (canRunCheck, errorReason) = canExecuteTask()
                        if (!canRunCheck) {
                            taskNoticeMessage = errorReason
                        } else {
                            val res = onTakeTask(task.id)
                            if (res.isFailure) {
                                taskNoticeMessage = res.exceptionOrNull()?.message
                            }
                        }
                    },
                    onContinueTask = {
                        val (canRunCheck, errorReason) = canExecuteTask()
                        if (!canRunCheck) {
                            taskNoticeMessage = errorReason
                        } else {
                            // Start genuine native music playback!
                            UmgMusicPlayer.start()
                            activeTaskId = task.id
                        }
                    }
                )
            }
        }
    }

    // Modal when user taps "Continue"
    if (activeTaskId != null) {
        val currentTask = tasks.find { it.id == activeTaskId }
        if (currentTask != null) {
            TaskMusicDialog(
                task = currentTask,
                isMusicPlaying = isMusicPlaying,
                onToggleMusic = { UmgMusicPlayer.toggle() },
                onDismiss = {
                    UmgMusicPlayer.stop()
                    activeTaskId = null
                },
                onFinished = {
                    UmgMusicPlayer.stop()
                    val res = onCompleteTask(currentTask.id)
                    activeTaskId = null
                    if (!res.isSuccess) {
                        taskNoticeMessage = res.exceptionOrNull()?.message
                    }
                }
            )
        }
    }
}

@Composable
fun TaskCardItem(
    task: TaskItem,
    canRun: Boolean,
    onTakeTask: () -> Unit,
    onContinueTask: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSlateCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                task.isCompleted -> EmeraldGreen.copy(alpha = 0.5f)
                task.isTaken -> GoldPrimary
                else -> DarkBorder
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSlateElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.category,
                            color = GoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+${task.reward.toInt()} ETB",
                        color = EmeraldGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = task.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (task.isTaken && !task.isCompleted) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "● Taken • Tap Continue to play music & verify",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            when {
                task.isCompleted -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreen.copy(alpha = 0.2f))
                            .border(1.dp, EmeraldGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Completed",
                            color = EmeraldGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                task.isTaken -> {
                    // Step 2: "Continue" button
                    Button(
                        onClick = onContinueTask,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldGreen,
                            disabledContainerColor = DarkSlateElevated
                        ),
                        modifier = Modifier.testTag("continue_task_button_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Continue",
                            tint = PureBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Continue",
                            color = PureBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                else -> {
                    // Step 1: "Take" button
                    Button(
                        onClick = onTakeTask,
                        enabled = canRun,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            disabledContainerColor = DarkSlateElevated
                        ),
                        modifier = Modifier.testTag("take_task_button_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = "Take",
                            tint = PureBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Take",
                            color = PureBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Task Music Dialog with real-time audio playback notification and active 20-second timer
 */
@Composable
fun TaskMusicDialog(
    task: TaskItem,
    isMusicPlaying: Boolean,
    onToggleMusic: () -> Unit,
    onDismiss: () -> Unit,
    onFinished: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(20) }
    var isDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        secondsLeft = 20
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
        }
        isDone = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("task_music_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSlate),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music",
                            tint = GoldLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Music Task Playing",
                            color = GoldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Professional UMG notification stating music playback status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isMusicPlaying) DarkSlateCard else DarkSlateElevated)
                        .border(
                            1.dp,
                            if (isMusicPlaying) EmeraldGreen else GoldPrimary.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                        .testTag("music_playing_notification_box")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            AnimatedSoundWave(isPlaying = isMusicPlaying)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isMusicPlaying)
                                        "Music is currently playing inside the application."
                                    else
                                        "Music playback is currently paused.",
                                    color = if (isMusicPlaying) EmeraldGreen else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "UMG Ambient Audio • LoFi Task Beats (432Hz)",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onToggleMusic,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkSlateElevated)
                        ) {
                            Icon(
                                imageVector = if (isMusicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = GoldLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = task.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Circular Progress Indicator
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progress = (20 - secondsLeft) / 20f

                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = if (isDone) EmeraldGreen else GoldPrimary,
                        strokeWidth = 6.dp,
                        trackColor = DarkSlateElevated
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isDone) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Done",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            Text(
                                text = "${secondsLeft}s",
                                color = GoldLight,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("task_timer_seconds")
                            )
                            Text(
                                text = "Listening",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isDone) {
                    Text(
                        text = "Listen while task verification proceeds. Keep music playing to complete the session.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Verification completed! Click below to receive your +${task.reward.toInt()} ETB reward.",
                        color = EmeraldGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (isDone) {
                    Button(
                        onClick = onFinished,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("claim_task_earnings_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text(
                            text = "Submit & Claim +${task.reward.toInt()} ETB",
                            color = PureBlack,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSlateElevated)
                    ) {
                        Text("Cancel Task", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedSoundWave(isPlaying: Boolean) {
    val transition = rememberInfiniteTransition(label = "SoundWave")
    val h1 by transition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(550, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 8f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(24.dp)
    ) {
        listOf(h1, h2, h3, h2, h1).forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (isPlaying) h.dp else 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isPlaying) EmeraldGreen else TextMuted)
            )
        }
    }
}
