package vn.trunglt.demoloadmoretwoway

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import vn.trunglt.demoloadmoretwoway.databinding.ActivityMainBinding
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    companion object {
        const val PAGE_LIMIT = 10
    }
    private lateinit var binding: ActivityMainBinding
    private val countryAdapter by lazy {
        CountryAdapter {
            callApi()
        }
    }
    private var page = 1
    private var isLoading = false

    private val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvCountry.adapter = countryAdapter
        binding.rvCountry.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val llm = binding.rvCountry.layoutManager as LinearLayoutManager
                val visibleItemsCount = llm.childCount
                val totalItems = llm.itemCount
                val first = llm.findFirstVisibleItemPosition()
                val last = llm.findLastVisibleItemPosition()
                if (!isLoading) {
                    if (dy > 0) {
                        if (totalItems - first > PAGE_LIMIT) {
                            countryAdapter.doCacheFirst()
                        }
                        countryAdapter.insertBelow()
                    } else if (dy < 0) {
                        if (totalItems - last > PAGE_LIMIT) {
                            countryAdapter.doCacheLast()
                        }
                        countryAdapter.insertAbove()
                    }
                    println("Adapter size: ${countryAdapter.mList.size}")
                }
            }
        })

        callApi()
    }

    private fun callApi() {
        Thread {
            try {
                isLoading = true
                val url = URL("https://api.first.org/data/v1/countries")
                val urlConnection = url.openConnection() as HttpURLConnection
                val bytes = urlConnection.inputStream.readBytes()
                urlConnection.disconnect()
                val data = Gson().fromJson<Map<String, Country>>(
                    JSONObject(String(bytes)).getString("data"),
                    object: TypeToken<Map<String, Country>>(){}.type
                )
                println(data.size)
                val pageData = data.values.toList().subList((page - 1) * PAGE_LIMIT, PAGE_LIMIT * page)
                if (pageData.isNotEmpty()) {
                    println(pageData)
                    mainHandler.post {
                        page++
                        countryAdapter.setData(pageData)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }.start()
    }
}