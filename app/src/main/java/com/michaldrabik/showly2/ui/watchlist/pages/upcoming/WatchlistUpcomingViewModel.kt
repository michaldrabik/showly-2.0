package com.michaldrabik.showly2.ui.watchlist.pages.upcoming

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.base.BaseViewModel
import com.michaldrabik.showly2.ui.watchlist.WatchlistInteractor
import com.michaldrabik.showly2.ui.watchlist.WatchlistUiModel
import com.michaldrabik.showly2.ui.watchlist.pages.upcoming.WatchlistUpcomingViewModel.Section.LATER
import com.michaldrabik.showly2.ui.watchlist.pages.upcoming.WatchlistUpcomingViewModel.Section.NEXT_WEEK
import com.michaldrabik.showly2.ui.watchlist.pages.upcoming.WatchlistUpcomingViewModel.Section.THIS_WEEK
import com.michaldrabik.showly2.ui.watchlist.pages.upcoming.WatchlistUpcomingViewModel.Section.TODAY
import com.michaldrabik.showly2.ui.watchlist.pages.upcoming.WatchlistUpcomingViewModel.Section.TOMORROW
import com.michaldrabik.showly2.ui.watchlist.recycler.WatchlistItem
import com.michaldrabik.showly2.utilities.extensions.findReplace
import com.michaldrabik.showly2.utilities.extensions.nowUtc
import com.michaldrabik.showly2.utilities.extensions.toLocalTimeZone
import kotlinx.coroutines.launch
import org.threeten.bp.DayOfWeek.SUNDAY
import org.threeten.bp.LocalTime.NOON
import javax.inject.Inject

class WatchlistUpcomingViewModel @Inject constructor(
  private val interactor: WatchlistInteractor
) : BaseViewModel<WatchlistUpcomingUiModel>() {

  enum class Section(@StringRes val headerRes: Int) {
    TODAY(R.string.textWatchlistToday),
    TOMORROW(R.string.textWatchlistTomorrow),
    THIS_WEEK(R.string.textWatchlistThisWeek),
    NEXT_WEEK(R.string.textWatchlistNextWeek),
    LATER(R.string.textWatchlistLater)
  }

  fun handleParentAction(model: WatchlistUiModel) {
    val allItems = model.items?.toMutableList() ?: mutableListOf()

    val items = allItems
      .filter { it.upcomingEpisode != Episode.EMPTY }
      .sortedBy { it.upcomingEpisode.firstAired }
      .toMutableList()

    val groupedItems = groupByTime(items)

    uiState = WatchlistUpcomingUiModel(items = groupedItems)
  }

  private fun groupByTime(items: MutableList<WatchlistItem>): List<WatchlistItem> {
    val today = nowUtc().toLocalTimeZone()
    val nextWeekStart = today.plusDays(((SUNDAY.value - today.dayOfWeek.value) + 1L))

    val timeMap = mutableMapOf<Section, MutableList<WatchlistItem>>()
    val sectionsList = mutableListOf<WatchlistItem>()

    items.forEach { item ->
      val dateTime = item.upcomingEpisode.firstAired!!.toLocalTimeZone()
      when {
        dateTime.dayOfYear == today.dayOfYear ->
          timeMap.getOrPut(TODAY, { mutableListOf() }).add(item)
        dateTime.dayOfYear == today.plusDays(1).dayOfYear ->
          timeMap.getOrPut(TOMORROW, { mutableListOf() }).add(item)
        dateTime.with(NOON).isBefore(nextWeekStart.with(NOON)) ->
          timeMap.getOrPut(THIS_WEEK, { mutableListOf() }).add(item)
        dateTime.with(NOON).isBefore(nextWeekStart.plusWeeks(1).with(NOON)) ->
          timeMap.getOrPut(NEXT_WEEK, { mutableListOf() }).add(item)
        else ->
          timeMap.getOrPut(LATER, { mutableListOf() }).add(item)
      }
    }

    timeMap.entries.forEach { (section, items) ->
      sectionsList.run {
        add(WatchlistItem.EMPTY.copy(headerTextResId = section.headerRes))
        addAll(items.toList())
      }
    }

    return sectionsList
  }

  fun findMissingImage(item: WatchlistItem, force: Boolean) {

    fun updateItem(new: WatchlistItem) {
      val currentItems = uiState?.items?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = WatchlistUpcomingUiModel(items = currentItems)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = interactor.loadMissingImage(item.show, item.image.type, force)
        updateItem(item.copy(image = image, isLoading = false))
      } catch (t: Throwable) {
        val unavailable = Image.createUnavailable(item.image.type)
        updateItem(item.copy(image = unavailable, isLoading = false))
      }
    }
  }
}
