package com.michaldrabik.ui_base.common.sheets.context_menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_ITEM_MENU

abstract class ContextMenuBottomSheet<T : BaseViewModel> : BaseBottomSheetFragment<T>() {

  companion object {
    fun createBundle(idTrakt: IdTrakt) =
      bundleOf(ARG_ID to idTrakt)
  }

  private val cornerRadius by lazy { dimenToPx(R.dimen.mediaTileCorner).toFloat() }
  private val cornerBigRadius by lazy { dimenToPx(R.dimen.collectionItemCorner).toFloat() }
  protected val centerCropTransformation by lazy { CenterCrop() }
  protected val cornersTransformation by lazy { GranularRoundedCorners(cornerBigRadius, cornerRadius, cornerRadius, cornerRadius) }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  protected fun dismissWithSuccess() {
    setFragmentResult(REQUEST_ITEM_MENU, Bundle.EMPTY)
    dismiss()
  }
}
