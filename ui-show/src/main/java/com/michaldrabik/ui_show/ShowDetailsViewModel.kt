package com.michaldrabik.ui_show

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseViewModel
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.common.OnlineStatusProvider
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.images.ShowImagesProvider
import com.michaldrabik.ui_base.notifications.AnnouncementManager
import com.michaldrabik.ui_base.trakt.quicksync.QuickSyncManager
import com.michaldrabik.ui_base.utilities.ActionEvent
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.launchDelayed
import com.michaldrabik.ui_base.utilities.extensions.replace
import com.michaldrabik.ui_episodes.EpisodesManager
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.EpisodeBundle
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.ImageType.POSTER
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.SeasonBundle
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.TraktRating
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_repository.SettingsRepository
import com.michaldrabik.ui_repository.UserTraktManager
import com.michaldrabik.ui_show.cases.ShowDetailsActorsCase
import com.michaldrabik.ui_show.cases.ShowDetailsArchiveCase
import com.michaldrabik.ui_show.cases.ShowDetailsCommentsCase
import com.michaldrabik.ui_show.cases.ShowDetailsEpisodesCase
import com.michaldrabik.ui_show.cases.ShowDetailsMainCase
import com.michaldrabik.ui_show.cases.ShowDetailsMyShowsCase
import com.michaldrabik.ui_show.cases.ShowDetailsRatingCase
import com.michaldrabik.ui_show.cases.ShowDetailsRelatedShowsCase
import com.michaldrabik.ui_show.cases.ShowDetailsTranslationCase
import com.michaldrabik.ui_show.cases.ShowDetailsWatchlistCase
import com.michaldrabik.ui_show.episodes.EpisodeListItem
import com.michaldrabik.ui_show.quickSetup.QuickSetupListItem
import com.michaldrabik.ui_show.related.RelatedListItem
import com.michaldrabik.ui_show.seasons.SeasonListItem
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates.notNull

