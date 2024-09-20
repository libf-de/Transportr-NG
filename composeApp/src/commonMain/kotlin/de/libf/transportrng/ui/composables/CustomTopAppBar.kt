/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2024 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.libf.transportrng.ui.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.util.fastFirst
import de.libf.transportrng.ui.trips.invert
import de.libf.transportrng.ui.trips.mapRange
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt


@ExperimentalMaterial3Api
@Composable
fun CustomSmallTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    height: Dp = TopAppBarSmallTokens.ContainerHeight,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    SingleRowTopAppBar(
        modifier = modifier,
        title = title,
        titleTextStyle = MaterialTheme.typography.fromToken(TopAppBarSmallTokens.HeadlineFont),
        centeredTitle = false,
        navigationIcon = navigationIcon,
        actions = actions,
        windowInsets = windowInsets,
        colors = colors,
        heightDp = height,
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomLargeTopAppBar(
    title: @Composable () -> Unit,
    expandedTitle: @Composable () -> Unit = title,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.largeTopAppBarColors(),
    expandedHeight: Dp = TopAppBarLargeTokens.ContainerHeight,
    collapsedHeight: Dp = TopAppBarSmallTokens.ContainerHeight,
    expandedRadius: Dp = 0.dp,
    collapsedRadius: Dp = 0.dp,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TwoRowsTopAppBar(
        title = expandedTitle,
        titleTextStyle = MaterialTheme.typography.fromToken(TopAppBarLargeTokens.HeadlineFont),
        smallTitleTextStyle = MaterialTheme.typography.fromToken(TopAppBarSmallTokens.HeadlineFont),
        titleBottomPadding = LargeTitleBottomPadding,
        smallTitle = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        windowInsets = windowInsets,
        maxHeight = expandedHeight,
        pinnedHeight = collapsedHeight,
        scrollBehavior = scrollBehavior,
        expandedRadius = expandedRadius,
        collapsedRadius = collapsedRadius
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleRowTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    titleTextStyle: TextStyle,
    centeredTitle: Boolean,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    windowInsets: WindowInsets,
    colors: TopAppBarColors,
    heightDp: Dp = TopAppBarSmallTokens.ContainerHeight,
    scrollBehavior: TopAppBarScrollBehavior?
) {
    // Sets the app bar's height offset to collapse the entire bar's height when content is
    // scrolled.
    val heightOffsetLimit =
        with(LocalDensity.current) { -heightDp.toPx() }
    SideEffect {
        if (scrollBehavior?.state?.heightOffsetLimit != heightOffsetLimit) {
            scrollBehavior?.state?.heightOffsetLimit = heightOffsetLimit
        }
    }

    // Obtain the container color from the TopAppBarColors using the `overlapFraction`. This
    // ensures that the colors will adjust whether the app bar behavior is pinned or scrolled.
    // This may potentially animate or interpolate a transition between the container-color and the
    // container's scrolled-color according to the app bar's scroll state.
    val colorTransitionFraction = scrollBehavior?.state?.overlappedFraction ?: 0f
    val fraction = if (colorTransitionFraction > 0.01f) 1f else 0f
    val appBarContainerColor by animateColorAsState(
        targetValue = colors.containerColor(fraction),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
    )

    // Wrap the given actions in a Row.
    val actionsRow = @Composable {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }

    // Set up support for resizing the top app bar when vertically dragging the bar itself.
    val appBarDragModifier = if (scrollBehavior != null && !scrollBehavior.isPinned) {
        Modifier.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
                scrollBehavior.state.heightOffset = scrollBehavior.state.heightOffset + delta
            },
            onDragStopped = { velocity ->
                settleAppBar(
                    scrollBehavior.state,
                    velocity,
                    scrollBehavior.flingAnimationSpec,
                    scrollBehavior.snapAnimationSpec
                )
            }
        )
    } else {
        Modifier
    }

    // Compose a Surface with a TopAppBarLayout content.
    // The surface's background color is animated as specified above.
    // The height of the app bar is determined by subtracting the bar's height offset from the
    // app bar's defined constant height value (i.e. the ContainerHeight token).
    Surface(modifier = modifier.then(appBarDragModifier), color = appBarContainerColor) {
        val height = LocalDensity.current.run {
            heightDp.toPx() + (scrollBehavior?.state?.heightOffset
                ?: 0f)
        }
        TopAppBarLayout(
            modifier = Modifier
                .windowInsetsPadding(windowInsets)
                // clip after padding so we don't show the title over the inset area
                .clipToBounds(),
            heightPx = height,
            navigationIconContentColor = colors.navigationIconContentColor,
            titleContentColor = colors.titleContentColor,
            actionIconContentColor = colors.actionIconContentColor,
            title = title,
            titleTextStyle = titleTextStyle,
            titleAlpha = 1f,
            titleVerticalArrangement = Arrangement.Center,
            titleHorizontalArrangement =
            if (centeredTitle) Arrangement.Center else Arrangement.Start,
            titleBottomPadding = 0,
            hideTitleSemantics = false,
            navigationIcon = navigationIcon,
            actions = actionsRow,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TwoRowsTopAppBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    titleTextStyle: TextStyle,
    titleBottomPadding: Dp,
    smallTitle: @Composable () -> Unit,
    smallTitleTextStyle: TextStyle,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    windowInsets: WindowInsets,
    colors: TopAppBarColors,
    maxHeight: Dp,
    pinnedHeight: Dp,
    scrollBehavior: TopAppBarScrollBehavior?,
    expandedRadius: Dp = 0.dp,
    collapsedRadius: Dp = 0.dp
) {
    if (maxHeight <= pinnedHeight) {
        throw IllegalArgumentException(
            "A TwoRowsTopAppBar max height should be greater than its pinned height"
        )
    }
    val pinnedHeightPx: Float
    val maxHeightPx: Float
    val titleBottomPaddingPx: Int
    LocalDensity.current.run {
        pinnedHeightPx = pinnedHeight.toPx()
        maxHeightPx = maxHeight.toPx()
        titleBottomPaddingPx = titleBottomPadding.roundToPx()
    }

    // Sets the app bar's height offset limit to hide just the bottom title area and keep top title
    // visible when collapsed.
    SideEffect {
        if (scrollBehavior?.state?.heightOffsetLimit != pinnedHeightPx - maxHeightPx) {
            scrollBehavior?.state?.heightOffsetLimit = pinnedHeightPx - maxHeightPx
        }
    }

    // Obtain the container Color from the TopAppBarColors using the `collapsedFraction`, as the
    // bottom part of this TwoRowsTopAppBar changes color at the same rate the app bar expands or
    // collapse.
    // This will potentially animate or interpolate a transition between the container color and the
    // container's scrolled color according to the app bar's scroll state.
    val colorTransitionFraction = scrollBehavior?.state?.collapsedFraction ?: 0f
    val appBarContainerColor = colors.containerColor(colorTransitionFraction)

    // Wrap the given actions in a Row.
    val actionsRow = @Composable {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
    val topTitleAlpha = TopTitleAlphaEasing.transform(colorTransitionFraction)
    val bottomTitleAlpha = 1f - colorTransitionFraction
    // Hide the top row title semantics when its alpha value goes below 0.5 threshold.
    // Hide the bottom row title semantics when the top title semantics are active.
    val hideTopRowSemantics = colorTransitionFraction < 0.5f
    val hideBottomRowSemantics = !hideTopRowSemantics

    // Set up support for resizing the top app bar when vertically dragging the bar itself.
    val appBarDragModifier = if (scrollBehavior != null && !scrollBehavior.isPinned) {
        Modifier.draggable(
            orientation = Orientation.Vertical,
            state = rememberDraggableState { delta ->
                scrollBehavior.state.heightOffset = scrollBehavior.state.heightOffset + delta
            },
            onDragStopped = { velocity ->
                settleAppBar(
                    scrollBehavior.state,
                    velocity,
                    scrollBehavior.flingAnimationSpec,
                    scrollBehavior.snapAnimationSpec
                )
            }
        )
    } else {
        Modifier
    }


    val btmRadius = colorTransitionFraction.invert().mapRangeDp(collapsedRadius, expandedRadius)
    Surface(
        modifier = modifier.then(appBarDragModifier),
        color = appBarContainerColor,
        shape = RoundedCornerShape(
            bottomEnd = btmRadius,
            bottomStart = btmRadius
        )
    ) {
        Column {
            TopAppBarLayout(
                modifier = Modifier
                    .windowInsetsPadding(windowInsets)
                    // clip after padding so we don't show the title over the inset area
                    .clipToBounds(),
                heightPx = pinnedHeightPx,
                navigationIconContentColor =
                colors.navigationIconContentColor,
                titleContentColor = colors.titleContentColor,
                actionIconContentColor =
                colors.actionIconContentColor,
                title = smallTitle,
                titleTextStyle = smallTitleTextStyle,
                titleAlpha = topTitleAlpha,
                titleVerticalArrangement = Arrangement.Center,
                titleHorizontalArrangement = Arrangement.Start,
                titleBottomPadding = 0,
                hideTitleSemantics = hideTopRowSemantics,
                navigationIcon = navigationIcon,
                actions = actionsRow,
            )
            TopAppBarLayout(
                modifier = Modifier
                    // only apply the horizontal sides of the window insets padding, since the top
                    // padding will always be applied by the layout above
                    .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Horizontal))
                    .clipToBounds(),
                heightPx = maxHeightPx - pinnedHeightPx + (scrollBehavior?.state?.heightOffset
                    ?: 0f),
                navigationIconContentColor =
                colors.navigationIconContentColor,
                titleContentColor = colors.titleContentColor,
                actionIconContentColor =
                colors.actionIconContentColor,
                title = title,
                titleTextStyle = titleTextStyle,
                titleAlpha = bottomTitleAlpha,
                titleVerticalArrangement = Arrangement.Bottom,
                titleHorizontalArrangement = Arrangement.Start,
                titleBottomPadding = titleBottomPaddingPx,
                hideTitleSemantics = hideBottomRowSemantics,
                navigationIcon = {},
                actions = {}
            )
        }
    }
}




@Composable
private fun TopAppBarLayout(
    modifier: Modifier,
    heightPx: Float,
    navigationIconContentColor: Color,
    titleContentColor: Color,
    actionIconContentColor: Color,
    title: @Composable () -> Unit,
    titleTextStyle: TextStyle,
    titleAlpha: Float,
    titleVerticalArrangement: Arrangement.Vertical,
    titleHorizontalArrangement: Arrangement.Horizontal,
    titleBottomPadding: Int,
    hideTitleSemantics: Boolean,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable () -> Unit,
) {
    Layout(
        {
            Box(
                Modifier
                    .layoutId("navigationIcon")
                    .padding(start = TopAppBarHorizontalPadding)
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides navigationIconContentColor,
                    content = navigationIcon
                )
            }
            Box(
                Modifier
                    .layoutId("title")
                    .padding(horizontal = TopAppBarHorizontalPadding)
                    .then(if (hideTitleSemantics) Modifier.clearAndSetSemantics { } else Modifier)
                    .graphicsLayer(alpha = titleAlpha)
            ) {
                ProvideContentColorTextStyle(
                    contentColor = titleContentColor,
                    textStyle = titleTextStyle,
                    content = title)
            }
            Box(
                Modifier
                    .layoutId("actionIcons")
                    .padding(end = TopAppBarHorizontalPadding)
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides actionIconContentColor,
                    content = actions
                )
            }
        },
        modifier = modifier
    ) { measurables, constraints ->
        val navigationIconPlaceable =
            measurables.fastFirst { it.layoutId == "navigationIcon" }
                .measure(constraints.copy(minWidth = 0))
        val actionIconsPlaceable =
            measurables.fastFirst { it.layoutId == "actionIcons" }
                .measure(constraints.copy(minWidth = 0))

        val maxTitleWidth = if (constraints.maxWidth == Constraints.Infinity) {
            constraints.maxWidth
        } else {
            (constraints.maxWidth - navigationIconPlaceable.width - actionIconsPlaceable.width)
                .coerceAtLeast(0)
        }
        val titlePlaceable =
            measurables.fastFirst { it.layoutId == "title" }
                .measure(constraints.copy(minWidth = 0, maxWidth = maxTitleWidth))

        // Locate the title's baseline.
        val titleBaseline =
            if (titlePlaceable[LastBaseline] != AlignmentLine.Unspecified) {
                titlePlaceable[LastBaseline]
            } else {
                0
            }

        val layoutHeight = if (heightPx.isNaN()) 0 else heightPx.roundToInt()

        layout(constraints.maxWidth, layoutHeight) {
            // Navigation icon
            navigationIconPlaceable.placeRelative(
                x = 0,
                y = (layoutHeight - navigationIconPlaceable.height) / 2
            )

            // Title
            titlePlaceable.placeRelative(
                x = when (titleHorizontalArrangement) {
                    Arrangement.Center -> {
                        var baseX = (constraints.maxWidth - titlePlaceable.width) / 2
                        if (baseX < navigationIconPlaceable.width) {
                            // May happen if the navigation is wider than the actions and the
                            // title is long. In this case, prioritize showing more of the title by
                            // offsetting it to the right.
                            baseX += (navigationIconPlaceable.width - baseX)
                        } else if (baseX + titlePlaceable.width >
                            constraints.maxWidth - actionIconsPlaceable.width
                        ) {
                            // May happen if the actions are wider than the navigation and the title
                            // is long. In this case, offset to the left.
                            baseX += ((constraints.maxWidth - actionIconsPlaceable.width) -
                                    (baseX + titlePlaceable.width))
                        }
                        baseX
                    }

                    Arrangement.End ->
                        constraints.maxWidth - titlePlaceable.width - actionIconsPlaceable.width
                    // Arrangement.Start.
                    // An TopAppBarTitleInset will make sure the title is offset in case the
                    // navigation icon is missing.
                    else -> max(TopAppBarTitleInset.roundToPx(), navigationIconPlaceable.width)
                },
                y = when (titleVerticalArrangement) {
                    Arrangement.Center -> (layoutHeight - titlePlaceable.height) / 2
                    // Apply bottom padding from the title's baseline only when the Arrangement is
                    // "Bottom".
                    Arrangement.Bottom ->
                        if (titleBottomPadding == 0) layoutHeight - titlePlaceable.height
                        else layoutHeight - titlePlaceable.height - max(
                            0,
                            titleBottomPadding - titlePlaceable.height + titleBaseline
                        )
                    // Arrangement.Top
                    else -> 0
                }
            )

            // Action icons
            actionIconsPlaceable.placeRelative(
                x = constraints.maxWidth - actionIconsPlaceable.width,
                y = (layoutHeight - actionIconsPlaceable.height) / 2
            )
        }
    }
}

