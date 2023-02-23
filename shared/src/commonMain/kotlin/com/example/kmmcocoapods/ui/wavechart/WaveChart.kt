@file:OptIn(ExperimentalMaterialApi::class)

package com.example.kmmcocoapods.ui.wavechart


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.IntSize
import com.example.kmmcocoapods.axis.AxisData
import com.example.kmmcocoapods.axis.YAxis
import com.example.kmmcocoapods.axis.getXAxisScale
import com.example.kmmcocoapods.axis.getYAxisScale
import com.example.kmmcocoapods.chartcontainer.container.ScrollableCanvasContainer
import com.example.kmmcocoapods.common.components.ItemDivider
import com.example.kmmcocoapods.common.components.accessibility.AccessibilityBottomSheetDialog
import com.example.kmmcocoapods.common.components.accessibility.LinePointInfo
import com.example.kmmcocoapods.common.extensions.drawGridLines
import com.example.kmmcocoapods.common.extensions.getTextHeight
import com.example.kmmcocoapods.common.extensions.getTextWidth
import com.example.kmmcocoapods.common.extensions.isNotNull
import com.example.kmmcocoapods.common.model.Point
import com.example.kmmcocoapods.ui.linechart.*
import com.example.kmmcocoapods.ui.wavechart.model.Wave
import com.example.kmmcocoapods.ui.wavechart.model.WaveChartData
import kotlinx.coroutines.launch
import com.example.kmmcocoapods.ui.linechart.getMaxElementInYAxis as getMaxElementInLineYAxis

