package com.example.conch

import com.example.conch.data.dto.IOProgress
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {


    @Test
    fun test() {
        val ioProgress = IOProgress(0, 342121340, 342121342)
        print(ioProgress.progress)
    }
}