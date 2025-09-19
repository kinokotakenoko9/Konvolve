package pipeline

import image.Image
import kernels.Kernel
import parallel.ParallelMode
import java.util.concurrent.BlockingQueue

class ConvolutionTask(
    private val readQueue: BlockingQueue<Image>,
    private val writeQueue: BlockingQueue<Image>,
    private val kernel: Kernel,
    private val mode: ParallelMode
    ) : Runnable {
    override fun run() {
        try {
            while (!Thread.currentThread().isInterrupted) {
                val rawImage = readQueue.take()
                println("Convolution thread is processing image ${rawImage.name}...")
                rawImage
                    .setParallelMode(mode)
                    .applyKernel(kernel)
                writeQueue.put(rawImage)
                println("Convolution placed image ${rawImage.name} in write queue.")
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}