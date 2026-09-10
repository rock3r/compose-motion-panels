package dev.letstri.motionpanels

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.hypot
import kotlin.math.roundToInt

public enum class PanelOrientation { Horizontal, Vertical }

@Stable
private class PanelGroupState(var orientation: PanelOrientation) {
    val panels = mutableStateListOf<MotionPanelState>()
    var extent by mutableIntStateOf(0)
    var density: Density = Density(1f)

    fun room(forPanel: MotionPanelState): Float =
        (extent - panels.filter { it !== forPanel }.sumOf { it.targetPx.toDouble() })
            .toFloat().coerceAtLeast(0f)
}

private val LocalPanelGroup = compositionLocalOf<PanelGroupState?> { null }

private sealed interface PanelNode {
    data class Panel(val state: MotionPanelState?, val pin: Boolean) : PanelNode
    class Separator(initialState: MotionPanelState?) : PanelNode {
        var state: MotionPanelState? by mutableStateOf(initialState)
    }
}

private data class PanelParentData(val node: PanelNode)

private class PanelParentDataModifier(private val node: PanelNode) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?): Any = PanelParentData(node)
}

private fun Modifier.panelNode(node: PanelNode): Modifier = then(PanelParentDataModifier(node))

/**
 * A one-dimensional group containing exactly one filling [MotionPanel] and up to one sized panel
 * on either side. Groups can be nested without limit.
 */
@Composable
public fun MotionPanelGroup(
    orientation: PanelOrientation = PanelOrientation.Horizontal,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val group = remember { PanelGroupState(orientation) }
    group.orientation = orientation
    group.density = LocalDensity.current
    androidx.compose.runtime.CompositionLocalProvider(LocalPanelGroup provides group) {
        Layout(content = content, modifier = modifier.clipToBounds()) { measurables, constraints ->
            val width = constraints.maxWidth
            val height = constraints.maxHeight
            val axisExtent = if (orientation == PanelOrientation.Horizontal) width else height
            group.extent = axisExtent

            val nodes = measurables.map { (it.parentData as? PanelParentData)?.node }
            val fillIndex = nodes.indexOfFirst { it is PanelNode.Panel && it.state == null }
            require(fillIndex >= 0) { "MotionPanelGroup needs one filling MotionPanel (state = null)" }

            nodes.forEachIndexed { index, node ->
                if (node is PanelNode.Panel && node.state != null) {
                    node.state.side = if (index < fillIndex) PanelSide.Start else PanelSide.End
                    val neighbour = if (index < fillIndex) nodes.getOrNull(index + 1) else nodes.getOrNull(index - 1)
                    node.state.bare = neighbour !is PanelNode.Separator
                }
            }
            nodes.forEachIndexed { index, node ->
                if (node is PanelNode.Separator && node.state == null) {
                    node.state = if (index < fillIndex) {
                        nodes.subList(0, index).asReversed()
                            .filterIsInstance<PanelNode.Panel>().firstOrNull()?.state
                    } else {
                        nodes.subList(index + 1, nodes.size)
                            .filterIsInstance<PanelNode.Panel>().firstOrNull()?.state
                    }
                }
            }

            val fixed = nodes.filterIsInstance<PanelNode.Panel>()
                .mapNotNull { it.state }
                .sumOf { it.visualPx.coerceAtLeast(0f).roundToInt() }
            val fillExtent = (axisExtent - fixed).coerceAtLeast(0)
            val placeables = measurables.mapIndexed { index, measurable ->
                when (val node = nodes[index]) {
                    is PanelNode.Panel -> {
                        val extent = node.state?.visualPx?.coerceAtLeast(0f)?.roundToInt() ?: fillExtent
                        measurable.measure(axisConstraints(orientation, extent, width, height))
                    }
                    is PanelNode.Separator -> measurable.measure(
                        if (orientation == PanelOrientation.Horizontal) {
                            Constraints(minHeight = height, maxHeight = height)
                        } else {
                            Constraints(minWidth = width, maxWidth = width)
                        },
                    )
                    null -> measurable.measure(Constraints.fixed(0, 0))
                }
            }

            layout(width, height) {
                var cursor = 0
                placeables.forEachIndexed { index, placeable ->
                    when (nodes[index]) {
                        is PanelNode.Panel -> {
                            if (orientation == PanelOrientation.Horizontal) placeable.place(cursor, 0)
                            else placeable.place(0, cursor)
                            cursor += if (orientation == PanelOrientation.Horizontal) placeable.width else placeable.height
                        }
                        is PanelNode.Separator -> {
                            if (orientation == PanelOrientation.Horizontal) placeable.place(cursor - placeable.width / 2, 0)
                            else placeable.place(0, cursor - placeable.height / 2)
                        }
                        null -> Unit
                    }
                }
            }
        }
    }
}

