package dev.letstri.motionpanels.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.letstri.motionpanels.MotionPanel
import dev.letstri.motionpanels.MotionPanelGroup
import dev.letstri.motionpanels.MotionPanelSeparator
import dev.letstri.motionpanels.PanelOrientation
import dev.letstri.motionpanels.PanelSize
import dev.letstri.motionpanels.panelSize
import dev.letstri.motionpanels.rememberMotionPanelState

private val Background = Color(0xFFF8FAFC)
private val Panel = Color(0xFFFFFFFF)
private val Border = Color(0xFFE2E8F0)
private val Muted = Color(0xFF64748B)

@Composable
public fun MotionPanelsDemo() {
    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = Background) {
            val files = rememberMotionPanelState(
                initialSize = 220.dp.panelSize,
                minSize = 140.dp.panelSize,
                maxSize = 380.dp.panelSize,
            )
            val terminal = rememberMotionPanelState(
                initialSize = 150.dp.panelSize,
                minSize = 80.dp.panelSize,
                maxSize = 280.dp.panelSize,
            )

            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Compose Motion Panels", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Drag either seam—or their crossing. Focus a seam for keyboard controls.", color = Muted)
                        val fileSize = (files.size as? PanelSize.Fixed)?.value?.value?.toInt() ?: 0
                        Text("Files: ${fileSize}dp", Modifier.testTag("files-size"), color = Muted)
                    }
                    Button(onClick = { files.collapsed = !files.collapsed }, modifier = Modifier.testTag("toggle-files")) {
                        Text(if (files.collapsed) "Open files" else "Collapse files")
                    }
                }

                MotionPanelGroup(
                    modifier = Modifier.fillMaxSize().border(1.dp, Border, RoundedCornerShape(12.dp)),
                ) {
                    MotionPanel(files, Modifier.background(Panel)) {
                        ToolWindow("Files", listOf("src", "  commonMain", "  MotionPanels.kt", "  PanelSize.kt", "README.md"))
                    }
                    MotionPanelSeparator(Modifier.testTag("files-separator"), state = files, color = Border)
                    MotionPanel(pin = true, modifier = Modifier.background(Color(0xFF0F172A))) {
                        MotionPanelGroup(PanelOrientation.Vertical, Modifier.fillMaxSize()) {
                            MotionPanel(modifier = Modifier.background(Color(0xFF0F172A))) {
                                Editor()
                            }
                            MotionPanelSeparator(Modifier.testTag("terminal-separator"), state = terminal, color = Color(0xFF334155))
                            MotionPanel(terminal, Modifier.background(Color(0xFF111827))) {
                                ToolWindow("Terminal", listOf("$ ./gradlew desktopRun", "> BUILD SUCCESSFUL", "> Drag the crossing ↖"), dark = true)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolWindow(title: String, lines: List<String>, dark: Boolean = false) {
    val foreground = if (dark) Color(0xFFE2E8F0) else Color(0xFF0F172A)
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, color = foreground, fontWeight = FontWeight.Bold)
        lines.forEach { Text(it, color = if (dark) Color(0xFF94A3B8) else Muted, fontFamily = FontFamily.Monospace) }
    }
}

@Composable
private fun Editor() {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("Workspace.kt", color = Color(0xFFE2E8F0), fontWeight = FontWeight.Bold)
        CodeLine("1", "MotionPanelGroup {")
        CodeLine("2", "    MotionPanel(files) { FileTree() }")
        CodeLine("3", "    MotionPanelSeparator()")
        CodeLine("4", "    MotionPanel(pin = true) { Editor() }")
        CodeLine("5", "}")
    }
}

@Composable
private fun CodeLine(number: String, code: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(number, color = Color(0xFF475569), modifier = Modifier.width(20.dp), fontFamily = FontFamily.Monospace)
        Text(code, color = Color(0xFFCBD5E1), fontFamily = FontFamily.Monospace)
    }
}
