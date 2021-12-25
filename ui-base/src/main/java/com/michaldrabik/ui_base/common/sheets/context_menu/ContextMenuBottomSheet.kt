package com.michaldrabik.ui_base.common.sheets.context_menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_ITEM_MENU
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_REMOVE_TRAKT
import com.michaldrabik.ui_navigation.java.NavigationArgs.RESULT

abstract class ContextMenuBottomSheet<T : BaseViewModel> : BaseBottomSheetFragment<T>() {

  companion object {
    fun createBundle(idTrakt: IdTrakt) =
      bundleOf(ARG_ID to idTrakt)
  }

  protected val itemId by lazy { requireArguments().getParcelable<IdTrakt>(ARG_ID)!! }

  private val cornerRadius by lazy { dimenToPx(R.dimen.mediaTileCorner).toFloat() }
  private val cornerBigRadius by lazy { dimenToPx(R.dimen.collectionItemCorner).toFloat() }
  protected val centerCropTransformation by lazy { CenterCrop() }
  protected val cornersTransformation by lazy { GranularRoundedCorners(cornerBigRadius, cornerRadius, cornerRadius, cornerRadius) }

  protected val colorAccent by lazy { ContextCompat.getColor(requireContext(), R.color.colorAccent) }
  protected val colorGray by lazy { ContextCompat.getColor(requireContext(), R.color.colorGrayLight) }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  protected fun openRemoveTraktSheet(@IdRes action: Int) {
    setFragmentResultListener(REQUEST_REMOVE_TRAKT) { _, bundle ->
      if (bundle.getBoolean(RESULT, false)) {
        val text = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, 1)
        (requireActivity() as SnackbarHost).provideSnackbarLayout().showInfoSnackbar(text)
      }
      close()
    }
    val args = bundleOf(ARG_ID to itemId.id, NavigationArgs.ARG_TYPE to Mode.SHOWS)
    navigateTo(action, args)
  }

  protected fun close() {
    setFragmentResult(REQUEST_ITEM_MENU, Bundle.EMPTY)
    closeSheet()
    dismiss()
  }
}
