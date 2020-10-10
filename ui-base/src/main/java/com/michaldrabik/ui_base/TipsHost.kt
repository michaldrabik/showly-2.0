package com.michaldrabik.ui_base

import com.michaldrabik.ui_model.Tip

interface TipsHost {
  fun isTipShown(tip: Tip): Boolean
  fun showTip(tip: Tip)
}
