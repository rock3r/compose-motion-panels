package dev.letstri.motionpanels.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        val window = DemoWindow(title = "motion-panels-${palette.name}") { JewelIslandsDemo(palette) }
        try {
            window.start()
            val screenshot = File("build/reports/spectre/$fileName")
            window.capture(screenshot)
            assertTrue(screenshot.length() > 24 * 1024, "expected a non-empty ${palette.name} screenshot")
            val image = ImageIO.read(screenshot)
            assertEquals(
                palette.toolWindow.toArgb(),
                image.getRGB(100, 300),
                "tool-window island must use toolwindowBackground",
            )
            assertEquals(
                palette.panel.toArgb(),
                image.getRGB(15, 300),
                "exposed window canvas must use panelBackground",
            )
        } finally {
            window.stop()
        }
    }
}

@Composable
private fun JewelIslandsDemo(palette: IslandsPalette) {
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
                initialSize = 220.dp.panelSize,
                minSize = 150.dp.panelSize,
                maxSize = 340.dp.panelSize,
            )
            val deviceManager = rememberMotionPanelState(
                initialSize = 190.dp.panelSize,
                minSize = 150.dp.panelSize,
                maxSize = 280.dp.panelSize,
            )

            Column(Modifier.fillMaxSize().background(palette.panel)) {
                TopChrome(palette)
                Breadcrumbs(palette)
                Row(Modifier.weight(1f)) {
                    ToolRail(palette, left = true)
                    JewelMotionPanelGroup(modifier = Modifier.weight(1f)) {
                        JewelMotionPanel(
                            state = project,
                            modifier = Modifier.padding(end = IslandGap / 2).clip(IslandShape),
                        ) {
                            ProjectIsland(palette)
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
                                EditorIsland(palette)
                            }
                        }
                        JewelMotionPanelSeparator(
                            state = deviceManager,
                            thickness = IslandGap,
                            color = Color.Transparent,
                        )
                        JewelMotionPanel(
                            state = deviceManager,
                            modifier = Modifier.padding(start = IslandGap / 2).clip(IslandShape),
                        ) {
                            DeviceManagerIsland(palette)
                        }
                    }
                    ToolRail(palette, left = false)
                }
                StatusBar(palette)
            }
        }
    }
}

@Composable
private fun TopChrome(palette: IslandsPalette) {
    Row(
        Modifier.fillMaxWidth().height(45.dp).background(palette.chrome).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TrafficLight(Color(0xFFFF5F57))
        TrafficLight(Color(0xFFFFBD2E))
        TrafficLight(Color(0xFF28C840))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(25.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF289CB5)), contentAlignment = Alignment.Center) {
            Label("MP", Color.White, 9.sp, FontWeight.Bold)
        }
        Label("compose-motion-panels", palette.text, 12.sp, FontWeight.SemiBold)
        Label("⌄", palette.muted, 13.sp)
        Spacer(Modifier.width(12.dp))
        Label("⌘", palette.muted, 13.sp)
        Label("main", palette.text, 12.sp)
        Label("⌄", palette.muted, 13.sp)
        Spacer(Modifier.weight(1f))
        Label("demo", palette.text, 12.sp, FontWeight.Medium)
        Label("⌄", palette.muted, 13.sp)
        Label("▶", palette.run, 15.sp)
        Label("⚙", palette.muted, 15.sp)
        Label("⌕", palette.muted, 18.sp)
    }
}

@Composable
private fun TrafficLight(color: Color) {
    Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(color))
}

