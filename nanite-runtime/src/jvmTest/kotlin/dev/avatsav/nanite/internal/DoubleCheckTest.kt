package dev.avatsav.nanite.internal

import kotlinx.atomicfu.atomic
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class DoubleCheckTest {
    private val invocationCount = atomic(0)

    @BeforeTest
    fun setup() {
        invocationCount.value = 0
    }

    @Test
    fun `returns the same instance on multiple invocations`() {
        val provider = Provider {
            invocationCount.incrementAndGet()
            Any()
        }

        val doubleCheck = DoubleCheck(provider)

        val instance1 = doubleCheck.invoke()
        val instance2 = doubleCheck.invoke()

        assertSame(instance1, instance2)
        assertEquals(1, invocationCount.value)
    }

    @Test
    fun `uses delegate to create the instance when called first time`() {
        var isDelegateCalled = false
        val provider = Provider {
            isDelegateCalled = true
            "testValue"
        }

        val doubleCheck = DoubleCheck(provider)

        val instance = doubleCheck.invoke()

        assertEquals("testValue", instance)
        assertTrue(isDelegateCalled)
    }

    @Test
    fun `handles concurrent access and returns the same instance`() {
        val provider = Provider {
            invocationCount.incrementAndGet()
            Any()
        }

        val doubleCheck = DoubleCheck(provider)

        val threads = List(10) {
            Thread {
                repeat(100) {
                    doubleCheck.invoke()
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        assertEquals(1, invocationCount.value)
    }
}
