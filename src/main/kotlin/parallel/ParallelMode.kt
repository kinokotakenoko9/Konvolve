package parallel

import image.ImageData
import kernels.Kernel

interface ParallelMode {
    fun run(img: ImageData, kernel: Kernel)
}
