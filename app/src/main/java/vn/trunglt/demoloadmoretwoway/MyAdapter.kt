package vn.trunglt.demoloadmoretwoway

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.squareup.picasso.Picasso
import vn.trunglt.demoloadmoretwoway.databinding.ItemCountryBinding
import vn.trunglt.demoloadmoretwoway.models.User
import vn.trunglt.demoloadmoretwoway.utils.AppCacher
import java.util.Stack

class CountryAdapter(private val loadMoreApi: () -> Unit) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {
    val mList = mutableListOf<User>()
    val cacheFirst = Stack<List<User>>()
    val cacheLast = Stack<List<User>>()

    override fun onViewAttachedToWindow(holder: CountryViewHolder) {
        super.onViewAttachedToWindow(holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        return CountryViewHolder(
            ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        holder.bind(position)
    }

    fun setData(data: List<User>) {
        val preSize = mList.size
        mList.addAll(data)
        notifyItemRangeInserted(preSize, data.size)
    }
    fun insertBelow(data: List<User>? = null, callback: () -> Unit) {
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
        println("${cacheFirst.size} insertBelow cacheFirst: $cacheFirst")
        println("${cacheLast.size} insertBelow cacheLast: $cacheLast")
        callback()
    }

    fun insertAbove(callback: () -> Unit) {
        if (cacheFirst.isNotEmpty()) {
            cacheFirst.pop().also {
                mList.addAll(0, it)
                notifyItemRangeInserted(0, it.size)
            }
        }
        println("${cacheFirst.size} insertBelow cacheFirst: $cacheFirst")
        println("${cacheLast.size} insertBelow cacheLast: $cacheLast")
        callback()
    }

    fun doCacheLast() {
        mList.takeLast(MainActivity.PAGE_LIMIT).also {
            if (it.isNotEmpty()) {
                cacheLast.push(it)
            }
        }

        val preSize = mList.size
        for (i in 0 until MainActivity.PAGE_LIMIT) {
            mList.removeLast()
            notifyItemRemoved(mList.size)
        }
    }

    fun doCacheFirst() {
        val preSize = mList.size
        cacheFirst.push(mList.take(MainActivity.PAGE_LIMIT))
        for (i in 0 until MainActivity.PAGE_LIMIT) {
            mList.removeFirst()
            notifyItemRemoved(0)
        }
    }

    inner class CountryViewHolder(val binding: ItemCountryBinding) : ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.tvCountryItmName.text = mList[position].nodeId
            binding.tvCountryItmRegion.text = mList[position].nodeId
            AppCacher.load(
                url = mList[position].owner.avatarUrl,
                imageView = binding.ivCountry
            )
//            Picasso.get().load(mList[position].owner.avatarUrl).into(binding.ivCountry)
        }
    }
}