class ShowDetailsViewModel @Inject constructor(
  private val mainCase: ShowDetailsMainCase,
  private val actorsCase: ShowDetailsActorsCase,
  private val translationCase: ShowDetailsTranslationCase,
  private val ratingsCase: ShowDetailsRatingCase,
  private val watchlistCase: ShowDetailsWatchlistCase,
  private val archiveCase: ShowDetailsArchiveCase,
  private val myShowsCase: ShowDetailsMyShowsCase,
  private val episodesCase: ShowDetailsEpisodesCase,
  private val commentsCase: ShowDetailsCommentsCase,
  private val relatedShowsCase: ShowDetailsRelatedShowsCase,
  private val settingsRepository: SettingsRepository,
  private val userManager: UserTraktManager,
  private val episodesManager: EpisodesManager,
  private val quickSyncManager: QuickSyncManager,
  private val announcementManager: AnnouncementManager,
  private val imagesProvider: ShowImagesProvider,
  private val dateFormatProvider: DateFormatProvider
) : BaseViewModel<ShowDetailsUiModel>() {

  private var show by notNull<Show>()
  private var areSeasonsLoaded = false
  private var areSeasonsLocal = false
  private val seasonItems = mutableListOf<SeasonListItem>()

  fun loadShowDetails(id: IdTrakt, context: Context) {
    viewModelScope.launch {
      val progressJob = launchDelayed(500) {
        uiState = ShowDetailsUiModel(showLoading = true)
      }
      try {
        show = mainCase.loadDetails(id)
        Analytics.logShowDetailsDisplay(show)

        val isSignedIn = userManager.isAuthorized()
        val isFollowed = async { myShowsCase.isMyShows(show) }
        val isWatchLater = async { watchlistCase.isWatchlist(show) }
        val isArchived = async { archiveCase.isArchived(show) }
        val followedState = FollowedState(
          isMyShows = isFollowed.await(),
          isWatchlist = isWatchLater.await(),
          isArchived = isArchived.await(),
          withAnimation = false
        )

        progressJob.cancel()
        uiState = ShowDetailsUiModel(
          show = show,
          showLoading = false,
          followedState = followedState,
          ratingState = RatingState(rateAllowed = isSignedIn, rateLoading = false),
          country = AppCountry.fromCode(settingsRepository.getCountry()),
          commentsDateFormat = dateFormatProvider.loadShortDayFormat()
        )

        launch { loadNextEpisode(show) }
        launch { loadBackgroundImage(show) }
        launch { loadActors(show) }
        launch {
          areSeasonsLoaded = false
          loadSeasons(show, (context as OnlineStatusProvider).isOnline())
          if (followedState.isMyShows) {
            announcementManager.refreshShowsAnnouncements(context)
          }
          areSeasonsLoaded = true
        }
        launch { loadRelatedShows(show) }
        launch { loadTranslation(show) }
        if (isSignedIn) launch { loadRating(show) }
      } catch (t: Throwable) {
        progressJob.cancel()
        _messageLiveData.value = MessageEvent.error(R.string.errorCouldNotLoadShow)
        Logger.record(t, "Source" to "ShowDetailsViewModel")
      }
    }
  }

  private suspend fun loadNextEpisode(show: Show) {
    try {
      val episode = episodesCase.loadNextEpisode(show.ids.trakt)
      val dateFormat = dateFormatProvider.loadFullHourFormat()
      episode?.let {
        uiState = ShowDetailsUiModel(nextEpisode = Pair(show, it), dateFormat = dateFormat)
        val translation = translationCase.loadTranslation(episode, show)
        if (translation?.title?.isNotBlank() == true) {
          val translated = it.copy(title = translation.title)
          uiState = ShowDetailsUiModel(nextEpisode = Pair(show, translated), dateFormat = dateFormat)
        }
      }
    } catch (t: Throwable) {
      Logger.record(t, "Source" to "${ShowDetailsViewModel::class.simpleName}::loadNextEpisode()")
    }
  }

  private suspend fun loadBackgroundImage(show: Show) {
    uiState = try {
      val backgroundImage = imagesProvider.loadRemoteImage(show, FANART)
      ShowDetailsUiModel(image = backgroundImage)
    } catch (t: Throwable) {
      ShowDetailsUiModel(image = Image.createUnavailable(FANART))
    }
  }

  private suspend fun loadActors(show: Show) {
    uiState = try {
      val actors = actorsCase.loadActors(show)
      ShowDetailsUiModel(actors = actors)
    } catch (t: Throwable) {
      ShowDetailsUiModel(actors = emptyList())
    }
  }

  private suspend fun loadSeasons(show: Show, isOnline: Boolean): List<Season> = try {
    val (seasons, isLocal) = episodesCase.loadSeasons(show, isOnline)
    areSeasonsLocal = isLocal
    val format = dateFormatProvider.loadFullHourFormat()
    val seasonsItems = seasons
      .map {
        val episodes = it.episodes.map { episode ->
          val rating = ratingsCase.loadRating(episode)
          val translation = translationCase.loadTranslation(episode, show, onlyLocal = true)
          EpisodeListItem(episode, it, false, translation, rating, format)
        }
        SeasonListItem(show, it, episodes, isWatched = false)
      }
      .sortedByDescending { it.season.number }

    val calculated = markWatchedEpisodes(seasonsItems)
    uiState = ShowDetailsUiModel(seasons = calculated)
    seasons
  } catch (t: Throwable) {
    uiState = ShowDetailsUiModel(seasons = emptyList())
    emptyList()
  }

  private suspend fun loadRelatedShows(show: Show) {
    uiState = try {
      val relatedShows = relatedShowsCase.loadRelatedShows(show).map {
        val image = imagesProvider.findCachedImage(it, POSTER)
        RelatedListItem(it, image)
      }
      ShowDetailsUiModel(relatedShows = relatedShows)
    } catch (t: Throwable) {
      ShowDetailsUiModel(relatedShows = emptyList())
    }
  }

  private suspend fun loadTranslation(show: Show) {
    try {
      val translation = translationCase.loadTranslation(show)
      translation?.let {
        uiState = ShowDetailsUiModel(translation = it)
      }
    } catch (error: Throwable) {
      Logger.record(error, "Source" to "${ShowDetailsViewModel::class.simpleName}::loadTranslation()")
    }
  }

  fun loadSeasonTranslation(seasonItem: SeasonListItem) {
    viewModelScope.launch {
      try {
        val translations = translationCase.loadTranslations(seasonItem.season, show)
        if (translations.isEmpty()) return@launch

        val episodes = seasonItem.episodes.toMutableList()
        translations.forEach { translation ->
          val episode = episodes.find { it.id == translation.ids.trakt.id }
          episode?.let { ep ->
            if (translation.overview.isNotBlank()) {
              val t = Translation(translation.title, translation.overview, translation.language)
              val withTranslation = ep.copy(translation = t)
              episodes.findReplace(withTranslation) { it.id == withTranslation.id }
            }
          }
        }

        val updatedItem = seasonItem.copy(episodes = episodes)
        seasonItems.findReplace(updatedItem) { it.id == updatedItem.id }
        uiState = ShowDetailsUiModel(seasonTranslation = ActionEvent(updatedItem))
      } catch (error: Throwable) {
        Logger.record(error, "Source" to "${ShowDetailsViewModel::class.simpleName}::loadSeasonTranslation()")
      }
    }
  }

  fun loadComments() {
    viewModelScope.launch {
      uiState = try {
        val comments = commentsCase.loadComments(show)
        ShowDetailsUiModel(comments = comments)
      } catch (t: Throwable) {
        ShowDetailsUiModel(comments = emptyList())
      }
    }
    Analytics.logShowCommentsClick(show)
  }

  fun loadMissingImage(item: RelatedListItem, force: Boolean) {

    fun updateItem(new: RelatedListItem) {
      val currentItems = uiState?.relatedShows?.toMutableList()
      currentItems?.findReplace(new) { it.isSameAs(new) }
      uiState = uiState?.copy(relatedShows = currentItems)
    }

    viewModelScope.launch {
      updateItem(item.copy(isLoading = true))
      try {
        val image = imagesProvider.loadRemoteImage(item.show, item.image.type, force)
        updateItem(item.copy(isLoading = false, image = image))
      } catch (t: Throwable) {
        updateItem(item.copy(isLoading = false, image = Image.createUnavailable(item.image.type)))
      }
    }
  }

  private suspend fun loadRating(show: Show) {
    try {
      uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = true))
      val rating = ratingsCase.loadRating(show)
      uiState = ShowDetailsUiModel(ratingState = RatingState(userRating = rating ?: TraktRating.EMPTY, rateLoading = false))
    } catch (error: Throwable) {
      Timber.e(error)
      uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = false))
    }
  }

  fun addRating(rating: Int) {
    viewModelScope.launch {
      try {
        uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = true))
        ratingsCase.addRating(show, rating)
        val userRating = TraktRating(show.ids.trakt, rating)
        uiState = ShowDetailsUiModel(ratingState = RatingState(userRating = userRating, rateLoading = false))
        _messageLiveData.value = MessageEvent.info(R.string.textShowRated)
        Analytics.logShowRated(show, rating)
      } catch (error: Throwable) {
        uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = false))
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }

  fun deleteRating() {
    viewModelScope.launch {
      try {
        uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = true))
        ratingsCase.deleteRating(show)
        uiState = ShowDetailsUiModel(ratingState = RatingState(userRating = TraktRating.EMPTY, rateLoading = false))
        _messageLiveData.value = MessageEvent.info(R.string.textShowRatingDeleted)
      } catch (error: Throwable) {
        uiState = ShowDetailsUiModel(ratingState = RatingState(rateLoading = false))
        _messageLiveData.value = MessageEvent.error(R.string.errorGeneral)
      }
    }
  }

  fun addFollowedShow(context: Context) {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      val seasons = seasonItems.map { it.season }
      val episodes = seasonItems.flatMap { it.episodes.map { e -> e.episode } }
      myShowsCase.addToMyShows(show, seasons, episodes)

      uiState = ShowDetailsUiModel(followedState = FollowedState.inMyShows())

      announcementManager.refreshShowsAnnouncements(context)
      Analytics.logShowAddToMyShows(show)
    }
  }

  fun addWatchlistShow(context: Context) {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      watchlistCase.addToWatchlist(show)
      quickSyncManager.scheduleShowsWatchlist(context, listOf(show.traktId))

      uiState = ShowDetailsUiModel(followedState = FollowedState.inWatchlist())

      Analytics.logShowAddToWatchlistShows(show)
    }
  }

  fun addArchiveShow() {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      archiveCase.addToArchive(show, removeLocalData = !areSeasonsLocal)
      uiState = ShowDetailsUiModel(followedState = FollowedState.inArchive())
      Analytics.logShowAddToArchive(show)
    }
  }

  fun removeFromFollowed(context: Context) {
    if (!checkSeasonsLoaded()) return
    viewModelScope.launch {
      val isMyShows = myShowsCase.isMyShows(show)
      val isWatchlist = watchlistCase.isWatchlist(show)
      val isArchived = archiveCase.isArchived(show)

      when {
        isMyShows -> {
          myShowsCase.removeFromMyShows(show, removeLocalData = !areSeasonsLocal)
        }
        isWatchlist -> {
          watchlistCase.removeFromWatchlist(show)
          quickSyncManager.clearShowsWatchlist(listOf(show.traktId))
        }
        isArchived -> {
          archiveCase.removeFromArchive(show)
        }
      }

      val traktQuickRemoveEnabled = settingsRepository.load().traktQuickRemoveEnabled
      val showRemoveTrakt = userManager.isAuthorized() && traktQuickRemoveEnabled && !areSeasonsLocal

      val state = FollowedState.notFollowed()
      val event = ActionEvent(showRemoveTrakt)
      uiState = when {
        isMyShows || isArchived -> ShowDetailsUiModel(followedState = state, removeFromTraktHistory = event)
        isWatchlist -> ShowDetailsUiModel(followedState = state, removeFromTraktWatchlist = event)
        else -> error("Unexpected show state")
      }

      announcementManager.refreshShowsAnnouncements(context)
    }
  }

  fun removeFromTraktHistory() {
    viewModelScope.launch {
      try {
        uiState = ShowDetailsUiModel(showFromTraktLoading = true)
        myShowsCase.removeTraktHistory(show)
        refreshWatchedEpisodes()
        _messageLiveData.value = MessageEvent.info(R.string.textTraktSyncRemovedFromTrakt)
        uiState = ShowDetailsUiModel(showFromTraktLoading = false, removeFromTraktHistory = ActionEvent(false))
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorTraktSyncGeneral)
        uiState = ShowDetailsUiModel(showFromTraktLoading = false)
      }
    }
  }

  fun removeFromTraktWatchlist() {
    viewModelScope.launch {
      try {
        uiState = ShowDetailsUiModel(showFromTraktLoading = true)
        watchlistCase.removeTraktWatchlist(show)
        _messageLiveData.value = MessageEvent.info(R.string.textTraktSyncRemovedFromTrakt)
        uiState = ShowDetailsUiModel(showFromTraktLoading = false, removeFromTraktWatchlist = ActionEvent(false))
      } catch (error: Throwable) {
        _messageLiveData.value = MessageEvent.error(R.string.errorTraktSyncGeneral)
        uiState = ShowDetailsUiModel(showFromTraktLoading = false)
      }
    }
  }

  fun setWatchedEpisode(
    context: Context,
    episode: Episode,
    season: Season,
    isChecked: Boolean
  ) {
    viewModelScope.launch {
      val bundle = EpisodeBundle(episode, season, show)
      when {
        isChecked -> {
          episodesManager.setEpisodeWatched(bundle)
          if (myShowsCase.isMyShows(show) || watchlistCase.isWatchlist(show) || archiveCase.isArchived(show)) {
            quickSyncManager.scheduleEpisodes(context, listOf(episode.ids.trakt.id))
          }
        }
        else -> episodesManager.setEpisodeUnwatched(bundle)
      }
      refreshWatchedEpisodes()
    }
  }

  fun setWatchedSeason(context: Context, season: Season, isChecked: Boolean) {
    viewModelScope.launch {
      val bundle = SeasonBundle(season, show)
      when {
        isChecked -> {
          val episodesAdded = episodesManager.setSeasonWatched(bundle)
          if (myShowsCase.isMyShows(show) || watchlistCase.isWatchlist(show) || archiveCase.isArchived(show)) {
            quickSyncManager.scheduleEpisodes(context, episodesAdded.map { it.ids.trakt.id })
          }
        }
        else -> episodesManager.setSeasonUnwatched(bundle)
      }
      refreshWatchedEpisodes()
    }
  }

  private fun checkSeasonsLoaded(): Boolean {
    if (!areSeasonsLoaded) {
      _messageLiveData.value = MessageEvent.info(R.string.errorSeasonsNotLoaded)
      return false
    }
    return true
  }

  fun refreshEpisodesRatings() {
    viewModelScope.launch {
      val items = seasonItems.map { seasonItem ->
        val episodes = seasonItem.episodes.map { episodeItem ->
          val rating = ratingsCase.loadRating(episodeItem.episode)
          episodeItem.copy(myRating = rating)
        }
        seasonItem.copy(episodes = episodes)
      }
      seasonItems.replace(items)
      uiState = ShowDetailsUiModel(seasons = items)
    }
  }

  private suspend fun refreshWatchedEpisodes() {
    val updatedSeasonItems = markWatchedEpisodes(seasonItems)
    uiState = ShowDetailsUiModel(seasons = updatedSeasonItems)
  }

  private suspend fun markWatchedEpisodes(seasonsList: List<SeasonListItem>): List<SeasonListItem> {
    val items = mutableListOf<SeasonListItem>()

    val watchedSeasonsIds = episodesManager.getWatchedSeasonsIds(show)
    val watchedEpisodesIds = episodesManager.getWatchedEpisodesIds(show)

    seasonsList.forEach { item ->
      val isSeasonWatched = watchedSeasonsIds.any { id -> id == item.id }
      val episodes = item.episodes.map { episodeItem ->
        val isEpisodeWatched = watchedEpisodesIds.any { id -> id == episodeItem.id }
        episodeItem.copy(season = item.season, isWatched = isEpisodeWatched)
      }
      val updated = item.copy(episodes = episodes, isWatched = isSeasonWatched)
      items.add(updated)
    }

    seasonItems.replace(items)
    return items
  }

  fun setQuickProgress(context: Context, item: QuickSetupListItem?) {
    if (item == null) return
    if (!areSeasonsLoaded) {
      _messageLiveData.value = MessageEvent.info(R.string.errorSeasonsNotLoaded)
      return
    }
    viewModelScope.launch {
      episodesManager.setAllUnwatched(show)
      val seasons = seasonItems.map { it.season }
      seasons
        .filter { it.number < item.season.number }
        .forEach { season ->
          setWatchedSeason(context, season, true)
        }

      val season = seasons.find { it.number == item.season.number }
      season?.episodes
        ?.filter { it.number <= item.episode.number }
        ?.forEach { episode ->
          setWatchedEpisode(context, episode, season, true)
        }

      _messageLiveData.value = MessageEvent.info(R.string.textShowQuickProgressDone)
      Analytics.logShowQuickProgress(show)
    }
  }
}
