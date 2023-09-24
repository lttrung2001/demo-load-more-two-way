package vn.trunglt.demoloadmoretwoway.utils

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object NetworkManager {
    private val mainHandler = Handler(Looper.getMainLooper())
    @Throws(Exception::class)
    fun<T> doRequest(
        url: String,
        onPrepare: () -> Unit,
        onSuccess: (T) -> Unit,
        onError: (e: Exception) -> Unit
    ) {
        onPrepare()
        Thread {
            try {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                val bytes = urlConnection.inputStream.readBytes()
                urlConnection.disconnect()
                val dataConverted = Gson().fromJson<T>(
                    JSONObject(String(bytes)).toString(),
                    object: TypeToken<T>(){}.type
                )
                mainHandler.post {
                    onSuccess(dataConverted)
                }
            } catch (e: Exception) {
                mainHandler.post {
                    onError(e)
                }
            }
        }.start()
    }
}