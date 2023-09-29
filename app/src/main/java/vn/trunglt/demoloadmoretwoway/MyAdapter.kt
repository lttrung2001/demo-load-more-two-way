package vn.trunglt.demoloadmoretwoway

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import vn.trunglt.demoloadmoretwoway.databinding.ItemCountryBinding
import java.util.Stack

class CountryAdapter(private val loadMoreApi: () -> Unit) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {
    val mList = mutableListOf<Country>()
    val cacheFirst = Stack<List<Country>>()
    val cacheLast = Stack<List<Country>>()

    var isLoading = false

    override fun onViewDetachedFromWindow(holder: CountryViewHolder) {
        holder.reset()
        super.onViewDetachedFromWindow(holder)
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
        println("${cacheFirst.size} insertBelow cacheFirst: $cacheFirst")
        println("${cacheLast.size} insertBelow cacheLast: $cacheLast")
    }

    fun insertAbove() {
        if (cacheFirst.isNotEmpty()) {
            cacheFirst.pop().also {
                mList.addAll(0, it)
                notifyItemRangeInserted(0, it.size)
            }
        }
        println("${cacheFirst.size} insertBelow cacheFirst: $cacheFirst")
        println("${cacheLast.size} insertBelow cacheLast: $cacheLast")
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
        var downX = 0F
        var dataLocation = IntArray(2)
        var editLocation = IntArray(2)
        var removeLocation = IntArray(2)
        var isReadDefaultLocation = false

        @SuppressLint("ClickableViewAccessibility")
        fun bind(position: Int) {
            binding.tvCountryItmName.text = mList[position].country
            binding.tvCountryItmRegion.text = mList[position].region
            binding.tv1.setOnClickListener {
                println("Button 1 clicked")
            }
            binding.tv2.setOnClickListener {
                println("Button 2 clicked")
            }
            binding.cardView.setOnTouchListener { view, motionEvent ->
                if (!isReadDefaultLocation) {
                    binding.linearLayout.getLocationOnScreen(dataLocation)
                    binding.tv1.getLocationOnScreen(editLocation)
                    binding.tv2.getLocationOnScreen(removeLocation)
                    isReadDefaultLocation = true
                }

                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = motionEvent.x
                    }
                    MotionEvent.ACTION_MOVE -> {
                        println("default: ${dataLocation[0]}")
                        println("event: ${motionEvent.x}")

                        val deltaX = downX - motionEvent.x
                        if (deltaX < 200F && deltaX  > -200F) {
                            onSwiping(deltaX)
                        } else if (deltaX >= 200F) {
                            handleSwipeLeft()
//                            binding.linearLayout.x = dataLocation[0] - 800F
//                            binding.tv1.x = editLocation[0] - 800F
//                            binding.tv2.x = removeLocation[0] - 800F
                        } else {
                            reset()
//                            binding.linearLayout.x = dataLocation[0].toFloat()
//                            binding.tv1.x = editLocation[0].toFloat()
//                            binding.tv2.x = removeLocation[0].toFloat()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val deltaX = downX - motionEvent.x
                        if (deltaX > 200F) {
                            handleSwipeLeft()
                        } else {
                            reset()
//                            binding.linearLayout.x = dataLocation[0].toFloat()
//                            binding.tv1.x = editLocation[0].toFloat()
//                            binding.tv2.x = removeLocation[0].toFloat()
                        }
                    }
                }
                return@setOnTouchListener true
            }
        }

        fun handleSwipeLeft() {
            binding.apply {
                linearLayout.animate()
                    .translationXBy(-600F)
                    .start()
                tv1.animate()
                    .translationXBy(-600F)
                    .start()
                tv2.animate()
                    .translationXBy(-600F)
                    .start()
            }
        }

        fun onSwiping(deltaX: Float) {
            binding.linearLayout.x = dataLocation[0] - deltaX
            binding.tv1.x = editLocation[0] - deltaX
            binding.tv2.x = removeLocation[0] - deltaX
        }

        fun reset() {
            binding.apply {
                linearLayout.animate()
                    .translationXBy(dataLocation[0] - linearLayout.x)
                    .start()
                tv1.animate()
                    .translationXBy(editLocation[0] - tv1.x)
                    .start()
                tv2.animate()
                    .translationXBy(removeLocation[0] - tv2.x)
                    .start()
            }
        }
    }
}