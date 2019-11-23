package sample

import kotlin.test.Test
import kotlin.test.assertTrue

fun hello(): String = "Hello, Kotlin/Native!"

class SampleTests {
    @Test
    fun testHello() {
        assertTrue("Kotlin/Native" in hello())
    }
}
