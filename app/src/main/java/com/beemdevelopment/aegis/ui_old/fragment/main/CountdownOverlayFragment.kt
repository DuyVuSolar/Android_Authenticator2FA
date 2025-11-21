package com.beemdevelopment.aegis.ui_old.fragment.main

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.beemdevelopment.aegis.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.apply

class CountdownOverlayFragment(
    private val onFinish: () -> Unit
) : DialogFragment() {

    private var countdown = 3
    private var job: Job? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Translucent_NoTitleBar)
        dialog.setContentView(R.layout.fragment_overlay)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable("#B3000000".toColorInt().toDrawable())
        }

        val countdownText = dialog?.findViewById<TextView>(R.id.text_countdown)

        job = lifecycleScope.launch {
            while (countdown > 0) {
                countdownText?.text = countdown.toString()
                delay(1000)
                countdown--
            }
            dismissAllowingStateLoss()
            onFinish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job?.cancel()
    }
}
