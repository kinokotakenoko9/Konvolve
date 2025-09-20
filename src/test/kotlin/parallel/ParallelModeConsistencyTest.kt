package parallel

import image.Image
import kernels.GaussianKernel
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ParallelModeConsistencyTest {
    @TempDir
    lateinit var tempTestDir: File

    private lateinit var inputImageDir: File
    private lateinit var outputImageDir: File
    private val testImageFilename = "test_image"

    @BeforeEach
    fun setup() {
        inputImageDir = File(tempTestDir, "input").apply { mkdirs() }
        outputImageDir = File(tempTestDir, "output").apply { mkdirs() }

        val width = 50
        val height = 50
        val dummyImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val red = (x * 255 / width)
                val green = (y * 255 / height)
                val blue = 100
                dummyImage.setRGB(x, y, (red shl 16) or (green shl 8) or blue)
            }
        }

        ImageIO.write(dummyImage, "bmp", File(inputImageDir, "$testImageFilename.bmp"))
    }

    @Test
    fun `GridParallelMode should produce the same image as NoParallelMode`() {
        val kernel = GaussianKernel(3)

        val imageInstance = Image(inputImageDir.absolutePath, testImageFilename)
            .setParallelMode(NoParallelMode())
            .applyKernel(kernel)

        val pixelDataNoParallel = imageInstance.getPixelData()

        imageInstance.resetData()
            .setParallelMode(GridParallelMode(threadNumber = 2, blockSize = 32))
            .applyKernel(kernel)

        val pixelDataGridParallel = imageInstance.getPixelData()

        assertArrayEquals(
            pixelDataNoParallel,
            pixelDataGridParallel,
            "The pixel data from GridParallelMode should be identical to NoParallelMode"
        )
    }

    @Test
    fun `ColumnParallelMode should produce the same image as NoParallelMode`() {
        val kernel = GaussianKernel(3)

        val imageInstance = Image(inputImageDir.absolutePath, testImageFilename)
            .setParallelMode(NoParallelMode())
            .applyKernel(kernel)

        val pixelDataNoParallel = imageInstance.getPixelData()

        imageInstance.resetData()
            .setParallelMode(ColumnParallelMode(threadNumber = 2))
            .applyKernel(kernel)

        val pixelDataGridParallel = imageInstance.getPixelData()

        assertArrayEquals(
            pixelDataNoParallel,
            pixelDataGridParallel,
            "The pixel data from ColumnParallelMode should be identical to NoParallelMode"
        )
    }

    @Test
    fun `RowParallelMode should produce the same image as NoParallelMode`() {
        val kernel = GaussianKernel(3)

        val imageInstance = Image(inputImageDir.absolutePath, testImageFilename)
            .setParallelMode(NoParallelMode())
            .applyKernel(kernel)

        val pixelDataNoParallel = imageInstance.getPixelData()

        imageInstance.resetData()
            .setParallelMode(RowParallelMode(threadNumber = 2))
            .applyKernel(kernel)

        val pixelDataGridParallel = imageInstance.getPixelData()

        assertArrayEquals(
            pixelDataNoParallel,
            pixelDataGridParallel,
            "The pixel data from RowParallelMode should be identical to NoParallelMode"
        )
    }

    @Test
    fun `PixelParallelMode should produce the same image as NoParallelMode`() {
        val kernel = GaussianKernel(3)

        val imageInstance = Image(inputImageDir.absolutePath, testImageFilename)
            .setParallelMode(NoParallelMode())
            .applyKernel(kernel)

        val pixelDataNoParallel = imageInstance.getPixelData()

        imageInstance.resetData()
            .setParallelMode(PixelParallelMode(threadNumber = 2))
            .applyKernel(kernel)

        val pixelDataGridParallel = imageInstance.getPixelData()

        assertArrayEquals(
            pixelDataNoParallel,
            pixelDataGridParallel,
            "The pixel data from PixelParallelMode should be identical to NoParallelMode"
        )
    }
}