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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavigationScreen
import com.example.ui.theme.*

@Composable
fun NaniBottomBar(
    currentScreen: NavigationScreen,
    onNavigate: (NavigationScreen) -> Unit,
    onVoiceClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Surface(
            color = ObsidianCard,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(CyberCyan.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                BottomNavItem(
                    label = "Home",
                    icon = if (currentScreen == NavigationScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    isSelected = currentScreen == NavigationScreen.HOME,
                    onClick = { onNavigate(NavigationScreen.HOME) },
                    testTag = "nav_home"
                )

                // Chat
                BottomNavItem(
                    label = "Chat",
                    icon = if (currentScreen == NavigationScreen.CHAT) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                    isSelected = currentScreen == NavigationScreen.CHAT,
                    onClick = { onNavigate(NavigationScreen.CHAT) },
                    testTag = "nav_chat"
                )

                // Central Floating Orb Spacer
                Spacer(modifier = Modifier.width(56.dp))

                // Study
                BottomNavItem(
                    label = "Study",
                    icon = if (currentScreen == NavigationScreen.STUDY) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                    isSelected = currentScreen == NavigationScreen.STUDY,
                    onClick = { onNavigate(NavigationScreen.STUDY) },
                    testTag = "nav_study"
                )

                // Goals & Dashboard
                BottomNavItem(
                    label = "Goals",
                    icon = if (currentScreen == NavigationScreen.GOALS || currentScreen == NavigationScreen.DASHBOARD) Icons.Filled.TrackChanges else Icons.Outlined.TrackChanges,
                    isSelected = currentScreen == NavigationScreen.GOALS || currentScreen == NavigationScreen.DASHBOARD,
                    onClick = { onNavigate(NavigationScreen.GOALS) },
                    testTag = "nav_goals"
                )
            }
        }

        // Central Animated AI Voice Orb FAB
        CentralVoiceOrbButton(
            onClick = onVoiceClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
        )
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) CyberCyan else TextSecondaryDark,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) CyberCyan else TextMutedDark
        )
    }
}

@Composable
fun CentralVoiceOrbButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VoiceOrbGlow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbScale"
    )

    Box(
        modifier = modifier
            .size(58.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(CyberCyan, CyberPurple)
                )
            )
            .border(2.dp, CyberCyan, CircleShape)
            .clickable { onClick() }
            .testTag("central_voice_orb"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = "Voice Assistant Orb",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}
