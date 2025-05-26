package org.example.image

import org.example.kernels.Kernel
import org.example.parallelismStrategies.ParallelismStrategy


interface ImageFilter {
    fun writeToFile()
    fun applyParallelismStrategy(parallelismStrategy: ParallelismStrategy, threadsNumber: Int)
    fun applyKernel(kernel: Kernel)
    fun startTimer()
    fun endTimer(): Long
}

