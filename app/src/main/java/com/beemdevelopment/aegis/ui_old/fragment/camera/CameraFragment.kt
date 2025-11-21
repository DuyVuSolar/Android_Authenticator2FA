package com.beemdevelopment.aegis.ui_old.fragment.camera



import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseFragment
import com.beemdevelopment.aegis.databinding.FragmentCameraBinding
import com.beemdevelopment.aegis.utils.Constant
import com.beemdevelopment.aegis.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("DEPRECATION")
@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>() {

    companion object {
        fun newBundle(effect: String) = bundleOf().apply {
            putString(Constant.KEY_EXTRA_DATA, effect)
        }
    }

    private val viewModel: CameraViewModel by viewModels()

    override fun isDarkTheme(): Boolean = false

    override fun getLayoutId(): Int = R.layout.fragment_camera


    override fun setUp() {
        if (PermissionUtils.isHasPermissionCamera(requireContext()) && !PermissionUtils.checkHasPerCameraAndRecord(requireActivity())) {
            return
        }
        binding.viewmodel = viewModel
        viewModel.activity = requireActivity()
        setUpEvent()

    }

    private fun setUpEvent() {

    }

}