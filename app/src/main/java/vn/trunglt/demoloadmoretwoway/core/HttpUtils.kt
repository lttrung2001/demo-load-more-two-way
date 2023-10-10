package vn.trunglt.demoloadmoretwoway.core

import android.accounts.NetworkErrorException
import android.os.Build
import android.util.Log
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate


class HttpUtils {

    companion object {

        private const val CONNECT_TIMEOUT = 5 * 1000

        private const val READ_TIMEOUT = 10 * 1000

        private lateinit var instance: HttpUtils

        fun getInstance(): HttpUtils {
            synchronized(this) {
                if (!::instance.isInitialized) {
                    instance = HttpUtils()
                }
                return instance
            }
        }
    }

    fun createConnection(url: String?): HttpURLConnection {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        urlConnection.connectTimeout = CONNECT_TIMEOUT
        urlConnection.readTimeout = READ_TIMEOUT
        return urlConnection
    }

    fun getInputStream(url: String?): InputStream? {
        var inputStream: InputStream? = null
        try {
            val conn = createConnection(url)
            conn.requestMethod = "GET"
            inputStream = conn.inputStream
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return inputStream
    }


    fun get(url: String, apiResult: (ApiResult) -> Unit) {
        AppExecutors.getInstance().apply {
            apiResult.invoke(ApiResult.Loading(isLoading = true))
            var connection: HttpURLConnection? = null
            val buffer = StringBuffer()
            networkIO().execute {
                try {
                    connection = createConnection(url)
                    connection?.requestMethod = "GET"
                    val inputStream = connection?.inputStream
                    val bf = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                    var line: String? = ""
                    while (bf.readLine().also { line = it } != null) {
                        buffer.append(line)
                    }
                    bf.close()
                    inputStream?.close()
                    mainThread().execute {
                        apiResult.invoke(ApiResult.Successful(JSONObject(buffer.toString())))
                    }
                } catch (exception: MalformedURLException) {
                    mainThread().execute {
                        apiResult.invoke(ApiResult.Error(exception.message.toString()))
                    }
                } catch (exception: IOException) {
                    mainThread().execute {
                        apiResult.invoke(ApiResult.Error(exception.message.toString()))
                    }
                } finally {
                    mainThread().execute {
                        apiResult.invoke(ApiResult.Loading(isLoading = false))
                    }
                    connection?.disconnect()
                }
            }
        }
    }

//    fun getString(url: String?): String? {
//        var result: String? = null
//        var `is`: InputStream? = null
//        var br: BufferedReader? = null
//        try {
//            `is` = getInputStream(url)
//            br = BufferedReader(InputStreamReader(`is`, CHARSET))
//            var line: String? = null
//            val sb = StringBuffer()
//            while (br.readLine().also { line = it } != null) {
//                sb.append(line)
//            }
//            result = sb.toString()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } finally {
//            try {
//                br?.close()
//            } catch (e: IOException) {
//            }
//            try {
//                `is`?.close()
//            } catch (e: IOException) {
//            }
//        }
//        return result
//    }
//
//    fun postString(url: String?, params: String?): String? {
//        var result: String? = null
//        var os: OutputStream? = null
//        var `is`: InputStream? = null
//        var br: BufferedReader? = null
//        try {
//            val conn = createConnection(url)
//            conn.requestMethod = "POST"
//            conn.doOutput = true
//            conn.doInput = true
//            conn.useCaches = false
//            // conn.setRequestProperty(field, newValue);//header
//            conn.setRequestProperty("Content-Type", "application/json; charset=" + CHARSET)
//            // conn.setRequestProperty("Connection", "keep-alive");
//            // conn.setRequestProperty("User-Agent",
//            // "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0");
//            if (params != null) {
//                os = conn.outputStream
//                val dos = DataOutputStream(os)
//                dos.write(params.toByteArray(charset(CHARSET)))
//                dos.flush()
//                dos.close()
//            }
//            `is` = conn.inputStream
//            br = BufferedReader(InputStreamReader(`is`, CHARSET))
//            var line: String? = null
//            val sb = StringBuffer()
//            while (br.readLine().also { line = it } != null) {
//                sb.append(line)
//            }
//            result = sb.toString()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        } finally {
//            try {
//                os?.close()
//            } catch (e: IOException) {
//            }
//            try {
//                br?.close()
//            } catch (e: IOException) {
//            }
//            try {
//                `is`?.close()
//            } catch (e: IOException) {
//            }
//        }
//        return result
//    }

    @Throws(IOException::class)
    protected fun shouldBeProcessed(conn: HttpURLConnection): Boolean {
        return conn.responseCode == 200
    }

    protected fun disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false")
        }
    }

    private fun setupSSl() {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate>? {
                return null
            }

            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
        })
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(NullHostNameVerifier())
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection
                .setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    private inner class NullHostNameVerifier : HostnameVerifier {
        override fun verify(hostname: String, session: SSLSession): Boolean {
            Log.i("RestUtilImpl", "Approving certificate for $hostname")
            return true
        }
    }
}

sealed class ApiResult {
    data class Loading(val isLoading: Boolean) : ApiResult()
    data class Successful(val data: JSONObject) : ApiResult()
    data class Error(val error: String) : ApiResult()
}
