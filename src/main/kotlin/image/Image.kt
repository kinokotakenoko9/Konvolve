package image

import kernels.Kernel
import parallel.NoParallelMode
import parallel.ParallelMode

open class Image {
    private val dirName: String
    private val filename: String
    private var data: ImageData?
    private var _parallelMode: ParallelMode = NoParallelMode()
    val name: String
    private var isFresh = true

    constructor(dirName: String, filename: String) {
        this.dirName = dirName
        this.filename = filename
        this.data = ImageData(dirName, filename)
        this.name = "$filename.bmp"
    }

    private constructor() {
        this.dirName = ""
        this.filename = "POISON_PILL"
        this.name = "POISON_PILL"
        this.data = null
        this.isFresh = false
    }

    companion object {
        val POISON_PILL = Image()
    }

    fun writeToFile(label: String, dirOut: String): Image {
        data?.writeToFile("$filename-$label", dirOut)
        return this
    }

    fun setParallelMode(parallelMode: ParallelMode): Image {
        _parallelMode = parallelMode
        return this
    }

    fun applyKernel(kernel: Kernel): Image {
        data?.let {
            _parallelMode.run(it, kernel)
        }
        isFresh = false
        return this
    }

    fun resetData(): Image {
        if (isFresh) return this
        data = ImageData(dirName, filename)
        isFresh = true
        return this
    }

    fun getPixelData(): IntArray? {
        return data?.clonePixelData()
    }
}
