package com.michaldrabik.ui_base.common.sheets.context_menu.show

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.common.Config
import com.michaldrabik.ui_base.R
import com.michaldrabik.ui_base.common.sheets.context_menu.ContextMenuBottomSheet
import com.michaldrabik.ui_base.common.sheets.context_menu.show.helpers.ShowContextItem
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
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageStatus
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_collection_item_context.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ShowContextMenuBottomSheet : ContextMenuBottomSheet<ShowContextMenuViewModel>() {

  override val layoutResId = R.layout.view_collection_item_context

  private val showId by lazy { requireArguments().getParcelable<IdTrakt>(ARG_ID)!! }

  override fun createViewModel() = ViewModelProvider(this)[ShowContextMenuViewModel::class.java]

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()

    launchAndRepeatStarted(
      { viewModel.messageChannel.collect { renderSnackbar(it) } },
      { viewModel.uiState.collect { render(it) } },
      doAfterLaunch = { viewModel.loadShow(showId) }
    )
  }

  private fun setupView() {
    contextMenuItemDescription.setInitialLines(5)
    contextMenuItemMoveToMyButton.text = getString(R.string.textMoveToMyShows)
    contextMenuItemRemoveFromMyButton.text = getString(R.string.textRemoveFromMyShows)

    contextMenuItemMoveToMyButton.onClick { viewModel.moveToMyShows() }
    contextMenuItemRemoveFromMyButton.onClick { viewModel.removeFromMyShows() }
    contextMenuItemMoveToWatchlistButton.onClick { viewModel.moveToWatchlist() }
    contextMenuItemRemoveFromWatchlistButton.onClick { viewModel.removeFromWatchlist() }
    contextMenuItemMoveToHiddenButton.onClick { viewModel.moveToHidden() }
    contextMenuItemRemoveFromHiddenButton.onClick { viewModel.removeFromHidden() }
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: ShowContextMenuUiState) {
    uiState.run {
      isFinished?.let {
        if (it) {
          dismissWithSuccess()
          return@run
        }
      }
      isLoading?.let {
        contextMenuItemProgress.visibleIf(it)
        contextMenuItemButtonsLayout.visibleIf(!it, gone = false)
      }
      item?.let {
        renderItem(it)
        renderImage(it.image)
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

    contextMenuItemMoveToMyButton.visibleIf(!item.isMyShow)
    contextMenuItemMoveToWatchlistButton.visibleIf(!item.isWatchlist)
    contextMenuItemMoveToHiddenButton.visibleIf(!item.isHidden)

    contextMenuItemRemoveFromMyButton.visibleIf(item.isMyShow)
    contextMenuItemRemoveFromWatchlistButton.visibleIf(item.isWatchlist)
    contextMenuItemRemoveFromHiddenButton.visibleIf(item.isHidden)

    if (!item.isInCollection()) {
      contextMenuItemMoveToMyButton.text = getString(R.string.textAddToMyShows)
      contextMenuItemMoveToWatchlistButton.text = getString(R.string.textAddToWatchlist)
      contextMenuItemMoveToHiddenButton.text = getString(R.string.textAddToHidden)
    }
  }

  private fun renderImage(image: Image) {
    Glide.with(this).clear(contextMenuItemImage)

    if (image.status != ImageStatus.AVAILABLE) {
      contextMenuItemPlaceholder.visible()
      contextMenuItemImage.gone()
      return
    }

    Glide.with(this)
      .load(image.fullFileUrl)
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
}
