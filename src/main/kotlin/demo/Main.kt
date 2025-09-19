package demo

import image.Image
import kernels.GaussianKernel
import parallel.GridParallelMode
import kotlin.time.measureTime

fun main() {
    demo1()
}

fun demo1() {
    val img = Image("assets/images/input", "flower")
        .setParallelMode(GridParallelMode(4, 32))
    val gk5 = GaussianKernel(5)
    val d = measureTime {
        img.applyKernel(gk5)
    }
    img.writeToFile("gs5-grid32", "assets/images/output")

    println(d)
}
