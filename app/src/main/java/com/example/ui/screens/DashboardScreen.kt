package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GoalEntity
import com.example.data.local.StudyTopicEntity
import com.example.data.local.TaskEntity
import com.example.data.local.UserProfileEntity
import com.example.ui.NavigationScreen
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    userProfile: UserProfileEntity?,
    goals: List<GoalEntity>,
    tasks: List<TaskEntity>,
    studyTopics: List<StudyTopicEntity>,
    onNavigate: (NavigationScreen) -> Unit
) {
    val completedTasks = tasks.count { it.isCompleted }
    val totalTasks = tasks.size.coerceAtLeast(1)
    val taskPercentage = (completedTasks * 100) / totalTasks

    val completedTopics = studyTopics.count { it.isCompleted }
    val totalTopics = studyTopics.size.coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard Title
        item {
            Column {
                Text(
                    text = "Personal Dashboard 📊",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Analytics & performance stats across all AI modules",
                    fontSize = 13.sp,
                    color = TextMutedDark
                )
            }
        }

        // Stats Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatMetricCard(
                    title = "Tasks Done",
                    value = "$completedTasks/$totalTasks",
                    subtitle = "$taskPercentage% Completed",
                    icon = Icons.Default.CheckCircle,
                    color = NeonTeal,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Study Topics",
                    value = "$completedTopics/$totalTopics",
                    subtitle = "Mastered",
                    icon = Icons.Default.School,
                    color = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Streak Banner
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = ObsidianCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(NeonPink.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🔥", fontSize = 24.sp)
                    }

                    Column {
                        Text(
                            text = "7 Day Active Learning Streak",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Keep up the daily practice to boost knowledge retention!",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
        }

        // Quick Navigation Links
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
                    Text(
                        text = "Module Quick Access",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { onNavigate(NavigationScreen.GOALS) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = ObsidianDark)
                        ) {
                            Text("Goals")
                        }

                        Button(
                            onClick = { onNavigate(NavigationScreen.STUDY) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple, contentColor = Color.White)
                        ) {
                            Text("Study")
                        }

                        Button(
                            onClick = { onNavigate(NavigationScreen.TOOLS) },
                            colors = ButtonDefaults.buttonColors(containerColor = GlassSurface, contentColor = TextPrimaryDark)
                        ) {
                            Text("AI Tools")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = ObsidianCard,
        modifier = modifier.border(1.dp, ObsidianBorder, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Text(text = title, fontSize = 12.sp, color = TextMutedDark)
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Text(text = subtitle, fontSize = 11.sp, color = color)
        }
    }
}
