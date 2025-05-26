package org.example

import org.example.image.Image
import org.example.kernels.GaussianKernel
import org.example.parallel.GridParallelMode

fun main() {

    val imgName = "flower"

    val im = Image(imgName)
    im.setParallelMode(GridParallelMode(4, 4))
    var startTime = System.nanoTime()
    im.applyKernel(GaussianKernel(5))
    val endTime = System.nanoTime()
    val d = (endTime - startTime) / 1_000_000
    im.writeToFile("gb-grid")
    println("grid:5, gb:3 in " + d + "ms")
}
