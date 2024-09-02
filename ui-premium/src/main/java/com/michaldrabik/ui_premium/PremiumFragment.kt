package com.michaldrabik.ui_premium

import android.os.Bundle
import android.view.View
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_premium.databinding.FragmentPremiumBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PremiumFragment : BaseFragment<PremiumViewModel>(R.layout.fragment_premium) {

  override val viewModel by viewModels<PremiumViewModel>()
  private val binding by viewBinding(FragmentPremiumBinding::bind)

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?,
  ) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
    )
  }

  private fun setupView() {
    with(binding) {
      premiumToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
      premiumRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
        val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        view.updatePadding(top = padding.top + inset)
      }
    }
  }

  private fun render(uiState: PremiumUiState) {
    uiState.run {
      with(binding) {
        premiumProgress.visibleIf(isLoading)
      }
    }
  }
}
