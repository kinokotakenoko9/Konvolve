package kernels

abstract class Kernel {
    abstract val kernel: Array<FloatArray>

    private val size
        get() = kernel.size

    private val offset
        get() = size / 2

    fun applyKernelToPixel(srcPixelData: IntArray, destPixelData: IntArray, w: Int, h: Int, x: Int, y: Int) {
        var sumR = 0f
        var sumG = 0f
        var sumB = 0f

        for (ky in 0 until size) {
            for (kx in 0 until size) {
                val px = (x + kx - offset).coerceIn(0, w - 1)
                val py = (y + ky - offset).coerceIn(0, h - 1)
                val color = srcPixelData[py * w + px]

                val r = (color shr 16) and 0xFF
                val g = (color shr 8) and 0xFF
                val b = color and 0xFF

                sumR += r * kernel[ky][kx]
                sumG += g * kernel[ky][kx]
                sumB += b * kernel[ky][kx]
            }
        }

        val newR = sumR.toInt().coerceIn(0, 255)
        val newG = sumG.toInt().coerceIn(0, 255)
        val newB = sumB.toInt().coerceIn(0, 255)

        destPixelData[y * w + x] = (newR shl 16) or (newG shl 8) or newB
    }
}
