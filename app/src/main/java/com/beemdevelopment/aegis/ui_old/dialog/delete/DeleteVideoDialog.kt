package com.beemdevelopment.aegis.ui_old.dialog.delete

import android.content.Context
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseDialogAlert
import com.beemdevelopment.aegis.databinding.DialogDeleteVideoBinding
import com.beemdevelopment.aegis.utils.AnimationUtils

class DeleteVideoDialog(val mContext : Context) : BaseDialogAlert<DialogDeleteVideoBinding>(mContext) {

    private var callBackOk : () -> Unit = {}
    private var isDeleteOne : Boolean = false

    fun setUpData(isOne : Boolean, callback : () -> Unit): DeleteVideoDialog {
        callBackOk = callback
        isDeleteOne = isOne
        return this
    }

    override fun setAnimation() {
        AnimationUtils.animateDialog(binding.llConfirmParent)
    }

    override fun initView() {
        binding.llConfirmParent.setOnClickListener {
            dismiss()
        }
        binding.tvDelete.setOnClickListener {
            callBackOk.invoke()
            dismiss()
        }
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun dismiss() {
        super.dismiss()
    }

    override fun getLayout(): Int = R.layout.dialog_delete_video

    override fun isCanceledOnTouchOut(): Boolean = true

    override fun show() {
        super.show()
        setTextAndIcon()
    }

    private fun setTextAndIcon(){

    }

}