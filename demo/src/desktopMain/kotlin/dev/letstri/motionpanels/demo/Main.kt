package dev.letstri.motionpanels.demo

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

public fun main(): Unit = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose Motion Panels",
        state = WindowState(width = 1100.dp, height = 720.dp),
    ) {
        MotionPanelsDemo()
    }
}
