package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.VoiceState
import com.example.ui.theme.*

@Composable
fun VoiceAssistantOverlay(
    voiceState: VoiceState,
    spokenText: String,
    aiResponseText: String,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onStopSpeaking: () -> Unit,
    onReplay: () -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = ObsidianDark.copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (voiceState is VoiceState.Listening) NeonTeal else CyberCyan)
                        )
                        Text(
                            text = "NANI JARVIS VOICE OS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.5.sp
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GlassSurface)
                            .testTag("voice_overlay_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Voice Assistant",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Center Animated AI Orb & Waveform
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    AnimatedJarvisOrb(voiceState = voiceState, onClick = {
                        if (voiceState is VoiceState.Listening) onStopListening() else onStartListening()
                    })

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = when (voiceState) {
                            is VoiceState.Listening -> "Listening..."
                            is VoiceState.Processing -> "Processing Neural Request..."
                            is VoiceState.Speaking -> "NANI Speaking..."
                            is VoiceState.Error -> "Voice Alert"
                            else -> "Tap Orb to Speak"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Transcription / Output Box
                    val displayMessage = when (voiceState) {
                        is VoiceState.Processing -> spokenText.ifBlank { "Processing..." }
                        is VoiceState.Speaking -> aiResponseText
                        is VoiceState.Error -> (voiceState as VoiceState.Error).message
                        else -> spokenText.ifBlank { "Say commands like: 'Explain recursion in C' or 'Remember my project goal'." }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
                    ) {
                        Text(
                            text = displayMessage,
                            fontSize = 15.sp,
                            color = TextPrimaryDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }

                // Bottom Controls bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Replay
                    IconButton(
                        onClick = onReplay,
                        enabled = aiResponseText.isNotBlank(),
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(GlassSurface)
                            .testTag("voice_replay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Replay Response",
                            tint = if (aiResponseText.isNotBlank()) CyberCyan else TextMutedDark
                        )
                    }

                    // Mic toggle
                    FloatingActionButton(
                        onClick = {
                            if (voiceState is VoiceState.Listening) onStopListening() else onStartListening()
                        },
                        containerColor = if (voiceState is VoiceState.Listening) NeonPink else CyberCyan,
                        contentColor = ObsidianDark,
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("voice_mic_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (voiceState is VoiceState.Listening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Toggle Mic",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Stop Speaking
                    IconButton(
                        onClick = onStopSpeaking,
                        enabled = voiceState is VoiceState.Speaking,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(GlassSurface)
                            .testTag("voice_stop_speaking_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Speech",
                            tint = if (voiceState is VoiceState.Speaking) NeonPink else TextMutedDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedJarvisOrb(
    voiceState: VoiceState,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (voiceState is VoiceState.Listening || voiceState is VoiceState.Speaking) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Box(
        modifier = Modifier
            .size(160.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = when (voiceState) {
                        is VoiceState.Listening -> listOf(NeonTeal, CyberCyan, ObsidianDark)
                        is VoiceState.Speaking -> listOf(CyberPurple, NeonPink, ObsidianDark)
                        else -> listOf(CyberCyan, CyberPurple, ObsidianDark)
                    }
                )
            )
            .border(
                width = 3.dp,
                brush = Brush.sweepGradient(
                    colors = listOf(CyberCyan, CyberPurple, NeonTeal, CyberCyan)
                ),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (voiceState) {
                is VoiceState.Speaking -> Icons.Default.VolumeUp
                is VoiceState.Listening -> Icons.Default.GraphicEq
                else -> Icons.Default.Psychology
            },
            contentDescription = "JARVIS AI Orb",
            tint = Color.White,
            modifier = Modifier.size(64.dp)
        )
    }
}
