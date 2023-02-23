package com.example.kmmcocoapods.ui.linechart.model

import com.example.kmmcocoapods.common.model.PlotData
import com.example.kmmcocoapods.common.model.PlotType

/**
 * LinePlotData is a data class that holds line graph related data and styling components
 * @param plotType : Defines the type of plot/graph
 * @param lines : Data related to the list of lines to be drawn.
 */
data class LinePlotData(
    override val plotType: PlotType = PlotType.Line,
    val lines: List<Line>
) : PlotData {
    companion object {
        fun default() =
            LinePlotData(lines = listOf())
    }
}
