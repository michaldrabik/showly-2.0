package com.michaldrabik.ui_premium

import android.os.Bundle
import android.view.View
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_premium.views.PurchaseItemView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_premium.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PremiumFragment : BaseFragment<PremiumViewModel>(R.layout.fragment_premium) {

  override val viewModel by viewModels<PremiumViewModel>()

  private val billingClient: BillingClient by lazy {
    BillingClient.newBuilder(requireAppContext())
      .setListener(purchasesUpdateListener)
      .enablePendingPurchases()
      .build()
  }

  private val purchasesUpdateListener =
    PurchasesUpdatedListener { billingResult, purchases ->
      viewModel.handlePurchase(billingClient, billingResult, purchases ?: mutableListOf())
    }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { showSnack(it) } }
          loadBilling(billingClient)
        }
      }
    }
  }

  private fun setupView() {
    premiumToolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    premiumRoot.doOnApplyWindowInsets { view, insets, padding, _ ->
      view.updatePadding(top = padding.top + insets.systemWindowInsetTop)
    }
  }

  private fun render(uiState: PremiumUiState) {
    uiState.run {
      premiumProgress.visibleIf(isLoading)
      premiumStatus.visibleIf(isPurchasePending)
      purchaseItems?.let { renderPurchaseItems(it, isLoading) }
      onFinish?.let {
        it.consume()?.let {
          requireActivity().onBackPressed()
        }
      }
    }
  }

  private fun renderPurchaseItems(items: List<SkuDetails>, isLoading: Boolean) {
    premiumPurchaseItems.removeAllViews()
    premiumPurchaseItems.visibleIf(items.isNotEmpty() && !isLoading)

    if (items.isEmpty()) return
    items.forEach { item ->
      val view = PurchaseItemView(requireContext()).apply {
        bind(item)
        val flowParams = BillingFlowParams.newBuilder()
          .setSkuDetails(item)
          .build()
        onClick { billingClient.launchBillingFlow(requireActivity(), flowParams) }
      }
      premiumPurchaseItems.addView(view)
    }
  }
}
