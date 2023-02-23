package com.example.kmmcocoapods.ui.wavechart.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.kmmcocoapods.axis.AxisData
import com.example.kmmcocoapods.common.model.AccessibilityConfig
import com.example.kmmcocoapods.common.model.Point
import com.example.kmmcocoapods.ui.linechart.model.GridLines
import com.example.kmmcocoapods.ui.linechart.model.IntersectionPoint
import com.example.kmmcocoapods.ui.linechart.model.LineStyle
import com.example.kmmcocoapods.ui.linechart.model.SelectionHighlightPoint
import com.example.kmmcocoapods.ui.linechart.model.SelectionHighlightPopUp
import com.example.kmmcocoapods.ui.linechart.model.ShadowUnderLine
import co.yml.charts.ui.wavechart.model.WavePlotData


/**
 *
 * WaveChartData data class that contains all params user need to define to draw a wave graph.
 * @param wavePlotData: The path to be drawn on the graph represented by a wave.
 * @param xAxisData: All the configurations related to X-Axis to be defined here in [AxisData]
 * @param yAxisData: All the configurations related to Y-Axis to be defined here in [AxisData]
 * @param isZoomAllowed: True if zoom in for all vertical graph components is allowed else false.
 * @param paddingTop: Padding from the top of the canvas to start of the graph container.
 * @param paddingRight: Padding from the end of the canvas to end of the graph container.
 * @param bottomPadding: Padding from the bottom of the canvas to bottom of the graph container.
 * @param containerPaddingEnd: Container inside padding end after the last point of the graph.
 * @param backgroundColor: Background color of the Y & X components,
 * @param gridLines: This enables graph to draw horizontal and vertical grid lines
 * @param accessibilityConfig: Configs related to accessibility service defined here in [AccessibilityConfig]
 */
data class WaveChartData(
    val wavePlotData: WavePlotData,
    val xAxisData: AxisData = AxisData.Builder().build(),
    val yAxisData: AxisData = AxisData.Builder().build(),
    val isZoomAllowed: Boolean = true,
    val paddingTop: Dp = 30.dp,
    val bottomPadding: Dp = 10.dp,
    val paddingRight: Dp = 10.dp,
    val containerPaddingEnd: Dp = 15.dp,
    val backgroundColor: Color = Color.White,
    val gridLines: GridLines? = null,
    val accessibilityConfig: AccessibilityConfig = AccessibilityConfig()
)

/**
 * Represent a Wave in the [co.yml.charts.ui.wavechart]
 *
 * @param dataPoints list of points [Point] in the wave
 * @param waveStyle Adds styling options in [LineStyle] to the wave path drawn.
 * @param intersectionPoint drawing logic to draw the point itself in [IntersectionPoint].
 * If null, the point is not drawn.
 * @param selectionHighlightPoint drawing logic to draw the highlight at the point when it is selected
 * in [SelectionHighlightPoint] If null, the point won't be highlighted on selection
 * @param shadowUnderLine drawing logic for the section under the wave in [ShadowUnderLine].
 * @param selectionHighlightPopUp All prams related to selection popup to be added here in [SelectionHighlightPopUp]
 */
data class Wave(
    val dataPoints: List<Point>,
    val waveStyle: LineStyle = LineStyle(),
    val intersectionPoint: IntersectionPoint? = null,
    val selectionHighlightPoint: SelectionHighlightPoint? = null,
    val shadowUnderLine: ShadowUnderLine? = null,
    val selectionHighlightPopUp: SelectionHighlightPopUp? = null
)
