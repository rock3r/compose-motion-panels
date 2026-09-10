package dev.letstri.motionpanels.jewel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.letstri.motionpanels.MotionPanel
import dev.letstri.motionpanels.MotionPanelGroup
import dev.letstri.motionpanels.MotionPanelSeparator
import dev.letstri.motionpanels.MotionPanelState
import dev.letstri.motionpanels.PanelOrientation
import org.jetbrains.jewel.foundation.GlobalColors
import org.jetbrains.jewel.foundation.theme.JewelTheme

/** A panel group painted with Jewel's window/panel background. */
@Composable
public fun JewelMotionPanelGroup(
    orientation: PanelOrientation = PanelOrientation.Horizontal,
    modifier: Modifier = Modifier,
    background: Color = Color.Unspecified,
    content: @Composable () -> Unit,
) {
    val resolvedBackground = background.orDefault(JewelTheme.globalColors.panelBackground)
    MotionPanelGroup(orientation, modifier.background(resolvedBackground), content)
}

/**
 * A Jewel-styled motion panel. Sized panels use the IDE tool-window background; the filling
 * panel uses the window/panel background. Standalone Jewel does not define a tool-window color,
 * so it falls back to the panel background.
 */
@Composable
public fun JewelMotionPanel(
    state: MotionPanelState? = null,
    modifier: Modifier = Modifier,
    pin: Boolean = false,
    background: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit,
) {
    val resolvedBackground = background.orDefault(defaultPanelBackground(state, JewelTheme.globalColors))
    MotionPanel(state, modifier.background(resolvedBackground), pin, content)
}

/** A Jewel-colored zero-layout-extent resize grip. */
@Composable
public fun JewelMotionPanelSeparator(
    modifier: Modifier = Modifier,
    state: MotionPanelState? = null,
    thickness: Dp = 14.dp,
    color: Color = Color.Unspecified,
    activeColor: Color = Color.Unspecified,
    contentDescription: String = "Resize panel",
) {
    val colors = JewelTheme.globalColors
    MotionPanelSeparator(
        modifier = modifier,
        state = state,
        thickness = thickness,
        color = color.orDefault(colors.borders.normal),
        activeColor = activeColor.orDefault(colors.outlines.focused),
        contentDescription = contentDescription,
    )
}

internal fun defaultPanelBackground(state: MotionPanelState?, colors: GlobalColors): Color =
    if (state == null) colors.panelBackground else colors.toolwindowBackground.orDefault(colors.panelBackground)

private fun Color.orDefault(default: Color): Color = if (this == Color.Unspecified) default else this
