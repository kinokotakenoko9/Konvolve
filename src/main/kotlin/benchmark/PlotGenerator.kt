package org.example.benchmark

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomTile
import org.jetbrains.letsPlot.ggplot
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.scale.scaleFillGradient
import java.io.File

@Serializable
data class FullBenchmarkResult(
    val benchmark: String,
    val params: Map<String, String> = emptyMap(),
    val primaryMetric: PrimaryMetric
)

@Serializable
data class PrimaryMetric(
    val score: Double
)

private val json = Json {
    ignoreUnknownKeys = true
}

fun plotBenchmark(file: File) {
    val rawJson = file.readText()
    val results = json.decodeFromString<List<FullBenchmarkResult>>(rawJson)

    val data = mapOf(
        "Image" to results.map { it.params["imageName"] ?: "?" },
        "Mode" to results.map { it.params["modeName"] ?: "?" },
        "Score" to results.map { it.primaryMetric.score }
    )

    val p = ggplot(data) + geomTile {
        x = "Image"; y = "Mode"; fill = "Score"
    } + scaleFillGradient(low = "#f0f9e8", high = "#08589e") +
            labs(title = "Benchmark Heatmap (ms)", fill = "Time (ms)")

    ggsave(p, "benchmark_plot.png", dpi = 150, path = ".")
}

fun main(args: Array<String>) {
    val input = args.getOrNull(0) ?: error("Expected path to JSON benchmark file.")
    val file = File(input)
    plotBenchmark(file)
}
