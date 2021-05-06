package com.michaldrabik.ui_premium.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.android.billingclient.api.SkuDetails
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_premium.R
import kotlinx.android.synthetic.main.view_purchase_item.view.*

@SuppressLint("SetTextI18n")
class PurchaseItemView : MaterialCardView {

  companion object {
    private const val INDIA_CURRENCY_CODE = "INR"
    private const val PERIOD_1_MONTH = "P1M"
    private const val PERIOD_1_YEAR = "P1Y"
  }

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_purchase_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setCardBackgroundColor(context.colorStateListFromAttr(R.attr.colorAccent))
  }

  fun bind(item: SkuDetails) {
    if (item.priceCurrencyCode == INDIA_CURRENCY_CODE) {
      bindForIndia(item)
      return
    }

    viewPurchaseItemTitle.text = item.title.substringBefore("(").trim()
    viewPurchaseItemDescription.text = "Try 7 days for free!"
    val period = when (item.subscriptionPeriod) {
      PERIOD_1_MONTH -> "month"
      PERIOD_1_YEAR -> "year"
      else -> ""
    }
    viewPurchaseItemDescriptionDetails.text =
      "You will be automatically enrolled in a paid subscription at the end of the free period. " +
      "Cancel anytime during free period if you do not want to convert to a paid subscription. " +
      "Subscription will be automatically renewed and charged every $period."
    viewPurchaseItemPrice.text = "${item.price} / $period"
  }

  /**
   * https://www.xda-developers.com/google-play-suspend-free-trials-auto-renewing-subscriptions/
   */
  private fun bindForIndia(item: SkuDetails) {
    viewPurchaseItemTitle.text = item.title.substringBefore("(").trim()
    viewPurchaseItemDescription.gone()
    viewPurchaseItemDescriptionDetails.gone()
    when (item.subscriptionPeriod) {
      PERIOD_1_MONTH -> viewPurchaseItemPrice.text = "${item.price} for month"
      PERIOD_1_YEAR -> viewPurchaseItemPrice.text = "${item.price} for year"
    }
  }
}
