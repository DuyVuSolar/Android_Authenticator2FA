package com.beemdevelopment.aegis.ui_old.fragment.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.beemdevelopment.aegis.BR
import com.beemdevelopment.aegis.model.MyGalleryModel
import com.beemdevelopment.aegis.utils.adapter.GalleryDiffUtilCallBack

class GalleryAdapter(
    private val inflater: LayoutInflater,
    @LayoutRes private val resLayout: Int,
    @LayoutRes private val resLayoutNative: Int,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // region Const and Fields

    private var oldList: ArrayList<MyGalleryModel> = arrayListOf()
    var viewmodel: ViewModel? = null

    companion object{
        const val TYPE_NORMAL = 1
        const val TYPE_ADS = 2
    }

    fun setData(newData: ArrayList<MyGalleryModel>) {
        val diffResult = DiffUtil.calculateDiff(
            GalleryDiffUtilCallBack(
                newData,
                oldList
            )
        )
        diffResult.dispatchUpdatesTo(this)
        oldList.clear()
        this.oldList.addAll(newData)
    }

    override fun getItemViewType(position: Int): Int {
        return if(oldList[position].currentVideo.isEmpty() && oldList[position].typeFilter.isEmpty()){
            TYPE_ADS
        }else{
            TYPE_NORMAL
        }
    }

    var listener: ListItemListener? = null
    // endregion

    // region override function
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding : ViewDataBinding
        if(viewType == TYPE_ADS){
            binding = DataBindingUtil.inflate(inflater, resLayoutNative, parent, false)
            return ViewHolderAds(binding)
        }else{
            binding = DataBindingUtil.inflate(inflater, resLayout, parent, false)
            return ViewHolderNormal(binding)
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(oldList.isEmpty()) return
        when(holder){
            is ViewHolderNormal -> {
                val item = oldList[position]
                holder.binding.setVariable(BR.item, item)
                if(viewmodel != null) holder.binding.setVariable(BR.viewmodel, viewmodel)
                holder.binding.executePendingBindings()
            }

            is ViewHolderAds -> {
                if(viewmodel != null) holder.binding.setVariable(BR.viewmodel, viewmodel)
                holder.binding.executePendingBindings()
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
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

    class ViewHolderNormal(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    class ViewHolderAds(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    // region listener
    interface ListItemListener
    // endregion
}