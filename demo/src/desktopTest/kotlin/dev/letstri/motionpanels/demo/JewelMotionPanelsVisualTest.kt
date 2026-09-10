package dev.letstri.motionpanels.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.letstri.motionpanels.PanelOrientation
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
import org.junit.jupiter.api.Test

class JewelMotionPanelsVisualTest {
    @Test
    fun `Jewel adapter renders light and dark Islands surfaces`() {
        renderAndVerify(IslandsLight, "jewel-islands-light.png")
        renderAndVerify(IslandsDark, "jewel-islands-dark.png")
    }

    private fun renderAndVerify(palette: IslandsPalette, fileName: String) {
        val window = DemoWindow(title = "motion-panels-${palette.name}") { JewelPanelsDemo(palette) }
        try {
            window.start()
            val screenshot = File("build/reports/spectre/$fileName")
            window.capture(screenshot)
            assertTrue(screenshot.length() > 16 * 1024, "expected a non-empty ${palette.name} screenshot")
            val image = ImageIO.read(screenshot)
            assertEquals(
                palette.toolWindow.toArgb(),
                image.getRGB(100, 300),
                "sized panel must use toolwindowBackground",
            )
            assertEquals(
                palette.panel.toArgb(),
                image.getRGB(500, 300),
                "filling panel must use panelBackground",
            )
        } finally {
            window.stop()
        }
    }
}

@Composable
private fun JewelPanelsDemo(palette: IslandsPalette) {
    IntUiTheme(isDark = palette.isDark) {
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
                initialSize = 230.dp.panelSize,
                minSize = 160.dp.panelSize,
                maxSize = 360.dp.panelSize,
            )
            val terminal = rememberMotionPanelState(
                initialSize = 145.dp.panelSize,
                minSize = 90.dp.panelSize,
                maxSize = 240.dp.panelSize,
            )
            Box(Modifier.fillMaxSize().background(palette.frame).padding(18.dp)) {
                JewelMotionPanelGroup(
                    modifier = Modifier.fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, palette.border, RoundedCornerShape(12.dp)),
                ) {
                    JewelMotionPanel(project) { ProjectPanel(palette) }
                    JewelMotionPanelSeparator(state = project, thickness = 8.dp)
                    JewelMotionPanel {
                        JewelMotionPanelGroup(PanelOrientation.Vertical, Modifier.fillMaxSize()) {
                            JewelMotionPanel { EditorPanel(palette) }
                            JewelMotionPanelSeparator(state = terminal, thickness = 8.dp)
                            JewelMotionPanel(terminal) { TerminalPanel(palette) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectPanel(palette: IslandsPalette) {
    Column(Modifier.fillMaxSize().padding(14.dp)) {
        PanelHeader("Project", "⌘1", palette)
        Spacer(Modifier.height(14.dp))
        TreeLine("⌄  compose-motion-panels", palette, bold = true)
        TreeLine("    ▸  androidApp", palette)
        TreeLine("    ▸  demo", palette)
        TreeLine("    ▾  panels-jewel", palette)
        Box(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(palette.selection)
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Label("      ◇  JewelMotionPanels.kt", palette.text, 12.sp)
        }
        TreeLine("    ▸  panels-jewel-standalone", palette)
        TreeLine("       README.md", palette)
    }
}

@Composable
private fun EditorPanel(palette: IslandsPalette) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(42.dp).border(0.dp, Color.Transparent)
                .background(palette.panel).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(7.dp).clip(RoundedCornerShape(50)).background(Color(0xFF6AAB73)))
            Label("JewelMotionPanels.kt", palette.text, 12.sp, FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Label("Islands · ${palette.name}", palette.muted, 11.sp)
        }
        Column(
            Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            CodeLine("1", "JewelMotionPanelGroup {", palette)
            CodeLine("2", "    JewelMotionPanel(project) { Project() }", palette)
            CodeLine("3", "    JewelMotionPanelSeparator(project)", palette)
            CodeLine("4", "    JewelMotionPanel { Editor() }", palette)
            CodeLine("5", "}", palette)
            Spacer(Modifier.height(10.dp))
            Label("// toolwindowBackground + panelBackground", palette.comment, 12.sp, FontFamily.Monospace)
        }
    }
}

@Composable
private fun TerminalPanel(palette: IslandsPalette) {
    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        PanelHeader("Terminal", "⌘2", palette)
        Label("$ ./gradlew :panels-jewel:test", palette.text, 12.sp, FontFamily.Monospace)
        Label("BUILD SUCCESSFUL", palette.success, 12.sp, FontFamily.Monospace, FontWeight.Medium)
    }
}

@Composable
private fun PanelHeader(title: String, shortcut: String, palette: IslandsPalette) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Label(title, palette.text, 12.sp, FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Label(shortcut, palette.muted, 10.sp)
    }
}

@Composable
private fun TreeLine(text: String, palette: IslandsPalette, bold: Boolean = false) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
        Label(text, palette.text, 12.sp, if (bold) FontWeight.Medium else FontWeight.Normal)
    }
}

@Composable
private fun CodeLine(number: String, code: String, palette: IslandsPalette) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Label(number, palette.lineNumber, 12.sp, FontFamily.Monospace)
        Label(code, palette.code, 12.sp, FontFamily.Monospace)
    }
}

@Composable
private fun Label(
    text: String,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit,
    weight: FontWeight = FontWeight.Normal,
) {
    BasicText(text, style = TextStyle(color = color, fontSize = size, fontWeight = weight))
}

@Composable
private fun Label(
    text: String,
    color: Color,
    size: androidx.compose.ui.unit.TextUnit,
    family: FontFamily,
    weight: FontWeight = FontWeight.Normal,
) {
    BasicText(
        text,
        style = TextStyle(color = color, fontSize = size, fontFamily = family, fontWeight = weight),
    )
}

private data class IslandsPalette(
    val name: String,
    val isDark: Boolean,
    val frame: Color,
    val panel: Color,
    val toolWindow: Color,
    val border: Color,
    val selection: Color,
    val text: Color,
    val muted: Color,
    val code: Color,
    val comment: Color,
    val lineNumber: Color,
    val success: Color,
)

private val IslandsLight = IslandsPalette(
    name = "Light",
    isDark = false,
    frame = Color(0xFFE9EAEC),
    panel = Color(0xFFF7F8FA),
    toolWindow = Color(0xFFF0F1F2),
    border = Color(0xFFD4D5D8),
    selection = Color(0xFFDDEBFF),
    text = Color(0xFF1F2329),
    muted = Color(0xFF6C707E),
    code = Color(0xFF2B2D30),
    comment = Color(0xFF6A8759),
    lineNumber = Color(0xFF9DA0A8),
    success = Color(0xFF357B42),
)

private val IslandsDark = IslandsPalette(
    name = "Dark",
    isDark = true,
    frame = Color(0xFF191A1C),
    panel = Color(0xFF1E1F22),
    toolWindow = Color(0xFF2B2D30),
    border = Color(0xFF393B40),
    selection = Color(0xFF2E436E),
    text = Color(0xFFDFE1E5),
    muted = Color(0xFF9DA0A8),
    code = Color(0xFFBCBEC4),
    comment = Color(0xFF7A9B63),
    lineNumber = Color(0xFF6F737A),
    success = Color(0xFF6AAB73),
)