private fun axisConstraints(
    orientation: PanelOrientation,
    extent: Int,
    width: Int,
    height: Int,
): Constraints = if (orientation == PanelOrientation.Horizontal) {
    Constraints.fixed(extent, height)
} else {
    Constraints.fixed(width, extent)
}

/** A panel. Omit [state] for the single filling panel in a group. */
@Composable
public fun MotionPanel(
    state: MotionPanelState? = null,
    modifier: Modifier = Modifier,
    pin: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val group = checkNotNull(LocalPanelGroup.current) { "MotionPanel must be inside MotionPanelGroup" }
    val scope = rememberCoroutineScope()
    if (state != null) {
        val extent = group.extent
        // These reads make externally written state drive the synchronization side effect.
        val requestedSize = state.size
        val requestedCollapsed = state.collapsed
        DisposableEffect(group, state) {
            group.panels += state
            onDispose { group.panels -= state }
        }
        SideEffect {
            state.sync(extent.toFloat(), group.room(state), group.density, scope)
        }
    }

    val node = remember(state, pin) { PanelNode.Panel(state, pin) }
    if (state == null) {
        if (pin) {
            PinnedFill(group, modifier.panelNode(node), content)
        } else {
            Box(modifier.panelNode(node).fillMaxSize(), content = content)
        }
    } else {
        PanelContent(state, group, modifier.panelNode(node), content)
    }
}

@Composable
private fun PinnedFill(
    group: PanelGroupState,
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val folding = group.panels.firstOrNull { it.isFolding }
    Layout(
        content = { Box(Modifier.fillMaxSize(), content = content) },
        modifier = modifier.clipToBounds(),
    ) { measurables, constraints ->
        val finalExtent = if (folding == null) {
            if (group.orientation == PanelOrientation.Horizontal) constraints.maxWidth else constraints.maxHeight
        } else {
            (group.extent - group.panels.sumOf { it.targetPx.toDouble() }).toInt().coerceAtLeast(0)
        }
        val placeable = measurables.single().measure(
            axisConstraints(group.orientation, finalExtent, constraints.maxWidth, constraints.maxHeight),
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            val trailing = folding?.side == PanelSide.Start
            val x = if (group.orientation == PanelOrientation.Horizontal && trailing) constraints.maxWidth - placeable.width else 0
            val y = if (group.orientation == PanelOrientation.Vertical && trailing) constraints.maxHeight - placeable.height else 0
            placeable.place(x, y)
        }
    }
}

@Composable
private fun PanelContent(
    state: MotionPanelState,
    group: PanelGroupState,
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.clipToBounds()) {
        Layout(
            content = { Box(Modifier.fillMaxSize(), content = content) },
            modifier = Modifier.fillMaxSize(),
        ) { measurables, constraints ->
            val contentExtent = state.contentPx.coerceAtLeast(0f).roundToInt()
            val childConstraints = axisConstraints(
                group.orientation,
                contentExtent,
                constraints.maxWidth,
                constraints.maxHeight,
            )
            val placeable = measurables.single().measure(childConstraints)
            layout(constraints.maxWidth, constraints.maxHeight) {
                val trailing = state.side == PanelSide.Start
                val x = if (group.orientation == PanelOrientation.Horizontal && trailing) constraints.maxWidth - placeable.width else 0
                val y = if (group.orientation == PanelOrientation.Vertical && trailing) constraints.maxHeight - placeable.height else 0
                placeable.place(x, y)
            }
        }
        if (state.bare) {
            // Bare panels expose the same invisible edge grip as the reference implementation.
            MotionPanelEdgeGrip(state, group)
        }
    }
}

@Composable
private fun BoxScope.MotionPanelEdgeGrip(state: MotionPanelState, group: PanelGroupState) {
    val alignmentModifier = if (group.orientation == PanelOrientation.Horizontal) {
        Modifier
            .width(20.dp)
            .fillMaxHeight()
            .align(if (state.side == PanelSide.Start) androidx.compose.ui.Alignment.CenterEnd else androidx.compose.ui.Alignment.CenterStart)
    } else {
        Modifier
            .height(20.dp)
            .fillMaxWidth()
            .align(if (state.side == PanelSide.Start) androidx.compose.ui.Alignment.BottomCenter else androidx.compose.ui.Alignment.TopCenter)
    }
    Box(alignmentModifier.grip(state, group, "Resize panel"))
}

