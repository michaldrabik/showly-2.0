package com.michaldrabik.ui_base.common.sheets.context_menu.show

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.context_menu.ContextMenuBottomSheet
import com.michaldrabik.ui_base.common.sheets.context_menu.show.events.FinishEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.show.events.RemoveTraktEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.MessageEvent.Type
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_model.ImageStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_collection_item_context.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ShowContextMenuBottomSheet : ContextMenuBottomSheet<ShowContextMenuViewModel>() {

  override val layoutResId = R.layout.view_collection_item_context

  override fun createViewModel() = ViewModelProvider(this)[ShowContextMenuViewModel::class.java]

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.messageChannel.collect { renderSnackbar(it) } },
      { viewModel.eventChannel.collect { handleEvent(it) } },
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = { viewModel.loadShow(itemId) }
    )
  }

  private fun setupView() {
    contextMenuItemDescription.setInitialLines(5)
    contextMenuItemMoveToMyButton.text = getString(R.string.textMoveToMyShows)
    contextMenuItemRemoveFromMyButton.text = getString(R.string.textRemoveFromMyShows)
    contextMenuItemPinButtonsLayout.visibleIf(showPinButtons)

    contextMenuItemMoveToMyButton.onClick { viewModel.moveToMyShows() }
    contextMenuItemRemoveFromMyButton.onClick { viewModel.removeFromMyShows() }
    contextMenuItemMoveToWatchlistButton.onClick { viewModel.moveToWatchlist() }
    contextMenuItemRemoveFromWatchlistButton.onClick { viewModel.removeFromWatchlist() }
    contextMenuItemMoveToHiddenButton.onClick { viewModel.moveToHidden() }
    contextMenuItemRemoveFromHiddenButton.onClick { viewModel.removeFromHidden() }
    contextMenuItemPinButton.onClick { viewModel.addToTopPinned() }
    contextMenuItemUnpinButton.onClick { viewModel.removeFromTopPinned() }
  }

  private fun render(uiState: ShowContextMenuUiState) {
    uiState.run {
      isLoading?.let {
        contextMenuItemProgress.visibleIf(it)
        contextMenuItemButtonsLayout.visibleIf(!it, gone = false)
      }
      item?.let {
        renderItem(it)
        renderImage(it)
      }
    }
  }

  private fun renderItem(item: ShowContextItem) {
    contextMenuItemTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.show.title
      else item.translation?.title

    contextMenuItemDescription.text =
      if (item.translation?.overview.isNullOrBlank()) item.show.overview
      else item.translation?.overview

    contextMenuItemNetwork.text =
      if (item.show.year > 0) getString(R.string.textNetwork, item.show.network, item.show.year.toString())
      else String.format("%s", item.show.network)

    contextMenuItemDescription.visibleIf(item.show.overview.isNotBlank())
    contextMenuItemNetwork.visibleIf(item.show.network.isNotBlank())

    contextMenuItemPinButton.visibleIf(!item.isPinnedTop)
    contextMenuItemUnpinButton.visibleIf(item.isPinnedTop)

    contextMenuItemMoveToMyButton.visibleIf(!item.isMyShow)
    contextMenuItemMoveToWatchlistButton.visibleIf(!item.isWatchlist)
    contextMenuItemMoveToHiddenButton.visibleIf(!item.isHidden)

    contextMenuItemRemoveFromMyButton.visibleIf(item.isMyShow)
    contextMenuItemRemoveFromWatchlistButton.visibleIf(item.isWatchlist)
    contextMenuItemRemoveFromHiddenButton.visibleIf(item.isHidden)

    contextMenuItemBadge.visibleIf(item.isMyShow || item.isWatchlist)
    val color = if (item.isMyShow) colorAccent else colorGray
    ImageViewCompat.setImageTintList(contextMenuItemBadge, ColorStateList.valueOf(color))

    if (!item.isInCollection()) {
      contextMenuItemMoveToMyButton.text = getString(R.string.textAddToMyShows)
      contextMenuItemMoveToWatchlistButton.text = getString(R.string.textAddToWatchlist)
      contextMenuItemMoveToHiddenButton.text = getString(R.string.textHide)
    }
  }

  private fun renderImage(item: ShowContextItem) {
    Glide.with(this).clear(contextMenuItemImage)
    var imageUrl = item.image.fullFileUrl

    if (item.image.status == ImageStatus.UNAVAILABLE) {
      contextMenuItemPlaceholder.visible()
      contextMenuItemImage.gone()
      return
    }

    if (item.image.status == ImageStatus.UNKNOWN) {
      imageUrl = "${Config.TVDB_IMAGE_BASE_POSTER_URL}${item.show.ids.tvdb.id}-1.jpg"
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

  private fun renderSnackbar(message: MessageEvent) {
    message.consume()?.let {
      when (message.type) {
        Type.INFO -> contextMenuItemRoot.showInfoSnackbar(getString(it))
        Type.ERROR -> contextMenuItemRoot.showErrorSnackbar(getString(it))
      }
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (val result = event.peek()) {
      is RemoveTraktEvent -> when {
        result.removeProgress -> openRemoveTraktSheet(R.id.actionShowItemContextDialogToRemoveTraktProgress)
        result.removeWatchlist -> openRemoveTraktSheet(R.id.actionShowItemContextDialogToRemoveTraktWatchlist)
        result.removeHidden -> openRemoveTraktSheet(R.id.actionShowItemContextDialogToRemoveTraktHidden)
        else -> close()
      }
      is FinishEvent -> if (result.isSuccess) close()
      else -> throw IllegalStateException()
    }
  }
}
