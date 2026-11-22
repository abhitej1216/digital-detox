package com.example.digitaldetox.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Emil Kowalski-inspired CraftCard with crisp hairline border,
 * smooth rounded geometry, and optional tactile spring feedback.
 */
@Composable
fun CraftCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .tactilePress(scale = 0.98f, onClick = onClick)
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .background(backgroundColor, shape)
    } else {
        modifier
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .background(backgroundColor, shape)
    }

    Column(
        modifier = cardModifier.padding(18.dp),
        content = content
    )
}

/**
 * Apple/Linear-style translucent status pill with glowing dot indicator.
 */
@Composable
fun CraftPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Fluid Segmented Pill Control with smooth spring animations.
 */
@Composable
fun <T> AnimatedSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                val isSelected = item == selectedItem
                val targetBg = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                val animatedBg by animateColorAsState(
                    targetValue = targetBg,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "pill_bg"
                )
                val targetTextColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                val animatedTextColor by animateColorAsState(
                    targetValue = targetTextColor,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "pill_text"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(animatedBg)
                        .tactilePress(scale = 0.95f) {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onItemSelected(item)
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelProvider(item),
                        style = MaterialTheme.typography.labelLarge,
                        color = animatedTextColor,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
