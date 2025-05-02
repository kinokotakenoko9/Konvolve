package org.example

import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import java.util.concurrent.Executors
import javax.imageio.ImageIO
import kotlin.math.*

interface Kernel {
    val kernel:Array<FloatArray>

    val size: Int
        get() = kernel.size

    fun getCoeff(x : Int, y : Int): Float {
        return kernel[y][x]
    }
}

class GaussianKernel(val kernelSize: Int) : Kernel {
    private val sigma = kernelSize / 6f
    private fun generateGaussianKernel(size: Int, sigma: Float): Array<FloatArray> {
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

    override val kernel = generateGaussianKernel(kernelSize, sigma)

}

data class Pixel (val red: Int, val green: Int, val blue: Int)

interface ParallelismStrategy {
    fun parallelise(imageData: Filter, kernel: Kernel, f: (p : Pixel) -> Unit)
}

object GridParallelismStrategy : ParallelismStrategy{
    override fun parallelise(image: Filter, kernel: Kernel, f: (p: Pixel) -> Unit) {
        val kernelSize = kernel.size
        val kernelOffset = kernelSize / 2

        val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())


        // Iterate through the image in blocks
        for (blockY in 0 until height step blockSize) {
            for (blockX in 0 until width step blockSize) {
                executor.execute {
                    val blockEndX = min(blockX + blockSize, width)
                    val blockEndY = min(blockY + blockSize, height)

                    for (y in blockY until blockEndY) {
                        for (x in blockX until blockEndX) {
                            var sumR = 0f
                            var sumG = 0f
                            var sumB = 0f

                            // Apply Gaussian kernel to each pixel inside the block
                            for (ky in 0 until kernelSize) {
                                for (kx in 0 until kernelSize) {
                                    val px = (x + kx - kernelOffset).coerceIn(0, width - 1)
                                    val py = (y + ky - kernelOffset).coerceIn(0, height - 1)
                                    val color = inputPixels[py * width + px]

                                    val r = (color shr 16) and 0xFF
                                    val g = (color shr 8) and 0xFF
                                    val b = color and 0xFF

                                    sumR += r * kernel[ky][kx]
                                    sumG += g * kernel[ky][kx]
                                    sumB += b * kernel[ky][kx]
                                }
                            }

                            // Clamp values and store the new pixel
                            val newR = sumR.toInt().coerceIn(0, 255)
                            val newG = sumG.toInt().coerceIn(0, 255)
                            val newB = sumB.toInt().coerceIn(0, 255)

                            outputPixels[y * width + x] = (newR shl 16) or (newG shl 8) or newB
                        }
                    }
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) {
        }
    }

}

interface Filter {
    fun writeToFile()
    fun applyKernel(kernel: Kernel, parallelMode: ParallelismStrategy)


}

open class Image(private val filename: String) : Filter {
    private val image = ImageIO.read(File("assets/images/input/$filename")).toRGB()
    private val width = image.width
    private val height = image.height

    override fun writeToFile() {
        ImageIO.write(image, "bmp", File("assets/images/output/$filename"))
    }

