package com.michaldrabik.ui_premium

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_premium.di.UiPremiumComponentProvider
import kotlinx.android.synthetic.main.fragment_premium.*

class PremiumFragment : BaseFragment<PremiumViewModel>(R.layout.fragment_premium) {

  override val viewModel by viewModels<PremiumViewModel> { viewModelFactory }

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiPremiumComponentProvider).providePremiumComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner) { render(it!!) }
      messageLiveData.observe(viewLifecycleOwner) { showSnack(it) }
    }
  }

  private fun setupView() {
    premiumToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    premiumRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      view.updatePadding(top = padding.top + insets.systemWindowInsetTop)
    }
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
  }

  private fun render(uiModel: PremiumUiModel) {
    uiModel.run {

    }
  }

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      remove()
      showNavigation()
      findNavControl().popBackStack()
    }
  }
}