/** A zero-layout-width grip centred over the seam. */
@Composable
public fun MotionPanelSeparator(
    modifier: Modifier = Modifier,
    state: MotionPanelState? = null,
    thickness: Dp = 14.dp,
    color: Color = Color(0xFF94A3B8),
    activeColor: Color = Color(0xFF2563EB),
    contentDescription: String = "Resize panel",
) {
    val group = checkNotNull(LocalPanelGroup.current) { "MotionPanelSeparator must be inside MotionPanelGroup" }
    val node = remember(state) { PanelNode.Separator(state) }
    val actualState = node.state ?: state
    val sized = if (group.orientation == PanelOrientation.Horizontal) {
        modifier.width(thickness).fillMaxHeight()
    } else {
        modifier.height(thickness).fillMaxWidth()
    }
    Box(
        sized
            .panelNode(node)
            .then(if (actualState == null) Modifier else Modifier.grip(actualState, group, contentDescription))
            .background(if (actualState?.isDragging == true) activeColor else color),
    )
}

private data class GripEntry(
    val key: Any,
    val state: MotionPanelState,
    val group: PanelGroupState,
    var bounds: Rect = Rect.Zero,
)

private object GripRegistry {
    val entries = mutableListOf<GripEntry>()

    fun at(point: Offset, margin: Float): List<GripEntry> {
        val hits = entries.filter { it.bounds.inflate(margin).contains(point) }
        return if (hits.size > 1) hits else emptyList()
    }
}

private fun Rect.inflate(value: Float): Rect = Rect(left - value, top - value, right + value, bottom + value)

@Composable
private fun Modifier.grip(
    state: MotionPanelState,
    group: PanelGroupState,
    description: String,
): Modifier {
    val scope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }
    val key = remember { Any() }
    val entry = remember(state, group) { GripEntry(key, state, group) }
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    DisposableEffect(entry) {
        GripRegistry.entries += entry
        onDispose { GripRegistry.entries -= entry }
    }

    fun sign(target: GripEntry): Float {
        var value = if (target.state.side == PanelSide.Start) 1f else -1f
        if (target.group.orientation == PanelOrientation.Horizontal && layoutDirection == LayoutDirection.Rtl) value *= -1f
        return value
    }

    fun keyEvent(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false
        if (event.key == Key.Enter) {
            state.collapsed = !state.collapsed
            return true
        }
        val fast = event.isShiftPressed || event.key == Key.PageUp || event.key == Key.PageDown
        val step = if (fast) 50f else 10f
        val room = group.room(state)
        return when (event.key) {
            Key.DirectionRight, Key.DirectionDown -> { state.resizeBy(step * sign(entry), room); true }
            Key.DirectionLeft, Key.DirectionUp -> { state.resizeBy(-step * sign(entry), room); true }
            Key.PageDown -> { state.resizeBy(step * sign(entry), room); true }
            Key.PageUp -> { state.resizeBy(-step * sign(entry), room); true }
            Key.MoveHome -> { state.resizeToEnd(room, false); true }
            Key.MoveEnd -> { state.resizeToEnd(room, true); true }
            Key.Escape -> { state.cancelDrag(); true }
            else -> false
        }
    }

    return this
        .onGloballyPositioned {
            coordinates = it
            entry.bounds = it.boundsInWindow()
        }
        .pointerInput(entry, layoutDirection) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                focusRequester.requestFocus()
                val origin = coordinates?.positionInWindow() ?: Offset.Zero
                val press = origin + down.position
                val crossed = GripRegistry.at(press, with(density) { 5.dp.toPx() })
                val targets = if (crossed.isEmpty()) listOf(entry) else crossed.distinctBy { it.state }
                var dragging = false
                val pointer: PointerId = down.id
                while (true) {
                    val event = awaitPointerEvent()
                    val change: PointerInputChange = event.changes.firstOrNull { it.id == pointer } ?: break
                    val current = origin + change.position
                    val delta = current - press
                    if (!dragging && hypot(delta.x, delta.y) >= with(density) { 3.dp.toPx() }) {
                        dragging = true
                        targets.forEach { it.state.startDrag(it.group.room(it.state)) }
                    }
                    if (dragging) {
                        targets.forEach { target ->
                            val axis = if (target.group.orientation == PanelOrientation.Horizontal) delta.x else delta.y
                            target.state.dragBy(axis, sign(target))
                        }
                        change.consume()
                    }
                    if (!change.pressed) {
                        if (dragging) targets.forEach { it.state.endDrag(scope) }
                        break
                    }
                }
            }
        }
        .pointerInput(state) { detectTapGestures(onDoubleTap = { state.reset() }) }
        .onPreviewKeyEvent(::keyEvent)
        .semantics {
            contentDescription = description
            val bounds = state.bounds(group.room(state))
            progressBarRangeInfo = ProgressBarRangeInfo(state.targetPx, bounds)
        }
        .focusRequester(focusRequester)
        .focusable()
}
