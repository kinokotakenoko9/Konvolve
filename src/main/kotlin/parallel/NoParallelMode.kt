package parallel

import image.ImageData
import kernels.Kernel

class NoParallelMode : ParallelMode {
    override fun run(
        img: ImageData,
        kernel: Kernel,
    ) {
        val src = img.clonePixelData()
        val dest = img.pixelData

        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                kernel.applyKernelToPixel(src, dest, img.width, img.height, x, y)
            }
        }
    }
}
