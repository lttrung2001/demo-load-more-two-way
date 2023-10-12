package vn.trunglt.demoloadmoretwoway.core.file_json

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader


val gson: Gson by lazy {
    Gson()
}

inline fun <reified T : Any> Gson.fromJson(json: String): T = this.fromJson(json, T::class.java)

fun Any.toJson(): String = gson.toJson(this)

inline fun <reified T> String.fromJson(): T {
    return gson.fromJson(this, T::class.java)
}

inline fun <reified T> String.fromJsonTypeToken(): T {
    return gson.fromJson(this, object : TypeToken<T>() {}.type)
}

fun <T> T.toFileExternal(fileName: String, context: Context) {
    val externalDir = context.getExternalFilesDir(null)
    val file = File(externalDir, fileName)
    val jsonString = this?.toJson()
    val fileWriter = FileWriter(file)
    fileWriter.write(jsonString)
    fileWriter.flush()
    fileWriter.close()
}

// get data class from file and delete file
inline fun <reified T> Class<T>.fromFile(fileName: String, context: Context): T? {
    val externalDir = context.getExternalFilesDir(null)
    val file = File(externalDir, fileName)
    val fileReader = file.bufferedReader()
    val jsonString = fileReader.use { it.readText() }
    fileReader.close()
    file.delete()
    return jsonString.fromJson()
}


inline fun <reified T> readDataFromAsset(fileName: String,context: Context): T? {
    val gson = Gson()
    var myData: T? = null
    try {
        val inputStream = context.assets.open(fileName)
        val reader = InputStreamReader(inputStream)
        myData = gson.fromJson(reader, T::class.java)
        reader.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return myData
}
