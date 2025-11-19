package com.kuemiin.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
//import com.kuemiin.base.BR

/**
 * Usecase:
 * class BooksAdapter : DataBindingAdapter<Listable>(DiffCallback()) {
 *      class DiffCallback : DiffUtil.ItemCallback<Listable>() {
 *          //..
 *      }
 *
 *      override fun getItemViewType(position: Int) = if (getItem(position) is Book) {
 *          //layout
 *      } else {
 *          //layout
 *      }
 *
 * it?.let(adapter::submitList)
 */
abstract class BaseDiffAdapter<T>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val inflater: LayoutInflater
) :
    ListAdapter<T, BaseDiffAdapter.ViewHolderBase<T>>(diffCallback) {
    // region Const and Fields
    var listener: ListItemDiffListener? = null
    // endregion

    // region override function
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBase<T> {
        return ViewHolderBase(
            DataBindingUtil.inflate(
                inflater, viewType, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolderBase<T>, position: Int) =
        holder.bind(getItem(position), listener)
    // endregion

    // region ViewHolder
    open class ViewHolderBase<T>(
        private val binding: ViewDataBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: T, diffListener: ListItemDiffListener?) {
            binding.apply {
//                setVariable(BR.item, data)
//                setVariable(BR.listener, diffListener)
                executePendingBindings()
            }
        }
    }
    // endregion

    // region listener
    interface ListItemDiffListener
    // endregion
}

class BaseListAdapter<T>(
    diffCallback: DiffUtil.ItemCallback<T>,
    inflater: LayoutInflater,
    @LayoutRes private val resLayout: Int
) : BaseDiffAdapter<T>(diffCallback, inflater) {
    override fun getItemViewType(position: Int): Int {
        return resLayout
    }
}
class ConditionAdapter<T>(
    diffCallback: DiffUtil.ItemCallback<T>,
    inflater: LayoutInflater,
    private val condition: (T) -> Int

) : BaseDiffAdapter<T>(diffCallback, inflater) {
    override fun getItemViewType(position: Int): Int {
        return condition(getItem(position))
    }
}
