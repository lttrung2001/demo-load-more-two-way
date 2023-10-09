package vn.trunglt.demoloadmoretwoway.utils

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import vn.trunglt.demoloadmoretwoway.models.User
import java.net.HttpURLConnection
import java.net.URL

object NetworkManager {
    val mainHandler = Handler(Looper.getMainLooper())
    @Throws(Exception::class)
    fun doRequest(
        url: String,
        onPrepare: () -> Unit,
        onSuccess: (List<User>) -> Unit,
        onError: (e: Exception) -> Unit
    ) {
        onPrepare()
        Thread {
            try {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                val bytes = urlConnection.inputStream.readBytes()
                urlConnection.disconnect()
                val typeToken = object : TypeToken<List<User>>() {}.type
                val dataConverted = Gson().fromJson<List<User>>(String(bytes), typeToken)
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