package org.example

import org.example.benchmark.Benchmark
import org.example.image.Image
import org.example.kernels.GaussianKernel
import org.example.parallel.GridParallelMode
import org.example.parallel.ParallelMode

fun main() {
    val imgName = "flower"
    val im = Image(imgName)
    im.setParallelMode(GridParallelMode(4, 32))
    im.applyKernel(GaussianKernel(5))
    im.writeToFile("gs5-grid32")
    println("start")
}
