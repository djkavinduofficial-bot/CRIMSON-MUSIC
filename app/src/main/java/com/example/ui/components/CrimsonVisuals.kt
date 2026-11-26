package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.playback.VisualizerMode
import com.example.ui.formatDurationMs
import com.example.ui.theme.CrimsonArcTrack
import com.example.ui.theme.CrimsonBadgeBg
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonLineBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.CrimsonRingDashed
import com.example.ui.theme.CrimsonRingOuter
import com.example.ui.theme.CrimsonSoftPink
import com.example.ui.theme.CrimsonSubtitle
import com.example.ui.theme.CrimsonVizHigh
import com.example.ui.theme.CrimsonVizLow
import com.example.ui.theme.CrimsonVizMid
import com.example.ui.theme.CrimsonWhite
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun CrimsonHeroDisc(
    isPlaying: Boolean,
    artwork: Bitmap?,
    discContainerSize: Dp,
    modifier: Modifier = Modifier
) {
    var rotationDegrees by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var lastNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastNanos != 0L) {
                    val deltaSec = (frameNanos - lastNanos) / 1_000_000_000f
                    rotationDegrees = (rotationDegrees + deltaSec * 15f) % 360f
                }
                lastNanos = frameNanos
            }
        }
    }

    val breatheTransition = rememberInfiniteTransition(label = "ring_breathe")
    val ringScale by breatheTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale"
    )

    val activeScale = if (isPlaying) ringScale else 1f
    val albumArtDesc = stringResource(R.string.cd_album_art)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(discContainerSize)
    ) {
        // Outer glowing + dashed orbital ring
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(activeScale)
        ) {
            val radius = size.minDimension / 2f
            drawCircle(
                color = CrimsonRingOuter,
                radius = radius - 1.dp.toPx(),
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawCircle(
                color = CrimsonRingDashed,
                radius = radius + 4.dp.toPx(),
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            )
            if (isPlaying) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CrimsonNeon.copy(alpha = 0.25f),
                            CrimsonNeon.copy(alpha = 0.0f)
                        ),
                        center = center,
                        radius = radius * 1.15f
                    ),
                    radius = radius * 1.15f
                )
            }
        }

        // Spinning Vinyl Cover Disc
        val discSize = discContainerSize * 0.85f
        val artworkBitmap = remember(artwork) { artwork?.asImageBitmap() }

        Box(
            modifier = Modifier
                .size(discSize)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    ambientColor = CrimsonNeon,
                    spotColor = CrimsonNeon
                )
                .clip(CircleShape)
                .border(2.dp, CrimsonSoftPink, CircleShape)
                .rotate(rotationDegrees)
        ) {
            if (artworkBitmap != null) {
                Image(
                    bitmap = artworkBitmap,
                    contentDescription = albumArtDesc,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_crimson_cover),
                    contentDescription = albumArtDesc,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun CrimsonVisualizerCanvas(
    mode: VisualizerMode,
    spectrumBins: FloatArray,
    waveformSamples: FloatArray,
    modifier: Modifier = Modifier
) {
    val vizDesc = stringResource(R.string.cd_visualizer)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = vizDesc }
            .testTag("audio_visualizer")
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        val verticalGrad = Brush.verticalGradient(
            0.0f to CrimsonVizHigh,
            0.5f to CrimsonVizMid,
            1.0f to CrimsonVizLow
        )

        when (mode) {
            VisualizerMode.EDM_SPECTRUM -> {
                val barCount = 55
                val barSlot = w / barCount
                val barWidth = (barSlot - 2.dp.toPx()).coerceAtLeast(1.5f)
                for (i in 0 until barCount) {
                    val distFromCenter = abs(i - barCount / 2f)
                    val binIndex = (distFromCenter * 2.2f).toInt()
                        .coerceIn(0, (spectrumBins.size - 1).coerceAtLeast(0))
                    val energy = spectrumBins.getOrElse(binIndex) { 0f }
                    val barHeight = 3.dp.toPx() + energy * (h * 0.90f)
                    val left = i * barSlot + (barSlot - barWidth) / 2f
                    val top = (h - barHeight) / 2f

                    // Subtle crimson glow behind active bars
                    if (energy > 0.08f) {
                        drawRoundRect(
                            color = CrimsonNeon.copy(alpha = 0.25f * energy),
                            topLeft = Offset(left - 1.5f, top - 2f),
                            size = Size(barWidth + 3f, barHeight + 4f),
                            cornerRadius = CornerRadius(barWidth, barWidth)
                        )
                    }
                    drawRoundRect(
                        brush = verticalGrad,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }

            VisualizerMode.TRANCE_RING -> {
                val cx = w / 2f
                val cy = h / 2f
                val baseRadius = min(w, h) * 0.22f
                val spokeCount = 72
                val strokeW = 2.dp.toPx()

                for (i in 0 until spokeCount) {
                    val angle = (i.toFloat() / spokeCount) * (2.0 * PI)
                    val energy = spectrumBins.getOrElse(i % spectrumBins.size.coerceAtLeast(1)) { 0f }
                    val outerRadius = baseRadius + 3.dp.toPx() + energy * (min(w, h) * 0.26f)
                    val cosA = cos(angle).toFloat()
                    val sinA = sin(angle).toFloat()
                    val start = Offset(cx + cosA * baseRadius, cy + sinA * baseRadius)
                    val end = Offset(cx + cosA * outerRadius, cy + sinA * outerRadius)

                    drawLine(
                        brush = verticalGrad,
                        start = start,
                        end = end,
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }

            VisualizerMode.NEON_WAVE -> {
                val count = waveformSamples.size.coerceAtLeast(2)
                val path = Path()
                for (i in 0 until count) {
                    val x = (i.toFloat() / (count - 1)) * w
                    val y = h / 2f + waveformSamples[i] * (h * 0.45f)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(
                    path = path,
                    color = CrimsonNeon.copy(alpha = 0.35f),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = path,
                    brush = verticalGrad,
                    style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun CrimsonArcSeekBar(
    progress: Float,
    positionMs: Long,
    durationMs: Long,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val clamped = progress.coerceIn(0f, 1f)
    val wordmark = stringResource(R.string.brand_wordmark)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .testTag("arc_seek_bar")
    ) {
        // Curved quadratic Bezier arc progress canvas + drag/tap gesture
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .align(Alignment.TopCenter)
                .pointerInput(durationMs) {
                    detectTapGestures { offset ->
                        if (size.width > 0) {
                            onSeek((offset.x / size.width.toFloat()).coerceIn(0f, 1f))
                        }
                    }
                }
                .pointerInput(durationMs) {
                    detectHorizontalDragGestures { change, _ ->
                        change.consume()
                        if (size.width > 0) {
                            onSeek((change.position.x / size.width.toFloat()).coerceIn(0f, 1f))
                        }
                    }
                }
        ) {
            val padX = 10.dp.toPx()
            val startX = padX
            val endX = size.width - padX
            val endY = size.height * 0.88f
            val controlX = size.width / 2f
            val controlY = -size.height * 0.56f

            // Base arc path
            val basePath = Path().apply {
                moveTo(startX, endY)
                quadraticTo(controlX, controlY, endX, endY)
            }
            drawPath(
                path = basePath,
                color = CrimsonArcTrack,
                style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Active progress segment sampled smoothly along Bezier parameter t in [0, clamped]
            val steps = 48
            val activePath = Path()
            for (step in 0..steps) {
                val t = clamped * (step.toFloat() / steps)
                val inv = 1f - t
                val x = inv * inv * startX + 2f * inv * t * controlX + t * t * endX
                val y = inv * inv * endY + 2f * inv * t * controlY + t * t * endY
                if (step == 0) activePath.moveTo(x, y) else activePath.lineTo(x, y)
            }

            if (clamped > 0.002f) {
                drawPath(
                    path = activePath,
                    color = CrimsonNeon.copy(alpha = 0.35f),
                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = activePath,
                    color = CrimsonGlow,
                    style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Glowing thumb dot on the curve
            val invT = 1f - clamped
            val thumbX = invT * invT * startX + 2f * invT * clamped * controlX + clamped * clamped * endX
            val thumbY = invT * invT * endY + 2f * invT * clamped * controlY + clamped * clamped * endY
            val thumbCenter = Offset(thumbX, thumbY)

            drawCircle(
                color = CrimsonNeon.copy(alpha = 0.45f),
                radius = 11.dp.toPx(),
                center = thumbCenter
            )
            drawCircle(
                color = CrimsonGlow,
                radius = 6.dp.toPx(),
                center = thumbCenter
            )
            drawCircle(
                color = CrimsonSoftPink,
                radius = 6.dp.toPx(),
                center = thumbCenter,
                style = Stroke(width = 1.4.dp.toPx())
            )
        }

        // Elapsed time, CRIMSON pill badge, duration
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = formatDurationMs(positionMs),
                style = MaterialTheme.typography.labelMedium,
                color = CrimsonSubtitle,
                modifier = Modifier.testTag("elapsed_time")
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(CrimsonBadgeBg)
                    .border(1.dp, CrimsonLineBorder, RoundedCornerShape(30.dp))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = wordmark,
                    style = MaterialTheme.typography.labelSmall,
                    color = CrimsonWhite
                )
            }

            Text(
                text = formatDurationMs(durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = CrimsonSubtitle,
                modifier = Modifier.testTag("duration_time")
            )
        }
    }
}
