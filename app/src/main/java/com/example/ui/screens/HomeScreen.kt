package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GoalEntity
import com.example.data.local.TaskEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.NavigationScreen
import com.example.ui.theme.*
import java.util.Calendar

data class ImmersiveQuickAction(
    val emoji: String,
    val label: String,
    val prompt: String,
    val targetScreen: NavigationScreen? = null
)

@Composable
fun HomeScreen(
    userProfile: UserProfileEntity?,
    goals: List<GoalEntity>,
    tasks: List<TaskEntity>,
    onQuickAction: (String) -> Unit,
    onNavigate: (NavigationScreen) -> Unit,
    onVoiceClick: () -> Unit
) {
    val userName = userProfile?.name ?: "Alex"
    val greeting = getTimeGreeting()

    val actions = listOf(
        ImmersiveQuickAction("📚", "Study", "Explain C pointers with real-world analogies and code examples.", NavigationScreen.STUDY),
        ImmersiveQuickAction("💻", "Coding", "Help me write and debug clean Kotlin / C code snippets.", NavigationScreen.CHAT),
        ImmersiveQuickAction("🧠", "Ask Anything", "What are the core fundamentals of quantum computing?", NavigationScreen.CHAT),
        ImmersiveQuickAction("📄", "Analyze", "Analyze my document and highlight the key takeaways.", NavigationScreen.CHAT)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Greeting Header
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "$greeting,",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Light,
                    color = TextSecondaryDark
                )
                Text(
                    text = "$userName 👋",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "How can I help you today?",
                    fontSize = 14.sp,
                    color = TextMutedDark
                )
            }
        }

        // Immersive Central AI Orb Trigger
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background Cyan/Blue Glow Blur
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    CyberCyan.copy(alpha = 0.25f),
                                    CyberBlue.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Rotating Ring Animation
                val infiniteTransition = rememberInfiniteTransition(label = "OrbSpin")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(12000, easing = LinearEasing)
                    ),
                    label = "Rotation"
                )

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(rotation)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    CyberCyan.copy(alpha = 0.8f),
                                    Color.Transparent,
                                    CyberBlue.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Central Interactive Orb
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(CyberCyan, CyberBlue, CyberPurple)
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable { onVoiceClick() }
                        .testTag("immersive_home_orb_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✦",
                        fontSize = 32.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Quick Action 2x2 Grid + Plan My Day
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 2x2 Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    actions.take(2).forEach { action ->
                        ImmersiveCardButton(
                            action = action,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (action.targetScreen != null) onNavigate(action.targetScreen)
                                else onQuickAction(action.prompt)
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    actions.drop(2).forEach { action ->
                        ImmersiveCardButton(
                            action = action,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (action.targetScreen != null) onNavigate(action.targetScreen)
                                else onQuickAction(action.prompt)
                            }
                        )
                    }
                }

                // Full Width Plan My Day Banner
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(CyberBlue.copy(alpha = 0.4f), CyberCyan.copy(alpha = 0.4f))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { onQuickAction("Make my study plan and schedule for today with hour-by-hour milestones.") }
                        .testTag("plan_my_day_button")
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(CyberBlue.copy(alpha = 0.12f), CyberCyan.copy(alpha = 0.12f))
                                )
                            )
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "📅", fontSize = 20.sp)
                            Text(
                                text = "Plan My Day",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "→", fontSize = 14.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Today's Progress Snapshot
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = ObsidianCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Dashboard, contentDescription = null, tint = NeonTeal)
                            Text(text = "Daily Overview", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        }
                        TextButton(onClick = { onNavigate(NavigationScreen.DASHBOARD) }) {
                            Text(text = "Details", color = CyberCyan, fontSize = 12.sp)
                        }
                    }

                    val completedTasksCount = tasks.count { it.isCompleted }
                    val totalTasks = tasks.size.coerceAtLeast(1)
                    val taskPercent = (completedTasksCount * 100) / totalTasks

                    LinearProgressIndicator(
                        progress = { taskPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = CyberCyan,
                        trackColor = ObsidianDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Tasks Done: $completedTasksCount/$totalTasks", fontSize = 12.sp, color = TextSecondaryDark)
                        Text(text = "$taskPercent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    }

                    if (goals.isNotEmpty()) {
                        val activeGoal = goals.first()
                        Divider(color = ObsidianBorder, modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Flag, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(16.dp))
                            Text(text = "Active Goal: ${activeGoal.title} (${activeGoal.progressPercent}%)", fontSize = 13.sp, color = TextPrimaryDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImmersiveCardButton(
    action: ImmersiveQuickAction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = ObsidianCard,
        modifier = modifier
            .border(1.dp, ObsidianBorder, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag("immersive_card_${action.label.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = action.emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimaryDark
            )
        }
    }
}

private fun getTimeGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 5..11 -> "Good morning"
        hour in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
}

