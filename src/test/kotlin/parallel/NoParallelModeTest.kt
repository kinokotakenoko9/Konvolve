package parallel

import image.Image
import kernels.GaussianKernel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class NoParallelModeTest {
    @TempDir
    lateinit var tempTestDir: File

    private lateinit var inputImageDir: File
    private lateinit var outputImageDir: File
    private val testImageFilename = "test_image"

    private val width = 5
    private val height = 5

    @BeforeEach
    fun setup() {
        inputImageDir = File(tempTestDir, "input").apply { mkdirs() }
        outputImageDir = File(tempTestDir, "output").apply { mkdirs() }

        val dummyImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        /** Test image:
         *  0 0 0 0 0
         *  0 0 1 0 0
         *  0 0 0 0 0
         */

        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = if (x == 2 && y == 2) 255 else 0
                dummyImage.setRGB(x, y, (color shl 16) or (color shl 8) or color)
            }
        }

        ImageIO.write(dummyImage, "bmp", File(inputImageDir, "$testImageFilename.bmp"))
    }

    @Test
    fun `GaussianKernel on single pixel image should produce expected diffusion`() {
        val kernel = GaussianKernel(3)

        val imageInstance =
            Image(inputImageDir.absolutePath, testImageFilename)
                .setParallelMode(NoParallelMode())
                .applyKernel(kernel)

        val actualPixelData = imageInstance.getPixelData()

        val expectedPixelData = IntArray(width * height)
        val kernelArray = kernel.kernel
        val inputVal = 255

        for (y in 0 until height) {
            for (x in 0 until width) {
                val kx = 3 - x
                val ky = 3 - y

                var color = 0

                if (kx >= 0 && kx < kernelArray[0].size && ky >= 0 && ky < kernelArray.size) {
                    val kValue = kernelArray[ky][kx]
                    color = (kValue * inputVal).toInt().coerceIn(0, 255)
                }

                expectedPixelData[y * width + x] = (color shl 16) or (color shl 8) or color
            }
        }

        assertArrayEquals(
            expectedPixelData,
            actualPixelData,
            "The Gaussian kernel applied to a single pixel image should produce the expected diffused values.",
        )
    }
}