/**
 *
 * [WaveChart] compose method used for drawing a Wave Chart.
 * @param modifier :All modifier related property.
 * Data class [WaveChartData] to save all params needed to draw the wave chart.
 * @param waveChartData : Add data related to wave chart.
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalTextApi::class)
@Composable
internal fun WaveChart(modifier: Modifier, waveChartData: WaveChartData) {
    val accessibilitySheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    val isTalkBackEnabled = false
    Surface(modifier = modifier.testTag("wave_chart").background(Color.Black)) {
        with(waveChartData) {
            var columnWidth by remember { mutableStateOf(300f) }
            val rowHeight by remember { mutableStateOf(0f) }
            var xOffset by remember { mutableStateOf(0f) }
            val bgColor = MaterialTheme.colors.surface
            var isTapped by remember { mutableStateOf(false) }
            var tapOffset by remember { mutableStateOf(Offset(0f, 0f)) }
            var selectionTextVisibility by remember { mutableStateOf(false) }
            var identifiedPoint by remember { mutableStateOf(Point(0f, 0f)) }
            val wave = wavePlotData.lines.first()
            // Update must required values
            val xAxisData = xAxisData.copy(
                axisBottomPadding = bottomPadding,
            )
            val yAxisData = yAxisData.copy(
                axisBottomPadding = LocalDensity.current.run { rowHeight.toDp() },
            )
            val textMeasure = rememberTextMeasurer()


            val (xMin, xMax, xAxisScale) = getXAxisScale(wave.dataPoints, xAxisData.steps)
            val (yMin, _, yAxisScale) = getYAxisScale(wave.dataPoints, yAxisData.steps)
            val maxElementInYAxis = getMaxElementInLineYAxis(yAxisScale, yAxisData.steps)

            ScrollableCanvasContainer(modifier = modifier
                .semantics {
                    contentDescription = waveChartData.accessibilityConfig.chartDescription
                }
                .testTag("scrollable_container")
                .clickable {
                    if (isTalkBackEnabled) {
                        scope.launch {
                            accessibilitySheetState.animateTo(
                                ModalBottomSheetValue.Expanded
                            )
                        }
                    }
                },
                calculateMaxDistance = { xZoom ->
                    xOffset = xAxisData.axisStepSize.toPx() * xZoom
                    getMaxScrollDistance(
                        columnWidth,
                        xMax,
                        xMin,
                        xOffset,
                        paddingRight.toPx(),
                        size.width,
                        containerPaddingEnd.toPx()
                    )
                },
                containerBackgroundColor = backgroundColor,
                isPinchZoomEnabled = isZoomAllowed,
                drawXAndYAxis = { _, _ ->
                    YAxis(
                        modifier = Modifier
                            .fillMaxHeight()
                            .onGloballyPositioned {
                                columnWidth = it.size.width.toFloat()
                            }, yAxisData = yAxisData
                    )
                },
                onDraw = { scrollOffset, xZoom ->
                    val yBottom = size.height - rowHeight
                    val yOffset = ((yBottom - paddingTop.toPx()).div(maxElementInYAxis))
                    xOffset = xAxisData.axisStepSize.toPx() * xZoom
                    val xLeft = columnWidth // To add extra space if needed
                    val pointsData = getMappingPointsToGraph(
                        wave.dataPoints, xMin, xOffset, xLeft, scrollOffset, yBottom, yMin, yOffset
                    )
                    val (cubicPoints1, cubicPoints2) = getCubicPoints(
                        pointsData
                    )
                    val yPointOfOrigin = yBottom - ((0.minus(yMin)) * yOffset)
                    val tapPointLocks = mutableMapOf<Int, Pair<Point, Offset>>()

                    var xPos = xLeft - scrollOffset

                    // Draw guide lines
                    gridLines?.let {
                        drawGridLines(
                            yBottom,
                            yAxisData.axisTopPadding.toPx(),
                            xLeft,
                            paddingRight,
                            scrollOffset,
                            pointsData.size,
                            xZoom,
                            xAxisScale,
                            yAxisData.steps,
                            xAxisData.axisStepSize,
                            it
                        )
                    }

                    // Draw cubic line using the points and form a line graph
                    val cubicPath = drawStraightOrCubicLine(
                        pointsData, cubicPoints1, cubicPoints2, wave.waveStyle
                    )

                    // Draw Lines and Points and AreaUnderLine
                    // Draw area under curve
                    drawShadowUnderLineAndIntersectionPoint(
                        cubicPath, pointsData, yPointOfOrigin, wave
                    )

                    // Draw the Y-axis
                    for (index in 0..xAxisData.steps) {
                        drawXAxisLabel(
                            xAxisData,
                            index,
                            xAxisScale,
                            yPointOfOrigin,
                            xPos,
                            textMeasure
                        )
                        drawAxisLineWithPointers(
                            xPos,
                            xAxisData,
                            xZoom,
                            xAxisScale,
                            yPointOfOrigin,
                            index != xAxisData.steps
                        )
                        xPos += (xAxisData.axisStepSize.toPx() * (xZoom * xAxisScale))
                    }

                    // Draw column to make graph look scrollable under Yaxis
                    drawUnderScrollMask(columnWidth, paddingRight, bgColor)

                    pointsData.forEachIndexed { index, point ->
                        if (isTapped && point.isTapped(tapOffset.x, xOffset)) {
                            // Dealing with only one line graph hence tapPointLocks[0]
                            tapPointLocks[0] = wave.dataPoints[index] to point
                        }
                    }

                    val selectedOffset = tapPointLocks.values.firstOrNull()?.second
                    if (selectionTextVisibility && selectedOffset.isNotNull()) {
                        drawHighlightText(
                            identifiedPoint,
                            selectedOffset ?: Offset(0f, 0f),
                            wave.selectionHighlightPopUp,
                            textMeasure
                        )
                    }
                    if (isTapped) {
                        val x = tapPointLocks.values.firstOrNull()?.second?.x
                        if (x != null) identifiedPoint =
                            tapPointLocks.values.map { it.first }.first()
                        drawHighLightOnSelectedPoint(
                            tapPointLocks,
                            columnWidth,
                            paddingRight,
                            yBottom,
                            wave.selectionHighlightPoint
                        )
                    }
                },
                onPointClicked = { offset: Offset, _: Float ->
                    isTapped = true
                    selectionTextVisibility = true
                    tapOffset = offset
                },
                onScroll = {
                    isTapped = false
                    selectionTextVisibility = false
                },
                onZoomInAndOut = {
                    isTapped = false
                    selectionTextVisibility = false
                })
        }
        if (isTalkBackEnabled) {
            with(waveChartData) {
                AccessibilityBottomSheetDialog(
                    modifier = Modifier.fillMaxSize(),
                    backgroundColor = Color.White,
                    content = {
                        val linePoints = wavePlotData.lines.firstOrNull()?.dataPoints
                        LazyColumn {
                            items(linePoints?.size ?: 0) { index ->
                                Column {
                                    LinePointInfo(
                                        xAxisData.axisLabelDescription(
                                            xAxisData.labelData(
                                                index
                                            )
                                        ),
                                        linePoints?.get(index)?.description ?: "",
                                        wavePlotData.lines.firstOrNull()?.waveStyle?.color
                                            ?: Color.Transparent
                                    )
                                    ItemDivider(
                                        thickness = accessibilityConfig.dividerThickness,
                                        dividerColor = accessibilityConfig.dividerColor
                                    )
                                }
                            }
                        }
                    },
                    popUpTopRightButtonTitle = accessibilityConfig.popUpTopRightButtonTitle,
                    popUpTopRightButtonDescription = accessibilityConfig.popUpTopRightButtonDescription,
                    sheetState = accessibilitySheetState
                )
            }
        }
    }
}

/**
 *
 * DrawScope.drawShadowUnderLineAndIntersectionPoint extension method used  for drawing a
 * shadow below the line graph points and also drawing intersection points on the line graph.
 * @param cubicPath : Path used to draw the shadow
 * @param pointsData : List of the points on the Line graph.
 * @param yBottom : Offset of X-Axis starting position i.e shade to be drawn until.
 * @param line : line on which shadow & intersectionPoints has to be drawn.
 */
