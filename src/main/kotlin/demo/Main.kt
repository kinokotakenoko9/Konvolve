package demo

import image.Image
import kernels.GaussianKernel
import parallel.GridParallelMode
import pipeline.ImagePipeline
import kotlin.time.measureTime

fun main() {
    demo2()
}

fun demo1() {
    val img =
        Image("assets/images/input", "flower")
            .setParallelMode(GridParallelMode(4, 32))
    val gk5 = GaussianKernel(5)
    val d =
        measureTime {
            img.applyKernel(gk5)
        }
    img.writeToFile("gs5-grid32", "assets/images/output")

    println(d)
}

fun demo2() {
    println("Starting Image Pipeline Demo...")

    val inputDir = "assets/images/input/dataset"
    val outputDir = "assets/images/output/dataset"

    val readQueueSize = 10
    val writeQueueSize = 10
    val numReaderThreads = 2
    val numConvolutionThreads = 4
    val numWriterThreads = 2

    val pipeline =
        ImagePipeline(
            imagesDirInput = inputDir,
            imagesDirOutput = outputDir,
            readQueueSize = readQueueSize,
            writeQueueSize = writeQueueSize,
            numReaderThreads = numReaderThreads,
            numConvolutionThreads = numConvolutionThreads,
            numWriterThreads = numWriterThreads,
        )

    val kernel = GaussianKernel(5)
    val label = "gaussian-5-pipeline"

    val d =
        measureTime {
            pipeline.start(kernel, label, GridParallelMode(threadNumber = 4, blockSize = 32))
        }

    println(d)
}
