package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BackdropStyle
import com.example.ui.components.CrimsonBackdrop
import com.example.ui.components.CrimsonPrimaryCtaButton
import com.example.ui.theme.CrimsonAccentBar
import com.example.ui.theme.CrimsonActiveIcon
import com.example.ui.theme.CrimsonRoseText
import com.example.ui.theme.CrimsonSubtitle
import com.example.ui.theme.CrimsonWhite

@Composable
fun OnboardingScreen(
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CrimsonBackdrop(style = BackdropStyle.ONBOARDING)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Text(
                text = stringResource(R.string.brand_header),
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 3.8.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = CrimsonRoseText,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 8.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(CrimsonAccentBar)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = stringResource(R.string.onboard_headline),
                        style = MaterialTheme.typography.displayMedium,
                        color = CrimsonWhite
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.onboard_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = CrimsonSubtitle
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Indicator dots: ● ━ ●
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(CrimsonActiveIcon)
                    )
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(CrimsonActiveIcon)
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(50))
                            .background(CrimsonActiveIcon.copy(alpha = 0.65f))
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                CrimsonPrimaryCtaButton(
                    onClick = onExploreClick,
                    modifier = Modifier.testTag("explore_music_button")
                ) {
                    Text(
                        text = stringResource(R.string.onboard_cta),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        color = CrimsonWhite
                    )
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = stringResource(R.string.onboard_cta),
                        tint = CrimsonWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
