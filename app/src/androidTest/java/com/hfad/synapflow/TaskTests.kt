package com.hfad.synapflow

import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

class TaskTests {

    private lateinit var task: Task
    private lateinit var taskWithUid: Task

    @Before
    fun setUp() {
        val name = "Task 1"
        val description = "This is a test task."
        val category = "Work"
        val startTimeStamp = Timestamp.now()
        val priority = 3L
        val completed = false
        val uid = "test-uid-123"

        task = Task(name, description, category, startTimeStamp, priority, completed)
        taskWithUid = Task(name, description, category, startTimeStamp, priority, completed, uid)
    }

    @Test
    fun testName() {
        assertEquals("Task 1", task.name)
        assertEquals("Task 1", taskWithUid.name)
    }

    @Test
    fun testDescription() {
        assertEquals("This is a test task.", task.description)
        assertEquals("This is a test task.", taskWithUid.description)
    }

    @Test
    fun testCategory() {
        assertEquals("Work", task.category)
        assertEquals("Work", taskWithUid.category)
    }

    @Test
    fun testStartTimeStamp() {
        assertEquals(Timestamp.now().seconds, task.startTimeStamp.seconds)
        assertEquals(Timestamp.now().seconds, taskWithUid.startTimeStamp.seconds)
    }

    @Test
    fun testPriority() {
        assertEquals(3L, task.priority)
        assertEquals(3L, taskWithUid.priority)
    }

    @Test
    fun testCompleted() {
        assertFalse(task.completed)
        assertFalse(taskWithUid.completed)
    }

    @Test
    fun testUid() {
        assertEquals("", task.getUID())
        assertEquals("test-uid-123", taskWithUid.getUID())
    }
}