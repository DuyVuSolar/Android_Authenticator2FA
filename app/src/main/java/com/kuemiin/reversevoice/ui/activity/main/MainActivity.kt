package com.kuemiin.reversevoice.ui.activity.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.kuemiin.reversevoice.R
import com.kuemiin.reversevoice.base.BaseActivity
import com.kuemiin.reversevoice.base.BaseFragment
import com.kuemiin.reversevoice.databinding.ActivityMainBinding
import com.kuemiin.reversevoice.receiver.NetworkStateMonitor
import com.kuemiin.reversevoice.ui.activity.splash.SplashActivity
import com.kuemiin.reversevoice.utils.NetworkUtils
import com.kuemiin.reversevoice.utils.extension.exhaustive
import com.kuemiin.reversevoice.utils.extension.observer
import com.kuemiin.reversevoice.utils.isAndroid13
import dagger.hilt.android.AndroidEntryPoint
import com.kuemiin.reversevoice.utils.MaxUtils
import kotlinx.coroutines.launch
import com.kuemiin.reversevoice.utils.extension.gone
import com.kuemiin.reversevoice.utils.extension.visible

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    companion object{
        //normal
        fun newIntent(context: Context): Intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        //use for choose language
        fun newIntentClear(context: Context): Intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }

    private val viewModel by viewModels<MainViewModel>()

    override fun getLayoutId(): Int = R.layout.activity_main

    override fun initViews() {
        binding.viewmodel = viewModel
        binding.maxSingleton = MaxUtils
        setUpEvents()
        viewModel.checkShowIntro()
        startRotationAnimation()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun setUpEvents() {
        if (isAndroid13()) {
            registerReceiver(
                NetworkStateMonitor(this),
                IntentFilter("android.intent.action.ACCESS_NETWORK_STATE"),
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                NetworkStateMonitor(this),
                IntentFilter("android.intent.action.ACCESS_NETWORK_STATE"),
            )
        }

        observer(NetworkStateMonitor.isConnectNetwork) {
            it?.let {
                if(!it) {
                    viewModel.hasConnectedInternet.set(false)
                    showNoInternet()
                }
                if (it && !viewModel.hasConnectedInternet.get()) return@observer
                viewModel.hasConnectedInternet.set(it)
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mainEvent.collect { event ->
                    when (event) {
                        MainViewModel.MainEvent.OnClickBottom -> {

                        }
                        MainViewModel.MainEvent.OnRestartApp -> {
                            if (NetworkUtils.isNetworkConnected(this@MainActivity)) {
                                restartActivity()
                            } else {
                            }
                        }
                        MainViewModel.MainEvent.OnConnectAgainInternet -> {
                            showNoInternet()
                        }
                    }.exhaustive
                }
            }
        }

        binding.clRoot.viewTreeObserver.addOnGlobalLayoutListener {
            try {
                val r = Rect()
                binding.clRoot.getWindowVisibleDisplayFrame(r)
                val screenHeight = binding.clRoot.rootView.height
                val keypadHeight = screenHeight - r.bottom
                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    // Keyboard is visible

//                    val layoutParams = binding.llAlignOptions.layoutParams as ConstraintLayout.LayoutParams
//                    layoutParams.bottomMargin = keypadHeight
                    val a = keypadHeight
                } else {
                    // Keyboard is hidden
                    val a = keypadHeight

                }
            }catch (e : Exception){
            }
        }

    }

    private fun restartActivity() {
        startActivity(SplashActivity.newIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        })
        overridePendingTransition(R.anim.wait_anim, R.anim.fade_in)
        finish()
    }

    fun showNoInternet(){
        if (NetworkUtils.isNetworkConnected(this@MainActivity)) {
            binding.clNoInternet.gone()
        } else {
            binding.clNoInternet.visible()
        }
    }


    private fun startRotationAnimation() {
        // 1. Load the animation from the XML resource file
        val rotateAnimation: Animation = AnimationUtils.loadAnimation(this, R.anim.rotate_endless)
        // 2. Apply the animation to your ImageView
        binding.imvGif.startAnimation(rotateAnimation)
    }

    override fun onDestroy() {
        binding.imvGif.clearAnimation()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        try {
            val navHostFragment =
                supportFragmentManager.primaryNavigationFragment as NavHostFragment?
            val fragmentManager: FragmentManager = navHostFragment!!.childFragmentManager
            val fragment: Fragment? = fragmentManager.primaryNavigationFragment
            if (fragment is BaseFragment<*>) {
                fragment.onBackPressed()
            }
        } catch (_: Exception) {
        }
    }

}