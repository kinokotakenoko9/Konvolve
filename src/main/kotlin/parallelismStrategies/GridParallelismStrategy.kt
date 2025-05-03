package org.example.parallelismStrategies

import org.example.Filter
import org.example.Pixel
import org.example.kernels.Kernel
import java.util.concurrent.Executors

class GridParallelismStrategy : ParallelismStrategy {
    override fun parallelise(image: Filter, kernel: Kernel, f: (p: Pixel) -> Unit) {
        val kernelSize = kernel.size
        val kernelOffset = kernelSize / 2

        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())


        // Iterate through the image in blocks
        for (blockY in 0 until height step blockSize) {
            for (blockX in 0 until width step blockSize) {
                executor.execute {
                    val blockEndX = min(blockX + blockSize, width)
                    val blockEndY = min(blockY + blockSize, height)

                    for (y in blockY until blockEndY) {
                        for (x in blockX until blockEndX) {
                            var sumR = 0f
                            var sumG = 0f
                            var sumB = 0f

                            // Apply Gaussian kernel to each pixel inside the block
                            for (ky in 0 until kernelSize) {
                                for (kx in 0 until kernelSize) {
                                    val px = (x + kx - kernelOffset).coerceIn(0, width - 1)
                                    val py = (y + ky - kernelOffset).coerceIn(0, height - 1)
                                    val color = inputPixels[py * width + px]

                                    val r = (color shr 16) and 0xFF
                                    val g = (color shr 8) and 0xFF
                                    val b = color and 0xFF

                                    sumR += r * kernel[ky][kx]
                                    sumG += g * kernel[ky][kx]
                                    sumB += b * kernel[ky][kx]
                                }
                            }

                            // Clamp values and store the new pixel
                            val newR = sumR.toInt().coerceIn(0, 255)
                            val newG = sumG.toInt().coerceIn(0, 255)
                            val newB = sumB.toInt().coerceIn(0, 255)

                            outputPixels[y * width + x] = (newR shl 16) or (newG shl 8) or newB
                        }
                    }
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) {
        }
    }

}
