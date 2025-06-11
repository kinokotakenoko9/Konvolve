package org.example

import org.example.benchmark.Benchmark
import org.example.image.Image

fun main() {

    val imgName = "flower"
    val im = Image(imgName)
    println("start")
    val b = Benchmark(im)
    b.getStatistic()
}
