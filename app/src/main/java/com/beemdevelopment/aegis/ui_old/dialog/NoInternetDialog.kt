package com.beemdevelopment.aegis.ui_old.dialog

import android.content.Context
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseDialogAlert
import com.beemdevelopment.aegis.databinding.DialogInternetBinding
import com.beemdevelopment.aegis.utils.AnimationUtils
import com.beemdevelopment.aegis.utils.NetworkUtils

class NoInternetDialog(mContext : Context) : BaseDialogAlert<DialogInternetBinding>(mContext) {

    private var callBackOk : () -> Unit = {}

    fun setUpData(callback : () -> Unit): NoInternetDialog {
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
        binding.tvTryAgain.setOnClickListener {
            if(NetworkUtils.isNetworkConnected(context)){
                callBackOk.invoke()
                dismiss()
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
    }

    override fun getLayout(): Int = R.layout.dialog_internet

    override fun isCanceledOnTouchOut(): Boolean = true

    private fun setTextAndIcon(){

    }

}