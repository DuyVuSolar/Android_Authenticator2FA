package com.kuemiin.reversevoice.ui.activity.intro

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.ViewPager
import com.kuemiin.base.adapter.FragmentAdapter
import com.kuemiin.reversevoice.ui.activity.main.MainActivity
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseActivity
import com.kuemiin.reversevoice.databinding.ActivityIntroBinding
import com.kuemiin.reversevoice.ui.activity.guide.GuideActivity
import com.kuemiin.reversevoice.ui.activity.intro.fragment.IntroFragment
import com.kuemiin.reversevoice.ui.activity.main.MainActivity3
import com.kuemiin.reversevoice.utils.extension.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import com.kuemiin.reversevoice.utils.MaxUtils
import com.kuemiin.reversevoice.utils.MaxUtils.getBoolean
import com.kuemiin.reversevoice.utils.MaxUtils.isCheck
import com.kuemiin.reversevoice.utils.MaxUtils.logFirebaseEvent
import com.kuemiin.reversevoice.utils.extension.gone
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class IntroActivity : BaseActivity<ActivityIntroBinding>() {

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, IntroActivity::class.java)
    }

    private var adapterFragment: FragmentAdapter? = null

    private val viewModel by viewModels<IntroViewModel>()

    override fun getLayoutId(): Int = R.layout.activity_intro

    override fun initViews() {
        binding.viewmodel = viewModel
        binding.maxSingleton = MaxUtils
        setUpIntro()
        setUpEvents()
        lifecycleScope.launch {
            delay(3000L)
            binding.loadingLanguage.gone()
        }
        this@IntroActivity.logFirebaseEvent("intro_1")

    }

    private fun setUpIntro() {
        viewModel.getListIntroUse().forEach {
            viewModel.listFragment.add(IntroFragment.newInstance(layoutInflater, viewModel, it))
        }

        adapterFragment = FragmentAdapter(supportFragmentManager, viewModel.listFragment)
        binding.vPagerIntroFragment.adapter = adapterFragment!!
        binding.vPagerIntroFragment.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if (position != viewModel.indexIntro.get()) {
                    viewModel.indexIntro.set(position)
                    this@IntroActivity.logFirebaseEvent("intro_$position")
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    private fun setUpEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.introEvent.collect { event ->
                    when (event) {
                        IntroViewModel.IntroEvent.OnClickContinue -> {
                            try {
                                binding.vPagerIntroFragment.currentItem = viewModel.indexIntro.get()
                            } catch (e: Exception) {

                            }
                        }

                        IntroViewModel.IntroEvent.OnClickBackIntro -> {
                            try {
                                binding.vPagerIntroFragment.currentItem = viewModel.indexIntro.get()
                            } catch (e: Exception) {

                            }
                        }

                        IntroViewModel.IntroEvent.OnClickStartToHome -> {
                            this@IntroActivity.logFirebaseEvent("click_start_home_intro")
                            if (getBoolean(
                                    this@IntroActivity,
                                    MaxUtils.ISSHOWINGNATIVE_FULLINTRONEXT,
                                    true
                                )
                            ) {
                                if (isCheck(this@IntroActivity)) {
                                    MaxUtils.loadAdmobIntro(
                                        this@IntroActivity,
                                        object : MaxUtils.NextScreenListener {
                                            override fun nextScreen() {
                                                startToHome()
                                            }
                                        })
                                } else {
                                    startToHome()
                                }
                            } else {
                                startToHome()
                            }
                        }
                    }.exhaustive
                }
            }
        }
    }

    private fun startToHome() {
        viewModel.setShowIntro()
        if (viewModel.showGuide.get()) {
            startActivity(MainActivity.newIntent(this))
            finish()
        } else {
            startActivity(GuideActivity.newIntent(this))
            finish()
        }
    }

    override fun initEvents() {

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        viewModel.onClickBackIntro()
    }

}