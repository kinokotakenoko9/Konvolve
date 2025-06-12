package org.example.image

import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.io.File
import javax.imageio.ImageIO

class ImageData(
    filename: String,
) {
    private val img: BufferedImage = ImageIO.read(File("assets/images/input/$filename.bmp")).toRGB()

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
        val imageOut = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_RGB)
        val imageOutData = (imageOut.raster.dataBuffer as DataBufferInt).data
        System.arraycopy(pixelData, 0, imageOutData, 0, pixelData.size)

        ImageIO.write(imageOut, "bmp", File("assets/images/output/$filename.bmp"))
    }
}

fun BufferedImage.toRGB(): BufferedImage {
    if (type == BufferedImage.TYPE_INT_RGB) return this
    return BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).apply {
        createGraphics().drawImage(this@toRGB, 0, 0, null)
    }
}

