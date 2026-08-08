package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class AiToolCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val tools: List<AiToolItem>
)

data class AiToolItem(
    val name: String,
    val description: String,
    val prompt: String
)

@Composable
fun ToolsScreen(
    onToolClick: (String) -> Unit
) {
    val categories = listOf(
        AiToolCategory(
            title = "📚 Study Tools",
            icon = Icons.Default.MenuBook,
            color = CyberCyan,
            tools = listOf(
                AiToolItem("Notes Generator", "Convert complex topics into organized revision notes", "Generate comprehensive study notes with headers and bullet points for: "),
                AiToolItem("Quiz Generator", "Generate active recall MCQs and practice tests", "Create a 5-question multiple choice quiz with answers for: "),
                AiToolItem("Flashcards", "Create question and answer flashcards", "Generate 10 flashcards (Front: Question, Back: Answer) for: "),
                AiToolItem("Study Planner", "Create a structured study timetable", "Build a detailed 7-day study timetable and topic breakdown for: "),
                AiToolItem("PDF Analyzer", "Summarize and extract key exam topics from documents", "Analyze my document and extract the 10 most critical exam topics for: ")
            )
        ),
        AiToolCategory(
            title = "💻 Developer Tools",
            icon = Icons.Default.Code,
            color = NeonTeal,
            tools = listOf(
                AiToolItem("Code Explainer", "Step-by-step breakdown of source code", "Explain this code line-by-line with complexity analysis: "),
                AiToolItem("Debugger", "Identify syntax, memory & logic bugs", "Analyze this code for memory leaks, null pointers, or logic bugs and provide the fixed code: "),
                AiToolItem("Code Generator", "Generate clean Kotlin / C / Python code", "Write clean, modular code with comments for: "),
                AiToolItem("Algorithm Explainer", "Big-O time complexity & space breakdown", "Explain the time complexity, space complexity, and step-by-step algorithm for: ")
            )
        ),
        AiToolCategory(
            title = "🧠 Productivity",
            icon = Icons.Default.Psychology,
            color = CyberPurple,
            tools = listOf(
                AiToolItem("Daily Planner", "AI prioritized daily task schedule", "Create an optimized hour-by-hour daily schedule prioritizing my goals for: "),
                AiToolItem("Goal Planner", "Break long-term goals into actionable tasks", "Decompose this goal into 5 milestone phases with explicit tasks: "),
                AiToolItem("Task Generator", "Generate actionable checklist items", "Generate 10 concrete sub-tasks to complete: "),
                AiToolItem("Idea Generator", "Brainstorm creative solutions & project ideas", "Brainstorm 5 innovative project ideas and feature blueprints for: ")
            )
        ),
        AiToolCategory(
            title = "🎨 Creator Tools",
            icon = Icons.Default.Lightbulb,
            color = NeonPink,
            tools = listOf(
                AiToolItem("YouTube Ideas", "High-CTR video titles and concepts", "Generate 5 viral YouTube video titles and outline concepts for: "),
                AiToolItem("Video Scripts", "Hook, story arc & call to action script", "Write a captivating 60-second video script with a strong hook for: "),
                AiToolItem("Captions", "Engaging social media captions & hashtags", "Write 3 engaging social media captions with hashtags for: "),
                AiToolItem("Content Ideas", "Weekly content calendar blueprint", "Plan a 7-day content calendar with hook ideas for: ")
            )
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "🛠️ NANI AI Tools Suite",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = "Specialized AI Engines for Study, Code, Productivity & Creation",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }
        }

        items(categories) { cat ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ObsidianCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = cat.icon, contentDescription = cat.title, tint = cat.color, modifier = Modifier.size(20.dp))
                        Text(text = cat.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    cat.tools.forEach { tool ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GlassSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onToolClick(tool.prompt) }
                                .testTag("tool_${tool.name.lowercase().replace(" ", "_")}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = tool.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = cat.color)
                                    Text(text = tool.description, fontSize = 11.sp, color = TextSecondaryDark)
                                }
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Launch", tint = cat.color, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
