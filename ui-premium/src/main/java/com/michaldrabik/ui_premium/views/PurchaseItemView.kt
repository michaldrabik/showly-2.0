package com.michaldrabik.ui_premium.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.content.ContextCompat
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.ProductDetails
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_premium.R
import com.michaldrabik.ui_premium.databinding.ViewPurchaseItemBinding

@SuppressLint("SetTextI18n")
class PurchaseItemView : MaterialCardView {

  companion object {
    private const val INDIA_CURRENCY_CODE = "INR"
    private const val PERIOD_1_MONTH = "P1M"
    private const val PERIOD_1_YEAR = "P1Y"
  }

  private val binding = ViewPurchaseItemBinding.inflate(LayoutInflater.from(context), this)

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    strokeWidth = 0
    setCardBackgroundColor(context.colorStateListFromAttr(R.attr.colorAccent))
  }

  fun bind(item: ProductDetails) {
//    if (item.priceCurrencyCode == INDIA_CURRENCY_CODE) {
//      bindForIndia(item)
//      return
//    }
    when (item.productType) {
      ProductType.SUBS -> bindSubscription(item)
      ProductType.INAPP -> bindInApp(item)
    }
  }

  private fun bindSubscription(item: ProductDetails) {
    with(binding) {
      viewPurchaseItemTitle.text = item.title.substringBefore("(").trim()
      viewPurchaseItemDescription.text = "Try 7 days for free and then:"
      val period = when (item.subscriptionOfferDetails?.firstOrNull()?.offerId) {
        PERIOD_1_MONTH -> "month"
        PERIOD_1_YEAR -> "year"
        else -> ""
      }
      viewPurchaseItemDescriptionDetails.text =
        "You will be automatically enrolled in a paid subscription at the end of the free period. " +
          "Cancel anytime during free period if you do not want to convert to a paid subscription. " +
          "Subscription will be automatically renewed and charged every $period."
//      viewPurchaseItemPrice.text = "${item.price} / $period"
    }
  }

  private fun bindInApp(item: ProductDetails) {
    with(binding) {
      viewPurchaseItemTitle.text = item.title.substringBefore("(").trim()
      viewPurchaseItemDescription.text = "Pay once, unlock forever!"
      viewPurchaseItemDescriptionDetails.text =
        "You will unlock all bonus features with a single payment and enjoy them forever."
      viewPurchaseItemPrice.text = item.oneTimePurchaseOfferDetails?.formattedPrice

      val colorBlack = ContextCompat.getColor(context, R.color.colorBlack)
      val colorWhite = ContextCompat.getColor(context, R.color.colorWhite)

      viewPurchaseItemTitle.setTextColor(colorBlack)
      viewPurchaseItemDescription.setTextColor(colorBlack)
      viewPurchaseItemDescriptionDetails.setTextColor(colorBlack)
      viewPurchaseItemPrice.setTextColor(colorBlack)
      viewPurchaseItemSeparator.setBackgroundColor(colorBlack)
      setCardBackgroundColor(colorWhite)
    }
  }

  /**
   * https://www.xda-developers.com/google-play-suspend-free-trials-auto-renewing-subscriptions/
   */
  private fun bindForIndia(item: ProductDetails) {
    with(binding) {
      viewPurchaseItemTitle.text = item.title.substringBefore("(").trim()
      viewPurchaseItemDescription.gone()
      viewPurchaseItemDescriptionDetails.gone()
      viewPurchaseItemSeparator.gone()
//      when (item.subscriptionPeriod) {
//        PERIOD_1_MONTH -> viewPurchaseItemPrice.text = "${item.price} for month"
//        PERIOD_1_YEAR -> viewPurchaseItemPrice.text = "${item.price} for year"
//      }
    }
  }
}
