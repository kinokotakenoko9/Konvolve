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
            while (true) {
                val image = writeQueue.take()

                if (image === Image.POISON_PILL)
                    break

                println("Writer thread is saving image ${image.name}...")
                image.writeToFile(label, imageDirOut)
                println("Writer finished saving image ${image.name}.")
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            println("WriterTask was interrupted.")
        }
    }

}
