package benchmark

import kernels.*
import kotlinx.benchmark.*
import kotlinx.benchmark.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Scope
import parallel.*
import pipeline.ImagePipeline

@BenchmarkMode(Mode.SingleShotTime)
@Fork(0)
@State(Scope.Benchmark)
open class BenchmarkPipeline {
    private val numThreads = 5
    private var numReaderThreads: Int = 3
    private var numConvolutionThreads: Int = 4
    private var numWriterThreads: Int = 3

    @Param(
        "no parallel",
        "column",
        "row",
        "pixel",
        "grid 16",
        "grid 32",
        "grid 128",
    )
    private lateinit var modeName: String

    @Param(
        "Gaussian 9",
//        , "Gaussian 3",  "Motion Blur 9 Diagonal-tl-br", "Find Edges 5 All-directions"
    )
    private lateinit var kernelName: String

    private lateinit var pipeline: ImagePipeline
    private lateinit var kernel: Kernel
    private lateinit var mode: ParallelMode
    private val inputDir = "assets/images/input/dataset"
    private val outputDir = "assets/images/output/dataset"

    @Setup
    fun setup() {
        mode =
            when (modeName) {
                "no parallel" -> NoParallelMode()
                "column" -> ColumnParallelMode(numThreads)
                "row" -> RowParallelMode(numThreads)
                "pixel" -> PixelParallelMode(numThreads)
                "grid 16" -> GridParallelMode(numThreads, 16)
                "grid 32" -> GridParallelMode(numThreads, 32)
                "grid 128" -> GridParallelMode(numThreads, 128)
                else -> error("Unknown mode: $modeName")
            }

        kernel =
            when (kernelName) {
                "Gaussian 3" -> GaussianKernel(3)
                "Gaussian 9" -> GaussianKernel(9)
                "Motion Blur 9 Diagonal-tl-br" -> MotionBlurKernel(9, MotionBlurDirection.DIAGONAL_TL_BR)
                "Find Edges 5 All-directions" -> FindEdgesKernel(5, EdgeDirection.ALL_DIRECTIONS)
                else -> error("Unknown kernel: $kernelName")
            }
    }

    @Benchmark
    fun processImages() {
        pipeline =
            ImagePipeline(
                imagesDirInput = inputDir,
                imagesDirOutput = outputDir,
                readQueueSize = 20,
                writeQueueSize = 20,
                numReaderThreads = numReaderThreads,
                numConvolutionThreads = numConvolutionThreads,
                numWriterThreads = numWriterThreads,
            )
        val label = "benchmark-pipeline"
        pipeline.start(kernel, label, mode)
    }
}
