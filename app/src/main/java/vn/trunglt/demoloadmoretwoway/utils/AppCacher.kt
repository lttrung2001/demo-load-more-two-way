package vn.trunglt.demoloadmoretwoway.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import okhttp3.internal.cache.DiskLruCache
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest


@SuppressLint("StaticFieldLeak")
object AppCacher {
    private const val DELTA_TIME = 60 * 60 * 2 * 1000
    private var mContext: Context? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private val memoryCache = object : LruCache<String, Bitmap>(30) {
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
        if (key != null && oldValue != null) {
            try {
                File(mContext?.cacheDir, key.md5()).writeBytes(oldValue.getBytes())
            } catch (e: Exception) {
                println(e.message)
                e.printStackTrace()
            }
        }
    }

    fun ImageView.load(url: String) {
        mContext = context
        var bitmap: Bitmap?
        Thread {
            try {
                bitmap = kotlin.run {
                    memoryCache.get(url).also {
                        println("GET FROM MEMORY CACHE")
                    }
                } ?: kotlin.run {
                    val file = File(mContext?.cacheDir, url.md5())
                    if (file.exists()) {
//                        println("lastModified: ${file.lastModified()} current: ${System.currentTimeMillis()}")
                        if (file.lastModified() + DELTA_TIME < System.currentTimeMillis()) {
                            file.delete()
                            null
                        } else {
                            println("GET FROM DISK CACHE")
                            file.readBytes().toBitmap()
                        }
                    } else {
                        null
                    }
                }
                if (bitmap == null) {
                    println("CALL NETWORK")
                    val urlConnection = URL(url).openConnection() as HttpURLConnection
                    val bytes = urlConnection.inputStream.readBytes()
                    urlConnection.disconnect()
                    bitmap = bytes.toBitmap()
                    memoryCache.put(url, bitmap)
                    mainHandler.post {
                        setImageBitmap(bitmap)
                    }
                } else {
                    mainHandler.post {
                        setImageBitmap(bitmap)
                    }
                }
            } catch (e: Exception) {
                println(e.message)
                e.printStackTrace()
            }
        }.start()
    }

    private fun Bitmap.getBytes(): ByteArray {
        val bos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, bos)
        return bos.toByteArray()
    }

    private fun ByteArray.toBitmap(): Bitmap {
        return BitmapFactory.decodeByteArray(this, 0, size)
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }
}