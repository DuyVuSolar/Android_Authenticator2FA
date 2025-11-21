package com.beemdevelopment.aegis.base

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.beemdevelopment.aegis.BR

class BaseAdapter<T>(
        private val inflater: LayoutInflater,
        @LayoutRes private val resLayout: Int
) : RecyclerView.Adapter<BaseAdapter.ViewHolderBase>(){

    var data: ArrayList<T> = arrayListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var mCallbackDrag : ((ViewHolder) -> Unit)? = null
    var viewmodel: ViewModel? = null

    fun setCallbackDrag(callback : (ViewHolder) -> Unit){
        mCallbackDrag = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBase {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, resLayout, parent, false)
        return ViewHolderBase(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolderBase, position: Int) {
        val item = data[position]
        holder.binding.setVariable(BR.item, item)
        if(viewmodel != null) holder.binding.setVariable(BR.viewmodel, viewmodel)
        if(mCallbackDrag != null) holder.setTouchToDrag(mCallbackDrag)
        holder.binding.executePendingBindings()
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = data.removeAt(fromPosition)
        item?.let { data.add(toPosition, it) }
        notifyItemMoved(fromPosition, toPosition)
    }

    class ViewHolderBase(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root){
        var itemDrag : ImageView? = null

        fun setTouchToDrag(mCallbackDrag : ((ViewHolder) -> Unit)? = null){
//            itemDrag = binding.root.findViewById(R.id.imvMenu)
//            itemDrag?.onToucheDown{
//                mCallbackDrag?.invoke(this)
//            }

        }
    }

    private fun isItemFullyVisible(recyclerView: RecyclerView, position: Int): Boolean {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

        return position in firstVisibleItemPosition + 1..lastVisibleItemPosition - 1
    }

    fun handleItemClick(recyclerView: RecyclerView, position: Int) {
        if (!isItemFullyVisible(recyclerView, position)) {
            recyclerView.smoothScrollToPosition(position)
        }
    }

//    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
//        if (fromPosition < toPosition) {
//            for (i in fromPosition until toPosition) {
//                Collections.swap(data, i, i + 1)
//            }
//        } else {
//            for (i in fromPosition downTo toPosition + 1) {
//                Collections.swap(data, i, i - 1)
//            }
//        }
//        notifyItemMoved(fromPosition, toPosition)
//    }
//
//    override fun onItemRemoved(position: Int) {
//
//    }


}
