package kernels

class IdentityKernel(
    private val kernelSize: Int,
) : Kernel() {
    init {
        require(kernelSize % 2 == 1) { "Kernel size must be odd." }
        require(kernelSize > 0) { "Kernel size must be positive." }
    }

    private fun generateIdentiKernel(size: Int): Array<FloatArray> {
        val kernel = Array(size) { FloatArray(size) }

        for (y in 0 until size) {
            for (x in 0 until size) {
                kernel[y][x] = if (x == y) 1f else 0f
            }
        }

        return kernel
    }

    override val kernel: Array<FloatArray> = generateIdentiKernel(kernelSize)
}
