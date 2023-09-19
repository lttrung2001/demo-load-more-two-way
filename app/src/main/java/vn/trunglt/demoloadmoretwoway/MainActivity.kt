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
    // Bien nay tuong trung cho dialog loading
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
                val totalItems = llm.itemCount
                val childCount = llm.childCount
                val first = llm.findFirstCompletelyVisibleItemPosition()
                val last = llm.findLastCompletelyVisibleItemPosition()
                if (!countryAdapter.isLoading && !isLoading) {
                    countryAdapter.isLoading = true
                    if (
                        // dy > 0 scroll xuong
                        dy > 0 &&
                        // check co can load san trang sau khong
                        last + childCount > totalItems) {
                        // neu index cua trang hien tai > 1 thi cache n item dau tien mList
                        if (first / PAGE_LIMIT > 1) {
                            countryAdapter.doCacheFirst()
                        }
                        countryAdapter.insertBelow()
                    } else if (
                        // dy < 0 scroll len
                        dy < 0 &&
                        // check co can load san trang truoc khong
                        first - childCount < 0) {
                        // neu delta cua trang hien tai voi tong so trang > 1 thi cache n item cuoi cung mList
                        if ((totalItems / PAGE_LIMIT) - (last / PAGE_LIMIT) > 1) {
                            countryAdapter.doCacheLast()
                        }
                        countryAdapter.insertAbove()
                    }
                    println("Adapter size: ${countryAdapter.mList.size} first: $first last: $last")
                    countryAdapter.isLoading = false
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
                var i = 0
                val pageData = data.values.toList().map {
                    it.apply { region += i++ }
                }.subList((page - 1) * PAGE_LIMIT, PAGE_LIMIT * page)
                if (pageData.isNotEmpty()) {
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