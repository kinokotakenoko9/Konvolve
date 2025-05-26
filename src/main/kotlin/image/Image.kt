package org.example.image

import org.example.kernels.Kernel
import org.example.parallelismStrategies.GridParallelismStrategy
import org.example.parallelismStrategies.ParallelismStrategy
import org.example.toRGB
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import java.util.concurrent.Executors
import javax.imageio.ImageIO


open class Image(private val filename: String) : ImageFilter {
    private val image = ImageIO.read(File("assets/images/input/$filename")).toRGB()
    private var imageParallelismStrategy: ParallelismStrategy = GridParallelismStrategy(4)
    private var imageThreadsNumber = 1

    override fun writeToFile() {
        ImageIO.write(image, "bmp", File("assets/images/output/$filename"))
    }

    override fun applyParallelismStrategy(parallelismStrategy: ParallelismStrategy, threadsNumber: Int) {
        imageParallelismStrategy = parallelismStrategy
        imageThreadsNumber = threadsNumber
    }

    override fun applyKernel(kernel: Kernel) {

        val inputPixels = (image.raster.dataBuffer as DataBufferInt).data.clone()
        val outputPixels = (image.raster.dataBuffer as DataBufferInt).data

        parallelismStrategy.parallelise(outputPixels, kernel) { p ->

        }



        val imageOut = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_RGB)
        val imageOutData = (imageOut.raster.dataBuffer as DataBufferInt).data
        System.arraycopy(outputPixels, 0, imageOutData, 0, outputPixels.size)

        ImageIO.write(imageOut, "bmp", File("assets/images/output/$filename"))
    }

    override fun startTimer() {
        TODO("Not yet implemented")
    }

    override fun endTimer(): Long {
        TODO("Not yet implemented")
    }
}
