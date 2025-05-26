package org.example.kernels

import kotlin.math.PI
import kotlin.math.exp

class GaussianKernel(private val kernelSize: Int, private val sigma: Float = kernelSize / 6f) : Kernel() {
    private fun generateGaussianKernel(size: Int, sigma: Float): Array<FloatArray> {
        require(size % 2 == 1) { "Kernel size must be odd" }

        val kernel = Array(size) { FloatArray(size) }
        val mean = size / 2
        var sum = 0f

        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = (x - mean).toFloat()
                val dy = (y - mean).toFloat()

                kernel[y][x] = exp(-(dx * dx + dy * dy) / (2 * sigma * sigma)) / (2 * PI.toFloat() * sigma * sigma)
                sum += kernel[y][x]
            }
        }

        for (y in 0 until size) {
            for (x in 0 until size) {
                kernel[y][x] /= sum
            }
        }

        return kernel
    }

    override val kernel: Array<FloatArray> = generateGaussianKernel(kernelSize, sigma)
}
