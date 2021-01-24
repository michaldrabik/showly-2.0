package com.michaldrabik.ui_premium.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.android.billingclient.api.SkuDetails
import com.google.android.material.card.MaterialCardView
import com.michaldrabik.ui_base.utilities.extensions.colorStateListFromAttr
import com.michaldrabik.ui_premium.R
import kotlinx.android.synthetic.main.view_purchase_item.view.*

class PurchaseItemView : MaterialCardView {

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

  init {
    inflate(context, R.layout.view_purchase_item, this)
    layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    setCardBackgroundColor(context.colorStateListFromAttr(R.attr.colorAccent))
  }

  fun bind(item: SkuDetails) {
    viewPurchaseItemTitle.text = item.title.substringBefore("(").trim()
    viewPurchaseItemDescription.text = item.description
    viewPurchaseItemPrice.text = "${item.price}"
  }
}
