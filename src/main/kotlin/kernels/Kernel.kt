package org.example.kernels

import org.example.image.ImageData

abstract class Kernel {
    abstract val kernel: Array<FloatArray>

    val size
        get() = kernel.size

    val offset
        get() = size / 2

    fun getCoeff(x: Int, y: Int): Float {
        return kernel[y][x]
    }

    fun applyKernelToPixel(img: ImageData, x: Int, y: Int) {
        val inputPixels = img.clonePixelData() // TODO: optimize, each call new array is created

        var sumR = 0f
        var sumG = 0f
        var sumB = 0f

        for (ky in 0 until size) {
            for (kx in 0 until size) {
                val px = (x + kx - offset).coerceIn(0, img.width - 1)
                val py = (y + ky - offset).coerceIn(0, img.height - 1)
                val color = inputPixels[py * img.width + px]

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

        img.pixelData[y * img.width + x] = (newR shl 16) or (newG shl 8) or newB
    }
}
