package tasks

import image.Image
import kernels.*
import parallel.GridParallelMode

fun applyKernels(kernels: Array<Pair<String, Kernel>>, images: Array<Image>) {
    images.forEach { img ->
        kernels.forEach { (name, kernel) ->
            img
                .resetData()
                .setParallelMode(GridParallelMode(Runtime.getRuntime().availableProcessors(), 32))
                .applyKernel(kernel)
                .writeToFile("[$name]", "assets/images/output")
        }
    }
}

fun main() {
    applyKernels(
        arrayOf(
            "MotionBlurKernel 5" to MotionBlurKernel(9, MotionBlurDirection.DIAGONAL_TL_BR),
            "GaussianKernel 15" to GaussianKernel(15),
            "FindEdgesKernel 15" to FindEdgesKernel(5, EdgeDirection.ALL_DIRECTIONS),
        ),
        arrayOf(
            Image("assets/images/input", "flower"),
            Image("assets/images/input", "cat"),
            Image("assets/images/input", "city")
        )
    )
}