package org.example.image

import org.example.kernels.Kernel
import org.example.parallel.NoParallelMode
import org.example.parallel.ParallelMode

open class Image(
    private val filename: String,
) {
    private val data = ImageData(filename)
    private var _parallelMode: ParallelMode = NoParallelMode()

    fun writeToFile(label: String): Image {
        data.writeToFile("$filename-$label")
        return this
    }

    fun setParallelMode(parallelMode: ParallelMode): Image {
        _parallelMode = parallelMode
        return this
    }

    fun applyKernel(kernel: Kernel): Image {
        _parallelMode.run(data, kernel)
        return this
    }
}
