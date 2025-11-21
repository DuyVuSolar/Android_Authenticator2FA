package com.beemdevelopment.aegis.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.beemdevelopment.aegis.BR
import com.beemdevelopment.aegis.model.AppAudio
import com.beemdevelopment.aegis.utils.player.AppPlayer

class RecordingsAdapter(
        @LayoutRes private val resLayoutChild: Int
) : ListAdapter<AppAudio, RecyclerView.ViewHolder>(AppAudioDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CHILD = 1
    }

    private val originalCategories = arrayListOf<AppAudio>()
    var oldList = listOf<AppAudio>()
    fun submitCategories(categories: List<AppAudio>) {
        originalCategories.clear()
        originalCategories.addAll(categories.map { it.copy() }) // Store copies to manage expansion state independently
        oldList = buildDisplayList()

        submitList(oldList)
    }

    fun buildDisplayList(): List<AppAudio> {
        val displayList = arrayListOf<AppAudio>()
        originalCategories.forEach { category ->
            displayList.add(category.clone())
        }
        return displayList
    }

    var viewmodel: ViewModel? = null
    var liveState = MutableLiveData(AppPlayer.State.NOT_READY)
    var isPlayReverse = MutableLiveData(false)
    var pathPlaying = MutableLiveData("")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, resLayoutChild, parent, false)
        return ChildViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChildViewHolder){
            val item = oldList[position]
            holder.binding.setVariable(BR.item, item)
            if(viewmodel != null) holder.binding.setVariable(BR.viewmodel, viewmodel)
            holder.binding.setVariable(BR.liveStatePlayer, liveState)
            holder.binding.setVariable(BR.isPlayReverse, isPlayReverse)
            holder.binding.setVariable(BR.pathPlaying, pathPlaying)
            holder.binding.executePendingBindings()
        }
    }

    class ChildViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

}


// DiffUtil.ItemCallback for efficient list updates
class AppAudioDiffCallback : DiffUtil.ItemCallback<AppAudio>() {
    override fun areItemsTheSame(oldItem: AppAudio, newItem: AppAudio): Boolean {
        return oldItem.isPlayingNormal == newItem.isPlayingNormal || oldItem.isPlayingReverse == newItem.isPlayingReverse
    }

    @SuppressLint("DiffUtilEquals") // Suppress if your data classes correctly implement equals
    override fun areContentsTheSame(oldItem: AppAudio, newItem: AppAudio): Boolean {
        // For simple data classes, default equals() might be enough.
        // If not, implement custom equals() in your data classes or compare fields here.
        return oldItem == newItem
    }
}
