package org.lida.launcher.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap

fun drawableToBitmap(drawable: Drawable, width: Int = 64, height: Int = 64): Bitmap {
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}