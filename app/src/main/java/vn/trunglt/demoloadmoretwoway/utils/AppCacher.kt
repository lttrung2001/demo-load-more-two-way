package vn.trunglt.demoloadmoretwoway.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import java.net.HttpURLConnection
import java.net.URL

object AppCacher {
    val mainHandler = Handler(Looper.getMainLooper())
    val c = object : LruCache<String, Bitmap>(4096) {
        override fun entryRemoved(
            evicted: Boolean,
            key: String?,
            oldValue: Bitmap?,
            newValue: Bitmap?
        ) {
            super.entryRemoved(evicted, key, oldValue, newValue)
            moveToDiskCache(key, oldValue)
        }
    }

    fun moveToDiskCache(key: String?, oldValue: Bitmap?) {
        // Put to disk cache
    }

    fun load(url: String, imageView: ImageView) {
        var bitmap: Bitmap? = c.get(url)
        if (bitmap == null) {
            println("NULL")
            Thread {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                val bytes = urlConnection.inputStream.readBytes()
                urlConnection.disconnect()
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options())
                c.put(url, bitmap)
                mainHandler.post {
                    imageView.setImageBitmap(bitmap)
                }
            }.start()
        } else {
            println("NOT NULL")
            imageView.setImageBitmap(bitmap)
            mainHandler.removeCallbacks()
        }
    }
}