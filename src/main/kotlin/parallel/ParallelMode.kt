package org.example.parallel

import org.example.image.ImageData
import org.example.kernels.Kernel

interface ParallelMode {
    fun run(img: ImageData, kernel: Kernel)
}