@Composable
internal fun ProvideContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit
) {
    val mergedStyle = LocalTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides mergedStyle,
        content = content
    )
}


private val LargeTitleBottomPadding = 28.dp
private val TopAppBarHorizontalPadding = 4.dp

// A title inset when the App-Bar is a Medium or Large one. Also used to size a spacer when the
// navigation icon is missing.
private val TopAppBarTitleInset = 16.dp - TopAppBarHorizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
private suspend fun settleAppBar(
    state: TopAppBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?
): Velocity {
    // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
    // and just return Zero Velocity.
    // Note that we don't check for 0f due to float precision with the collapsedFraction
    // calculation.
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapse the app bar.
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(
            initialValue = 0f,
            initialVelocity = velocity,
        )
            .animateDecay(flingAnimationSpec) {
                val delta = value - lastValue
                val initialHeightOffset = state.heightOffset
                state.heightOffset = initialHeightOffset + delta
                val consumed = abs(initialHeightOffset - state.heightOffset)
                lastValue = value
                remainingVelocity = this.velocity
                // avoid rounding errors and stop if anything is unconsumed
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
    }
    // Snap if animation specs were provided.
    if (snapAnimationSpec != null) {
        if (state.heightOffset < 0 &&
            state.heightOffset > state.heightOffsetLimit
        ) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.heightOffsetLimit
                },
                animationSpec = snapAnimationSpec
            ) { state.heightOffset = value }
        }
    }

    return Velocity(0f, remainingVelocity)
}

