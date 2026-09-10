package dev.letstri.motionpanels.jewel

import androidx.compose.ui.graphics.Color
import dev.letstri.motionpanels.PanelSize
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.jewel.foundation.BorderColors
import org.jetbrains.jewel.foundation.GlobalColors
import org.jetbrains.jewel.foundation.OutlineColors
import org.jetbrains.jewel.foundation.TextColors

class JewelMotionPanelsTest {
    private val panel = Color(0xFF112233)
    private val toolWindow = Color(0xFF445566)

    @Test
    fun `filling panel uses window background and sized panel uses tool window background`() {
        val colors = colors(toolWindow)

        assertEquals(panel, defaultPanelBackground(null, colors))
        assertEquals(toolWindow, defaultPanelBackground(sizedState(), colors))
    }

    @Test
    fun `standalone theme falls back to panel background`() {
        assertEquals(panel, defaultPanelBackground(sizedState(), colors(Color.Unspecified)))
    }

    private fun sizedState() = dev.letstri.motionpanels.MotionPanelState(
        initialSize = PanelSize.Fixed(androidx.compose.ui.unit.Dp(200f)),
    )

    private fun colors(toolWindow: Color) = GlobalColors(
        borders = BorderColors(Color.Black, Color.Blue, Color.Gray),
        outlines = OutlineColors(Color.Blue, Color.Yellow, Color.Red, Color.Yellow, Color.Red),
        text = TextColors(Color.Black, Color.White, Color.Gray, Color.Gray, Color.Blue, Color.Red, Color.Yellow),
        panelBackground = panel,
        toolwindowBackground = toolWindow,
    )
}
