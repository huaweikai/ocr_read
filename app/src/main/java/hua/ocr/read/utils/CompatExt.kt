@file:Suppress("DEPRECATION")

package hua.ocr.read.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi

fun Context.getBitmapFromUri(uri: Uri): Bitmap? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        getBitmapFromUriP(uri)
    } else {
        MediaStore.Images.Media.getBitmap(contentResolver, uri)
    }
}

@RequiresApi(Build.VERSION_CODES.P)
private fun Context.getBitmapFromUriP(uri: Uri): Bitmap? {
    return try {
        val source = ImageDecoder.createSource(contentResolver, uri)
        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
        }

        if (bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            bitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
