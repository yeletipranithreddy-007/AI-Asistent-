package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudyTopicEntity
import com.example.ui.components.MarkdownText
import com.example.ui.theme.*

@Composable
fun StudyScreen(
    studyTopics: List<StudyTopicEntity>,
    onToggleTopicCompleted: (StudyTopicEntity) -> Unit,
    onAddStudyTopic: (String, String) -> Unit,
    onLaunchStudyPrompt: (String) -> Unit
) {
    var selectedSubject by remember { mutableStateOf("All") }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var activeBreakdownTopic by remember { mutableStateOf<String?>(null) }

    val subjects = remember(studyTopics) {
        listOf("All") + studyTopics.map { it.subject }.distinct()
    }

    val filteredTopics = if (selectedSubject == "All") studyTopics else studyTopics.filter { it.subject == selectedSubject }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Study Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📚 Study Mode OS",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "AI Concept Explanations, Flashcards & Practice Quizzes",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }

                FloatingActionButton(
                    onClick = { showAddTopicDialog = true },
                    containerColor = CyberCyan,
                    contentColor = ObsidianDark,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("study_add_topic_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Topic")
                }
            }
        }

        // Quick AI Study Generators
        item {
            Text(text = "Quick Generators", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudyToolBadge("Notes", Icons.Default.Description, CyberCyan, Modifier.weight(1f)) {
                    onLaunchStudyPrompt("Generate concise study notes with bullet points for C Pointers & Memory Allocation.")
                }
                StudyToolBadge("MCQ Quiz", Icons.Default.Quiz, CyberPurple, Modifier.weight(1f)) {
                    onLaunchStudyPrompt("Generate 5 multiple choice quiz questions (MCQs) on Data Structures and Binary Trees with answers.")
                }
                StudyToolBadge("Flashcards", Icons.Default.Style, NeonTeal, Modifier.weight(1f)) {
                    onLaunchStudyPrompt("Create 8 active-recall flashcards for Cybersecurity protocols & encryption.")
                }
            }
        }

        // Subject Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subj ->
                    FilterChip(
                        selected = selectedSubject == subj,
                        onClick = { selectedSubject = subj },
                        label = { Text(subj, color = if (selectedSubject == subj) ObsidianDark else TextPrimaryDark) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            containerColor = ObsidianCard
                        )
                    )
                }
            }
        }

        // Active Topic 8-Part Breakdown Example View
        if (activeBreakdownTopic != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberCyan, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "8-Part Breakdown: $activeBreakdownTopic",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan
                            )
                            IconButton(onClick = { activeBreakdownTopic = null }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimaryDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        MarkdownText(
                            text = getSample8PartStudyBreakdown(activeBreakdownTopic!!),
                            textColor = TextPrimaryDark
                        )
                    }
                }
            }
        }

        // Study Topics List
        item {
            Text(text = "Tracked Topics & Revision Schedule", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        }

        items(filteredTopics, key = { it.id }) { topic ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ObsidianCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (topic.isCompleted) NeonTeal.copy(alpha = 0.5f) else ObsidianBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        activeBreakdownTopic = topic.topicName
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = topic.isCompleted,
                            onCheckedChange = { onToggleTopicCompleted(topic) },
                            colors = CheckboxDefaults.colors(checkedColor = NeonTeal, checkmarkColor = ObsidianDark)
                        )
                        Column {
                            Text(
                                text = topic.topicName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "${topic.subject} • Revision: ${topic.revisionDate}",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Button(
                        onClick = {
                            onLaunchStudyPrompt("Provide an in-depth 8-part study guide for ${topic.subject} -> ${topic.topicName}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GlassSurface, contentColor = CyberCyan),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Study AI", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Add Topic Dialog
    if (showAddTopicDialog) {
        var subjectInput by remember { mutableStateOf("C Programming") }
        var topicInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddTopicDialog = false },
            title = { Text("Add Study Topic", color = CyberCyan) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = subjectInput,
                        onValueChange = { subjectInput = it },
                        label = { Text("Subject (e.g. Data Structures)") }
                    )
                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text("Topic Name (e.g. Binary Search Trees)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topicInput.isNotBlank()) {
                            onAddStudyTopic(subjectInput, topicInput)
                            showAddTopicDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = ObsidianDark)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTopicDialog = false }) {
                    Text("Cancel", color = TextMutedDark)
                }
            },
            containerColor = ObsidianCard
        )
    }
}

@Composable
fun StudyToolBadge(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ObsidianCard,
        modifier = modifier
            .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 11.sp, color = TextPrimaryDark, fontWeight = FontWeight.Bold)
        }
    }
}

private fun getSample8PartStudyBreakdown(topicName: String): String {
    return """
        ### 1. Simple Explanation
        Pointers are memory addresses stored inside a variable. Instead of holding raw values, they point directly to where the data lives in RAM.

        ### 2. Real-World Analogy
        Think of a house variable holding people, while a pointer holds the **street address label** of that house.

        ### 3. Example
        ```c
        int age = 22;
        int *agePtr = &age;
        ```

        ### 4. C Source Code
        ```c
        #include <stdio.h>

        int main() {
            int score = 95;
            int *p = &score;

            printf("Value: %d\n", score);
            printf("Pointer Address: %p\n", p);
            printf("Dereferenced: %d\n", *p);
            return 0;
        }
        ```

        ### 5. Code Explanation
        `&score` retrieves the address of variable `score`. `*p` dereferences the pointer to access the value stored at that address.

        ### 6. Common Mistakes
        - Dereferencing uninitialized or `NULL` pointers (causes Segmentation Fault!).
        - Confusing `*` in variable declaration vs dereferencing.

        ### 7. Active Quiz
        **Q**: What operator returns the memory address of a variable in C?
        **A**: `&` (Address-of operator).

        ### 8. Practice Problem
        Write a function `swap(int *a, int *b)` that swaps two integer values in-place using pointers.
    """.trimIndent()
}
