package com.beemdevelopment.aegis.ui_old.dialog.delete

import android.content.Context
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseDialogAlert
import com.beemdevelopment.aegis.databinding.DialogDeleteCategoryBinding
import com.beemdevelopment.aegis.utils.AnimationUtils

class DeleteCategoryDialog(mContext : Context) : BaseDialogAlert<DialogDeleteCategoryBinding>(mContext) {

    private var callBackOk : () -> Unit = {}

    fun setUpData(callback : () -> Unit): DeleteCategoryDialog {
        callBackOk = callback
        return this
    }

    override fun setAnimation() {
        AnimationUtils.animateDialog(binding.llConfirmParent)
    }

    override fun initView() {
        setTextAndIcon()
        binding.llConfirmParent.setOnClickListener {
            cancel()
        }

        binding.tvChange.setOnClickListener {
            callBackOk.invoke()
            cancel()
        }
        binding.tvCancel.setOnClickListener {
            cancel()
        }
    }

    override fun getLayout(): Int = R.layout.dialog_delete_category

    override fun isCanceledOnTouchOut(): Boolean = true

    private fun setTextAndIcon(){

    }

}