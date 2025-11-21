package com.beemdevelopment.aegis.ui_old.dialog.delete

import android.content.Context
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseDialogAlert
import com.beemdevelopment.aegis.databinding.DialogDeleteAudioBinding
import com.beemdevelopment.aegis.utils.AnimationUtils

class DeleteAudioDialog(mContext : Context) : BaseDialogAlert<DialogDeleteAudioBinding>(mContext) {

    private var callBackOk : () -> Unit = {}

    fun setUpData(callback : () -> Unit): DeleteAudioDialog {
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

        binding.tvDelete.setOnClickListener {
            callBackOk.invoke()
            cancel()
        }
        binding.tvCancel.setOnClickListener {
            cancel()
        }
    }

    override fun dismiss() {
        super.dismiss()
    }

    override fun cancel() {
        super.cancel()
    }

    override fun getLayout(): Int = R.layout.dialog_delete_audio

    override fun isCanceledOnTouchOut(): Boolean = true

    private fun setTextAndIcon(){

    }

}