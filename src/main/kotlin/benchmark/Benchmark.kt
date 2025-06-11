package org.example.benchmark

import org.example.image.Image
import org.example.kernels.GaussianKernel
import org.example.parallel.*
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.label.xlab
import org.jetbrains.letsPlot.label.ylab
import org.jetbrains.letsPlot.letsPlot
import kotlin.time.measureTime

class Benchmark(
    val img: Image
) {
    private var entries:MutableMap<String,String> = mutableMapOf()

    fun getStatistic() {
        val k = GaussianKernel(5)
        val numThreads = 4
        val modes = listOf(
            "no parallel" to NoParallelMode(),
            "column" to ColumnParallelMode(numThreads),
            "row" to RowParallelMode(numThreads),
            "pixel" to PixelParallelMode(numThreads),
            "grid 32" to GridParallelMode(numThreads, 32)
        )

        val results = mutableListOf<Pair<String, Double>>() // (mode, ms)

        for ((name, m) in modes) {
            img.resetData().setParallelMode(m)
            val duration = measureTime {
                img.applyKernel(k)
            }
            img.writeToFile(name)
            val ms = duration.inWholeMilliseconds.toDouble()
            results.add(name to ms)
        }

        // Build plot data
        val data = mapOf(
            "mode" to results.map { it.first },
            "time_ms" to results.map { it.second }
        )

        val plot = letsPlot(data) +
                geomBar(stat = Stat.identity) {
                    x = "mode"
                    y = "time_ms"
                    fill = "mode"
                } +
                ggtitle("Image: ${img.name}") +
                ylab("ms") +
                xlab("mode") +
                ggsize(600, 400)

        ggsave(plot, "benchmark_plot.png", dpi = 150, path = ".")
        println("Saved benchmark_plot.png")
    }
}