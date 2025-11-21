package com.beemdevelopment.aegis.ui_old.dialog.add_category

import android.annotation.SuppressLint
import android.graphics.Color.*
import android.view.View
import androidx.core.widget.addTextChangedListener
import carbon.widget.TextView
import com.beemdevelopment.aegis.utils.binding.onDebouncedClick
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import com.beemdevelopment.aegis.databinding.DialogAddCategoryBinding

@AndroidEntryPoint
class EditCategoryDialog : BaseDialogFragment<DialogAddCategoryBinding>() {

    private val MAX_LENGTH_FILE_NAME = 25
    private var mCallbackChange : (String) -> Unit = {}
    private var firstCategory : String = ""

    fun setCallbackClick(category : String, callback : (String) -> Unit): EditCategoryDialog {
        mCallbackChange = callback
        firstCategory = category
        return this
    }

    override fun getColorStatusBar(): Int = R.color.black_45

    override fun setAnimation() = R.style.BottomSheetDialogAnimation

    override fun getLayoutId(): Int = R.layout.dialog_add_category
    override fun setUp() {
        binding.lifecycleOwner = this
        setUpEvent()
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun setUpEvent() {
        try {
            binding.edtName.setText(firstCategory)
            binding.edtName.setSelection(binding.edtName.length())
            binding.tvCurrentLength.text = "${firstCategory.length}/${MAX_LENGTH_FILE_NAME}"
        } catch (e: Exception) { }


        binding.edtName.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                isRequestFocus = hasFocus
            }
        }

        binding.tvChange.setEnableViewSave(firstCategory.isNotBlank() && firstCategory != firstCategory)
        binding.edtName.addTextChangedListener {
            val nameEdt = binding.edtName.text.toString().trim()
            binding.tvChange.setEnableViewSave(nameEdt.isNotBlank() && nameEdt != firstCategory)
            binding.tvCurrentLength.text = "${nameEdt.length}/${MAX_LENGTH_FILE_NAME}"
//            if(nameEdt.isNotBlank()){
//                binding.imvClearText.visible()
//            }else{
//                binding.imvClearText.gone()
//            }
        }

//        binding.imvClearText.onDebouncedClick {
//            binding.edtName.setText("")
//        }

        binding.imvOutside.onDebouncedClick {
            dismiss()
        }

        binding.tvCancel.onDebouncedClick {
            dismiss()
        }

        binding.tvChange.onDebouncedClick {
            val nameEdt = binding.edtName.text.toString().trim()

            mCallbackChange.invoke(nameEdt)
            dismiss()
        }
        binding.edtName.postDelayed({
            binding.edtName.requestFocus()
            showKeyboard(binding.edtName)
        },500)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        hideKeyboard(binding.edtName)
//        dismiss()
    }

    override fun dismiss() {
        hideKeyboard(binding.edtName)
        super.dismiss()
    }


    fun TextView.setEnableViewSave(enable: Boolean) {
        isEnabled = enable
        setTextColor(if(enable) parseColor("#3A64FC") else parseColor("#E6E6E6"))
    }


    var isRequestFocus = false
    var hasRequestPaused = false
    override fun onPause() {
        super.onPause()
        hasRequestPaused = isRequestFocus
    }

    override fun onResume() {
        super.onResume()
        if(hasRequestPaused){
            binding.edtName.postDelayed({
                binding.edtName.requestFocus()
                showKeyboard(binding.edtName)
            }, 500)
        }
    }


    //endregion
}