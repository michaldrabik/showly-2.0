package com.michaldrabik.ui_base.common.sheets.context_menu.movie

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.ViewModelProvider
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.context_menu.ContextMenuBottomSheet
import com.michaldrabik.ui_base.common.sheets.context_menu.events.FinishUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.events.RemoveTraktUiEvent
import com.michaldrabik.ui_base.common.sheets.context_menu.movie.helpers.MovieContextItem
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet.Mode
import com.michaldrabik.ui_base.utilities.Event
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_context_menu.*
import kotlinx.coroutines.flow.collect
import java.util.Locale

@AndroidEntryPoint
class MovieContextMenuBottomSheet : ContextMenuBottomSheet<MovieContextMenuViewModel>() {

  override fun createViewModel() = ViewModelProvider(this)[MovieContextMenuViewModel::class.java]

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.messageChannel.collect { renderSnackbar(it) } },
      { viewModel.eventChannel.collect { handleEvent(it) } },
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = { viewModel.loadMovie(itemId) }
    )
  }

  override fun setupView() {
    super.setupView()
    contextMenuItemMoveToMyButton.text = getString(R.string.textMoveToMyMovies)
    contextMenuItemRemoveFromMyButton.text = getString(R.string.textRemoveFromMyMovies)

    contextMenuItemMoveToMyButton.onClick { viewModel.moveToMyMovies() }
    contextMenuItemRemoveFromMyButton.onClick { viewModel.removeFromMyMovies() }
    contextMenuItemMoveToWatchlistButton.onClick { viewModel.moveToWatchlist() }
    contextMenuItemRemoveFromWatchlistButton.onClick { viewModel.removeFromWatchlist() }
    contextMenuItemMoveToHiddenButton.onClick { viewModel.moveToHidden() }
    contextMenuItemRemoveFromHiddenButton.onClick { viewModel.removeFromHidden() }
    contextMenuItemPinButton.onClick { viewModel.addToTopPinned() }
    contextMenuItemUnpinButton.onClick { viewModel.removeFromTopPinned() }
  }

  private fun render(uiState: MovieContextMenuUiState) {
    uiState.run {
      isLoading?.let {
        contextMenuItemProgress.visibleIf(it)
        contextMenuItemButtonsLayout.visibleIf(!it, gone = false)
      }
      item?.let {
        renderItem(it)
        renderImage(it.image, it.movie.ids.tvdb)
      }
    }
  }

  private fun renderItem(item: MovieContextItem) {
    contextMenuItemTitle.text =
      if (item.translation?.title.isNullOrBlank()) item.movie.title
      else item.translation?.title

    contextMenuItemDescription.text =
      if (item.translation?.overview.isNullOrBlank()) item.movie.overview
      else item.translation?.overview

    contextMenuItemNetwork.text = when {
      item.movie.released != null -> item.dateFormat?.format(item.movie.released)?.capitalizeWords()
      else -> String.format(Locale.ENGLISH, "%d", item.movie.year)
    }

    contextMenuItemDescription.visibleIf(item.movie.overview.isNotBlank())
    contextMenuItemNetwork.visibleIf(item.movie.released != null || item.movie.year > 0)

    contextMenuItemPinButton.visibleIf(!item.isPinnedTop)
    contextMenuItemUnpinButton.visibleIf(item.isPinnedTop)

    contextMenuItemMoveToMyButton.visibleIf(!item.isMyMovie)
    contextMenuItemMoveToWatchlistButton.visibleIf(!item.isWatchlist)
    contextMenuItemMoveToHiddenButton.visibleIf(!item.isHidden)

    contextMenuItemRemoveFromMyButton.visibleIf(item.isMyMovie)
    contextMenuItemRemoveFromWatchlistButton.visibleIf(item.isWatchlist)
    contextMenuItemRemoveFromHiddenButton.visibleIf(item.isHidden)

    contextMenuItemBadge.visibleIf(item.isMyMovie || item.isWatchlist)
    val color = if (item.isMyMovie) colorAccent else colorGray
    ImageViewCompat.setImageTintList(contextMenuItemBadge, ColorStateList.valueOf(color))

    if (!item.isInCollection()) {
      contextMenuItemMoveToMyButton.text = getString(R.string.textAddToMyMovies)
      contextMenuItemMoveToWatchlistButton.text = getString(R.string.textAddToWatchlist)
      contextMenuItemMoveToHiddenButton.text = getString(R.string.textHide)
    }
  }

  private fun handleEvent(event: Event<*>) {
    when (val result = event.peek()) {
      is RemoveTraktUiEvent -> when {
        result.removeProgress -> openRemoveTraktSheet(R.id.actionMovieItemContextDialogToRemoveTraktProgress, Mode.MOVIE)
        result.removeWatchlist -> openRemoveTraktSheet(R.id.actionMovieItemContextDialogToRemoveTraktWatchlist, Mode.MOVIE)
        result.removeHidden -> openRemoveTraktSheet(R.id.actionMovieItemContextDialogToRemoveTraktHidden, Mode.MOVIE)
        else -> close()
      }
      is FinishUiEvent -> if (result.isSuccess) close()
      else -> throw IllegalStateException()
    }
  }
}