private fun DrawScope.drawShadowUnderLineAndIntersectionPoint(
    cubicPath: Path, pointsData: MutableList<Offset>, yBottom: Float, line: Wave
) {
    if (line.shadowUnderLine.isNotNull()) {
        cubicPath.lineTo(pointsData.last().x, yBottom)
        cubicPath.lineTo(pointsData.first().x, yBottom)
        line.shadowUnderLine?.draw?.let { it(this, cubicPath) }
    }
    if (line.intersectionPoint.isNotNull()) {
        pointsData.forEach { offset ->
            drawPointOnLine(offset, line.intersectionPoint)
        }
    }
}

/**
 * DrawScope.drawAxisLineWithPointers extension method used for drawing the axis line and the
 * axis pointers.
 * @param xPos : X coordinate of the axis line
 * @param axisData : Axis data.
 * @param zoomScale : Zoom scale
 * @param xAxisScale : The scale of increment of X-Axis
 * @param yPos : Y coordinate of the axis line
 * @param canDrawEndLine : Boolean letting us know if it is the end point.
 */
private fun DrawScope.drawAxisLineWithPointers(
    xPos: Float,
    axisData: AxisData,
    zoomScale: Float,
    xAxisScale: Float,
    yPos: Float,
    canDrawEndLine: Boolean // Added check to avoid drawing an extra line post the last point
) {
    with(axisData) {
        if (axisConfig.isAxisLineRequired) {
            if (canDrawEndLine) {
                val axisStepWidth = (axisStepSize.toPx() * (zoomScale * xAxisScale))
                drawLine(
                    Color.Black,
                    Offset(xPos, yPos),
                    if (shouldDrawAxisLineTillEnd) {
                        Offset((xPos + (axisStepWidth / 2) + axisStepWidth), yPos)
                    } else {
                        Offset(xPos + axisStepWidth, yPos)
                    },                    strokeWidth = axisLineThickness.toPx()
                )
            }
            drawLine(
                axisLineColor,
                Offset(xPos, yPos),
                Offset(xPos, yPos + indicatorLineWidth.toPx()),
                strokeWidth = axisLineThickness.toPx()
            )
        }
    }
}

/**
 * DrawScope.drawXAxisLabel extension method used for drawing the axis line labels.
 * @param xPos : X coordinate of the axis line
 * @param axisData : Axis data.
 * @param index : Index of label data.
 * @param xAxisScale : The scale of increment of X-Axis
 * @param yPos : Y coordinate of the axis line
 */
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawXAxisLabel(
    axisData: AxisData,
    index: Int,
    xAxisScale: Float,
    yPos: Float,
    xPos: Float,
    textMeasure: TextMeasurer
) {
    with(axisData) {
        val xLabel = labelData((index * xAxisScale).toInt())
        val labelHeight = textMeasure.measure(text = AnnotatedString(xLabel)).size.height
        val labelWidth = textMeasure.measure(text = AnnotatedString(xLabel)).size.width
        drawContext.canvas.nativeCanvas.apply {
            val x = xPos - (labelWidth / 2)
            val y =
                yPos + labelHeight / 2 + indicatorLineWidth.toPx() + labelAndAxisLinePadding.toPx()
            print("Pointer x, y for the text position - $x, $y")
            drawText(
                textMeasurer = textMeasure,
                text = xLabel,
                topLeft = Offset(x, y),
                maxSize = IntSize(1440, 1050)
            )
        }
    }
}
