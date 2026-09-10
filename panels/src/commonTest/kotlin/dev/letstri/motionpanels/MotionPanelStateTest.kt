package dev.letstri.motionpanels

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class MotionPanelStateTest {
    private val density = Density(1f)

    @Test
    fun collapseThresholdIsStrictlyBelowHalfTheMinimum() = runTest {
        val state = state(initial = 200f, min = 100f)
        state.sync(total = 1_000f, room = 1_000f, density, this)
        state.startDrag(room = 1_000f)

        state.dragBy(axisOffset = -150f, growSign = 1f)
        assertFalse(state.collapsed, "exactly half of minSize must remain open")
        assertEquals(100f, state.visualPx)

        state.dragBy(axisOffset = -151f, growSign = 1f)
        assertTrue(state.collapsed)
        assertEquals(0f, state.visualPx)
        assertEquals(100f, state.contentPx, "collapsed content keeps its last open extent")
    }

    @Test
    fun percentagePanelsReportPercentagesAfterDragging() = runTest {
        val state = MotionPanelState(
            initialSize = PanelSize.Fraction(.25f),
            minSize = PanelSize.Fraction(.1f),
            maxSize = PanelSize.Fraction(.6f),
            initiallyCollapsed = false,
            defaultSize = PanelSize.Fraction(.25f),
        )
        state.sync(total = 1_000f, room = 1_000f, density, this)
        state.startDrag(room = 1_000f)
        state.dragBy(axisOffset = 123f, growSign = 1f)
        state.endDrag(this)

        assertEquals(.373f, assertIs<PanelSize.Fraction>(state.size).value)
    }

    @Test
    fun boundsReserveTheOtherPanelsRoom() = runTest {
        val state = state(initial = 300f, min = 200f, max = 900f)
        state.sync(total = 1_000f, room = 450f, density, this)

        assertEquals(200f..450f, state.bounds(room = 450f))
    }

    @Test
    fun cancelRestoresSizeAndCollapseState() = runTest {
        val state = state(initial = 240f, min = 120f)
        state.sync(total = 800f, room = 800f, density, this)
        state.startDrag(room = 800f)
        state.dragBy(axisOffset = -220f, growSign = 1f)
        assertTrue(state.collapsed)

        state.cancelDrag()

        assertFalse(state.collapsed)
        assertFalse(state.isDragging)
        assertEquals(240f, state.visualPx)
        assertEquals(240f, state.contentPx)
    }

    private fun state(initial: Float, min: Float, max: Float? = null) = MotionPanelState(
        initialSize = PanelSize.Fixed(initial.dp),
        minSize = PanelSize.Fixed(min.dp),
        maxSize = max?.let { PanelSize.Fixed(it.dp) },
        initiallyCollapsed = false,
        defaultSize = PanelSize.Fixed(initial.dp),
    )
}
