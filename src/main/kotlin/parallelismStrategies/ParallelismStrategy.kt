package org.example.parallelismStrategies

import org.example.Filter
import org.example.Pixel
import org.example.kernels.Kernel

interface ParallelismStrategy {
    fun parallelise(imageData: Filter, kernel: Kernel, f: (p : Pixel) -> Unit)
}