package com.hfad.synapflow.analytics

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

internal class FiguresTest {
    val plot = Figures()

    @Test
    fun dateTest() {
        val today = LocalDate.now()
        plot.createTestData()
        val x = plot.getDayCounts(today.toString())
        println("My Data: ${x}")
        // SHould read all dates for today
        assertEquals(x, 4f)
    }

    @Test
    fun badDateTest() {
        val today = LocalDate.now()
        plot.createTestData()
        // Get tomorrow, no data for tomorrow.
        val x = plot.getDayCounts(today.minusDays(-1).toString())

        // Should read all dates for tomorrow, but none.
        assertEquals(x, 0f)

    }

    @Test
    fun testBars() {

        plot.createTestData()

        val x = plot.getBarCount()
        var i = 4 // keeps track of x value
        println(plot.getTestData())
        for (z in x) {
            assertEquals(z.x, i.toFloat())
            assertEquals(z.y, 4f * 7)
            i--
        }

    }

    @Test
    fun testLines() {
        plot.createTestData()
        var i = 0
        val x = plot.getWeekLine()
        for (z in x) {
            assertEquals(z.x, i.toFloat())
            assertEquals(z.y, 4f)
            i++
        }
    }

    @Test
    fun testLines_3WeeksAway() {
        plot.createTestData()
        var i = 0
        val x = plot.getWeekLine(3)
        for (z in x) {
            assertEquals(z.x, i.toFloat())
            assertEquals(z.y, 4f)
            i++
        }
    }
}