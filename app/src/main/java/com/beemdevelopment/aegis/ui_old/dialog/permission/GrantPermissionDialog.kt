package com.beemdevelopment.aegis.ui_old.dialog.permission

import android.content.Context
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseDialogAlert
import com.beemdevelopment.aegis.databinding.DialogGrantPermissionBinding
import com.beemdevelopment.aegis.utils.AnimationUtils

class GrantPermissionDialog(mContext : Context) : BaseDialogAlert<DialogGrantPermissionBinding>(mContext) {

    private var callBackOk : () -> Unit = {}

    fun setUpData(callback : () -> Unit): GrantPermissionDialog {
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

        binding.tvOpenSetting.setOnClickListener {
            callBackOk.invoke()
            dismiss()
        }
    }

    override fun dismiss() {
        super.dismiss()
    }

    override fun getLayout(): Int = R.layout.dialog_grant_permission

    override fun isCanceledOnTouchOut(): Boolean = true

    private fun setTextAndIcon(){

    }

}