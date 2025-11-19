package com.kuemiin.reversevoice.ui.fragment.preview_gallery

import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseFragment
import com.kuemiin.reversevoice.databinding.FragmentPreviewDesignBinding
import com.kuemiin.reversevoice.model.MyGalleryModel
import com.kuemiin.reversevoice.ui.dialog.delete.DeleteVideoDialog
import com.kuemiin.reversevoice.utils.Constant
import com.kuemiin.reversevoice.utils.FileUtils
import com.kuemiin.reversevoice.utils.MaxUtils
import com.kuemiin.reversevoice.utils.binding.setImageAnyNormal
import com.kuemiin.reversevoice.utils.extension.exhaustive
import com.kuemiin.reversevoice.utils.extension.toast
import com.kuemiin.reversevoice.utils.visible
import com.kuemiin.reversevoice.widgets.ui.FadeInThenOutAnimationEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File


@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("DEPRECATION", "IMPLICIT_CAST_TO_ANY")
@AndroidEntryPoint
class PreviewGalleryFragment : BaseFragment<FragmentPreviewDesignBinding>() {

    companion object {
        fun newBundle(myDesignModel: MyGalleryModel) = bundleOf().apply {
            putParcelable(Constant.KEY_EXTRA_DATA, myDesignModel)
        }
    }

    private var animationEffect: FadeInThenOutAnimationEffect? = null
    private var dialogDelete: DeleteVideoDialog? = null
    override fun isDarkTheme(): Boolean = false

    private val viewModel: PreviewGalleryViewModel by viewModels()

    override fun getLayoutId(): Int = R.layout.fragment_preview_design

    override fun setUp() {
        binding.viewmodel = viewModel
        viewModel.activity = requireActivity()
        viewModel.myDesign = arguments?.getParcelable(Constant.KEY_EXTRA_DATA) ?: return
        setUpEvent()
        setUpView()
        setUpPlayer()
    }

    private fun setUpEvent() {
        lifecycleScope.launchWhenStarted {
            viewModel.homeEvent.collect { event ->
                when (event) {
                    PreviewGalleryViewModel.Event.OnClickBack -> {
                        if (viewModel.isDeleting.get()) {
                            return@collect
                        }

                        MaxUtils.showAdsWithCustomCount(
                            requireActivity(),
                            object : MaxUtils.NextScreenListener {
                                override fun nextScreen() {
                                    viewModel.NavigatenavigateUp()
                                }
                            })
                    }

                    PreviewGalleryViewModel.Event.NavigatenavigateUp -> {
                        navigateUp()
                    }

                    PreviewGalleryViewModel.Event.OnClickDelete -> {
                        viewModel.pausePlayer()
                        if (dialogDelete == null) dialogDelete = DeleteVideoDialog(requireContext())
                        dialogDelete?.apply {
                            setUpData(true) {
                                viewModel.deleteGallery {
//                                    hasDeleteGallery = true
                                    toast("Delete successfully!")
                                    navigateUp()
                                }
                            }
                            show()
                        }
                    }

//                    PreviewGalleryViewModel.Event.OnClickDownload -> {
//                        viewModel.convertFileToExternal(
//                            requireActivity(),
//                            viewModel.myDesign!!.currentVideo
//                        ) {
//                            toast("Download successfully")
//                        }
//                    }

                    PreviewGalleryViewModel.Event.OnClickShare -> {
                        FileUtils.shareVideo(
                            requireContext(),
                            File(viewModel.myDesign!!.currentVideo)
                        )
                    }

                    PreviewGalleryViewModel.Event.OnLoadVideoCompleted -> {
                        binding.imvClickPlayPause.visible(200)
                        animationEffect?.go(!viewModel.appPlayer.isPlaying())
                    }

                    PreviewGalleryViewModel.Event.OnClickPlayPause -> {
                        animationEffect?.go(!viewModel.appPlayer.isPlaying())
                    }

                    PreviewGalleryViewModel.Event.VideoEnd -> {
                        animationEffect?.go(!viewModel.appPlayer.isPlaying())
                    }
                }.exhaustive
            }
        }
        binding.sbProgressFull.setOnSeekBarChangeListener(listenerSeekbar)
    }

    private val listenerSeekbar = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                viewModel.isTouchingSeekbar.set(true)
                seekBar?.progress?.toLong()
                    ?.let { viewModel.appPlayer.seek(it, viewModel.appPlayer.isPlaying()) }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            viewModel.isTouchingSeekbar.set(true)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            viewModel.isTouchingSeekbar.set(false)
        }
    }

    private fun setUpView() {
        animationEffect = FadeInThenOutAnimationEffect(binding.imvClickPlayPause)
        if (viewModel.myDesign == null) return
    }

    private fun setUpPlayer() {
        binding.imvThumb.setImageAnyNormal(viewModel.myDesign!!.currentVideo)
        binding.imvBackVideo.postDelayed({
            binding.liveStatePlayer = viewModel.appPlayer.liveState
            viewModel.initVideo(binding.playerViewSmall)
        }, 200)
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    override fun onDestroyView() {
        viewModel.releaseAppPlayer()
        super.onDestroyView()
    }

    override fun onBackPressed() {

    }
}