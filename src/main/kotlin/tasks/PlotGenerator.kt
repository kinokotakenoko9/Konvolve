package tasks

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomText
import org.jetbrains.letsPlot.geom.geomTile
import org.jetbrains.letsPlot.ggplot
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.scale.scaleFillGradientN
import java.io.File
import kotlin.math.ceil

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
        "Kernel" to results.map { it.params["kernelName"] ?: "?" },
        "Time" to results.map { ceil(it.primaryMetric.score) }
    )

    val images = data["Image"]?.distinct() ?: emptyList()

    images.forEach { img ->
        val indices = data["Image"]!!.mapIndexedNotNull { i, k -> if (k == img) i else null }

        val filteredData = mapOf(
            "Image" to indices.map { data["Image"]!![it] },
            "Mode" to indices.map { data["Mode"]!![it] },
            "Kernel" to indices.map { data["Kernel"]!![it] },
            "Time" to indices.map { data["Time"]!![it] }
        )

        val p = ggplot(filteredData) +
                geomTile {
                    x = "Mode"; y = "Kernel"; fill = "Time"
                } +
                geomText {
                    x = "Mode"; y = "Kernel"; label = "Time"
                } +
                scaleFillGradientN(
                    colors = listOf("#baffc9", "#ffffba", "#ffdfba", "#ffb3ba")
                ) +
                labs(title = "Image: $img", fill = "Time (ms)")

        ggsave(p, "graph[$img].png", path = "assets/benchmarks")
    }
}


fun main(args: Array<String>) {
    val input = args.getOrNull(0) ?: error("Expected path to JSON benchmark file.")
    val file = File(input)
    plotBenchmark(file)
}
