package com.mightypen.tasc.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt

data class CalendarDayItem(
    val date: LocalDate,
    val dayOfMonth: Int = date.dayOfMonth,
    val dayOfWeek: String = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
    val isToday: Boolean = date == LocalDate.now()
) {
    val isoDate: String = date.toString()
}

// Inspo-exact colors
private val InspoSelectedPill = Color(0xFFD6F58F)        // Radiant pale lime/yellow
private val InspoSelectedNumber = Color(0xFF101D24)      // Dark charcoal/navy
private val InspoSelectedDay = Color(0xFF456A25)         // Olive green text for day of week
private val InspoUnselectedPill = Color(0xFF1D2D44)      // Dark slate-navy pill container
private val InspoUnselectedNumber = Color(0xFFCAD7EA)    // Crisp slate light blue number
private val InspoUnselectedDay = Color(0xFF788EA8)       // Muted slate day text
private val InspoDomeArcColor = Color(0xFF233651)        // Subtle arch divider

// Anchor date for endless, continuous loop-like rolling ribbon
private val BASE_DATE: LocalDate = LocalDate.of(2020, 1, 1)
private const val TOTAL_PAGES: Int = 50_000 // Covers ~137 years of seamless continuous rolling

@Composable
fun DateSelectorStrip(
    selectedDate: LocalDate,
    days: List<CalendarDayItem> = emptyList(),
    onDateSelected: (LocalDate) -> Unit,
    selectedPillColor: Color = InspoSelectedPill,
    selectedTextColor: Color = InspoSelectedNumber,
    domeArcColor: Color = InspoDomeArcColor,
    domeFillColor: Color = Color(0xFF0F1725),
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val currentSelectedDate by rememberUpdatedState(selectedDate)
    val currentOnDateSelected by rememberUpdatedState(onDateSelected)

    val targetInitialPage = remember(selectedDate) {
        ChronoUnit.DAYS.between(BASE_DATE, selectedDate).toInt().coerceIn(0, TOTAL_PAGES - 1)
    }

    val pagerState = rememberPagerState(
        initialPage = targetInitialPage,
        pageCount = { TOTAL_PAGES }
    )

    // When selectedDate changes externally (e.g. swipe on task screen, picker, jump to today),
    // smoothly glide the date ribbon to the target day
    LaunchedEffect(selectedDate) {
        val targetPage = ChronoUnit.DAYS.between(BASE_DATE, selectedDate).toInt().coerceIn(0, TOTAL_PAGES - 1)
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            val pageDiff = (pagerState.currentPage - targetPage).absoluteValue
            if (pageDiff > 60) {
                // If jumping very far (e.g. picked year 2028 from 2026), snap close and animate final few steps
                pagerState.scrollToPage(if (targetPage > pagerState.currentPage) targetPage - 3 else targetPage + 3)
            }
            pagerState.animateScrollToPage(
                page = targetPage,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                )
            )
        }
    }

    // When user scrolls / flings the ribbon and it settles, select the centered day
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settledPage ->
                val settledDate = BASE_DATE.plusDays(settledPage.toLong())
                if (settledDate != currentSelectedDate) {
                    currentOnDateSelected(settledDate)
                }
            }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(154.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val screenWidth = maxWidth
        val pillWidth = 54.dp
        val pillGap = 12.dp
        // Content padding so any day can roll directly into the center
        val horizontalPadding = (screenWidth - pillWidth) / 2

        // Horizon dome arc line running across below the pills, curving downward on both sides
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp)
        ) {
            val w = size.width
            val h = size.height

            val arcPath = Path().apply {
                moveTo(0f, h * 0.95f)
                quadraticTo(
                    w * 0.5f, h * 0.74f, // Apex of the dome right under the center pill
                    w, h * 0.95f
                )
            }
            val filledDomePath = Path().apply {
                moveTo(0f, h * 0.95f)
                quadraticTo(
                    w * 0.5f, h * 0.74f,
                    w, h * 0.95f
                )
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                path = filledDomePath,
                color = domeFillColor
            )
            drawPath(
                path = arcPath,
                color = domeArcColor.copy(alpha = 0.55f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Horizontal continuous rolling carousel of days
        HorizontalPager(
            state = pagerState,
            key = { page -> BASE_DATE.plusDays(page.toLong()).toString() },
            pageSize = PageSize.Fixed(pillWidth),
            pageSpacing = pillGap,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            beyondViewportPageCount = 5,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) { page ->
            val pageDate = BASE_DATE.plusDays(page.toLong())
            val day = CalendarDayItem(date = pageDate)

            // Continuous floating distance in slots from the center of the viewport
            val slotsAway = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

            // Parabolic downward drop:
            // Center (slotsAway = 0): drop = 0dp
            // 1 slot away: drop ~ 15dp
            // 2 slots away: drop ~ 46dp
            // 3+ slots away: drop ~ 60dp max
            val dropDp = (slotsAway.pow(1.65f) * 15f).coerceAtMost(60f)
            val dropPx = with(density) { dropDp.dp.toPx() }

            // Height:
            // Center is 104dp tall (noticeably taller than others, exactly like inspo)
            // 1 slot away: ~ 90dp tall
            // 2 slots away: ~ 80dp tall
            // 3+ slots away: ~ 72dp tall
            val heightDp = (104f - (slotsAway.coerceAtMost(2.5f) * 12f)).coerceAtLeast(70f)

            // Proximity: 1.0 when centered, drops to 0.0 at ~0.85 slot away
            val proximity = (1f - (slotsAway / 0.85f)).coerceIn(0f, 1f)

            Box(
                modifier = Modifier.offset { IntOffset(x = 0, y = dropPx.roundToInt()) }
            ) {
                RollingDatePill(
                    day = day,
                    height = heightDp.dp,
                    width = pillWidth,
                    proximity = proximity,
                    selectedPillColor = selectedPillColor,
                    selectedTextColor = selectedTextColor,
                    onClick = {
                        onDateSelected(day.date)
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = page,
                                animationSpec = tween(
                                    durationMillis = 380,
                                    easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RollingDatePill(
    day: CalendarDayItem,
    height: androidx.compose.ui.unit.Dp,
    width: androidx.compose.ui.unit.Dp,
    proximity: Float, // 1.0 when dead center, 0.0 when 1+ slot away
    selectedPillColor: Color = InspoSelectedPill,
    selectedTextColor: Color = InspoSelectedNumber,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Smoothly interpolate background color from dark navy to theme accent
    val backgroundColor = lerp(
        start = InspoUnselectedPill,
        stop = selectedPillColor,
        fraction = proximity
    )

    val numberColor = lerp(
        start = InspoUnselectedNumber,
        stop = selectedTextColor,
        fraction = proximity
    )

    val dayColor = lerp(
        start = InspoUnselectedDay,
        stop = selectedTextColor.copy(alpha = 0.85f),
        fraction = proximity
    )

    val numberFontSize = (17f + (23f - 17f) * proximity).sp
    val dayFontSize = (12f + (13f - 12f) * proximity).sp

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(percent = 50)) // Stadium / fully rounded capsule
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${day.dayOfMonth}",
                fontSize = numberFontSize,
                fontWeight = FontWeight.Bold,
                color = numberColor
            )
            Spacer(modifier = Modifier.height(if (proximity > 0.4f) 6.dp else 2.dp))
            Text(
                text = day.dayOfWeek,
                fontSize = dayFontSize,
                fontWeight = if (proximity > 0.4f) FontWeight.SemiBold else FontWeight.Medium,
                color = dayColor
            )
        }
    }
}
