package pipeline

import image.Image
import kernels.Kernel
import parallel.ParallelMode
import java.io.File
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ImagePipeline(
    private val imagesDirInput: String,
    private val imagesDirOutput: String,
    readQueueSize: Int,
    writeQueueSize: Int,
    private val numReaderThreads: Int,
    private val numConvolutionThreads: Int,
    private val numWriterThreads: Int,
) {
    private val readQueue: BlockingQueue<Image> = ArrayBlockingQueue(readQueueSize)
    private val writeQueue: BlockingQueue<Image> = ArrayBlockingQueue(writeQueueSize)

    private val readerPool = Executors.newFixedThreadPool(numReaderThreads)
    private val convolutionPool = Executors.newFixedThreadPool(numConvolutionThreads)
    private val writerPool = Executors.newFixedThreadPool(numWriterThreads)

    fun start(
        kernel: Kernel,
        label: String,
        mode: ParallelMode,
    ) {
        println("Starting the image processing pipeline...")

        val imagesDirectoryName = imagesDirInput
        val imagesToProcess =
            File(imagesDirInput)
                .listFiles()
                ?.filter { it.isFile && it.name.endsWith(".bmp") }
                ?.map { it.name.removeSuffix(".bmp") }
                ?: emptyList()
        println(imagesToProcess)
        for (imageFilename in imagesToProcess) {
            readerPool.submit(ReaderTask(imagesDirectoryName, imageFilename, readQueue))
        }

        repeat(numConvolutionThreads) { convolutionPool.submit(ConvolutionTask(readQueue, writeQueue, kernel, mode)) }
        repeat(numWriterThreads) { writerPool.submit(WriterTask(writeQueue, label, imagesDirOutput)) }

        readerPool.shutdown()
        readerPool.awaitTermination(5, TimeUnit.MINUTES)

        repeat(numConvolutionThreads) {
            readQueue.put(Image.POISON_PILL)
        }

        convolutionPool.shutdown()
        convolutionPool.awaitTermination(5, TimeUnit.MINUTES)

        repeat(numWriterThreads) {
            writeQueue.put(Image.POISON_PILL)
        }

        writerPool.shutdown()
        writerPool.awaitTermination(5, TimeUnit.MINUTES)

        println("Image pipeline has finished.")
    }
}