@Composable
private fun Breadcrumbs(palette: IslandsPalette) {
    Row(
        Modifier.fillMaxWidth().height(31.dp).background(palette.chrome).padding(horizontal = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        listOf("compose-motion-panels", "panels-jewel", "src", "JewelMotionPanels.kt").forEachIndexed { index, text ->
            if (index > 0) Label("›", palette.muted, 13.sp)
            Label(text, if (index == 3) palette.text else palette.muted, 11.sp)
        }
    }
}

@Composable
private fun ToolRail(palette: IslandsPalette, left: Boolean) {
    val icons = if (left) listOf("□", "≡", "⌘", "⑂", "▱") else listOf("◈", "▦", "◉", "◇")
    Column(
        Modifier.width(35.dp).fillMaxHeight().padding(top = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        icons.forEachIndexed { index, icon ->
            val selected = if (left) index == 1 else index == 2
            Box(
                Modifier.size(25.dp).clip(RoundedCornerShape(7.dp))
                    .background(if (selected) palette.railSelection else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                Label(icon, if (selected && !left) Color.White else palette.muted, 14.sp)
            }
        }
    }
}

@Composable
private fun ProjectIsland(palette: IslandsPalette) {
    Column(Modifier.fillMaxSize()) {
        IslandHeader("Project", palette)
        IslandToolbar("↻    ↔    ↓    ◉", palette)
        Column(Modifier.padding(horizontal = 9.dp, vertical = 8.dp)) {
            TreeLine("⌄  compose-motion-panels", palette, bold = true)
            TreeLine("    ▸  androidApp", palette)
            TreeLine("    ▸  demo", palette)
            TreeLine("    ▾  panels-jewel", palette)
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                    .background(palette.selection).padding(horizontal = 7.dp, vertical = 6.dp),
            ) {
                Label("      ◇  JewelMotionPanels.kt", palette.text, 10.sp)
            }
            TreeLine("    ▸  panels-jewel-standalone", palette)
            TreeLine("       README.md", palette)
        }
    }
}

@Composable
private fun EditorIsland(palette: IslandsPalette) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().height(38.dp).background(palette.header).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Label("◇", palette.kotlin, 14.sp)
            Label("JewelMotionPanels.kt", palette.text, 11.sp, FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Label("Islands · ${palette.name}", palette.muted, 10.sp)
        }
        Row(Modifier.fillMaxSize()) {
            Column(
                Modifier.width(34.dp).fillMaxHeight().background(palette.gutter).padding(top = 14.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                (1..8).forEach { Label("$it", palette.lineNumber, 10.sp, FontFamily.Monospace) }
            }
            Column(
                Modifier.fillMaxSize().padding(horizontal = 13.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Code("package dev.letstri.motionpanels.jewel", palette.keyword)
                Spacer(Modifier.height(8.dp))
                Code("@Composable", palette.annotation)
                Code("fun JewelMotionPanels() {", palette.code)
                Code("    JewelMotionPanelGroup {", palette.code)
                Code("        JewelMotionPanel(project) { Project() }", palette.code)
                Code("        JewelMotionPanelSeparator(project)", palette.code)
                Code("        JewelMotionPanel { Editor() }", palette.code)
                Code("    }", palette.code)
                Code("}", palette.code)
            }
        }
    }
}

@Composable
private fun DeviceManagerIsland(palette: IslandsPalette) {
    Column(Modifier.fillMaxSize()) {
        IslandHeader("Device Manager", palette)
        IslandToolbar("▦     ＋     ⌁", palette)
        Row(
            Modifier.fillMaxWidth().height(27.dp).background(palette.header).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Label("Name", palette.text, 10.sp, FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Label("API", palette.text, 10.sp, FontWeight.Medium)
        }
        DeviceRow("Pixel 7 API 36", "36", true, palette)
        DeviceRow("Pixel 9 Pro", "37", false, palette)
        DeviceRow("Pixel Tablet", "35", true, palette)
    }
}

@Composable
private fun IslandHeader(title: String, palette: IslandsPalette) {
    Row(
        Modifier.fillMaxWidth().height(37.dp).padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Label(title, palette.text, 11.sp, FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        Label("⋮", palette.muted, 14.sp)
    }
}

@Composable
private fun IslandToolbar(content: String, palette: IslandsPalette) {
    Box(
        Modifier.fillMaxWidth().height(32.dp).border(1.dp, palette.separator)
            .background(palette.header).padding(horizontal = 11.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Label(content, palette.muted, 12.sp)
    }
}

@Composable
private fun DeviceRow(name: String, api: String, warning: Boolean, palette: IslandsPalette) {
    Row(
        Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        if (warning) {
            Box(Modifier.size(13.dp).clip(RoundedCornerShape(50)).background(Color(0xFFE85B61)), contentAlignment = Alignment.Center) {
                Label("!", Color.White, 8.sp, FontWeight.Bold)
            }
        } else {
            Label("▷", palette.run, 13.sp)
        }
        Column(Modifier.weight(1f)) {
            Label(name, palette.text, 10.sp)
            Label(if (warning) "Missing system image" else "Ready", palette.muted, 9.sp)
        }
        Label(api, palette.text, 10.sp)
    }
}

@Composable
private fun StatusBar(palette: IslandsPalette) {
    Row(
        Modifier.fillMaxWidth().height(25.dp).padding(horizontal = 42.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Label("19:3     LF     UTF-8     4 spaces     124M", palette.muted, 10.sp)
    }
}

@Composable
private fun TreeLine(text: String, palette: IslandsPalette, bold: Boolean = false) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 6.dp)) {
        Label(text, palette.text, 10.sp, if (bold) FontWeight.Medium else FontWeight.Normal)
    }
}

@Composable
private fun Code(text: String, color: Color) {
    Label(text, color, 10.sp, FontFamily.Monospace, FontWeight.Medium)
}

@Composable
private fun Label(
    text: String,
    color: Color,
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal,
) {
    BasicText(text, style = TextStyle(color = color, fontSize = size, fontWeight = weight))
}

@Composable
private fun Label(
    text: String,
    color: Color,
    size: TextUnit,
    family: FontFamily,
    weight: FontWeight = FontWeight.Normal,
) {
    BasicText(text, style = TextStyle(color = color, fontSize = size, fontFamily = family, fontWeight = weight))
}

private data class IslandsPalette(
    val name: String,
    val isDark: Boolean,
    val panel: Color,
    val toolWindow: Color,
    val chrome: Color,
    val header: Color,
    val gutter: Color,
    val separator: Color,
    val railSelection: Color,
    val selection: Color,
    val text: Color,
    val muted: Color,
    val code: Color,
    val keyword: Color,
    val annotation: Color,
    val lineNumber: Color,
    val kotlin: Color,
    val run: Color,
)

private val IslandsLight = IslandsPalette(
    name = "Light",
    isDark = false,
    panel = Color(0xFFE9EBF0),
    toolWindow = Color(0xFFFFFFFF),
    chrome = Color(0xFFE8EDF2),
    header = Color(0xFFF8F9FB),
    gutter = Color(0xFFFAFBFD),
    separator = Color(0xFFE0E3E9),
    railSelection = Color(0xFFD8DCE3),
    selection = Color(0xFFECEEF4),
    text = Color(0xFF202124),
    muted = Color(0xFF6E7584),
    code = Color(0xFF25262A),
    keyword = Color(0xFF0033B3),
    annotation = Color(0xFF9E2A9C),
    lineNumber = Color(0xFF9DA0A8),
    kotlin = Color(0xFF8054F8),
    run = Color(0xFF299B53),
)

private val IslandsDark = IslandsPalette(
    name = "Dark",
    isDark = true,
    panel = Color(0xFF1B1C1F),
    toolWindow = Color(0xFF2B2D30),
    chrome = Color(0xFF23252A),
    header = Color(0xFF303238),
    gutter = Color(0xFF26282D),
    separator = Color(0xFF3C3F45),
    railSelection = Color(0xFF3A3D43),
    selection = Color(0xFF3D4657),
    text = Color(0xFFDFE1E5),
    muted = Color(0xFF9DA0A8),
    code = Color(0xFFBCBEC4),
    keyword = Color(0xFF6C95EB),
    annotation = Color(0xFFC77DBB),
    lineNumber = Color(0xFF6F737A),
    kotlin = Color(0xFFA78BFA),
    run = Color(0xFF6AAB73),
)

private val IslandShape = RoundedCornerShape(11.dp)
private val IslandGap = 8.dp
