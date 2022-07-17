package com.michaldrabik.ui_base.common.sheets.context_menu.show

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.viewModels
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.context_menu.ContextMenuBottomSheet
import com.michaldrabik.ui_base.common.sheets.context_menu.events.FinishUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet.Mode
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class ShowContextMenuBottomSheet : ContextMenuBottomSheet() {

  private val viewModel by viewModels<ShowContextMenuViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.messageFlow.collect { renderSnackbar(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } },
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = { viewModel.loadShow(itemId) }
    )
  }

  override fun setupView() {
    super.setupView()
    with(binding) {
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
      contextMenuItemAddOnHoldButton.onClick { viewModel.addToOnHoldPinned() }
      contextMenuItemRemoveOnHoldButton.onClick { viewModel.removeFromOnHoldPinned() }
    }
  }

  private fun render(uiState: ShowContextMenuUiState) {
    uiState.run {
      isLoading?.let {
        when {
          isLoading -> binding.contextMenuItemProgress.show()
          else -> binding.contextMenuItemProgress.hide()
        }
        binding.contextMenuItemButtonsLayout.visibleIf(!isLoading, gone = false)
      }
      item?.let {
        renderItem(it)
        renderImage(it.image, it.show.ids.tvdb)
      }
    }
  }

  private fun renderItem(item: ShowContextItem) {
    with(binding) {
      contextMenuItemTitle.text =
        if (item.translation?.title.isNullOrBlank()) item.show.title
        else item.translation?.title

      contextMenuItemDescription.text =
        if (item.translation?.overview.isNullOrBlank()) item.show.overview
        else item.translation?.overview

      contextMenuItemNetwork.text =
        if (item.show.year > 0) getString(R.string.textNetwork, item.show.network, item.show.year.toString())
        else String.format("%s", item.show.network)

      contextMenuRating.text = String.format(Locale.ENGLISH, "%.1f", item.show.rating)
      contextMenuRating.visibleIf(item.show.rating > 0)
      contextMenuRatingStar.visibleIf(item.show.rating > 0)

      contextMenuUserRating.text = String.format(Locale.ENGLISH, "%d", item.userRating)
      contextMenuUserRating.visibleIf(item.userRating != null)
      contextMenuUserRatingStar.visibleIf(item.userRating != null)

      contextMenuItemDescription.visibleIf(item.show.overview.isNotBlank())
      contextMenuItemNetwork.visibleIf(item.show.network.isNotBlank())

      contextMenuItemPinButton.visibleIf(!item.isPinnedTop)
      contextMenuItemUnpinButton.visibleIf(item.isPinnedTop)
      contextMenuItemAddOnHoldButton.visibleIf(!item.isOnHold)
      contextMenuItemRemoveOnHoldButton.visibleIf(item.isOnHold)

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
  }

  private fun handleEvent(event: Event<*>) {
    when (val result = event.peek()) {
      is RemoveTraktUiEvent -> when {
        result.removeProgress -> openRemoveTraktSheet(R.id.actionShowItemContextDialogToRemoveTraktProgress, Mode.SHOW)
        result.removeWatchlist -> openRemoveTraktSheet(R.id.actionShowItemContextDialogToRemoveTraktWatchlist, Mode.SHOW)
        result.removeHidden -> openRemoveTraktSheet(R.id.actionShowItemContextDialogToRemoveTraktHidden, Mode.SHOW)
        else -> close()
      }
      is FinishUiEvent -> if (result.isSuccess) close()
      else -> throw IllegalStateException()
    }
  }

  override fun openDetails() {
    val bundle = bundleOf(ARG_SHOW_ID to itemId.id)
    navigateTo(R.id.actionShowItemContextDialogToShowDetails, bundle)
  }
}
