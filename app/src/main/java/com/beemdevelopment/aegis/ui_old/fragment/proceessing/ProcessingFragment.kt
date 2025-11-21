package com.beemdevelopment.aegis.ui_old.fragment.proceessing

import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseFragment
import com.beemdevelopment.aegis.databinding.FragmentProcessingBinding
import com.beemdevelopment.aegis.model.EncoderModel
import com.beemdevelopment.aegis.ui_old.activity.main.MainActivity
import com.beemdevelopment.aegis.ui_old.dialog.ExitPreviewDialog
import com.beemdevelopment.aegis.ui_old.fragment.main.MainFragment.Companion.isDownloadedRecent
import com.beemdevelopment.aegis.utils.Constant
import com.beemdevelopment.aegis.utils.FileUtils
import com.beemdevelopment.aegis.utils.MaxUtils
import com.beemdevelopment.aegis.utils.MaxUtils.logFirebaseEvent
import com.beemdevelopment.aegis.utils.extension.exhaustive
import com.beemdevelopment.aegis.utils.extension.invisible
import com.beemdevelopment.aegis.utils.gone
import com.beemdevelopment.aegis.utils.visible
import com.beemdevelopment.aegis.widgets.ui.FadeInThenOutAnimationEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("DEPRECATION")
@AndroidEntryPoint
class ProcessingFragment : BaseFragment<FragmentProcessingBinding>(){

    companion object {
        fun newBundle(encoder: EncoderModel) = bundleOf().apply {
            putParcelable(Constant.KEY_EXTRA_DATA, encoder)
        }
    }

    private var animationEffect: FadeInThenOutAnimationEffect? = null


    override fun isDarkTheme(): Boolean = false

    private val viewModel: ProcessingViewModel by viewModels()

    override fun getLayoutId(): Int = R.layout.fragment_processing

    override fun setUp() {
        viewModel.encoderModel = arguments?.getParcelable(Constant.KEY_EXTRA_DATA) ?: return
        viewModel.typeFilter = viewModel.encoderModel.typeFilter
        viewModel.pathVideo = viewModel.encoderModel.pathVideo
        binding.viewmodel = viewModel
        viewModel.activity = requireActivity()
        setUpEvent()
        setUpView()

        viewModel.isLoadSuccess.set(true)
        viewModel.fileEncoder = viewModel.pathVideo
        setUpPlayVideo()
        requireContext().logFirebaseEvent("video_processing")

    }


    private fun setUpEvent() {
        lifecycleScope.launchWhenStarted {
            viewModel.homeEvent.collect { event ->
                when (event) {
                    ProcessingViewModel.Event.OnClickBack -> {
                        val back = {
                            viewModel.deleteVideo()
                            viewModel.pausePlayer()
                            viewModel.releaseAppPlayer()
                            MaxUtils.showAdsWithCustomCount(requireActivity(),
                                object : MaxUtils.NextScreenListener {
                                    override fun nextScreen() {
                                        viewModel.NavigateCamera()
                                    }
                                })
                        }
                        if (viewModel.isDownloading.get()) {
                            viewModel.isDownloading.set(false)
                            viewModel.progressDownload.set(0f)
                            return@collect
                        }

                        if (!viewModel.isSaved) {
                            ExitPreviewDialog(requireContext()).apply {
                                setUpData {
                                    File(viewModel.pathVideo).delete()
                                    back()
                                }
                                show()
                                viewModel.pausePlayer()
                            }
                            return@collect
                        }
                        back()
                    }

                    ProcessingViewModel.Event.NavigateGoToHome -> {
                        startActivity(MainActivity.newIntent(requireContext()))
                    }

                    ProcessingViewModel.Event.GoToHome -> {
                        goToHome()
                    }

                    ProcessingViewModel.Event.OnClickSave -> {
                        requireContext().logFirebaseEvent("click_video_preview_share")
                        
                        val saveDone = {
                            viewModel.isLoadSuccess.set(true)
                            isDownloadedRecent = true
                            binding.llSaved.visible()
                            binding.llSaved.gone(3000)

                            viewModel.addCollectionToDB {

                            }
                        }
                        saveDone()
                    }

                    ProcessingViewModel.Event.OnClickShare -> {
                        requireContext().logFirebaseEvent("click_video_preview_share")

                        FileUtils.shareVideo(requireContext(), File(viewModel.fileEncoder))
                    }

                    ProcessingViewModel.Event.OnClickPlayVideo -> {
                        animationEffect?.go(!viewModel.appPlayer.isPlaying())
                        if(viewModel.appPlayer.isPlaying()) requireContext().logFirebaseEvent("video_preview")
                        else{}
                    }

                    ProcessingViewModel.Event.LoadCompleted -> {
                        binding.playerView.visible(200)
                        binding.pbLoading.gone(200)
                        binding.imvClickPlayPause.visible(200)
                        animationEffect?.go(!viewModel.appPlayer.isPlaying())
                    }

                    ProcessingViewModel.Event.VideoEnd -> {
                        animationEffect?.go(!viewModel.appPlayer.isPlaying())
                    }

                    ProcessingViewModel.Event.LoadError -> {

                    }
                    ProcessingViewModel.Event.NavigateCamera -> {
                        goToHome()
                    }
                }.exhaustive
            }
        }
    }

    private fun setUpView() {
        animationEffect = FadeInThenOutAnimationEffect(binding.imvClickPlayPause)
    }

    private fun setUpPlayVideo() = viewModel.viewModelScope.launch {
        binding.playerView.invisible()
        binding.pbLoading.visible()
        delay(1000)
        binding.liveStatePlayer = viewModel.appPlayer.liveState
        viewModel.initVideo(binding.playerView)
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    private fun goToHome() {
        viewModel.deleteVideo()
        viewModel.pausePlayer()
        viewModel.releaseAppPlayer()

        MaxUtils.showAdsWithCustomCount(requireActivity(),
            object : MaxUtils.NextScreenListener {
                override fun nextScreen() {
                    viewModel.NavigateGoToHome()
                }
            })
    }

    override fun onBackPressed() {

    }
}