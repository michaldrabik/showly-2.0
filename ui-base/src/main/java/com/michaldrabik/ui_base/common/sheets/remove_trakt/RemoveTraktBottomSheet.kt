package com.michaldrabik.ui_base.common.sheets.remove_trakt

import android.content.DialogInterface
import android.os.Parcelable
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModel
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.utilities.extensions.requireLongArray
import com.michaldrabik.ui_base.utilities.extensions.requireParcelable
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import kotlinx.android.parcel.Parcelize

abstract class RemoveTraktBottomSheet<T : ViewModel>(@LayoutRes layoutResId: Int) : BaseBottomSheetFragment(layoutResId) {

  companion object {
    fun createBundle(itemIds: List<IdTrakt>, mode: Mode) = bundleOf(
      ARG_ID to itemIds.map { it.id }.toLongArray(),
      ARG_TYPE to mode
    )

    fun createBundle(itemId: IdTrakt, mode: Mode) = createBundle(listOf(itemId), mode)
  }

  protected val itemIds: List<IdTrakt> by lazy { requireLongArray(ARG_ID).map { IdTrakt(it) } }
  protected val itemType by lazy { requireParcelable<Mode>(ARG_TYPE) }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

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
