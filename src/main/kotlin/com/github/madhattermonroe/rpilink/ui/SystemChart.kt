package com.github.madhattermonroe.rpilink.ui

import com.github.madhattermonroe.rpilink.remote.RemoteOperations
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYChartBuilder
import java.util.*

object SystemChart {
    private const val MAX_HISTORY = 60

    private val timeHistory: Deque<Double> = LinkedList()
    private val cpuHistory: Deque<Double> = LinkedList()
    private val ramHistory: Deque<Double> = LinkedList()
    private val tempHistory: Deque<Double> = LinkedList()

    private var startTime: Double = System.currentTimeMillis() / 1000.0

    fun createChart(): XYChart {
        val chart = XYChartBuilder().width(600).height(400)
            .title("Raspberry Pi Monitor")
            .xAxisTitle("Time (s)")
            .yAxisTitle("Usage (%)")
            .build()

        for (i in 0 until MAX_HISTORY) {
            timeHistory.addLast(i.toDouble() - MAX_HISTORY)
            cpuHistory.addLast(0.0)
            ramHistory.addLast(0.0)
            tempHistory.addLast(0.0)
        }

        chart.addSeries("CPU", timeHistory.toList(), cpuHistory.toList())
        chart.addSeries("RAM", timeHistory.toList(), ramHistory.toList())

        return chart
    }

    fun updateChart(chart: XYChart) {
        val currentTime = System.currentTimeMillis() / 1000.0
        val elapsedTime = currentTime - startTime

        val cpuUsage =
            RemoteOperations.executeCommand("top -bn1 | grep '%Cpu' | awk '{print 100 - $8}'")?.toDoubleOrNull() ?: 0.0
        val ramUsage =
            RemoteOperations.executeCommand("free | grep Mem | awk '{print ($3/$2)*100}'")?.toDoubleOrNull() ?: 0.0

        if (timeHistory.size >= MAX_HISTORY) {
            timeHistory.removeFirst()
            cpuHistory.removeFirst()
            ramHistory.removeFirst()
            tempHistory.removeFirst()
        }

        timeHistory.addLast(elapsedTime)
        cpuHistory.addLast(cpuUsage)
        ramHistory.addLast(ramUsage)

        chart.updateXYSeries("CPU", timeHistory.toList(), cpuHistory.toList(), null)
        chart.updateXYSeries("RAM", timeHistory.toList(), ramHistory.toList(), null)
    }
}