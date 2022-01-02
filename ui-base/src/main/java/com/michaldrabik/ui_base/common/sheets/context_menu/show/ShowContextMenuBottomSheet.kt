package com.michaldrabik.ui_base.common.sheets.context_menu.show

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.context_menu.ContextMenuBottomSheet
import com.michaldrabik.ui_base.common.sheets.context_menu.events.FinishUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_context_menu.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ShowContextMenuBottomSheet : ContextMenuBottomSheet<ShowContextMenuViewModel>() {

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

  override fun setupView() {
    super.setupView()
    contextMenuItemMoveToMyButton.text = getString(R.string.textMoveToMyShows)
    contextMenuItemRemoveFromMyButton.text = getString(R.string.textRemoveFromMyShows)

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
        renderImage(it.image, it.show.ids.tvdb)
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

  private fun handleEvent(event: Event<*>) {
    when (val result = event.peek()) {
      is RemoveTraktUiEvent -> when {
        result.removeProgress -> openRemoveTraktSheet(R.id.actionShowItemContextDialogToRemoveTraktProgress, Mode.SHOWS)
        result.removeWatchlist -> openRemoveTraktSheet(R.id.actionShowItemContextDialogToRemoveTraktWatchlist, Mode.SHOWS)
        result.removeHidden -> openRemoveTraktSheet(R.id.actionShowItemContextDialogToRemoveTraktHidden, Mode.SHOWS)
        else -> close()
      }
      is FinishUiEvent -> if (result.isSuccess) close()
      else -> throw IllegalStateException()
    }
  }
}