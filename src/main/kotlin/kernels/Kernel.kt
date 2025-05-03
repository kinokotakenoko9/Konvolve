package org.example.kernels


interface Kernel {
    val kernel:Array<FloatArray>

    val size: Int
        get() = kernel.size

    fun getCoeff(x : Int, y : Int): Float {
        return kernel[y][x]
    }
}
