package benchmark

import image.Image
import kernels.*
import kotlinx.benchmark.*
import kotlinx.benchmark.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Scope
import parallel.*

@BenchmarkMode(Mode.SingleShotTime)
@Fork(0)
@State(Scope.Benchmark)
open class Benchmark {
    private val numThreads = 4
    private lateinit var img: Image
    private lateinit var kernel: Kernel

    @Param(
        "no parallel"
        , "column"
        , "row"
        , "pixel"
        , "grid 32"
        , "grid 8"
    )
    private lateinit var modeName: String

    @Param(
        "Gaussian 5"
//        , "Gaussian 15"
//        , "Motion Blur 9 Diagonal-tl-br"
//        , "Find Edges 5 All-directions"
        , "Identity 3"
    )
    private lateinit var kernelName: String

    @Param(
        "flower"
//        , "cat"
//        , "city"
    )
    private lateinit var imageName: String

    @Setup
    fun prepare() {
        img = Image("assets/images/input", imageName)

        val mode = when (modeName) {
            "no parallel" -> NoParallelMode()
            "column" -> ColumnParallelMode(numThreads)
            "row" -> RowParallelMode(numThreads)
            "pixel" -> PixelParallelMode(numThreads)
            "grid 32" -> GridParallelMode(numThreads, 32)
            "grid 8" -> GridParallelMode(numThreads, 8)
            else -> error("Unknown mode: $modeName")
        }

        kernel = when (kernelName) {
            "Gaussian 5" -> GaussianKernel(5)
            "Gaussian 15" -> GaussianKernel(15)
            "Motion Blur 9 Diagonal-tl-br" -> MotionBlurKernel(9, MotionBlurDirection.DIAGONAL_TL_BR)
            "Find Edges 5 All-directions" -> FindEdgesKernel(5, EdgeDirection.ALL_DIRECTIONS)
            "Identity 3" -> IdentityKernel(3)
            else -> error("Unknown kernel: $kernelName")
        }

        img.setParallelMode(mode)
    }

    @Benchmark
    fun convolve() {
        img.applyKernel(kernel)
    }
}
