package com.michaldrabik.ui_base.utilities

import com.michaldrabik.ui_model.Tip

interface TipsHost {
  fun isTipShown(tip: Tip): Boolean
  fun showTip(tip: Tip)
  fun setTipShow(tip: Tip)
}
