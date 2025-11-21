package com.beemdevelopment.aegis.ui_old.dialog

import android.content.Context
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseDialogAlert
import com.beemdevelopment.aegis.databinding.DialogExitPreviewBinding
import com.beemdevelopment.aegis.utils.AnimationUtils

class ExitPreviewDialog(val mContext : Context) : BaseDialogAlert<DialogExitPreviewBinding>(mContext) {

    private var callBackOk : () -> Unit = {}

    fun setUpData(callback : () -> Unit): ExitPreviewDialog {
        callBackOk = callback
        return this
    }

    override fun setAnimation() {
        AnimationUtils.animateDialog(binding.llConfirmParent)
    }

    override fun initView() {
        setTextAndIcon()
        binding.llConfirmParent.setOnClickListener {
            dismiss()
        }

        binding.tvDiscard.setOnClickListener {
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

    override fun getLayout(): Int = R.layout.dialog_exit_preview

    override fun isCanceledOnTouchOut(): Boolean = true

    private fun setTextAndIcon(){

    }

}