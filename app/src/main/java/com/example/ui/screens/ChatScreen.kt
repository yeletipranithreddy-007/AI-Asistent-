package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import com.example.ui.components.MarkdownText
import com.example.ui.components.copyToClipboard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversations: List<ConversationEntity>,
    selectedConversationId: String,
    messages: List<ChatMessageEntity>,
    isSending: Boolean,
    attachedImageUri: String?,
    attachedFileName: String?,
    onSelectConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onDeleteConversation: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onAttachImage: (String, String) -> Unit,
    onAttachFile: (String, String) -> Unit,
    onClearAttachments: () -> Unit,
    onVoiceClick: () -> Unit,
    onDeleteMessage: (Long) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var showHistoryDrawer by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Image Picker Launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onAttachImage(it.toString(), "")
        }
    }

    // Document Picker Launcher
    val docPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = it.lastPathSegment ?: "uploaded_document.pdf"
            onAttachFile(it.toString(), fileName)
        }
    }

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Surface(color = ObsidianCard, tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showHistoryDrawer = !showHistoryDrawer },
                            modifier = Modifier.testTag("chat_history_drawer_toggle")
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "History Drawer", tint = CyberCyan)
                        }
                        Text(
                            text = "NANI AI Assistant",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    IconButton(
                        onClick = onNewChat,
                        modifier = Modifier.testTag("chat_new_chat_button")
                    ) {
                        Icon(imageVector = Icons.Default.AddComment, contentDescription = "New Chat", tint = CyberCyan)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Messages List
                if (messages.isEmpty()) {
                    EmptyChatWelcome(
                        onPromptClick = { prompt ->
                            inputText = prompt
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            ChatMessageItem(
                                message = msg,
                                onCopy = { copyToClipboard(context, "AI Response", msg.text) },
                                onRegenerate = { onSendMessage(msg.text) },
                                onDelete = { onDeleteMessage(msg.id) }
                            )
                        }

                        if (isSending) {
                            item {
                                TypingIndicator()
                            }
                        }
                    }
                }

                // Attachments preview bar
                if (attachedImageUri != null || attachedFileName != null) {
                    Surface(
                        color = GlassSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AttachFile, contentDescription = null, tint = CyberCyan)
                                Text(
                                    text = attachedFileName ?: "Attached Image",
                                    fontSize = 12.sp,
                                    color = TextPrimaryDark
                                )
                            }
                            IconButton(onClick = onClearAttachments, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove Attachment", tint = NeonPink)
                            }
                        }
                    }
                }

                // Input Bar
                Surface(
                    color = ObsidianCard,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Image attachment button
                        IconButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("chat_attach_image")
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = "Attach Image", tint = TextSecondaryDark)
                        }

                        // File attachment button
                        IconButton(
                            onClick = { docPicker.launch("*/*") },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("chat_attach_file")
                        ) {
                            Icon(imageVector = Icons.Default.InsertDriveFile, contentDescription = "Attach File", tint = TextSecondaryDark)
                        }

                        // Text input field
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Ask NANI AI anything...", color = TextMutedDark, fontSize = 14.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ObsidianDark,
                                unfocusedContainerColor = ObsidianDark,
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 4,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_text_input")
                        )

                        // Voice or Send Button
                        if (inputText.isBlank() && attachedImageUri == null && attachedFileName == null) {
                            IconButton(
                                onClick = onVoiceClick,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CyberCyan)
                                    .testTag("chat_voice_input_button")
                            ) {
                                Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Input", tint = ObsidianDark)
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    val sendStr = inputText
                                    inputText = ""
                                    onSendMessage(sendStr)
                                },
                                enabled = !isSending,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isSending) TextMutedDark else CyberCyan)
                                    .testTag("chat_send_button")
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Send Message", tint = ObsidianDark)
                            }
                        }
                    }
                }
            }

            // History Drawer Overlay
            if (showHistoryDrawer) {
                Surface(
                    color = ObsidianDark.copy(alpha = 0.95f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Conversation History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                            IconButton(onClick = { showHistoryDrawer = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Drawer", tint = TextPrimaryDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onNewChat()
                                showHistoryDrawer = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = ObsidianDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("New Conversation")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(conversations, key = { it.id }) { conv ->
                                val isSelected = conv.id == selectedConversationId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) GlassSurface else ObsidianCard,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, if (isSelected) CyberCyan else ObsidianBorder, RoundedCornerShape(12.dp))
                                        .clickable {
                                            onSelectConversation(conv.id)
                                            showHistoryDrawer = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = conv.title,
                                            fontSize = 14.sp,
                                            color = if (isSelected) CyberCyan else TextPrimaryDark,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { onDeleteConversation(conv.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Conversation", tint = NeonPink, modifier = Modifier.size(16.dp))
                                        }
                                    }
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
fun ChatMessageItem(
    message: ChatMessageEntity,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit,
    onDelete: () -> Unit
) {
    val isUser = message.sender == "USER"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) Color(0xFF003847) else ObsidianCard,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .border(
                    1.dp,
                    if (isUser) CyberCyan.copy(alpha = 0.4f) else ObsidianBorder,
                    RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (isUser) {
                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        color = TextPrimaryDark,
                        lineHeight = 20.sp
                    )
                } else {
                    MarkdownText(
                        text = message.text,
                        textColor = TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Message Action bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Response", tint = TextMutedDark, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = onRegenerate, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate Response", tint = TextMutedDark, modifier = Modifier.size(14.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Message", tint = TextMutedDark, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatWelcome(onPromptClick: (String) -> Unit) {
    val samplePrompts = listOf(
        "Explain pointers in C with memory analogies.",
        "Make my study plan for tomorrow.",
        "Give me 10 cybersecurity practice questions.",
        "Help me debug my code logic."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "NANI Personal AI Operating System", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
        Text(text = "Intelligent assistant with persistent personal memory.", fontSize = 12.sp, color = TextSecondaryDark)

        Spacer(modifier = Modifier.height(28.dp))

        samplePrompts.forEach { prompt ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ObsidianCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                    .clickable { onPromptClick(prompt) }
            ) {
                Text(
                    text = "💡 $prompt",
                    fontSize = 13.sp,
                    color = CyberCyan,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = CyberCyan,
            strokeWidth = 2.dp
        )
        Text(text = "NANI AI processing neural response...", fontSize = 12.sp, color = CyberCyan)
    }
}
