package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GoalEntity
import com.example.data.local.TaskEntity
import com.example.ui.theme.*

@Composable
fun GoalsScreen(
    goals: List<GoalEntity>,
    tasks: List<TaskEntity>,
    onToggleTask: (TaskEntity) -> Unit,
    onAddGoal: (String, Int) -> Unit,
    onAddTask: (Long, String) -> Unit
) {
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var newGoalTitle by remember { mutableStateOf("") }

    var selectedGoalIdForTask by remember { mutableStateOf<Long?>(null) }
    var newTaskTitle by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Goal Roadmap 🎯",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Track your long-term milestones and active tasks",
                        fontSize = 13.sp,
                        color = TextMutedDark
                    )
                }

                IconButton(
                    onClick = { showAddGoalDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CyberCyan)
                        .testTag("add_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Goal",
                        tint = ObsidianDark
                    )
                }
            }
        }

        // Active Goals List
        items(goals) { goal ->
            val goalTasks = tasks.filter { it.goalId == goal.id }

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
                            Icon(imageVector = Icons.Default.Flag, contentDescription = null, tint = CyberPurple)
                            Text(
                                text = goal.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }

                        Text(
                            text = "${goal.progressPercent}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    LinearProgressIndicator(
                        progress = { goal.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = CyberCyan,
                        trackColor = ObsidianDark
                    )

                    Divider(color = ObsidianBorder)

                    Text(
                        text = "Tasks (${goalTasks.count { it.isCompleted }}/${goalTasks.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryDark
                    )

                    goalTasks.forEach { task ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onToggleTask(task) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                                contentDescription = if (task.isCompleted) "Completed" else "Incomplete",
                                tint = if (task.isCompleted) NeonTeal else TextMutedDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = task.title,
                                fontSize = 14.sp,
                                color = if (task.isCompleted) TextMutedDark else TextPrimaryDark
                            )
                        }
                    }

                    TextButton(
                        onClick = { selectedGoalIdForTask = goal.id },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("+ Add Task", color = CyberCyan, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("New Goal", color = TextPrimaryDark) },
            text = {
                OutlinedTextField(
                    value = newGoalTitle,
                    onValueChange = { newGoalTitle = it },
                    label = { Text("Goal Title", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGoalTitle.isNotBlank()) {
                            onAddGoal(newGoalTitle, 30)
                            newGoalTitle = ""
                            showAddGoalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = ObsidianDark)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Cancel", color = TextMutedDark)
                }
            },
            containerColor = ObsidianCard
        )
    }

    // Add Task Dialog
    selectedGoalIdForTask?.let { goalId ->
        AlertDialog(
            onDismissRequest = { selectedGoalIdForTask = null },
            title = { Text("Add Task to Goal", color = TextPrimaryDark) },
            text = {
                OutlinedTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    label = { Text("Task Description", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            onAddTask(goalId, newTaskTitle)
                            newTaskTitle = ""
                            selectedGoalIdForTask = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = ObsidianDark)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedGoalIdForTask = null }) {
                    Text("Cancel", color = TextMutedDark)
                }
            },
            containerColor = ObsidianCard
        )
    }
}
