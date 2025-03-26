package org.example

import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.*

fun generateGaussianKernel(size: Int, sigma: Float): Array<FloatArray> {
    require(size % 2 == 1) { "Kernel size must be odd" }

    val kernel = Array(size) { FloatArray(size) }
    val mean = size / 2
    var sum = 0f

    for (y in 0 until size) {
        for (x in 0 until size) {
            val dx = (x - mean).toFloat()
            val dy = (y - mean).toFloat()

            kernel[y][x] = exp(-(dx * dx + dy * dy) / (2 * sigma * sigma)) / (2 * PI.toFloat() * sigma * sigma)
            sum += kernel[y][x]
        }
    }

    for (y in 0 until size) {
        for (x in 0 until size) {
            kernel[y][x] /= sum
        }
    }

    return kernel
}

val kernelSize = 15
val kernelGB = generateGaussianKernel(kernelSize, kernelSize / 6f)

fun main() {

    val startTime = System.nanoTime()
    simpleApply()
    val endTime = System.nanoTime()
    val duration = (endTime - startTime) / 1_000_000
    print("done; " + duration + "ms")
}

private fun simpleApply() {
    val filename = "rose.bmp"
    val imagePath = "assets/images/input/$filename"
    val image = ImageIO.read(File(imagePath)).toRGB()

    val imageData = (image.raster.dataBuffer as DataBufferInt).data.clone()
    val newImageData = IntArray(imageData.size)

    val kernelHeight = kernelGB.size
    val kernelWidth = kernelGB[0].size
    val imagew = image.width
    val imageh = image.height

    for (y in 0 until imageh) {
        for (x in 0 until imagew) {
            var sumR = 0f
            var sumG = 0f
            var sumB = 0f

            for (ky in 0 until kernelHeight) {
                for (kx in 0 until kernelWidth) {
                    val ox = kx - kernelWidth / 2
                    val oy = ky - kernelHeight / 2

                    val newX = (x + ox).coerceIn(0, imagew - 1)
                    val newY = (y + oy).coerceIn(0, imageh - 1)
                    val curi = newY * imagew + newX
                    val p = imageData[curi]

                    val r = (p shr 16) and 0xFF
                    val g = (p shr 8) and 0xFF
                    val b = p and 0xFF

//                    println("$r;$g;$b ---${ky * 3 + kx}--- $x,$y")

                    sumR += r * kernelGB[ky][kx]
                    sumG += g * kernelGB[ky][kx]
                    sumB += b * kernelGB[ky][kx]
                }
            }

            val newR = sumR.toInt().coerceIn(0, 255)
            val newG = sumG.toInt().coerceIn(0, 255)
            val newB = sumB.toInt().coerceIn(0, 255)
//            println("$newR;$newG;$newB $x,$y")
            newImageData[y * imagew + x] = (newR shl 16) or (newG shl 8) or newB
        }
    }

    val imageOut = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    val imageOutData = (imageOut.raster.dataBuffer as DataBufferInt).data
    System.arraycopy(newImageData, 0, imageOutData, 0, newImageData.size)

    ImageIO.write(imageOut, "bmp", File("assets/images/output/$filename"))
}

fun BufferedImage.toRGB(): BufferedImage {
    if (type == BufferedImage.TYPE_INT_RGB) return this
    return BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).apply {
        createGraphics().drawImage(this@toRGB, 0, 0, null)
    }
}