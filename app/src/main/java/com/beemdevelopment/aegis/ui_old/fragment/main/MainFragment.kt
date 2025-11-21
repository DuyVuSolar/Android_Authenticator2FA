@file:Suppress("IMPLICIT_CAST_TO_ANY")

package com.beemdevelopment.aegis.ui_old.fragment.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.beemdevelopment.aegis.databinding.FragmentMainBinding
import com.beemdevelopment.aegis.utils.Constant
import com.beemdevelopment.aegis.utils.extension.exhaustive
import com.beemdevelopment.aegis.utils.extension.toast
import kotlinx.coroutines.launch
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseFragment
import com.beemdevelopment.aegis.ui_old.dialog.delete.DeleteAudioDialog
import com.beemdevelopment.aegis.utils.CommonUtils
import com.beemdevelopment.aegis.utils.FileUtils
import com.beemdevelopment.aegis.utils.MaxUtils
import com.beemdevelopment.aegis.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.GridLayoutManager
import com.beemdevelopment.aegis.ui_old.fragment.permission.PermisionFragment
import com.beemdevelopment.aegis.ui_old.fragment.preview_gallery.PreviewGalleryFragment
import com.beemdevelopment.aegis.utils.MaxUtils.logFirebaseEvent

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>() {

    companion object {
        var isDownloadedRecent = false
    }

    private var wl: PowerManager.WakeLock? = null

    private val viewModel by viewModels<MainFragmentViewModel>()
    private var dialogDelete: DeleteAudioDialog? = null


    private val mAdapterVideo by lazy {
        GalleryAdapter(
            layoutInflater,
            R.layout.item_video_record,
            R.layout.item_video_record//layout native

        ).apply {
            viewmodel = viewModel
        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_main

    override fun setUp() {
        hideKeyboard()
        viewModel.activity = requireActivity()
        binding.viewmodel = viewModel
        setUpAdapter()
        initEvent()
    }

    private fun setUpAdapter() {
        val linearLayoutManager = GridLayoutManager(requireContext(), 2)
    }

    @SuppressLint("NotifyDataSetChanged", "InvalidWakeLockTag")
    private fun initEvent() {
        setFragmentResultListener("key_show_reverse") { _, bundle ->
            val result = bundle.getBoolean("bundleKey")
            if (result) viewModel.isReversing.set(true)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mainEvent.collect { event ->
                    when (event) {
                        //region tab home
                        MainFragmentViewModel.MainEvent.BackToHome -> {
                            MaxUtils.showAdsWithCustomCount(
                                requireActivity(),
                                object : MaxUtils.NextScreenListener {
                                    override fun nextScreen() {
                                        viewModel.isReversing.set(false)
                                    }
                                })
                        }
                        MainFragmentViewModel.MainEvent.OnClickBottom -> {
                            when (viewModel.typeBottomSelected.get()) {
                                Constant.MAIN_TYPE_HOME -> {
                                    requireActivity().logFirebaseEvent("click_home_bottom")
                                }

                                Constant.MAIN_TYPE_RECORDINGS -> {
                                    requireActivity().logFirebaseEvent("click_recording_bottom")
                                }

                                else -> {
                                    requireActivity().logFirebaseEvent("click_settings_bottom")
                                }
                            }
                        }

                        MainFragmentViewModel.MainEvent.NavigateOnClickReverse -> {
                            viewModel.isReversing.set(true)
                        }

                        MainFragmentViewModel.MainEvent.OnClickReverse -> {
                            requireContext().logFirebaseEvent("click_reverse_singing")

                            if (PermissionUtils.checkHasPerRecord(requireActivity())) {
                                MaxUtils.showAdsWithCustomCount(
                                    requireActivity(),
                                    object : MaxUtils.NextScreenListener {
                                        override fun nextScreen() {
                                            viewModel.NavigateOnClickReverse()
                                        }
                                    })
                            } else {
                                navigateTo(
                                    R.id.action_mainFragment_to_permisionFragment,
                                    PermisionFragment.newBundle(false, "")
                                )
                            }
                        }

                        MainFragmentViewModel.MainEvent.OnClickSingAlong -> {
                            requireContext().logFirebaseEvent("click_sing_along")
                        }

                        MainFragmentViewModel.MainEvent.NavigateTryThis -> {

                        }

                        MainFragmentViewModel.MainEvent.NavigateOnClickTiktokVoice -> {

                        }
                        MainFragmentViewModel.MainEvent.OnClickTiktokVoice -> {
                            requireContext().logFirebaseEvent("click_the_voice")
                        }

                        MainFragmentViewModel.MainEvent.OnClickStartRecord -> {
                            requireActivity().logFirebaseEvent("click_start_record_home")


                        }

                        MainFragmentViewModel.MainEvent.OnClickStopRecord -> {
                            requireActivity().logFirebaseEvent("click_stop_record_home")
                        }

                        MainFragmentViewModel.MainEvent.OnClickPlayRecorded -> {
                            requireActivity().logFirebaseEvent("click_play_recorded_home")
                        }

                        MainFragmentViewModel.MainEvent.OnClickPlayReverse -> {
                            requireActivity().logFirebaseEvent("click_play_reverse_home")
                        }

                        MainFragmentViewModel.MainEvent.ShowLoading -> {
//                            if (viewModel.isLoading.get()) {
//                                binding.clLoading.visible()
//                            } else {
//                                binding.clLoading.gone()
//                            }
                        }
                        //endregion tab home

                        //region tab recording
                        is MainFragmentViewModel.MainEvent.EndAudioTabRecord -> {

                        }

                        is MainFragmentViewModel.MainEvent.PauseAudioTabRecord -> {

                        }

                        is MainFragmentViewModel.MainEvent.NotifyAdapterRecording -> {
                        }

                        is MainFragmentViewModel.MainEvent.NavigateOnClickItemVideo -> {
                            navigateTo(
                                R.id.action_mainFragment_to_previewGalleryFragment,
                                PreviewGalleryFragment.newBundle(event.item)
                            )
                        }

                        is MainFragmentViewModel.MainEvent.OnClickItemVideo -> {

                        }

                        is MainFragmentViewModel.MainEvent.OnHandleDelete -> {
                            requireActivity().logFirebaseEvent("click_delete_audio_recording")
                            if (event.list.isEmpty()) return@collect
                            else {
                            }
                            dialogDelete?.dismiss()
                            dialogDelete?.cancel()
                            dialogDelete = null
                            dialogDelete = DeleteAudioDialog(requireContext())
                            dialogDelete?.setUpData {
                                FileUtils.deleteFileInternalAudio(event.list.first())
                                viewModel.getListAudio()
                            }
                            dialogDelete?.show()
                        }

                        is MainFragmentViewModel.MainEvent.OnHandleShare -> {
                            requireActivity().logFirebaseEvent("click_share_audio_recording")

                            FileUtils.shareMultiplePath(
                                event.list.map { it.path },
                                requireActivity()
                            )
                        }
                        //endregion audio recording

                        //region tab settings
                        MainFragmentViewModel.MainEvent.OnClickShare -> {
                            requireActivity().logFirebaseEvent("click_share_app_settings")

                            CommonUtils.shareApp(requireActivity())
                        }

                        MainFragmentViewModel.MainEvent.OnClickPolicy -> {
                            requireActivity().logFirebaseEvent("click_privacy_policy_settings")

                            var url =
                                "https://docs.google.com/document/d/1HZxtyU6XqHdzLIDLERUNuo7RGuVY5blPlfspf7OMft0/edit"
                            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                url = "http://$url"
                            }

                            val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                            startActivity(browserIntent)
                        }

                        MainFragmentViewModel.MainEvent.OnClickTermOfUse -> {

                        }

                        MainFragmentViewModel.MainEvent.NotifyAdapterVideo -> {

                        }
                        //endregion tab settings
                    }.exhaustive
                }
            }
        }
    }

    //region LIFECYCLER

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (viewModel.isRecording.get()) {
            return
        }

        if (doubleBackToExitPressedOnce) {
            requireActivity().finish()
            return
        }
        doubleBackToExitPressedOnce = true
        toast("Tap again to exit")
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        wl?.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private var hasPausedRecord = false
    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    //endregion
}