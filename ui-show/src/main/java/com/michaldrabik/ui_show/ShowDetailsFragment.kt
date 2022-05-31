package com.michaldrabik.ui_show

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.Mode
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.common.AppCountry.UNITED_STATES
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.common.sheets.links.LinksBottomSheet
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Operation.REMOVE
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Operation.SAVE
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Type
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.copyToClipboard
import com.michaldrabik.ui_base.utilities.extensions.crossfadeTo
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.requireLong
import com.michaldrabik.ui_base.utilities.extensions.screenHeight
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_base.utilities.extensions.setTextIfEmpty
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.trimWithSuffix
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_comments.fragment.CommentsFragment
import com.michaldrabik.ui_episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Tip.SHOW_DETAILS_GALLERY
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_TAB_SELECTED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_WATCHED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_RATING_CHANGED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_CUSTOM_IMAGE_CLEARED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_FAMILY
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CUSTOM_IMAGE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_EPISODE_DETAILS
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_MANAGE_LISTS
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_PERSON_DETAILS
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_REMOVE_TRAKT
import com.michaldrabik.ui_people.details.PersonDetailsBottomSheet
import com.michaldrabik.ui_people.list.PeopleListBottomSheet
import com.michaldrabik.ui_show.ShowDetailsEvent.Finish
import com.michaldrabik.ui_show.ShowDetailsEvent.RemoveFromTrakt
import com.michaldrabik.ui_show.actors.ActorsAdapter
import com.michaldrabik.ui_show.helpers.NextEpisodeBundle
import com.michaldrabik.ui_show.helpers.ShowLink
import com.michaldrabik.ui_show.quick_setup.QuickSetupView
import com.michaldrabik.ui_show.related.RelatedListItem
import com.michaldrabik.ui_show.related.RelatedShowAdapter
import com.michaldrabik.ui_show.seasons.SeasonListItem
import com.michaldrabik.ui_show.seasons.SeasonsAdapter
import com.michaldrabik.ui_show.views.AddToShowsButton
import com.michaldrabik.ui_streamings.recycler.StreamingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_show_details.*
import kotlinx.android.synthetic.main.fragment_show_details_next_episode.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.time.Duration
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
@AndroidEntryPoint
class ShowDetailsFragment : BaseFragment<ShowDetailsViewModel>(R.layout.fragment_show_details) {

  override val viewModel by viewModels<ShowDetailsViewModel>()
  override val navigationId = R.id.showDetailsFragment

  private val showId by lazy { IdTrakt(requireLong(ARG_SHOW_ID)) }

  private var actorsAdapter: ActorsAdapter? = null
  private var relatedAdapter: RelatedShowAdapter? = null
  private var seasonsAdapter: SeasonsAdapter? = null
  private var streamingAdapter: StreamingAdapter? = null
  private var lastOpenedPerson: Person? = null

  private val imageHeight by lazy {
    if (resources.configuration.orientation == ORIENTATION_PORTRAIT) screenHeight()
    else screenWidth()
  }
  private val imageRatio by lazy { resources.getString(R.string.detailsImageRatio).toFloat() }
  private val imagePadded by lazy { resources.getBoolean(R.bool.detailsImagePadded) }

