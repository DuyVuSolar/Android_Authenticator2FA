package com.kuemiin.reversevoice.ui.dialog.permission

import android.content.Context
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseDialogAlert
import com.kuemiin.reversevoice.databinding.DialogGrantPermissionBinding
import com.kuemiin.reversevoice.utils.AnimationUtils

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