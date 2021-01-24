package com.michaldrabik.ui_premium

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_premium.di.UiPremiumComponentProvider
import com.michaldrabik.ui_premium.views.PurchaseItemView
import kotlinx.android.synthetic.main.fragment_premium.*

class PremiumFragment : BaseFragment<PremiumViewModel>(R.layout.fragment_premium) {

  override val viewModel by viewModels<PremiumViewModel> { viewModelFactory }

  private val billingClient: BillingClient by lazy {
    BillingClient.newBuilder(requireActivity())
      .setListener(purchasesUpdateListener)
      .enablePendingPurchases()
      .build()
  }

  private val purchasesUpdateListener =
    PurchasesUpdatedListener { billingResult, purchases ->
      viewModel.handlePurchase(billingClient, billingResult, purchases ?: mutableListOf())
    }

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
      loadBilling(billingClient)
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
      purchaseItems?.let { renderPurchaseItems(it, isLoading) }
      isLoading?.let {
        premiumProgress.visibleIf(it)
      }
      finishEvent?.let {
        it.consume()?.let {
          findNavControl().popBackStack()
        }
      }
    }
  }

  private fun renderPurchaseItems(items: List<SkuDetails>, isLoading: Boolean?) {
    premiumPurchaseItems.removeAllViews()
    premiumPurchaseItems.visibleIf(items.isNotEmpty() && isLoading != null && !isLoading)

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

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      remove()
      findNavControl().popBackStack()
    }
  }
}