internal val TopTitleAlphaEasing = CubicBezierEasing(.8f, 0f, .8f, .15f)

@OptIn(ExperimentalMaterial3Api::class)
internal fun TopAppBarColors.containerColor(colorTransitionFraction: Float): Color {
    return lerp(
        containerColor,
        scrolledContainerColor,
        FastOutLinearInEasing.transform(colorTransitionFraction)
    )
}

internal enum class ColorSchemeKeyTokens {
    Background,
    Error,
    ErrorContainer,
    InverseOnSurface,
    InversePrimary,
    InverseSurface,
    OnBackground,
    OnError,
    OnErrorContainer,
    OnPrimary,
    OnPrimaryContainer,
    OnPrimaryFixed,
    OnPrimaryFixedVariant,
    OnSecondary,
    OnSecondaryContainer,
    OnSecondaryFixed,
    OnSecondaryFixedVariant,
    OnSurface,
    OnSurfaceVariant,
    OnTertiary,
    OnTertiaryContainer,
    OnTertiaryFixed,
    OnTertiaryFixedVariant,
    Outline,
    OutlineVariant,
    Primary,
    PrimaryContainer,
    PrimaryFixed,
    PrimaryFixedDim,
    Scrim,
    Secondary,
    SecondaryContainer,
    SecondaryFixed,
    SecondaryFixedDim,
    Surface,
    SurfaceBright,
    SurfaceContainer,
    SurfaceContainerHigh,
    SurfaceContainerHighest,
    SurfaceContainerLow,
    SurfaceContainerLowest,
    SurfaceDim,
    SurfaceTint,
    SurfaceVariant,
    Tertiary,
    TertiaryContainer,
    TertiaryFixed,
    TertiaryFixedDim,
}

