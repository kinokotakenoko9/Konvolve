package org.example.parallel

import org.example.image.ImageData
import org.example.kernels.Kernel

class NoParallelMode : ParallelMode {
    override fun run(img: ImageData, kernel: Kernel) {
        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                kernel.applyKernelToPixel(img, x, y)
            }
        }
    }

}