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
import vn.trunglt.demoloadmoretwoway.models.User
import vn.trunglt.demoloadmoretwoway.responses.GetListUserResponse
import vn.trunglt.demoloadmoretwoway.utils.NetworkManager
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
                val canScrollDown = dy > 0
                val canScrollUp = dy < 0
                if (!isLoading) {
                    isLoading = true
                    println("Adapter size: ${countryAdapter.mList.size} first: $first last: $last")
                    if (canScrollDown && last + childCount > totalItems) {
                        val pageDistanceTop = first / PAGE_LIMIT
                        if (pageDistanceTop > 1) {
                            countryAdapter.doCacheFirst()
                        }
                        countryAdapter.insertBelow {
                            isLoading = false
                        }
                    } else if (canScrollUp && first - childCount < 0) {
                        val pageDistanceBottom = (totalItems / PAGE_LIMIT) - (last / PAGE_LIMIT)
                        if (pageDistanceBottom > 1) {
                            countryAdapter.doCacheLast()
                        }
                        countryAdapter.insertAbove {
                            isLoading = false
                        }
                    }
                }
            }
        })
        binding.root.setOnClickListener {
            callApi()
        }

    }

    private fun callApi() {
        NetworkManager.doRequest(
            url = "https://api.github.com/search/users?q=a&page=1&per_page=100",
            onPrepare = {
                isLoading = true
            },
            onSuccess = { data ->
                countryAdapter.setData(data.items )
                isLoading = false
            },
            onError = {
                it.printStackTrace()
                isLoading = false
            }
        )
    }
}