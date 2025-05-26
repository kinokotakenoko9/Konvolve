package org.example.parallel

import org.example.image.ImageData
import org.example.kernels.Kernel
import java.util.concurrent.Executors
import kotlin.math.min

class GridParallelMode(
    private val threadNumber: Int, private val blockSize: Int
) : ParallelMode {
    override fun run(img: ImageData, kernel: Kernel) {
        val executor = Executors.newFixedThreadPool(threadNumber)

        for (yStart in 0 until img.height step blockSize) {
            for (xStart in 0 until img.width step blockSize) {
                executor.execute {
                    val xEnd = min(xStart + blockSize, img.width)
                    val yEnd = min(yStart + blockSize, img.height)

                    for (y in yStart until yEnd) {
                        for (x in xStart until xEnd) {
                            kernel.applyKernelToPixel(img, x, y)
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
