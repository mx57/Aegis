package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LauncherIconDrawableTest {

    @Test
    fun launcherIconForeground_loadsAndRendersSuccessfully() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
        assertNotNull("ic_launcher_foreground drawable should not be null", drawable)

        drawable?.let { d ->
            val bitmap = Bitmap.createBitmap(108, 108, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            d.setBounds(0, 0, canvas.width, canvas.height)
            d.draw(canvas)

            assertTrue("Bitmap width should be 108", bitmap.width == 108)
            assertTrue("Bitmap height should be 108", bitmap.height == 108)
        }
    }

    @Test
    fun launcherIconBackground_loadsAndRendersSuccessfully() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)
        assertNotNull("ic_launcher_background drawable should not be null", drawable)

        drawable?.let { d ->
            val bitmap = Bitmap.createBitmap(108, 108, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            d.setBounds(0, 0, canvas.width, canvas.height)
            d.draw(canvas)

            assertTrue("Bitmap width should be 108", bitmap.width == 108)
            assertTrue("Bitmap height should be 108", bitmap.height == 108)
        }
    }
}
