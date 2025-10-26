package kernels

enum class MotionBlurDirection {
    HORIZONTAL,
    VERTICAL,
    DIAGONAL_TL_BR, // top-left to bottom-right
    DIAGONAL_TR_BL, // top-right to bottom-left
}

class MotionBlurKernel(
    private val kernelSize: Int,
    private val direction: MotionBlurDirection,
) : Kernel() {
    init {
        require(kernelSize % 2 == 1) { "Kernel size must be odd." }
        require(kernelSize > 0) { "Kernel size must be positive." }
    }

    private fun generateMotionBlurKernel(): Array<FloatArray> {
        val kernelMatrix = Array(kernelSize) { FloatArray(kernelSize) { 0.0f } }
        val factor = 1.0f / kernelSize.toFloat()

        when (direction) {
            MotionBlurDirection.HORIZONTAL -> {
                val midRow = kernelSize / 2
                for (x in 0 until kernelSize) {
                    kernelMatrix[midRow][x] = 1.0f
                }
            }
            MotionBlurDirection.VERTICAL -> {
                val midCol = kernelSize / 2
                for (y in 0 until kernelSize) {
                    kernelMatrix[y][midCol] = 1.0f
                }
            }
            MotionBlurDirection.DIAGONAL_TL_BR -> {
                for (i in 0 until kernelSize) {
                    kernelMatrix[i][i] = 1.0f
                }
            }
            MotionBlurDirection.DIAGONAL_TR_BL -> {
                for (i in 0 until kernelSize) {
                    kernelMatrix[i][kernelSize - 1 - i] = 1.0f
                }
            }
        }

        for (y in 0 until kernelSize) {
            for (x in 0 until kernelSize) {
                if (kernelMatrix[y][x] != 0.0f) {
                    kernelMatrix[y][x] *= factor
                }
            }
        }
        return kernelMatrix
    }

    override val kernel: Array<FloatArray> = generateMotionBlurKernel()
}
