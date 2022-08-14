package com.michaldrabik.ui_base.common.sheets.context_menu

import android.os.Bundle
import androidx.annotation.IdRes
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
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet.Mode
import com.michaldrabik.ui_base.databinding.ViewContextMenuBinding
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.requireBoolean
import com.michaldrabik.ui_base.utilities.extensions.requireParcelable
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.IdTvdb
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_LIST
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_ITEM_MENU
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_REMOVE_TRAKT
import com.michaldrabik.ui_navigation.java.NavigationArgs.RESULT

abstract class ContextMenuBottomSheet : BaseBottomSheetFragment(R.layout.view_context_menu) {

  companion object {
    fun createBundle(
      idTrakt: IdTrakt,
      showPinButtons: Boolean = false,
    ) = bundleOf(
      ARG_ID to idTrakt,
      ARG_LIST to showPinButtons
    )
  }

  protected val binding by viewBinding(ViewContextMenuBinding::bind)

  protected val itemId by lazy { requireParcelable<IdTrakt>(ARG_ID) }
  private val showPinButtons by lazy { requireBoolean(ARG_LIST) }

  private val cornerRadius by lazy { dimenToPx(R.dimen.mediaTileCorner).toFloat() }
  private val cornerBigRadius by lazy { dimenToPx(R.dimen.collectionItemCorner).toFloat() }
  private val centerCropTransformation by lazy { CenterCrop() }
  private val cornersTransformation by lazy { GranularRoundedCorners(cornerBigRadius, cornerRadius, cornerRadius, cornerRadius) }

  protected val colorAccent by lazy { ContextCompat.getColor(requireContext(), R.color.colorAccent) }
  protected val colorGray by lazy { ContextCompat.getColor(requireContext(), R.color.colorGrayLight) }

  protected abstract fun openDetails()

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  protected open fun setupView() {
    with(binding) {
      contextMenuItemDescription.setInitialLines(5)
      contextMenuItemPinButtonsLayout.visibleIf(showPinButtons)
      contextMenuItemSeparator2.visibleIf(showPinButtons)
      contextMenuItemImage.onClick { openDetails() }
      contextMenuItemPlaceholder.onClick { openDetails() }
    }
  }

  protected fun renderImage(image: Image, tvdbId: IdTvdb) {
    Glide.with(this).clear(binding.contextMenuItemImage)
    var imageUrl = image.fullFileUrl

    if (image.status == ImageStatus.UNAVAILABLE) {
      binding.contextMenuItemPlaceholder.visible()
      binding.contextMenuItemImage.gone()
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
        binding.contextMenuItemPlaceholder.gone()
        binding.contextMenuItemImage.visible()
      }
      .withFailListener {
        binding.contextMenuItemPlaceholder.visible()
        binding.contextMenuItemImage.gone()
      }
      .into(binding.contextMenuItemImage)
  }

  protected fun renderSnackbar(message: MessageEvent) {
    when (message) {
      is MessageEvent.Info -> binding.contextMenuItemSnackbarHost.showInfoSnackbar(getString(message.textRestId))
      is MessageEvent.Error -> binding.contextMenuItemSnackbarHost.showErrorSnackbar(getString(message.textRestId))
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
