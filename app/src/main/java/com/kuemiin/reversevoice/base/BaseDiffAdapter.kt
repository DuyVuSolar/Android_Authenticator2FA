package com.kuemiin.reversevoice.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kuemiin.base.BR
//import androidx.databinding.library.baseAdapters.BR
import com.kuemiin.reversevoice.model.BaseDiffModel

class BaseDiffAdapter(
        @LayoutRes private val resLayout: Int
) : ListAdapter<BaseDiffModel, BaseDiffAdapter.DiffViewHolder>(BaseListItemDiffCallback()) {

    private val originalCategories = mutableListOf<BaseDiffModel>()
    var ratio : String? = null

    fun submitCategories(categories: List<BaseDiffModel>) {
        originalCategories.clear()
        categories.forEach {
            originalCategories.add(it)
        }
        submitList(buildDisplayList())
    }

    private fun buildDisplayList(): List<BaseDiffModel> {
        val displayList = mutableListOf<BaseDiffModel>()
        originalCategories.forEach { category ->
            displayList.add(category.clone())
        }
        return displayList
    }

    var viewmodel: ViewModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiffViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, resLayout, parent, false)
        return DiffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiffViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.binding.setVariable(BR.item, currentItem)
        if(viewmodel != null) holder.binding.setVariable(BR.viewmodel, viewmodel)
        holder.binding.executePendingBindings()
    }

    class DiffViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

}


// DiffUtil.ItemCallback for efficient list updates
class BaseListItemDiffCallback : DiffUtil.ItemCallback<BaseDiffModel>() {
    override fun areItemsTheSame(oldItem: BaseDiffModel, newItem: BaseDiffModel): Boolean {
        return oldItem.uuid == newItem.uuid || oldItem.isSelected == newItem.isSelected
    }

    @SuppressLint("DiffUtilEquals") // Suppress if your data classes correctly implement equals
    override fun areContentsTheSame(oldItem: BaseDiffModel, newItem: BaseDiffModel): Boolean {
        return oldItem.compareTo(newItem)
    }
}
