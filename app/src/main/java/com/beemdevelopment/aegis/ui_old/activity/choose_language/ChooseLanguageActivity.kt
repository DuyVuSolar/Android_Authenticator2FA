package com.beemdevelopment.aegis.ui_old.activity.choose_language

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.LocaleList
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.beemdevelopment.aegis.BaseApplication
import com.beemdevelopment.aegis.base.BaseAdapter
import com.beemdevelopment.aegis.R
import com.beemdevelopment.aegis.base.BaseActivity
import com.beemdevelopment.aegis.databinding.ActivityChooseLanguageBinding
import com.beemdevelopment.aegis.model.Language
import com.beemdevelopment.aegis.ui_old.activity.main.MainActivity
import com.beemdevelopment.aegis.utils.Constant
import com.beemdevelopment.aegis.utils.extension.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import com.beemdevelopment.aegis.utils.LocaleHelper.dLocale
import com.beemdevelopment.aegis.utils.MaxUtils
import kotlinx.coroutines.launch
import java.util.Locale

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class ChooseLanguageActivity : BaseActivity<ActivityChooseLanguageBinding>() {

    companion object {
        fun newIntent(context: Context, isShowBtnBack: Boolean = false): Intent =
            Intent(context, ChooseLanguageActivity::class.java).apply {
                putExtra(Constant.KEY_EXTRA_BOOLEAN, isShowBtnBack)
            }
    }

    private val mAdapterLanguage by lazy {
        BaseAdapter<Language>(layoutInflater, R.layout.item_choose_language).apply {
            viewmodel = viewModel
        }
    }

    private val viewModel by viewModels<ChooseLanguageViewModel>()

    override fun getLayoutId(): Int = R.layout.activity_choose_language

    override fun initViews() {
        viewModel.isShowBackBtn.set(intent.getBooleanExtra(Constant.KEY_EXTRA_BOOLEAN, false))
        binding.viewmodel = viewModel
        binding.maxSingleton = MaxUtils
        setUpAdapter()
        setUpEvents()
        viewModel.checkShowIntro()
        viewModel.checkChooseLanguage()
    }

    private fun setUpAdapter() {
        mAdapterLanguage.data = viewModel.listLanguage
        binding.rcvLanguage.adapter = mAdapterLanguage
//        val index = viewModel.listLanguage.indexOf(viewModel.listLanguage.first { it.isSelected })
//        binding.rcvLanguage.scrollToPosition(index)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setUpEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.languageEvent.collect { event ->
                    when (event) {
                        ChooseLanguageViewModel.ChooseLanguageEvent.OnClickBack -> {
                            onBackPressed()
                        }

                        ChooseLanguageViewModel.ChooseLanguageEvent.OnClickSave -> {
                            handleLocale()
                            startToHome()
                        }

                        is ChooseLanguageViewModel.ChooseLanguageEvent.OnClickItemLanguage -> {
                            mAdapterLanguage.notifyDataSetChanged()
                        }
                    }.exhaustive
                }
            }
        }
    }

    private fun startToHome() {
        viewModel.setShowChooseLanguage()
        binding.rcvLanguage.postDelayed({
            startActivity(MainActivity.newIntentClear(this))
            finish()
//                startActivity(MainActivity.newIntent(this))
        }, 300)
    }

    private fun handleLocale() {
        val flag = viewModel.listLanguage.first { it.isSelected }.alpha2
        val locale = viewModel.listLanguage.first { it.isSelected }.localeCode
        dLocale = Locale(locale)

        BaseApplication.instance.setFlag(this, flag)
        BaseApplication.instance.setLanguage(this, locale)

        Locale.setDefault(dLocale)
        val configuration: Configuration = resources.configuration
        configuration.setLocale(dLocale)
        configuration.setLayoutDirection(dLocale)
        val localeList = LocaleList(dLocale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    override fun initEvents() {

    }

    override fun onBackPressed() {
        if (viewModel.isShowBackBtn.get()) super.onBackPressed()
    }

}