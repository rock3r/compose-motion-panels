package dev.letstri.motionpanels

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.round

public val MotionPanelAnimationSpec: AnimationSpec<Float> =
    tween(durationMillis = 250, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f))

/** Hoistable state for a sized panel. Size and collapse changes are observable and writable. */
@Stable
public class MotionPanelState(
    initialSize: PanelSize,
    public val minSize: PanelSize = PanelSize.Fixed(0.dp),
    public val maxSize: PanelSize? = null,
    initiallyCollapsed: Boolean = false,
    public val defaultSize: PanelSize = initialSize,
) {
    public var size: PanelSize by mutableStateOf(initialSize)
    public var collapsed: Boolean by mutableStateOf(initiallyCollapsed)

    public var isDragging: Boolean by mutableStateOf(false)
        internal set
    public var isFolding: Boolean by mutableStateOf(false)
        internal set

    internal var visualPx by mutableFloatStateOf(0f)
    internal var contentPx by mutableFloatStateOf(0f)
    internal var targetPx by mutableFloatStateOf(0f)
    internal var side: PanelSide? by mutableStateOf(null)
    internal var bare by mutableStateOf(false)
    internal var totalPx = 0f
    internal var density: Density = Density(1f)
    private var initialized = false
    private var animation: Job? = null
    private var dragStart = 0f
    private var dragMin = 0f
    private var dragMax = 0f
    private var dragCollapsed = false
    private var wasCollapsed = false

    internal fun sync(total: Float, room: Float, density: Density, scope: CoroutineScope) {
        this.totalPx = total
        this.density = density
        val measured = size.toPx(total, density)
        val next = if (collapsed) 0f else measured
        if (!initialized) {
            initialized = true
            targetPx = next
            visualPx = next
            contentPx = measured
            return
        }
        if (isDragging || next == targetPx) return
        val from = targetPx
        targetPx = next
        if (visualPx == next) {
            contentPx = measured
            isFolding = false
        } else {
            foldTo(next, from, scope)
        }
    }

    private fun foldTo(to: Float, from: Float, scope: CoroutineScope) {
        animation?.cancel()
        isFolding = true
        val closed = visualPx == 0f
        if (closed) contentPx = to
        animation = scope.launch {
            if (!closed && to > 0f) {
                launch {
                    animate(contentPx, to, animationSpec = MotionPanelAnimationSpec) { value, _ ->
                        contentPx = value
                    }
                }
            }
            // Kept separate from content to preserve the reference's clipped fold behavior.
            animate(visualPx, to, animationSpec = MotionPanelAnimationSpec) { value, _ ->
                visualPx = value
            }
            if (visualPx == targetPx) isFolding = false
        }
    }

    internal fun bounds(room: Float = totalPx): ClosedFloatingPointRange<Float> {
        val available = room.coerceAtLeast(0f)
        val min = minSize.toPx(totalPx, density).coerceAtMost(available)
        val requestedMax = maxSize?.toPx(totalPx, density) ?: available
        return min..requestedMax.coerceIn(min, available)
    }

    internal fun startDrag(room: Float) {
        animation?.cancel()
        val bounds = bounds(room)
        dragStart = visualPx
        dragMin = bounds.start
        dragMax = bounds.endInclusive
        dragCollapsed = collapsed
        wasCollapsed = collapsed
        isFolding = false
        isDragging = true
    }

    internal fun dragBy(axisOffset: Float, growSign: Float, canCollapse: Boolean = true) {
        if (!isDragging) return
        val raw = dragStart + axisOffset * growSign
        val nextCollapsed = canCollapse && raw < dragMin / 2f
        val next = if (nextCollapsed) 0f else round(raw.coerceIn(dragMin, dragMax))
        collapsed = nextCollapsed
        dragCollapsed = nextCollapsed
        visualPx = next
        if (!nextCollapsed) contentPx = next
    }

    internal fun endDrag(scope: CoroutineScope) {
        if (!isDragging) return
        isDragging = false
        if (!dragCollapsed) {
            size = size.fromPx(visualPx, totalPx, density)
            targetPx = visualPx
        }
        if (visualPx != targetPx) foldTo(targetPx, visualPx, scope)
    }

    internal fun cancelDrag() {
        if (!isDragging) return
        animation?.cancel()
        visualPx = dragStart
        contentPx = dragStart
        collapsed = wasCollapsed
        isDragging = false
    }

    public fun reset() {
        collapsed = false
        size = defaultSize
    }

    internal fun resizeBy(delta: Float, room: Float) {
        val range = bounds(room)
        val next = (targetPx + delta).coerceIn(range.start, range.endInclusive)
        if (collapsed && next > 0f) collapsed = false
        size = size.fromPx(next, totalPx, density)
    }

    internal fun resizeToEnd(room: Float, maximum: Boolean) {
        val range = bounds(room)
        val next = if (maximum) range.endInclusive else range.start
        if (collapsed && next > 0f) collapsed = false
        size = size.fromPx(next, totalPx, density)
    }
}

@Composable
public fun rememberMotionPanelState(
    initialSize: PanelSize,
    minSize: PanelSize = PanelSize.Fixed(0.dp),
    maxSize: PanelSize? = null,
    initiallyCollapsed: Boolean = false,
    defaultSize: PanelSize = initialSize,
): MotionPanelState = remember(initialSize, minSize, maxSize, defaultSize) {
    MotionPanelState(initialSize, minSize, maxSize, initiallyCollapsed, defaultSize)
}

internal enum class PanelSide { Start, End }