  private val animationEnterRight by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_in_from_right) }
  private val animationExitRight by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_out_from_right) }
  private val animationEnterLeft by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_in_from_left) }
  private val animationExitLeft by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_out_from_left) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
    setupView()
    setupStatusBar()
    setupActorsList()
    setupRelatedList()
    setupSeasonsList()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } },
      { viewModel.messageFlow.collect { renderSnack(it) } },
      doAfterLaunch = {
        if (!isInitialized) {
          viewModel.loadDetails(showId)
          isInitialized = true
        }
        viewModel.loadPremium()
        lastOpenedPerson?.let { openPersonSheet(it) }
      }
    )
  }

  private fun setupView() {
    hideNavigation()
    showDetailsImageGuideline.setGuidelineBegin((imageHeight * imageRatio).toInt())
    showDetailsEpisodesView.itemClickListener = { show, episode, season, isWatched ->
      openEpisodeDetails(show, episode, season, isWatched, episode.hasAired(season))
    }
    listOf(showDetailsBackArrow, showDetailsBackArrow2).onClick { requireActivity().onBackPressed() }
    showDetailsImage.onClick {
      val bundle = bundleOf(
        ARG_SHOW_ID to showId.id,
        ARG_FAMILY to SHOW,
        ARG_TYPE to FANART
      )
      navigateToSafe(R.id.actionShowDetailsFragmentToArtGallery, bundle)
      Analytics.logShowGalleryClick(showId.id)
    }
    showDetailsTipGallery.onClick {
      it.gone()
      showTip(SHOW_DETAILS_GALLERY)
    }
    showDetailsAddButton.run {
      isEnabled = false
      onAddMyShowsClickListener = { viewModel.addFollowedShow() }
      onAddWatchlistClickListener = { viewModel.addWatchlistShow() }
      onRemoveClickListener = { viewModel.removeFromFollowed() }
    }
    showDetailsManageListsLabel.onClick { openListsDialog() }
    showDetailsHideLabel.onClick { viewModel.addHiddenShow() }
    showDetailsTitle.onClick {
      requireContext().copyToClipboard(showDetailsTitle.text.toString())
      showSnack(MessageEvent.Info(R.string.textCopiedToClipboard))
    }
    showDetailsPremiumAd.onClick {
      navigateToSafe(R.id.actionShowDetailsFragmentToPremium)
    }
  }

  private fun setupStatusBar() {
    showDetailsBackArrow.doOnApplyWindowInsets { view, insets, _, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      if (imagePadded) {
        showDetailsMainLayout.updatePadding(top = inset)
      } else {
        (showDetailsShareButton.layoutParams as MarginLayoutParams)
          .updateMargins(top = inset)
      }
      arrayOf<View>(view, showDetailsBackArrow2, showDetailsEpisodesView)
        .forEach { v ->
          (v.layoutParams as MarginLayoutParams).updateMargins(top = inset)
        }
    }
  }

  private fun setupActorsList() {
    actorsAdapter = ActorsAdapter().apply {
      itemClickListener = { openPersonSheet(it) }
    }
    showDetailsActorsRecycler.apply {
      setHasFixedSize(true)
      adapter = actorsAdapter
      layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
  }

  private fun setupRelatedList() {
    relatedAdapter = RelatedShowAdapter(
      itemClickListener = {
        if (findNavControl()?.currentDestination?.id == R.id.showDetailsFragment) {
          val bundle = Bundle().apply { putLong(ARG_SHOW_ID, it.show.traktId) }
          navigateToSafe(R.id.actionShowDetailsFragmentToSelf, bundle)
        }
      },
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    )
    showDetailsRelatedRecycler.apply {
      setHasFixedSize(true)
      adapter = relatedAdapter
      layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
  }

  private fun setupSeasonsList() {
    seasonsAdapter = SeasonsAdapter().apply {
      itemClickListener = { showEpisodesView(it) }
      itemCheckedListener = { item: SeasonListItem, isChecked: Boolean ->
        viewModel.setSeasonWatched(item.season, isChecked, removeTrakt = true)
      }
    }
    showDetailsSeasonsRecycler.apply {
      adapter = seasonsAdapter
      layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
      itemAnimator = null
    }
  }

  private fun showEpisodesView(item: SeasonListItem) {
    showDetailsEpisodesView.run {
      bind(item)
      fadeIn(265, withHardware = true) {
        bindEpisodes(item.episodes)
        viewModel.loadSeasonTranslation(item)
      }
      startAnimation(animationEnterRight)
      itemCheckedListener = { episode, season, isChecked ->
        viewModel.setEpisodeWatched(episode, season, isChecked, removeTrakt = true)
      }
      seasonCheckedListener = { season, isChecked ->
        viewModel.setSeasonWatched(season, isChecked, removeTrakt = true)
      }
      rateClickListener = { season ->
        openRateSeasonDialog(season)
      }
    }
    showDetailsMainLayout.run {
      fadeOut(200)
      startAnimation(animationExitRight)
    }
    showDetailsBackArrow.crossfadeTo(showDetailsBackArrow2)
  }

  private fun hideExtraView(view: View) {
    if (view.animation != null) return

    view.run {
      fadeOut(300)
      startAnimation(animationExitLeft)
    }
    showDetailsMainLayout.run {
      fadeIn(withHardware = true)
      startAnimation(animationEnterLeft)
    }
    showDetailsBackArrow2.crossfadeTo(showDetailsBackArrow)

    viewModel.refreshAnnouncements()
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is Finish -> requireActivity().onBackPressed()
      is RemoveFromTrakt -> openRemoveTraktSheet(event)
    }
  }

  private fun render(uiState: ShowDetailsUiState) {
    uiState.run {
      show?.let { show ->
        showDetailsTitle.text = show.title
        showDetailsDescription.setTextIfEmpty(show.overview)
        showDetailsStatus.text = getString(show.status.displayName)
        val year = if (show.year > 0) String.format(ENGLISH, "%d", show.year) else ""
        val country = if (show.country.isNotBlank()) String.format(ENGLISH, "(%s)", show.country) else ""
        showDetailsExtraInfo.text = getString(
          R.string.textShowExtraInfo,
          show.network,
          year,
          country.uppercase(),
          show.runtime.toString(),
          getString(R.string.textMinutesShort),
          renderGenres(show.genres)
        )
        showDetailsCommentsButton.visible()
        showDetailsShareButton.run {
          isEnabled = show.ids.imdb.id.isNotBlank()
          alpha = if (isEnabled) 1.0F else 0.35F
          onClick { openShareSheet(show) }
        }
        showDetailsTrailerButton.run {
          isEnabled = show.trailer.isNotBlank()
          alpha = if (isEnabled) 1.0F else 0.35F
          onClick {
            openWebUrl(show.trailer) ?: showSnack(MessageEvent.Info(R.string.errorCouldNotFindApp))
            Analytics.logShowTrailerClick(show)
          }
        }
        showDetailsCustomImagesLabel.visibleIf(Config.SHOW_PREMIUM)
        showDetailsCustomImagesLabel.onClick { openCustomImagesSheet(show.traktId, isPremium) }
        showDetailsLinksButton.onClick {
          val args = LinksBottomSheet.createBundle(show)
          navigateToSafe(R.id.actionShowDetailsFragmentToLinks, args)
        }
        showDetailsCommentsButton.onClick {
          val bundle = CommentsFragment.createBundle(show)
          navigateToSafe(R.id.actionShowDetailsFragmentToComments, bundle)
        }
        showDetailsAddButton.isEnabled = true
        separator4.visible()
      }
      showLoading?.let {
        if (!showDetailsEpisodesView.isVisible) {
          showDetailsMainLayout.fadeIf(!it, hardware = true)
          showDetailsMainProgress.visibleIf(it)
        }
      }
      followedState?.let {
        when {
          it.isMyShows -> showDetailsAddButton.setState(AddToShowsButton.State.IN_MY_SHOWS, it.withAnimation)
          it.isWatchlist -> showDetailsAddButton.setState(AddToShowsButton.State.IN_WATCHLIST, it.withAnimation)
          it.isHidden -> showDetailsAddButton.setState(AddToShowsButton.State.IN_HIDDEN, it.withAnimation)
          else -> showDetailsAddButton.setState(AddToShowsButton.State.ADD, it.withAnimation)
        }
        showDetailsHideLabel.visibleIf(!it.isHidden)
      }
      listsCount?.let {
        val text =
          if (it > 0) getString(R.string.textShowManageListsCount, it)
          else getString(R.string.textShowManageLists)
        showDetailsManageListsLabel.text = text
      }
      image?.let { renderImage(it) }
      seasons?.let {
        renderSeasons(it)
        renderRuntimeLeft(it)
        (requireAppContext() as WidgetsProvider).requestShowsWidgetsUpdate()
      }
      ratings?.let { renderRatings(it, show) }
      nextEpisode?.let { renderNextEpisode(it) }
      actors?.let { renderActors(it) }
      crew?.let { renderCrew(it) }
      relatedShows?.let { renderRelatedShows(it) }
      translation?.let { renderTranslation(it) }
      ratingState?.let { renderRating(it) }
      isPremium.let {
        showDetailsPremiumAd.visibleIf(!it)
      }
    }
  }

  private fun renderGenres(genres: List<String>) =
    genres
      .take(2)
      .mapNotNull { Genre.fromSlug(it) }
      .joinToString(", ") { getString(it.displayName) }

  private fun renderRating(rating: RatingState) {
    showDetailsRateButton.visibleIf(rating.rateLoading == false, gone = false)
    showDetailsRateProgress.visibleIf(rating.rateLoading == true)

    showDetailsRateButton.text =
      if (rating.hasRating()) "${rating.userRating?.rating}/10"
      else getString(R.string.textRate)

    val typeFace = if (rating.hasRating()) BOLD else NORMAL
    showDetailsRateButton.setTypeface(null, typeFace)

    showDetailsRateButton.onClick {
      if (rating.rateAllowed == true) {
        openRateDialog()
      } else {
        showSnack(MessageEvent.Info(R.string.textSignBeforeRate))
      }
    }
  }

  private fun renderRatings(ratings: Ratings, show: Show?) {
    if (showDetailsRatings.isBound()) return
    showDetailsRatings.bind(ratings)
    show?.let {
      showDetailsRatings.onTraktClick = { openShowLink(ShowLink.TRAKT, show.traktId.toString()) }
      showDetailsRatings.onImdbClick = { openShowLink(ShowLink.IMDB, show.ids.imdb.id) }
      showDetailsRatings.onMetaClick = { openShowLink(ShowLink.METACRITIC, show.title) }
      showDetailsRatings.onRottenClick = {
        val url = it.rottenTomatoesUrl
        if (!url.isNullOrBlank()) {
          openWebUrl(url) ?: openShowLink(ShowLink.ROTTEN, "${show.title} ${show.year}")
        } else {
          openShowLink(ShowLink.ROTTEN, "${show.title} ${show.year}")
        }
      }
    }
  }

  private fun renderImage(image: Image) {
    if (image.status == UNAVAILABLE) {
      showDetailsImageProgress.gone()
      showDetailsPlaceholder.visible()
      showDetailsImage.isClickable = false
      showDetailsImage.isEnabled = false
      return
    }
    Glide.with(this)
      .load(image.fullFileUrl)
      .transform(CenterCrop())
      .transition(withCrossFade(IMAGE_FADE_DURATION_MS))
      .withFailListener {
        showDetailsImageProgress.gone()
        showDetailsPlaceholder.visible()
        showDetailsImage.isClickable = true
        showDetailsImage.isEnabled = true
      }
      .withSuccessListener {
        showDetailsImageProgress.gone()
        showDetailsPlaceholder.gone()
        showDetailsTipGallery.fadeIf(!isTipShown(SHOW_DETAILS_GALLERY))
      }
      .into(showDetailsImage)
  }

  private fun renderNextEpisode(episodeBundle: NextEpisodeBundle) {
    episodeBundle.run {
      val (show, episode) = episodeBundle.nextEpisode
      showDetailsEpisodeText.text =
        String.format(ENGLISH, getString(R.string.textEpisodeTitle), episode.season, episode.number, episode.title)

      with(showDetailsEpisodeCard) {
        onClick {
          openEpisodeDetails(show, episode, null, isWatched = false, showButton = false, showTabs = false)
        }
        fadeIn(withHardware = true)
      }

      episode.firstAired?.let {
        val displayDate = episodeBundle.dateFormat?.format(it.toLocalZone())?.capitalizeWords()
        showDetailsEpisodeAirtime.visible()
        showDetailsEpisodeAirtime.text = displayDate
      }
    }
  }

  private fun renderActors(actors: List<Person>) {
    if (actorsAdapter?.itemCount != 0) return
    actorsAdapter?.setItems(actors)
    showDetailsActorsRecycler.visibleIf(actors.isNotEmpty())
    showDetailsActorsEmptyView.visibleIf(actors.isEmpty())
    showDetailsActorsProgress.gone()
  }

  private fun renderCrew(crew: Map<Person.Department, List<Person>>) {

    fun renderPeople(labelView: View, valueView: TextView, people: List<Person>, department: Person.Department) {
      labelView.visibleIf(people.isNotEmpty())
      valueView.visibleIf(people.isNotEmpty())
      valueView.text = people
        .take(2)
        .joinToString("\n") { it.name.trimWithSuffix(20, "…") }
        .plus(if (people.size > 2) "\n…" else "")
      valueView.onClick { openPeopleListSheet(people, department) }
    }

    if (!crew.containsKey(Person.Department.DIRECTING)) {
      return
    }

    val directors = crew[Person.Department.DIRECTING] ?: emptyList()
    val writers = crew[Person.Department.WRITING] ?: emptyList()
    val sound = crew[Person.Department.SOUND] ?: emptyList()

    renderPeople(showDetailsDirectingLabel, showDetailsDirectingValue, directors, Person.Department.DIRECTING)
    renderPeople(showDetailsWritingLabel, showDetailsWritingValue, writers, Person.Department.WRITING)
    renderPeople(showDetailsMusicLabel, showDetailsMusicValue, sound, Person.Department.SOUND)
  }

  private fun renderSeasons(seasonsItems: List<SeasonListItem>) {
    seasonsAdapter?.setItems(seasonsItems)
    showDetailsEpisodesView.updateEpisodes(seasonsItems)
    showDetailsSeasonsProgress.gone()
    showDetailsSeasonsEmptyView.visibleIf(seasonsItems.isEmpty())
    showDetailsSeasonsRecycler.fadeIf(seasonsItems.isNotEmpty(), hardware = true)
    showDetailsSeasonsLabel.fadeIf(seasonsItems.isNotEmpty(), hardware = true)
    showDetailsQuickProgress.fadeIf(seasonsItems.isNotEmpty(), hardware = true)
    showDetailsQuickProgress.onClick {
      if (seasonsItems.any { !it.season.isSpecial() }) {
        openQuickSetupDialog(seasonsItems.map { it.season })
      } else {
        showSnack(MessageEvent.Info(R.string.textSeasonsEmpty))
      }
    }
  }

  private fun renderRuntimeLeft(seasonsItems: List<SeasonListItem>) {
    val runtimeLeft = seasonsItems
      .filter { !it.season.isSpecial() }
      .flatMap { it.episodes }
      .filterNot { it.isWatched }
      .sumOf { it.episode.runtime }
      .toLong()

    val duration = Duration.ofMinutes(runtimeLeft)
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()

    val runtimeText = when {
      hours <= 0 -> getString(R.string.textRuntimeLeftMinutes, minutes.toString())
      else -> getString(R.string.textRuntimeLeftHours, hours.toString(), minutes.toString())
    }
    showDetailsRuntimeLeft.text = runtimeText
    showDetailsRuntimeLeft.fadeIf(seasonsItems.isNotEmpty() && runtimeLeft > 0, hardware = true)
  }

  private fun renderRelatedShows(items: List<RelatedListItem>) {
    relatedAdapter?.setItems(items)
    showDetailsRelatedRecycler.visibleIf(items.isNotEmpty())
    showDetailsRelatedLabel.visibleIf(items.isNotEmpty())
    showDetailsRelatedProgress.gone()
  }

  private fun renderTranslation(translation: Translation?) {
    if (translation?.overview?.isNotBlank() == true) {
      showDetailsDescription.text = translation.overview
    }
    if (translation?.title?.isNotBlank() == true) {
      showDetailsTitle.text = translation.title
    }
  }

  private fun renderSnack(event: MessageEvent) {
    if (event.textResId == R.string.errorMalformedShow) {
      val host = (requireActivity() as SnackbarHost).provideSnackbarLayout()
      val snack = host.showInfoSnackbar(getString(event.textResId), length = Snackbar.LENGTH_INDEFINITE) {
        viewModel.removeMalformedShow(showId)
      }
      snackbars.add(snack)
      return
    }
    showSnack(event)
  }

  private fun openIMDbLink(id: IdImdb, type: String) {
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse("imdb:///$type/${id.id}")
    try {
      startActivity(i)
    } catch (e: ActivityNotFoundException) {
      // IMDb App not installed. Start in web browser
      openWebUrl("http://www.imdb.com/$type/${id.id}") ?: showSnack(MessageEvent.Info(R.string.errorCouldNotFindApp))
    }
  }

  private fun openShowLink(
    link: ShowLink,
    id: String,
    country: AppCountry = UNITED_STATES,
  ) {
    if (link == ShowLink.IMDB) {
      openIMDbLink(IdImdb(id), "title")
    } else {
      openWebUrl(link.getUri(id, country)) ?: showSnack(MessageEvent.Info(R.string.errorCouldNotFindApp))
    }
  }

  private fun openEpisodeDetails(
    show: Show,
    episode: Episode,
    season: Season?,
    isWatched: Boolean,
    showButton: Boolean = true,
    showTabs: Boolean = true,
  ) {
    if (season !== null) {
      setFragmentResultListener(REQUEST_EPISODE_DETAILS) { _, bundle ->
        when {
          bundle.containsKey(ACTION_RATING_CHANGED) -> viewModel.refreshEpisodesRatings()
          bundle.containsKey(ACTION_EPISODE_WATCHED) -> {
            val watched = bundle.getBoolean(ACTION_EPISODE_WATCHED)
            viewModel.setEpisodeWatched(episode, season, watched, removeTrakt = true)
          }
          bundle.containsKey(ACTION_EPISODE_TAB_SELECTED) -> {
            val selectedEpisode = bundle.getParcelable<Episode>(ACTION_EPISODE_TAB_SELECTED)!!
            showDetailsEpisodesView.selectEpisode(selectedEpisode)
          }
        }
      }
    }
    val bundle = Bundle().apply {
      val seasonEpisodes = season?.episodes?.map { it.number }?.toIntArray()
      putLong(EpisodeDetailsBottomSheet.ARG_ID_TRAKT, show.traktId)
      putLong(EpisodeDetailsBottomSheet.ARG_ID_TMDB, show.ids.tmdb.id)
      putParcelable(EpisodeDetailsBottomSheet.ARG_EPISODE, episode)
      putIntArray(EpisodeDetailsBottomSheet.ARG_SEASON_EPISODES, seasonEpisodes)
      putBoolean(EpisodeDetailsBottomSheet.ARG_IS_WATCHED, isWatched)
      putBoolean(EpisodeDetailsBottomSheet.ARG_SHOW_BUTTON, showButton)
      putBoolean(EpisodeDetailsBottomSheet.ARG_SHOW_TABS, showTabs)
    }
    navigateToSafe(R.id.actionShowDetailsFragmentEpisodeDetails, bundle)
  }

  private fun openPersonSheet(person: Person) {
    lastOpenedPerson = null
    setFragmentResultListener(REQUEST_PERSON_DETAILS) { _, _ ->
      lastOpenedPerson = person
    }
    val bundle = PersonDetailsBottomSheet.createBundle(person, showId)
    navigateToSafe(R.id.actionShowDetailsFragmentToPerson, bundle)
  }

  private fun openPeopleListSheet(people: List<Person>, department: Person.Department) {
    if (people.isEmpty()) return
    if (people.size == 1) {
      openPersonSheet(people.first())
      return
    }
    clearFragmentResultListener(REQUEST_PERSON_DETAILS)
    val title = showDetailsTitle.text.toString()
    val bundle = PeopleListBottomSheet.createBundle(showId, title, Mode.SHOWS, department)
    navigateToSafe(R.id.actionShowDetailsFragmentToPeopleList, bundle)
  }

  private fun openRemoveTraktSheet(event: RemoveFromTrakt) {
    setFragmentResultListener(REQUEST_REMOVE_TRAKT) { _, bundle ->
      if (bundle.getBoolean(NavigationArgs.RESULT, false)) {
        val text = resources.getString(R.string.textTraktSyncRemovedFromTrakt)
        (requireActivity() as SnackbarHost).provideSnackbarLayout().showInfoSnackbar(text)

        if (event.actionId == R.id.actionShowDetailsFragmentToRemoveTraktProgress) {
          viewModel.launchRefreshWatchedEpisodes()
        }
      }
    }
    val args = RemoveTraktBottomSheet.createBundle(event.traktIds, event.mode)
    navigateToSafe(event.actionId, args)
  }

  private fun openShareSheet(show: Show) {
    val intent = Intent().apply {
      val text = "Hey! Check out ${show.title}:\nhttps://trakt.tv/shows/${show.ids.slug.id}\nhttps://www.imdb.com/title/${show.ids.imdb.id}"
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, text)
      type = "text/plain"
    }

    val shareIntent = Intent.createChooser(intent, "Share ${show.title}")
    startActivity(shareIntent)

    Analytics.logShowShareClick(show)
  }

  private fun openRateDialog() {
    setFragmentResultListener(NavigationArgs.REQUEST_RATING) { _, bundle ->
      when (bundle.getParcelable<RatingsBottomSheet.Options.Operation>(NavigationArgs.RESULT)) {
        SAVE -> renderSnack(MessageEvent.Info(R.string.textRateSaved))
        REMOVE -> renderSnack(MessageEvent.Info(R.string.textRateRemoved))
        else -> Timber.w("Unknown result")
      }
      viewModel.loadRating()
    }
    val bundle = RatingsBottomSheet.createBundle(showId, Type.SHOW)
    navigateToSafe(R.id.actionShowDetailsFragmentToRating, bundle)
  }

  private fun openRateSeasonDialog(season: Season) {
    setFragmentResultListener(NavigationArgs.REQUEST_RATING) { _, bundle ->
      when (bundle.getParcelable<RatingsBottomSheet.Options.Operation>(NavigationArgs.RESULT)) {
        SAVE -> renderSnack(MessageEvent.Info(R.string.textRateSaved))
        REMOVE -> renderSnack(MessageEvent.Info(R.string.textRateRemoved))
        else -> Timber.w("Unknown result")
      }
      viewModel.refreshEpisodesRatings()
    }
    val bundle = RatingsBottomSheet.createBundle(season.ids.trakt, Type.SEASON)
    navigateToSafe(R.id.actionShowDetailsFragmentToRating, bundle)
  }

  private fun openQuickSetupDialog(seasons: List<Season>) {
    val context = requireContext()
    val view = QuickSetupView(context).apply {
      bind(seasons)
    }
    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
      .setView(view)
      .setPositiveButton(R.string.textSelect) { _, _ ->
        viewModel.setQuickProgress(view.getSelectedItem())
      }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  private fun openListsDialog() {
    if (findNavControl()?.currentDestination?.id != R.id.showDetailsFragment) {
      return
    }
    setFragmentResultListener(REQUEST_MANAGE_LISTS) { _, _ -> viewModel.loadListsCount() }
    val bundle = bundleOf(
      ARG_ID to showId.id,
      ARG_TYPE to Mode.SHOWS.type
    )
    navigateToSafe(R.id.actionShowDetailsFragmentToManageLists, bundle)
  }

  private fun openCustomImagesSheet(showId: Long, isPremium: Boolean?) {
    if (isPremium == false) {
      navigateToSafe(R.id.actionShowDetailsFragmentToPremium)
      return
    }

    setFragmentResultListener(REQUEST_CUSTOM_IMAGE) { _, bundle ->
      viewModel.loadBackgroundImage()
      if (!bundle.getBoolean(ARG_CUSTOM_IMAGE_CLEARED)) {
        openCustomImagesSheet(showId, true)
      }
    }

    val bundle = bundleOf(
      ARG_SHOW_ID to showId,
      ARG_FAMILY to SHOW
    )
    navigateToSafe(R.id.actionShowDetailsFragmentToCustomImages, bundle)
  }

  fun showStreamingsView(animate: Boolean) {
    if (!animate) {
      showDetailsStreamingsFragment.visible()
      return
    }
    val animation = ConstraintSet().apply {
      clone(showDetailsMainContent)
      setVisibility(showDetailsStreamingsFragment.id, View.VISIBLE)
    }
    val transition = AutoTransition().apply {
      interpolator = DecelerateInterpolator(1.5F)
      duration = 200
    }
    TransitionManager.beginDelayedTransition(showDetailsMainContent, transition)
    animation.applyTo(showDetailsMainContent)
  }

  override fun setupBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      when {
        showDetailsEpisodesView.isVisible -> {
          hideExtraView(showDetailsEpisodesView)
          return@addCallback
        }
        else -> {
          isEnabled = false
          findNavControl()?.popBackStack()
        }
      }
    }
  }

  override fun onDestroyView() {
    actorsAdapter = null
    relatedAdapter = null
    seasonsAdapter = null
    streamingAdapter = null
    super.onDestroyView()
  }
}
