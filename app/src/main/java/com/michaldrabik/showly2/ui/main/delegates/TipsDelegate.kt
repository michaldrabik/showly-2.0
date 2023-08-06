package com.michaldrabik.showly2.ui.main.delegates

import androidx.lifecycle.DefaultLifecycleObserver
import com.michaldrabik.showly2.databinding.ActivityMainBinding
import com.michaldrabik.showly2.ui.main.MainViewModel
import com.michaldrabik.ui_base.utilities.TipsHost
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_model.Tip

interface TipsDelegate : TipsHost {
  fun registerTipsDelegate(
    viewModel: MainViewModel,
    binding: ActivityMainBinding,
  )

  fun showAllTips()
  fun hideAllTips()
}

class MainTipsDelegate : TipsDelegate, DefaultLifecycleObserver {

  private lateinit var viewModel: MainViewModel
  private lateinit var binding: ActivityMainBinding

  private val tips by lazy {
    mapOf(
      Tip.MENU_DISCOVER to binding.tutorialTipDiscover,
      Tip.MENU_MY_SHOWS to binding.tutorialTipMyShows,
      Tip.MENU_MODES to binding.tutorialTipModeMenu
    )
  }

  override fun registerTipsDelegate(
    viewModel: MainViewModel,
    binding: ActivityMainBinding,
  ) {
    this.viewModel = viewModel
    this.binding = binding
    setupTips()
  }

  private fun setupTips() {
    tips.entries.forEach { (tip, view) ->
      view.visibleIf(!isTipShown(tip))
      view.onClick {
        it.gone()
        showTip(tip)
      }
    }
  }

  override fun setTipShow(tip: Tip) = viewModel.setTipShown(tip)

  override fun isTipShown(tip: Tip) = viewModel.isTipShown(tip)

  override fun showTip(tip: Tip) {
    binding.tutorialView.showTip(tip)
    setTipShow(tip)
  }

  override fun showAllTips() {
    tips.entries.forEach { (tip, view) -> view.visibleIf(!isTipShown(tip)) }
  }

  override fun hideAllTips() {
    tips.values.forEach { it.gone() }
  }
}
