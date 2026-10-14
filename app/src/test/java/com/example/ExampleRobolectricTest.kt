package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.network.YouTubeApiClient
import com.example.data.repository.SublyRepository
import com.example.data.repository.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Subly", appName)
    }

    @Test
    fun `test iso duration parser`() {
        assertEquals("16:42", YouTubeApiClient.parseIsoDuration("PT16M42S"))
        assertEquals("1:05:30", YouTubeApiClient.parseIsoDuration("PT1H5M30S"))
        assertEquals("0:45", YouTubeApiClient.parseIsoDuration("PT45S"))
        assertEquals(1002L, YouTubeApiClient.parseDurationToSeconds("PT16M42S"))
    }

    @Test
    fun `test api key edge case validations`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SublyRepository(context)

        // Empty key
        assertTrue(repository.validateApiKey("") is ValidationResult.Invalid)

        // Key with spaces
        assertTrue(repository.validateApiKey("AIzaSy 12345678901234567890") is ValidationResult.Invalid)

        // Key too short
        assertTrue(repository.validateApiKey("AIzaShort") is ValidationResult.Invalid)

        // Valid key
        assertTrue(repository.validateApiKey("AIzaSyB1234567890abcdefghijklmnopqrstuv") is ValidationResult.Valid)
    }

    @Test
    fun `test comment validation edge cases`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SublyRepository(context)

        // Empty comment
        assertTrue(repository.validateComment("   ") is ValidationResult.Invalid)

        // Too long comment (> 500 chars)
        val longText = "a".repeat(501)
        assertTrue(repository.validateComment(longText) is ValidationResult.Invalid)

        // Valid comment
        assertTrue(repository.validateComment("Great video breakdown!") is ValidationResult.Valid)
    }
}
