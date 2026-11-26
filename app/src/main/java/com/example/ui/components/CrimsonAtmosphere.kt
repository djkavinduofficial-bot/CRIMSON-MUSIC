package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CrimsonActiveBorder
import com.example.ui.theme.CrimsonActiveIcon
import com.example.ui.theme.CrimsonAddBottom
import com.example.ui.theme.CrimsonAddTop
import com.example.ui.theme.CrimsonCtaEnd
import com.example.ui.theme.CrimsonCtaStart
import com.example.ui.theme.CrimsonDeepVoid
import com.example.ui.theme.CrimsonGlassBottom
import com.example.ui.theme.CrimsonGlassTop
import com.example.ui.theme.CrimsonLineBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.CrimsonSurfaceBase
import com.example.ui.theme.CrimsonSurfaceDim
import com.example.ui.theme.CrimsonWhite

enum class BackdropStyle {
    ONBOARDING,
    PLAYER,
    LOCK_MODE
}

@Composable
fun CrimsonBackdrop(
    style: BackdropStyle,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CrimsonSurfaceBase)
    ) {
        val imageFraction = when (style) {
            BackdropStyle.ONBOARDING -> 0.72f
            BackdropStyle.PLAYER -> 0.66f
            BackdropStyle.LOCK_MODE -> 0.70f
        }
        Image(
            painter = painterResource(id = R.drawable.bg_crimson_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize(imageFraction)
                .align(Alignment.TopCenter)
        )

        val gradientBrush = when (style) {
            BackdropStyle.ONBOARDING -> Brush.verticalGradient(
                0.0f to Color.Transparent,
                0.24f to Color.Transparent,
                0.42f to CrimsonSurfaceDim.copy(alpha = 0.20f),
                0.68f to CrimsonSurfaceDim.copy(alpha = 0.91f),
                0.95f to CrimsonDeepVoid
            )
            BackdropStyle.PLAYER -> Brush.verticalGradient(
                0.0f to CrimsonSurfaceDim.copy(alpha = 0.30f),
                0.24f to CrimsonSurfaceDim.copy(alpha = 0.14f),
                0.55f to CrimsonSurfaceDim.copy(alpha = 0.78f),
                0.90f to CrimsonDeepVoid
            )
            BackdropStyle.LOCK_MODE -> Brush.verticalGradient(
                0.0f to CrimsonSurfaceDim.copy(alpha = 0.28f),
                0.38f to CrimsonSurfaceDim.copy(alpha = 0.56f),
                0.80f to CrimsonDeepVoid.copy(alpha = 0.92f),
                1.0f to CrimsonDeepVoid
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
        )
    }
}

@Composable
fun CrimsonGlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    size: Dp = 48.dp,
    shape: Shape = CircleShape,
    content: @Composable () -> Unit
) {
    val borderColor = if (active) CrimsonActiveBorder else CrimsonLineBorder
    val contentColor = if (active) CrimsonActiveIcon else CrimsonWhite

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(size)
            .then(
                if (active) Modifier.shadow(12.dp, shape, ambientColor = CrimsonNeon, spotColor = CrimsonNeon)
                else Modifier
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(CrimsonGlassTop, CrimsonGlassBottom)
                )
            )
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
fun CrimsonGlassPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    accentFill: Boolean = false,
    shape: Shape = RoundedCornerShape(99.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    content: @Composable RowScope.() -> Unit
) {
    val borderColor = when {
        active -> CrimsonActiveBorder
        accentFill -> CrimsonActiveBorder.copy(alpha = 0.58f)
        else -> CrimsonLineBorder
    }
    val gradientColors = if (accentFill) {
        listOf(CrimsonAddTop, CrimsonAddBottom)
    } else {
        listOf(CrimsonGlassTop, CrimsonGlassBottom)
    }
    val contentColor = if (active) CrimsonActiveIcon else CrimsonWhite

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .heightIn(min = 48.dp)
            .then(
                if (active) Modifier.shadow(12.dp, shape, ambientColor = CrimsonNeon, spotColor = CrimsonNeon)
                else Modifier
            )
            .clip(shape)
            .background(Brush.linearGradient(gradientColors))
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(contentPadding)
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
fun CrimsonPrimaryCtaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(99.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .heightIn(min = 56.dp)
            .shadow(18.dp, shape, ambientColor = CrimsonNeon, spotColor = CrimsonNeon)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(CrimsonCtaStart, CrimsonCtaEnd)
                )
            )
            .border(1.dp, CrimsonActiveBorder.copy(alpha = 0.72f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 15.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
            content()
        }
    }
}
