package dev.letstri.motionpanels.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import dev.letstri.motionpanels.panelSize
import dev.letstri.motionpanels.rememberMotionPanelState
import dev.letstri.motionpanels.jewel.JewelMotionPanel
import dev.letstri.motionpanels.jewel.JewelMotionPanelGroup
import dev.letstri.motionpanels.jewel.JewelMotionPanelSeparator
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.jewel.foundation.GlobalColors
import org.jetbrains.jewel.foundation.LocalGlobalColors
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JewelMotionPanelsVisualTest {
    private val window = DemoWindow(title = "motion-panels-jewel") { JewelPanelsDemo() }

    @BeforeAll fun start() = window.start()
    @AfterAll fun stop() = window.stop()

    @Test
    fun `Jewel adapter renders distinct Islands panel surfaces`() {
        Thread.sleep(1_500)
        val screenshot = File("build/reports/spectre/jewel.png")
        window.capture(screenshot)
        assertTrue(screenshot.length() > 8 * 1024, "expected a non-empty Jewel screenshot")
        val image = ImageIO.read(screenshot)
        assertEquals(ToolWindowColor.toArgb(), image.getRGB(100, 200), "sized panel must use toolwindowBackground")
        assertEquals(PanelColor.toArgb(), image.getRGB(500, 200), "filling panel must use panelBackground")
    }
}

@Composable
private fun JewelPanelsDemo() {
    IntUiTheme(isDark = false) {
        val standalone = LocalGlobalColors.current
        val islands = GlobalColors(
            borders = standalone.borders,
            outlines = standalone.outlines,
            text = standalone.text,
            panelBackground = PanelColor,
            toolwindowBackground = ToolWindowColor,
        )
        CompositionLocalProvider(LocalGlobalColors provides islands) {
            val toolWindow = rememberMotionPanelState(
                initialSize = 250.dp.panelSize,
                minSize = 140.dp.panelSize,
                maxSize = 400.dp.panelSize,
            )
            JewelMotionPanelGroup(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                JewelMotionPanel(toolWindow) {
                    PanelLabel("Tool window", "toolwindowBackground")
                }
                JewelMotionPanelSeparator(state = toolWindow)
                JewelMotionPanel {
                    PanelLabel("Editor window", "panelBackground")
                }
            }
        }
    }
}

private val PanelColor = Color(0xFFF7F8FA)
private val ToolWindowColor = Color(0xFFE3EAF4)

@Composable
private fun PanelLabel(title: String, colorToken: String) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BasicText(title)
        BasicText(colorToken)
    }
}
