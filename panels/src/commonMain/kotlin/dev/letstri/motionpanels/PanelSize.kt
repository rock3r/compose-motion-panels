package dev.letstri.motionpanels

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.round

/** A panel extent that keeps its original unit when resized. */
@Immutable
public sealed interface PanelSize {
    @Immutable
    public data class Fixed(val value: Dp) : PanelSize

    /** A value from 0f to 1f relative to the panel group's extent. */
    @Immutable
    public data class Fraction(val value: Float) : PanelSize {
        init {
            require(value >= 0f) { "A panel fraction cannot be negative" }
        }
    }
}

public val Dp.panelSize: PanelSize get() = PanelSize.Fixed(this)

public val Number.percent: PanelSize get() = PanelSize.Fraction(toFloat() / 100f)

internal fun PanelSize.toPx(extent: Float, density: Density): Float = when (this) {
    is PanelSize.Fixed -> with(density) { value.toPx() }
    is PanelSize.Fraction -> round(value * extent)
}

internal fun PanelSize.fromPx(px: Float, extent: Float, density: Density): PanelSize = when (this) {
    is PanelSize.Fixed -> PanelSize.Fixed(with(density) { px.toDp() })
    is PanelSize.Fraction -> PanelSize.Fraction(
        if (extent == 0f) 0f else round(px / extent * 10_000f) / 10_000f,
    )
}

internal val ZeroPanelSize = 0.dp.panelSize
