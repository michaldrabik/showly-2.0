package com.michaldrabik.ui_base.common.sheets.remove_trakt

import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModel
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import kotlinx.android.parcel.Parcelize

abstract class RemoveTraktBottomSheet<T : ViewModel> : BaseBottomSheetFragment<T>() {

  companion object {
    fun createBundle(itemIds: List<IdTrakt>, mode: Mode) = bundleOf(
      ARG_ID to itemIds.map { it.id }.toLongArray(),
      ARG_TYPE to mode
    )

    fun createBundle(itemId: IdTrakt, mode: Mode) = createBundle(listOf(itemId), mode)
  }

  protected val itemIds: List<IdTrakt> by lazy { requireArguments().getLongArray(ARG_ID)!!.map { IdTrakt(it) } }
  protected val itemType: Mode by lazy { requireArguments().getParcelable(ARG_TYPE)!! }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  override fun onCancel(dialog: DialogInterface) {
    setFragmentResult(NavigationArgs.REQUEST_REMOVE_TRAKT, bundleOf(NavigationArgs.RESULT to false))
    super.onCancel(dialog)
  }

  @Parcelize
  enum class Mode : Parcelable {
    SHOW,
    EPISODE,
    MOVIE
  }
}
