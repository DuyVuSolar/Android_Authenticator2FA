package com.kuemiin.reversevoice.utils.adapter

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.kuemiin.reversevoice.model.MyGalleryModel

class GalleryDiffUtilCallBack(
    var newList: ArrayList<MyGalleryModel>,
    var oldList: ArrayList<MyGalleryModel>
) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return newList[newItemPosition].id == oldList[oldItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val result = newList[newItemPosition].compareTo(oldList[oldItemPosition])
        return result == 0
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val newModel = newList[newItemPosition]
        val oldModel = oldList[oldItemPosition]

        val diff = Bundle()

        if (newModel.id != (oldModel.id) || newModel.isSelected != (oldModel.isSelected)
            || newModel.isSelecting != (oldModel.isSelecting) || newModel.currentVideo != (oldModel.currentVideo )) {
            diff.putBoolean("price", newModel.isSelected)
        }
        if (diff.size() == 0) {
            return null
        }
        return diff
        //return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}