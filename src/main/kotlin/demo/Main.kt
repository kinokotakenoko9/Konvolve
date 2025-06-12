package demo

import image.Image
import kernels.GaussianKernel
import parallel.GridParallelMode

fun main() {
    Image("assets/images/input", "flower")
        .setParallelMode(GridParallelMode(4, 32))
        .applyKernel(GaussianKernel(5))
        .writeToFile("gs5-grid32", "assets/images/output")
}
