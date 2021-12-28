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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet.Mode
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.IdTvdb
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_LIST
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_ITEM_MENU
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_REMOVE_TRAKT
import com.michaldrabik.ui_navigation.java.NavigationArgs.RESULT
import kotlinx.android.synthetic.main.view_context_menu.*

abstract class ContextMenuBottomSheet<T : BaseViewModel> : BaseBottomSheetFragment<T>() {

  companion object {
    fun createBundle(
      idTrakt: IdTrakt,
      showPinButtons: Boolean = false
    ) = bundleOf(
      ARG_ID to idTrakt,
      ARG_LIST to showPinButtons
    )
  }

  override val layoutResId = R.layout.view_context_menu

  protected val itemId by lazy { requireArguments().getParcelable<IdTrakt>(ARG_ID)!! }
  private val showPinButtons by lazy { requireArguments().getBoolean(ARG_LIST, false) }

  private val cornerRadius by lazy { dimenToPx(R.dimen.mediaTileCorner).toFloat() }
  private val cornerBigRadius by lazy { dimenToPx(R.dimen.collectionItemCorner).toFloat() }
  private val centerCropTransformation by lazy { CenterCrop() }
  private val cornersTransformation by lazy { GranularRoundedCorners(cornerBigRadius, cornerRadius, cornerRadius, cornerRadius) }

  protected val colorAccent by lazy { ContextCompat.getColor(requireContext(), R.color.colorAccent) }
  protected val colorGray by lazy { ContextCompat.getColor(requireContext(), R.color.colorGrayLight) }

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(layoutResId, container, false)
  }

  protected open fun setupView() {
    contextMenuItemDescription.setInitialLines(5)
    contextMenuItemPinButtonsLayout.visibleIf(showPinButtons)
    contextMenuItemSeparator2.visibleIf(showPinButtons)
  }

  protected fun renderImage(image: Image, tvdbId: IdTvdb) {
    Glide.with(this).clear(contextMenuItemImage)
    var imageUrl = image.fullFileUrl

    if (image.status == ImageStatus.UNAVAILABLE) {
      contextMenuItemPlaceholder.visible()
      contextMenuItemImage.gone()
      return
    }

    if (image.status == ImageStatus.UNKNOWN) {
      imageUrl = "${Config.TVDB_IMAGE_BASE_POSTER_URL}${tvdbId.id}-1.jpg"
    }

    Glide.with(this)
      .load(imageUrl)
      .transform(centerCropTransformation, cornersTransformation)
      .transition(DrawableTransitionOptions.withCrossFade(Config.IMAGE_FADE_DURATION_MS))
      .withSuccessListener {
        contextMenuItemPlaceholder.gone()
        contextMenuItemImage.visible()
      }
      .withFailListener {
        contextMenuItemPlaceholder.visible()
        contextMenuItemImage.gone()
      }
      .into(contextMenuItemImage)
  }

  protected fun renderSnackbar(message: MessageEvent) {
    message.consume()?.let {
      when (message.type) {
        MessageEvent.Type.INFO -> contextMenuItemRoot.showInfoSnackbar(getString(it))
        MessageEvent.Type.ERROR -> contextMenuItemRoot.showErrorSnackbar(getString(it))
      }
    }
  }

  protected fun openRemoveTraktSheet(@IdRes action: Int, mode: Mode) {
    setFragmentResultListener(REQUEST_REMOVE_TRAKT) { _, bundle ->
      if (bundle.getBoolean(RESULT, false)) {
        val text = resources.getQuantityString(R.plurals.textTraktQuickSyncComplete, 1)
        (requireActivity() as SnackbarHost).provideSnackbarLayout().showInfoSnackbar(text)
      }
      close()
    }
    val args = RemoveTraktBottomSheet.createBundle(itemId, mode)
    navigateTo(action, args)
  }

  protected fun close() {
    setFragmentResult(REQUEST_ITEM_MENU, Bundle.EMPTY)
    closeSheet()
    dismiss()
  }
}
