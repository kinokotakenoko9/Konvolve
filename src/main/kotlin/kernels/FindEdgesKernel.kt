package kernels

import org.jetbrains.letsPlot.commons.intern.math.distance
import kotlin.math.abs

enum class EdgeDirection {
    HORIZONTAL_EDGE,
    VERTICAL_EDGE,
    ALL_DIRECTIONS
}

class FindEdgesKernel(private val kernelSize: Int, private val direction: EdgeDirection) : Kernel() {
    init {
        require(kernelSize > 0) { "Kernel size must be positive." }
        require(kernelSize >= 3) { "Kernel size must be at least 3 for this edge detection pattern." }
        require(kernelSize % 2 == 1) { "Kernel size must be odd." }
    }

    private fun generateFindEdgesKernel(): Array<FloatArray> {
        val kernelMatrix = Array(kernelSize) { FloatArray(kernelSize) { 0.0f } }

        when (direction) {
            EdgeDirection.HORIZONTAL_EDGE -> {
                val centerCol = kernelSize / 2
                for (i in 0 until kernelSize) {
                    kernelMatrix[i][centerCol] = (-abs(i+1 - centerCol)).toFloat()
                }
                kernelMatrix[centerCol][centerCol] = -kernelMatrix.sumOf { row -> row.sum().toDouble() }.toFloat()
            }
            EdgeDirection.VERTICAL_EDGE -> {
                val centerRow = kernelSize / 2
                for (i in 0 until kernelSize) {
                    kernelMatrix[centerRow][i] = (-abs(i+1 - centerRow)).toFloat()
                }
                kernelMatrix[centerRow][centerRow] = -kernelMatrix.sumOf { row -> row.sum().toDouble() }.toFloat()
            }
            EdgeDirection.ALL_DIRECTIONS -> {
                require(kernelSize >= 3) { "Kernel size must be at least 3 for ALL_DIRECTIONS edge detection." }

                val center = kernelSize / 2

                for (y in 0 until kernelSize) {
                    for (x in 0 until kernelSize) {
                        val level = distance(center.toDouble(), center.toDouble(), x.toDouble(), y.toDouble()).toInt()
                        kernelMatrix[y][x] = (-level).toFloat()
                    }
                }

                kernelMatrix[center][center] = -kernelMatrix.sumOf { row -> row.sum().toDouble() }.toFloat()
            }
        }

        return kernelMatrix
    }

    override val kernel: Array<FloatArray> = generateFindEdgesKernel()
}