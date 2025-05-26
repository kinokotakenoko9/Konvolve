package org.example.parallel

import org.example.image.ImageData
import org.example.kernels.Kernel
import java.util.concurrent.Executors

class ColumnParallelMode(private val threadNumber: Int) : ParallelMode {
    override fun run(img: ImageData, kernel: Kernel) {
        val executor = Executors.newFixedThreadPool(threadNumber)

        for (x in 0 until img.width) {
            executor.execute {
                for (y in 0 until img.height) {
                    kernel.applyKernelToPixel(img, x, y)
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) {
        }
    }
}
