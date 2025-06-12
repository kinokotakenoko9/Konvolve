package image

import kernels.Kernel
import parallel.NoParallelMode
import parallel.ParallelMode

open class Image(
    private val dirName: String,
    private val filename: String
) {
    private var data = ImageData(dirName, filename)
    private var _parallelMode: ParallelMode = NoParallelMode()

    private var isFresh = true
    val name = "$filename.bmp"

    fun writeToFile(label: String, dirOut: String): Image {
        data.writeToFile("$filename-$label", dirOut)
        return this
    }

    fun setParallelMode(parallelMode: ParallelMode): Image {
        _parallelMode = parallelMode
        return this
    }

    fun applyKernel(kernel: Kernel): Image {
        _parallelMode.run(data, kernel)
        isFresh = false
        return this
    }

    fun resetData(): Image {
        if (isFresh) return this
        data = ImageData(dirName, filename)
        isFresh = true
        return this
    }

    fun getPixelData(): IntArray {
        return data.clonePixelData()
    }
}
