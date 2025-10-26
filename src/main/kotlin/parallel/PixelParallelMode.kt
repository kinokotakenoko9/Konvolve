package parallel

import image.ImageData
import kernels.Kernel
import java.util.concurrent.Executors

class PixelParallelMode(
    private val threadNumber: Int,
) : ParallelMode {
    override fun run(
        img: ImageData,
        kernel: Kernel,
    ) {
        val src = img.clonePixelData()
        val dest = img.pixelData

        val executor = Executors.newFixedThreadPool(threadNumber)

        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                executor.execute {
                    kernel.applyKernelToPixel(src, dest, img.width, img.height, x, y)
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) {
        }
    }
}