internal object ElevationTokens {
    val Level0 = 0.0.dp
    val Level1 = 1.0.dp
    val Level2 = 3.0.dp
    val Level3 = 6.0.dp
    val Level4 = 8.0.dp
    val Level5 = 12.0.dp
}

internal enum class ShapeKeyTokens {
    CornerExtraLarge,
    CornerExtraLargeTop,
    CornerExtraSmall,
    CornerExtraSmallTop,
    CornerFull,
    CornerLarge,
    CornerLargeEnd,
    CornerLargeTop,
    CornerMedium,
    CornerNone,
    CornerSmall,
}

internal object TopAppBarLargeTokens {
    val ContainerColor = ColorSchemeKeyTokens.Surface
    val ContainerElevation = ElevationTokens.Level0
    val ContainerHeight = 152.0.dp
    val ContainerShape = ShapeKeyTokens.CornerNone
    val ContainerSurfaceTintLayerColor = ColorSchemeKeyTokens.SurfaceTint
    val HeadlineColor = ColorSchemeKeyTokens.OnSurface
    val HeadlineFont = TypographyKeyTokens.HeadlineMedium
    val LeadingIconColor = ColorSchemeKeyTokens.OnSurface
    val LeadingIconSize = 24.0.dp
    val TrailingIconColor = ColorSchemeKeyTokens.OnSurfaceVariant
    val TrailingIconSize = 24.0.dp
}

