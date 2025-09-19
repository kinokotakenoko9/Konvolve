package pipeline

import image.Image
import java.util.concurrent.BlockingQueue

class WriterTask(
    private val writeQueue: BlockingQueue<Image>,
    private val label: String,
    private val imageDirOut: String
) : Runnable {
    override fun run() {
        try {
            while (!Thread.currentThread().isInterrupted) {
                val processedImage = writeQueue.take()
                println("Writer thread is saving image ${processedImage.name}...")
                processedImage.writeToFile(label, imageDirOut)
                println("Writer finished saving image ${processedImage.name}.")
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }

}
