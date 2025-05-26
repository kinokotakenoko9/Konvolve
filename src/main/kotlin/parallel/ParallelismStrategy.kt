package org.example.parallelismStrategies

import org.example.Filter
import org.example.Pixel
import org.example.image.ImageFilter
import org.example.kernels.Kernel

interface ParallelismStrategy {
    fun parallelise(imageData: ImageFilter, kernel: Kernel, f: (p : Pixel) -> Unit)
}