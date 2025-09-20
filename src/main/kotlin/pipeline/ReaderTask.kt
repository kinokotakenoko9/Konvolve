package pipeline

import image.Image
import java.util.concurrent.BlockingQueue

class ReaderTask(
    private val dirName: String,
    private val filename: String,
    private val readQueue: BlockingQueue<Image>,
) : Runnable {

    override fun run() {
        try {
            println("Reader thread is reading image $filename...")
            val image = Image(dirName, filename)
            readQueue.put(image)
            println("Reader placed image $filename in queue.")
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            print("ReaderTask was interrupted.")
        }
    }
}