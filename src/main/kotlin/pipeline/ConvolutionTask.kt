package pipeline

import image.Image
import kernels.Kernel
import parallel.ParallelMode
import java.util.concurrent.BlockingQueue

class ConvolutionTask(
    private val readQueue: BlockingQueue<Image>,
    private val writeQueue: BlockingQueue<Image>,
    private val kernel: Kernel,
    private val mode: ParallelMode,
) : Runnable {
    override fun run() {
        try {
            while (true) {
                val image = readQueue.take()

                if (image === Image.POISON_PILL) {
                    break
                }

                println("Convolution thread is processing image ${image.name}...")
                image
                    .setParallelMode(mode)
                    .applyKernel(kernel)
                writeQueue.put(image)
                println("Convolution placed image ${image.name} in write queue.")
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            println("ConvolutionTask was interrupted.")
        }
    }
}
