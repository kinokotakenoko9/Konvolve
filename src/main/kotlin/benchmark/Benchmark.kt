package org.example.benchmark

import kotlinx.benchmark.*
import kotlinx.benchmark.Benchmark
import org.example.image.Image
import org.example.kernels.GaussianKernel
import org.example.kernels.Kernel
import org.example.parallel.*
import org.openjdk.jmh.annotations.Scope

@State(Scope.Benchmark)
open class Benchmark {
    private val numThreads = 4
    private lateinit var img: Image
    private lateinit var kernel: Kernel

    @Param(
        "no parallel"
//        , "column"
//        , "row"
//        , "pixel"
        , "grid 32"
    )
    private lateinit var modeName: String

    @Param(
        "Gaussian 5"
//        , "Gaussian 15"
    )
    private lateinit var kernelName: String

    @Param(
        "flower"
        , "cat"
        , "monkey"
    )
    private lateinit var imageName: String

    @Setup
    fun prepare() {
        img = Image(imageName)

        val mode = when (modeName) {
            "no parallel" -> NoParallelMode()
            "column" -> ColumnParallelMode(numThreads)
            "row" -> RowParallelMode(numThreads)
            "pixel" -> PixelParallelMode(numThreads)
            "grid 32" -> GridParallelMode(numThreads, 32)
            else -> error("Unknown mode: $modeName")
        }

        kernel = when (kernelName) {
            "Gaussian 5" -> GaussianKernel(5)
            "Gaussian 15" -> GaussianKernel(15)
            else -> error("Unknown kernel: $kernelName")
        }

        img.setParallelMode(mode)
    }

    @Benchmark
    fun convolve() {
        img.applyKernel(kernel)
    }
}
//
//fun getStatistic() {
//    val k = GaussianKernel(5)
//
//    val modes = listOf(
//        "no parallel" to NoParallelMode(),
//        "column" to ColumnParallelMode(numThreads),
//        "row" to RowParallelMode(numThreads),
//        "pixel" to PixelParallelMode(numThreads),
//        "grid 32" to GridParallelMode(numThreads, 32)
//    )
//
//    val results = mutableListOf<Pair<String, Double>>() // (mode, ms)
//
//    for ((name, m) in modes) {
//        img.resetData().setParallelMode(m)
//        val duration = measureTime {
//            img.applyKernel(k)
//        }
//        img.writeToFile(name)
//        val ms = duration.inWholeMilliseconds.toDouble()
//        results.add(name to ms)
//    }
//
//    // Build plot data
//    val data = mapOf(
//        "mode" to results.map { it.first },
//        "time_ms" to results.map { it.second }
//    )
//
//    val plot = letsPlot(data) +
//            geomBar(stat = Stat.identity) {
//                x = "mode"
//                y = "time_ms"
//                fill = "mode"
//            } +
//            ggtitle("Image: ${img.name}") +
//            ylab("ms") +
//            xlab("mode") +
//            ggsize(600, 400)
//
//    ggsave(plot, "benchmark_plot.png", dpi = 150, path = ".")
//    println("Saved benchmark_plot.png")
//}