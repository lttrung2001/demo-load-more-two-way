package vn.trunglt.demoloadmoretwoway

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import vn.trunglt.demoloadmoretwoway.databinding.ItemCountryBinding
import java.util.Stack

class CountryAdapter(private val loadMoreApi: () -> Unit) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {
    val mList = mutableListOf<Country>()
    val cacheFirst = Stack<List<Country>>()
    val cacheLast = Stack<List<Country>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        return CountryViewHolder(
            ItemCountryBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(position)
    }

    fun setData(data: List<Country>) {
        val preSize = mList.size
        mList.addAll(data)
        notifyItemRangeInserted(preSize, data.size)
    }
    fun insertBelow(data: List<Country>? = null) {
        val preSize = mList.size
        if (cacheLast.isNotEmpty()) {
            cacheLast.pop().also {
                mList.addAll(it)
                notifyItemRangeInserted(preSize, it.size)
            }
        } else {
            if (data == null) {
                loadMoreApi()
            } else {
                mList.addAll(data)
                notifyItemRangeInserted(preSize, data.size)
            }
        }
        println("insertBelow cacheFirst: $cacheFirst")
        println("insertBelow cacheLast: $cacheLast")
    }

    fun insertAbove() {
        if (cacheFirst.isNotEmpty()) {
            cacheFirst.pop().also {
                mList.addAll(0, it)
                notifyItemRangeInserted(0, it.size)
            }
        }
        println("insertBelow cacheFirst: $cacheFirst")
        println("insertBelow cacheLast: $cacheLast")
    }

    fun doCacheLast() {
        val min = minOf(MainActivity.PAGE_LIMIT, itemCount % MainActivity.PAGE_LIMIT)

        mList.takeLast(min).also {
            if (it.isNotEmpty())
                cacheLast.push(it)
        }

        val preSize = mList.size
        for (i in 0 until min) {
            mList.removeLast()
        }
        notifyItemRangeRemoved(preSize, min)
    }

    fun doCacheFirst() {
        val preSize = mList.size
        cacheFirst.push(mList.take(MainActivity.PAGE_LIMIT))
        for (i in 0 until MainActivity.PAGE_LIMIT) {
            mList.removeFirst()
        }
        notifyItemRangeRemoved(preSize, MainActivity.PAGE_LIMIT)
    }

    inner class CountryViewHolder(val binding: ItemCountryBinding) : ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.tvCountryItmName.text = mList[position].country
            binding.tvCountryItmRegion.text = mList[position].region + " $position"
        }
    }
}