package org.example.benchmark

import kotlinx.benchmark.*
import kotlinx.benchmark.Benchmark
import org.example.image.Image
import org.example.kernels.GaussianKernel
import org.example.kernels.Kernel
import org.example.parallel.*
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Scope

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
    )
    private lateinit var modeName: String

    @Param(
        "Gaussian 5"
        , "Gaussian 15"
    )
    private lateinit var kernelName: String

    @Param(
        "flower"
        , "cat"
        , "monkey"
    )
    private lateinit var imageName: String

    @Setup
    fun prepare() {
        img = Image(imageName)

        val mode = when (modeName) {
            "no parallel" -> NoParallelMode()
            "column" -> ColumnParallelMode(numThreads)
            "row" -> RowParallelMode(numThreads)
            "pixel" -> PixelParallelMode(numThreads)
            "grid 32" -> GridParallelMode(numThreads, 32)
            else -> error("Unknown mode: $modeName")
        }

        kernel = when (kernelName) {
            "Gaussian 5" -> GaussianKernel(5)
            "Gaussian 15" -> GaussianKernel(15)
            else -> error("Unknown kernel: $kernelName")
        }

        img.setParallelMode(mode)
    }

    @Benchmark
    fun convolve() {
        img.applyKernel(kernel)
    }
}
