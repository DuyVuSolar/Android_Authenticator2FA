package com.beemdevelopment.aegis.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
//import androidx.databinding.library.baseAdapters.BR
import com.beemdevelopment.aegis.model.Image
import com.beemdevelopment.aegis.utils.adapter.GalleryDiffUtilCallBack
import com.beemdevelopment.aegis.BR

class BaseAdapterImage(
        private val inflater: LayoutInflater,
        @LayoutRes private val resLayout: Int
) : RecyclerView.Adapter<BaseAdapterImage.ViewHolderBase>(){

    private var data: ArrayList<Image> = arrayListOf()

    var mCallbackDrag : ((ViewHolder) -> Unit)? = null
    var viewmodel: ViewModel? = null

    fun setNewData(newData: ArrayList<Image>) {
        val new = arrayListOf<Image>()
        newData.forEach {
            new.add(it.clone())
        }
        data.clear()
        this.data.addAll(new)
    }

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


    override fun onBindViewHolder(holder: ViewHolderBase, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val o = payloads[0] as Bundle
            for (key in o.keySet()) {
                if (key == "price") {
                    onBindViewHolder(holder, position)
                }
            }
        }
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
