package vn.trunglt.demoloadmoretwoway.utils

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import vn.trunglt.demoloadmoretwoway.models.User
import vn.trunglt.demoloadmoretwoway.responses.GetListUserResponse
import java.net.HttpURLConnection
import java.net.URL

object NetworkManager {
    val mainHandler = Handler(Looper.getMainLooper())
    @Throws(Exception::class)
    fun doRequest(
        url: String,
        onPrepare: () -> Unit,
        onSuccess: (GetListUserResponse) -> Unit,
        onError: (e: Exception) -> Unit
    ) {
        onPrepare()
        Thread {
            try {
                val urlConnection = URL(url).openConnection() as HttpURLConnection
                val bytes = urlConnection.inputStream.readBytes()
                urlConnection.disconnect()
                val dataConverted = Gson().fromJson(String(bytes), GetListUserResponse::class.java)
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