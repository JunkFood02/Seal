package com.junkfood.seal

import com.junkfood.seal.util.connectWithDelimiter
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testTextJoin() {
        assertEquals(
            connectWithDelimiter("123", "456", "789", delimiter = ","),
            listOf(123, 456, 789).joinToString(separator = ",") { it.toString() },
        )
        assertEquals(connectWithDelimiter(delimiter = ","), "")
        assertEquals(emptyList<String>().joinToString(separator = ",") { it }, "")
    }
}
