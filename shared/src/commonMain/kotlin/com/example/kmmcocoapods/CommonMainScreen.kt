package com.example.kmmcocoapods

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.kmmcocoapods.ui.linechart.model.IntersectionPoint
import com.example.kmmcocoapods.ui.linechart.model.LineStyle
import com.example.kmmcocoapods.ui.linechart.model.LineType
import com.example.kmmcocoapods.ui.linechart.model.SelectionHighlightPopUp
import com.example.kmmcocoapods.ui.wavechart.WaveChart
import com.example.kmmcocoapods.ui.wavechart.model.Wave
import com.example.kmmcocoapods.ui.wavechart.model.WaveChartData
import co.yml.charts.ui.wavechart.model.WavePlotData
import com.example.kmmcocoapods.axis.AxisData
import com.example.kmmcocoapods.common.model.Point
import kotlin.math.roundToLong

@Composable
internal fun ChartScreen() {
    Scaffold(modifier = Modifier.fillMaxSize(),
        backgroundColor = Color.White,
        topBar = {
        })
    {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentAlignment = Alignment.TopCenter
        ) {
            val pointsData = listOf<Point>(
                Point(0f, 0f),
                Point(1f, -1f),
                Point(2f, 2f),
                Point(3f, -3f),
                Point(4f, 4f),
                Point(5f, -5f),
                Point(6f, 2f),
                Point(7f, -3f),
                Point(8f, 1f),
                Point(9f, 2f),
                )
            val xAxisData = AxisData.Builder()
                .axisStepSize(40.dp)
                .steps(pointsData.size - 1)
                .labelData { i -> i.toString() }
                .axisLabelAngle(20f)
                .labelAndAxisLinePadding(15.dp)
                .axisLabelColor(Color.Blue)
                .axisLineColor(Color.DarkGray)
                .build()
            val yAxisData = AxisData.Builder()
                .steps(10)
                .labelData { i ->
                    // Add yMin to get the negative axis values to the scale
                    val yMin = pointsData.minOf { it.y }
                    val yMax = pointsData.maxOf { it.y }
                    val yScale = (yMax - yMin) / 10
                    ((i * yScale) + yMin).toString()
                }
                .labelAndAxisLinePadding(30.dp)
                .axisLabelColor(Color.Blue)
                .axisLineColor(Color.DarkGray)
                .build()
            val data = WaveChartData(
                wavePlotData = WavePlotData(
                    lines = listOf(
                        Wave(
                            dataPoints = pointsData,
                            waveStyle = LineStyle(lineType = LineType.SmoothCurve(), color = Color.Blue),
                            intersectionPoint = IntersectionPoint(color = Color.Red),
                            selectionHighlightPopUp = SelectionHighlightPopUp(popUpLabel = { x, y ->
                                val xLabel = "x : ${x.toInt()} "
                                val yLabel = "y : ${y.toInt()}"
                                "$xLabel $yLabel"
                            })
                        )
                    )
                ),
                xAxisData = xAxisData,
                yAxisData = yAxisData
            )
            WaveChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                waveChartData = data
            )
        }
    }
}
