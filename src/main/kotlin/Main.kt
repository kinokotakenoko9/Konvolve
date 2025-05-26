package org.example

import org.example.image.Image
import org.example.kernels.GaussianKernel
import org.example.kernels.Kernel
import org.example.parallelismStrategies.GridParallelismStrategy
import org.example.parallelismStrategies.ParallelismStrategy
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import java.util.concurrent.Executors
import javax.imageio.ImageIO
import kotlin.math.*


fun main() {

    val imgName = "view.bmp"

    val im = Image(imgName)
    im.startTimer()
    im.applyParallelismStrategy(GridParallelismStrategy(5), 4)
    im.applyKernel(GaussianKernel(3))
    im.writeToFile()
    val d = im.endTimer()
    println("grid:5, gb:3 in " + d + "ms")


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

// CORE

fun BufferedImage.toRGB(): BufferedImage {
    if (type == BufferedImage.TYPE_INT_RGB) return this
    return BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).apply {
        createGraphics().drawImage(this@toRGB, 0, 0, null)
    }
}

// allows mutability of data
class ImageData (
    private val filename: String,
) {
    private val img: BufferedImage = ImageIO.read(File("assets/images/input/$filename")).toRGB()

    val width
        get() = img.width
    val height
        get() = img.height

    private val _pixelData = (img.raster.dataBuffer as DataBufferInt).data
    val pixelData: IntArray
        get() = _pixelData

    fun clonePixelData(): IntArray {
        return (img.raster.dataBuffer as DataBufferInt).data.clone()
    }

    fun writeToFile(filename: String) {
        // TODO: think
        val imageOut = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_RGB)
        val imageOutData = (imageOut.raster.dataBuffer as DataBufferInt).data
        System.arraycopy(pixelData, 0, imageOutData, 0, pixelData.size)

        ImageIO.write(imageOut, "bmp", File("assets/images/output/$filename"))
    }
}

class KernelData(
    private val kernel: Array<FloatArray>
) {
   val size
       get() = kernel.size

    val offset
        get() = size/2

    fun getCoeff(x: Int, y: Int): Float {
        return kernel[y][x]
    }

    fun applyKernelToPixel(img: ImageData, x: Int, y: Int) {
        val inputPixels = img.clonePixelData() // TODO: optimize, each call new array is created

        var sumR = 0f
        var sumG = 0f
        var sumB = 0f

        // Apply Gaussian kernel to each pixel inside the block
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

        // Clamp values and store the new pixel
        val newR = sumR.toInt().coerceIn(0, 255)
        val newG = sumG.toInt().coerceIn(0, 255)
        val newB = sumB.toInt().coerceIn(0, 255)

        img.pixelData[y * img.width + x] = (newR shl 16) or (newG shl 8) or newB
    }


    // [start; end) // TODO: remove
    fun applyKernel(img: ImageData, xStart: Int, yStart: Int, xEnd: Int, yEnd: Int) {
        val inputPixels = img.clonePixelData()
        val outputPixels = img.pixelData
        for (y in yStart until yEnd) {
            for (x in xStart until xEnd) {
                applyKernelToPixel(img, x, y)
            }
        }
    }
}

class ParallelData() {
    fun applySequential(imgg: ImageData, kernel: KernelData) {
        for (y in 0 until imgg.height) {
            for (x in 0 until imgg.width) {
                kernel.applyKernelToPixel(imgg, x, y)
            }
        }
    }

    // MAYBE: Runtime.getRuntime().availableProcessors()
    fun applyParallelPixel(img: ImageData, kernel: KernelData, threadNumber: Int) {
        val executor = Executors.newFixedThreadPool(threadNumber)

        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                executor.execute {
                    kernel.applyKernelToPixel(img, x, y)
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) { }
    }

    fun applyParallelGid(img: ImageData, kernel: KernelData, threadNumber: Int, blockSize: Int) {
        val executor = Executors.newFixedThreadPool(threadNumber)

        for (yStart in 0 until img.height step blockSize) {
            for (xStart in 0 until img.width step blockSize) {
                executor.execute {
                    val xEnd = min(xStart + blockSize, img.width)
                    val yEnd = min(yStart + blockSize, img.height)

                    for (y in yStart until yEnd) {
                        for (x in xStart until xEnd) {
                            kernel.applyKernelToPixel(img, x, y)
                        }
                    }
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) { }
    }

    fun applyParallelRow(img: ImageData, kernel: KernelData, threadNumber: Int) {
        val executor = Executors.newFixedThreadPool(threadNumber)

        for (y in 0 until img.height) {
            executor.execute {
                for (x in 0 until img.width) {
                    kernel.applyKernelToPixel(img, x, y)
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) { }
    }

    fun applyParallelColumn(img: ImageData, kernel: KernelData, threadNumber: Int) {
        val executor = Executors.newFixedThreadPool(threadNumber)

        for (x in 0 until img.width) {
            executor.execute {
                for (y in 0 until img.height) {
                    kernel.applyKernelToPixel(img, x, y)
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) { }
    }
}
