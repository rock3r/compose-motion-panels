package dev.letstri.motionpanels.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import dev.letstri.motionpanels.jewel.JewelMotionPanel
import dev.letstri.motionpanels.jewel.JewelMotionPanelGroup
import dev.letstri.motionpanels.jewel.JewelMotionPanelSeparator
import dev.letstri.motionpanels.panelSize
import dev.letstri.motionpanels.rememberMotionPanelState
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.jewel.foundation.GlobalColors
import org.jetbrains.jewel.foundation.LocalGlobalColors
import org.jetbrains.jewel.ui.component.Text
import org.junit.jupiter.api.Test

class JewelMotionPanelsVisualTest {
    @Test
    fun `Jewel adapter renders light and dark Islands surfaces`() {
        renderAndVerify(IslandsLight, "jewel-islands-light.png")
        renderAndVerify(IslandsDark, "jewel-islands-dark.png")
    }

    private fun renderAndVerify(palette: IslandsPalette, fileName: String) {
        val window = DemoWindow(
            title = "Motion Panels — ${palette.name}",
            decorated = true,
            dark = palette.isDark,
        ) {
            JewelIslandsDemo(palette)
        }
        try {
            window.start()
            val screenshot = File("build/reports/spectre/$fileName")
            window.capture(screenshot)
            assertTrue(screenshot.length() > 16 * 1024, "expected a non-empty ${palette.name} screenshot")
            val image = ImageIO.read(screenshot)
            assertEquals(palette.toolWindow.toArgb(), image.getRGB(100, 300))
            assertEquals(palette.panel.toArgb(), image.getRGB(224, 300))
        } finally {
            window.stop()
        }
    }
}

@Composable
private fun JewelIslandsDemo(palette: IslandsPalette) {
    val standalone = LocalGlobalColors.current
    val islands = GlobalColors(
        borders = standalone.borders,
        outlines = standalone.outlines,
        text = standalone.text,
        panelBackground = palette.panel,
        toolwindowBackground = palette.toolWindow,
    )
    CompositionLocalProvider(LocalGlobalColors provides islands) {
        val project = rememberMotionPanelState(
            initialSize = 210.dp.panelSize,
            minSize = 140.dp.panelSize,
            maxSize = 320.dp.panelSize,
        )
        val devices = rememberMotionPanelState(
            initialSize = 180.dp.panelSize,
            minSize = 140.dp.panelSize,
            maxSize = 260.dp.panelSize,
        )

        Box(Modifier.fillMaxSize().background(palette.panel).padding(10.dp)) {
            JewelMotionPanelGroup {
                JewelMotionPanel(
                    state = project,
                    modifier = Modifier.padding(end = IslandGap / 2).clip(IslandShape),
                ) {
                    SimplePanel(
                        title = "Project",
                        lines = listOf("⌄ compose-motion-panels", "  ▸ demo", "  ▾ panels-jewel", "    JewelMotionPanels.kt"),
                    )
                }
                JewelMotionPanelSeparator(
                    state = project,
                    thickness = IslandGap,
                    color = Color.Transparent,
                )
                JewelMotionPanel {
                    Box(
                        Modifier.fillMaxSize().padding(horizontal = IslandGap / 2)
                            .clip(IslandShape).background(palette.toolWindow),
                    ) {
                        SimplePanel(
                            title = "JewelMotionPanels.kt",
                            lines = listOf(
                                "@Composable",
                                "fun JewelMotionPanels() {",
                                "    JewelMotionPanelGroup { … }",
                                "}",
                            ),
                        )
                    }
                }
                JewelMotionPanelSeparator(
                    state = devices,
                    thickness = IslandGap,
                    color = Color.Transparent,
                )
                JewelMotionPanel(
                    state = devices,
                    modifier = Modifier.padding(start = IslandGap / 2).clip(IslandShape),
                ) {
                    SimplePanel(
                        title = "Devices",
                        lines = listOf("Pixel 9 Pro     37", "Pixel Tablet     35", "Desktop"),
                    )
                }
            }
        }
    }
}

@Composable
private fun SimplePanel(title: String, lines: List<String>) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(38.dp).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title)
        }
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            lines.forEach { Text(it) }
            Spacer(Modifier.height(1.dp))
        }
    }
}

private data class IslandsPalette(
    val name: String,
    val isDark: Boolean,
    val panel: Color,
    val toolWindow: Color,
)

private val IslandsLight = IslandsPalette(
    name = "Light",
    isDark = false,
    panel = Color(0xFFE9EBF0),
    toolWindow = Color.White,
)

private val IslandsDark = IslandsPalette(
    name = "Dark",
    isDark = true,
    panel = Color(0xFF1B1C1F),
    toolWindow = Color(0xFF2B2D30),
)

private val IslandShape = RoundedCornerShape(11.dp)
private val IslandGap = 8.dp
