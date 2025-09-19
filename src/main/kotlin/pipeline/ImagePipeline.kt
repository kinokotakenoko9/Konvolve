package pipeline

import image.Image
import kernels.Kernel
import java.nio.file.Path
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

class ImagePipeline(
    private val imagesDirInput: Path,
    private val imagesDirOutput: Path,
    readQueueSize: Int,
    writeQueueSize: Int,
    private val numReaderThreads: Int,
    private val numConvolutionThreads: Int,
    private val numWriterThreads: Int
) {
    private val readQueue: BlockingQueue<Image> = ArrayBlockingQueue(readQueueSize)
    private val writeQueue: BlockingQueue<Image> = ArrayBlockingQueue(writeQueueSize)

    private val readerPool = Executors.newFixedThreadPool(numReaderThreads)
    private val convolutionPool = Executors.newFixedThreadPool(numConvolutionThreads)
    private val writerPool = Executors.newFixedThreadPool(numWriterThreads)

    fun start(kernel: Kernel, label: String) {
        println("Starting the image processing pipeline...")

        // submit all reader tasks
        val imagesDirectoryName = imagesDirInput.toAbsolutePath().toString()
        val imagesToProcess = imagesDirInput.listDirectoryEntries().map { it.name }
        for (imageFilename in imagesToProcess) {
            readerPool.submit(ReaderTask(imagesDirectoryName, imageFilename, readQueue))
        }

        repeat(numConvolutionThreads) { convolutionPool.submit(ConvolutionTask(readQueue, writeQueue, kernel)) }
        repeat(numWriterThreads) { writerPool.submit(WriterTask(writeQueue, label, imagesDirOutput.toAbsolutePath().toString())) }

        readerPool.shutdown()
        readerPool.awaitTermination(5, TimeUnit.MINUTES) // wait for all images to be read

        // wait until all work is done
        while (readQueue.isNotEmpty() || writeQueue.isNotEmpty()) {
            Thread.sleep(100)
        }

        // stop other pools after the work is done
        convolutionPool.shutdownNow()
        writerPool.shutdownNow()

        println("Image pipeline has finished.")

    }

}