    override fun applyKernel(kernel: Kernel, parallelismStrategy: ParallelismStrategy) {

        val inputPixels = (image.raster.dataBuffer as DataBufferInt).data.clone()
        val outputPixels = (image.raster.dataBuffer as DataBufferInt).data

        parallelismStrategy.parallelise(outputPixels, kernel) { p ->

        }



        val imageOut = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        val imageOutData = (imageOut.raster.dataBuffer as DataBufferInt).data
        System.arraycopy(outputPixels, 0, imageOutData, 0, outputPixels.size)

        ImageIO.write(imageOut, "bmp", File("assets/images/output/$filename"))
    }
}

fun main() {

    val imgName = "view.bmp"

    var startTime = System.nanoTime()
    simpleApply(imgName, kernelGB)
    var endTime = System.nanoTime()
    var duration = (endTime - startTime) / 1_000_000
    println("simpleApply; " + duration + "ms")

    startTime = System.nanoTime()
    applyGaussianBlurParallelPixels(imgName, kernelGB)
    endTime = System.nanoTime()
    duration = (endTime - startTime) / 1_000_000
    println("applyGaussianBlurParallelPixels; " + duration + "ms")

    startTime = System.nanoTime()
    applyGaussianBlurParallelRows(imgName, kernelGB)
    endTime = System.nanoTime()
    duration = (endTime - startTime) / 1_000_000
    println("applyGaussianBlurParallelRows; " + duration + "ms")

    startTime = System.nanoTime()
    applyGaussianBlurParallelColumns(imgName, kernelGB)
    endTime = System.nanoTime()
    duration = (endTime - startTime) / 1_000_000
    println("applyGaussianBlurParallelColumns; " + duration + "ms")

    startTime = System.nanoTime()
    applyGaussianBlurParallelGrid(imgName, kernelGB, 48)
    endTime = System.nanoTime()
    duration = (endTime - startTime) / 1_000_000
    println("applyGaussianBlurParallelGrid; " + duration + "ms")
}


fun applyGaussianBlurParallelPixels(filename: String, kernel: Array<FloatArray>) {
    val imagePath = "assets/images/input/$filename"
    val image = ImageIO.read(File(imagePath)).toRGB()

    val width = image.width
    val height = image.height
    val inputPixels = (image.raster.dataBuffer as DataBufferInt).data.clone()
    val outputPixels = (image.raster.dataBuffer as DataBufferInt).data
    val kernelSize = kernel.size
    val kernelOffset = kernelSize / 2

    val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    for (y in 0 until height) {
        for (x in 0 until width) {
            executor.execute {
                var sumR = 0f
                var sumG = 0f
                var sumB = 0f
                for (ky in 0 until kernelSize) {
                    for (kx in 0 until kernelSize) {
                        val px = (x + kx - kernelOffset).coerceIn(0, width - 1)
                        val py = (y + ky - kernelOffset).coerceIn(0, height - 1)
                        val color = inputPixels[py * width + px]
                        val r = (color shr 16) and 0xFF
                        val g = (color shr 8) and 0xFF
                        val b = color and 0xFF
                        sumR += r * kernel[ky][kx]
                        sumG += g * kernel[ky][kx]
                        sumB += b * kernel[ky][kx]
                    }
                }
                val newColor = ((sumR.toInt().coerceIn(0, 255) shl 16) or
                        (sumG.toInt().coerceIn(0, 255) shl 8) or
                        (sumB.toInt().coerceIn(0, 255)))
                outputPixels[y * width + x] = newColor
            }
        }
    }
    executor.shutdown()
    while (!executor.isTerminated) {
    }

    val imageOut = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    val imageOutData = (imageOut.raster.dataBuffer as DataBufferInt).data
    System.arraycopy(outputPixels, 0, imageOutData, 0, outputPixels.size)

    ImageIO.write(imageOut, "bmp", File("assets/images/output/$filename"))
}

fun applyGaussianBlurParallelRows(filename: String, kernel: Array<FloatArray>) {
    val imagePath = "assets/images/input/$filename"
    val image = ImageIO.read(File(imagePath)).toRGB()

    val width = image.width
    val height = image.height
    val inputPixels = (image.raster.dataBuffer as DataBufferInt).data.clone()
    val outputPixels = (image.raster.dataBuffer as DataBufferInt).data
    val kernelSize = kernel.size
    val kernelOffset = kernelSize / 2

    val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    for (y in 0 until height) {
        executor.execute {
            for (x in 0 until width) {

                var sumR = 0f
                var sumG = 0f
                var sumB = 0f
                for (ky in 0 until kernelSize) {
                    for (kx in 0 until kernelSize) {
                        val px = (x + kx - kernelOffset).coerceIn(0, width - 1)
                        val py = (y + ky - kernelOffset).coerceIn(0, height - 1)
                        val color = inputPixels[py * width + px]
                        val r = (color shr 16) and 0xFF
                        val g = (color shr 8) and 0xFF
                        val b = color and 0xFF
                        sumR += r * kernel[ky][kx]
                        sumG += g * kernel[ky][kx]
                        sumB += b * kernel[ky][kx]
                    }
                }
                val newColor = ((sumR.toInt().coerceIn(0, 255) shl 16) or
                        (sumG.toInt().coerceIn(0, 255) shl 8) or
                        (sumB.toInt().coerceIn(0, 255)))
                outputPixels[y * width + x] = newColor
            }
        }
    }
    executor.shutdown()
    while (!executor.isTerminated) {
    }

    val imageOut = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    val imageOutData = (imageOut.raster.dataBuffer as DataBufferInt).data
    System.arraycopy(outputPixels, 0, imageOutData, 0, outputPixels.size)

    ImageIO.write(imageOut, "bmp", File("assets/images/output/$filename"))
}

fun applyGaussianBlurParallelColumns(filename: String, kernel: Array<FloatArray>) {
    val imagePath = "assets/images/input/$filename"
    val image = ImageIO.read(File(imagePath)).toRGB()

    val width = image.width
    val height = image.height
    val inputPixels = (image.raster.dataBuffer as DataBufferInt).data.clone()
    val outputPixels = (image.raster.dataBuffer as DataBufferInt).data
    val kernelSize = kernel.size
    val kernelOffset = kernelSize / 2

    val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    for (x in 0 until width) {
        executor.execute {
            for (y in 0 until height) {

                var sumR = 0f
                var sumG = 0f
                var sumB = 0f
                for (ky in 0 until kernelSize) {
                    for (kx in 0 until kernelSize) {
                        val px = (x + kx - kernelOffset).coerceIn(0, width - 1)
                        val py = (y + ky - kernelOffset).coerceIn(0, height - 1)
                        val color = inputPixels[py * width + px]
                        val r = (color shr 16) and 0xFF
                        val g = (color shr 8) and 0xFF
                        val b = color and 0xFF
                        sumR += r * kernel[ky][kx]
                        sumG += g * kernel[ky][kx]
                        sumB += b * kernel[ky][kx]
                    }
                }
                val newColor = ((sumR.toInt().coerceIn(0, 255) shl 16) or
                        (sumG.toInt().coerceIn(0, 255) shl 8) or
                        (sumB.toInt().coerceIn(0, 255)))
                outputPixels[y * width + x] = newColor
            }
        }
    }
    executor.shutdown()
    while (!executor.isTerminated) {
    }

    val imageOut = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    val imageOutData = (imageOut.raster.dataBuffer as DataBufferInt).data
    System.arraycopy(outputPixels, 0, imageOutData, 0, outputPixels.size)

    ImageIO.write(imageOut, "bmp", File("assets/images/output/$filename"))
}

fun applyGaussianBlurParallelGrid(filename: String, kernel: Array<FloatArray>, blockSize: Int) {
    val imagePath = "assets/images/input/$filename"
    val image = ImageIO.read(File(imagePath)).toRGB()

    val width = image.width
    val height = image.height
    val inputPixels = (image.raster.dataBuffer as DataBufferInt).data.clone()
    val outputPixels = (image.raster.dataBuffer as DataBufferInt).data
    val kernelSize = kernel.size
    val kernelOffset = kernelSize / 2

    val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    // Iterate through the image in blocks
    for (blockY in 0 until height step blockSize) {
        for (blockX in 0 until width step blockSize) {
            executor.execute {
                val blockEndX = min(blockX + blockSize, width)
                val blockEndY = min(blockY + blockSize, height)

                for (y in blockY until blockEndY) {
                    for (x in blockX until blockEndX) {
                        var sumR = 0f
                        var sumG = 0f
                        var sumB = 0f

                        // Apply Gaussian kernel to each pixel inside the block
                        for (ky in 0 until kernelSize) {
                            for (kx in 0 until kernelSize) {
                                val px = (x + kx - kernelOffset).coerceIn(0, width - 1)
                                val py = (y + ky - kernelOffset).coerceIn(0, height - 1)
                                val color = inputPixels[py * width + px]

                                val r = (color shr 16) and 0xFF
                                val g = (color shr 8) and 0xFF
                                val b = color and 0xFF

                                sumR += r * kernel[ky][kx]
                                sumG += g * kernel[ky][kx]
                                sumB += b * kernel[ky][kx]
                            }
                        }

                        // Clamp values and store the new pixel
                        val newR = sumR.toInt().coerceIn(0, 255)
                        val newG = sumG.toInt().coerceIn(0, 255)
                        val newB = sumB.toInt().coerceIn(0, 255)

                        outputPixels[y * width + x] = (newR shl 16) or (newG shl 8) or newB
                    }
                }
            }
        }
    }

    executor.shutdown()
    while (!executor.isTerminated) {
    }

    val imageOut = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
    val imageOutData = (imageOut.raster.dataBuffer as DataBufferInt).data
    System.arraycopy(outputPixels, 0, imageOutData, 0, outputPixels.size)

    ImageIO.write(imageOut, "bmp", File("assets/images/output/$filename"))
}

private fun simpleApply(filename: String, k: Array<FloatArray>) {
    val imagePath = "assets/images/input/$filename"
    val image = ImageIO.read(File(imagePath)).toRGB()

    val imageData = (image.raster.dataBuffer as DataBufferInt).data.clone()
    val newImageData = IntArray(imageData.size)

    val kernelHeight = k.size
    val kernelWidth = k[0].size
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

                    sumR += r * kernelGB[ky][kx]
                    sumG += g * kernelGB[ky][kx]
                    sumB += b * kernelGB[ky][kx]
                }
            }

            val newR = sumR.toInt().coerceIn(0, 255)
            val newG = sumG.toInt().coerceIn(0, 255)
            val newB = sumB.toInt().coerceIn(0, 255)

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