internal object TopAppBarSmallTokens {
    val ContainerColor = ColorSchemeKeyTokens.Surface
    val ContainerElevation = ElevationTokens.Level0
    val ContainerHeight = 64.0.dp
    val ContainerShape = ShapeKeyTokens.CornerNone
    val ContainerSurfaceTintLayerColor = ColorSchemeKeyTokens.SurfaceTint
    val HeadlineColor = ColorSchemeKeyTokens.OnSurface
    val HeadlineFont = TypographyKeyTokens.TitleLarge
    val LeadingIconColor = ColorSchemeKeyTokens.OnSurface
    val LeadingIconSize = 24.0.dp
    val OnScrollContainerElevation = ElevationTokens.Level2
    val TrailingIconColor = ColorSchemeKeyTokens.OnSurfaceVariant
    val TrailingIconSize = 24.0.dp
}


internal enum class TypographyKeyTokens {
    BodyLarge,
    BodyMedium,
    BodySmall,
    DisplayLarge,
    DisplayMedium,
    DisplaySmall,
    HeadlineLarge,
    HeadlineMedium,
    HeadlineSmall,
    LabelLarge,
    LabelMedium,
    LabelSmall,
    TitleLarge,
    TitleMedium,
    TitleSmall,
}

internal fun Typography.fromToken(value: TypographyKeyTokens): TextStyle {
    return when (value) {
        TypographyKeyTokens.DisplayLarge -> displayLarge
        TypographyKeyTokens.DisplayMedium -> displayMedium
        TypographyKeyTokens.DisplaySmall -> displaySmall
        TypographyKeyTokens.HeadlineLarge -> headlineLarge
        TypographyKeyTokens.HeadlineMedium -> headlineMedium
        TypographyKeyTokens.HeadlineSmall -> headlineSmall
        TypographyKeyTokens.TitleLarge -> titleLarge
        TypographyKeyTokens.TitleMedium -> titleMedium
        TypographyKeyTokens.TitleSmall -> titleSmall
        TypographyKeyTokens.BodyLarge -> bodyLarge
        TypographyKeyTokens.BodyMedium -> bodyMedium
        TypographyKeyTokens.BodySmall -> bodySmall
        TypographyKeyTokens.LabelLarge -> labelLarge
        TypographyKeyTokens.LabelMedium -> labelMedium
        TypographyKeyTokens.LabelSmall -> labelSmall
    }
}

fun Float.mapRangeDp(min: Dp, max: Dp): Dp = (this * (max - min)) + min