package com.kuemiin.base.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.kuemiin.base.BR

class BaseAdapter<T>(
    private val inflater: LayoutInflater,
    @LayoutRes private val resLayout: Int
) : RecyclerView.Adapter<BaseAdapter.ViewHolderBase>() {
    // region Const and Fields
    var data: List<T>? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var viewmodel: ViewModel? = null

    var listener: ListItemListener? = null
    // endregion

    // region override function
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBase {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            inflater, resLayout, parent, false
        )
        return ViewHolderBase(binding)
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return data!![position].hashCode().toLong()
    }

    override fun onBindViewHolder(holder: ViewHolderBase, position: Int) {
        val item = data?.get(position)
        holder.binding.setVariable(BR.item, item)
        if(listener != null) holder.binding.setVariable(BR.listener, listener)
        if(viewmodel != null) holder.binding.setVariable(BR.viewmodel, viewmodel)
        holder.binding.executePendingBindings()
    }

    // endregion

    // region ViewHolder
    class ViewHolderBase(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)
    // endregion

    // region listener
    interface ListItemListener
    // endregion
}