package com.michaldrabik.ui_show

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Color.TRANSPARENT
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.Config.INITIAL_RATING
import com.michaldrabik.common.Config.TMDB_IMAGE_BASE_ACTOR_FULL_URL
import com.michaldrabik.common.extensions.toDisplayString
import com.michaldrabik.common.extensions.toLocalTimeZone
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.AppCountry
import com.michaldrabik.ui_base.common.AppCountry.UNITED_STATES
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.common.views.RateView
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.crossfadeTo
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.openWebUrl
import com.michaldrabik.ui_base.utilities.extensions.screenHeight
import com.michaldrabik.ui_base.utilities.extensions.screenWidth
import com.michaldrabik.ui_base.utilities.extensions.setTextIfEmpty
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Tip.SHOW_DETAILS_GALLERY
import com.michaldrabik.ui_model.Tip.SHOW_DETAILS_QUICK_PROGRESS
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_CUSTOM_IMAGE_CLEARED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_FAMILY
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CUSTOM_IMAGE
import com.michaldrabik.ui_show.actors.ActorsAdapter
import com.michaldrabik.ui_show.di.UiShowDetailsComponentProvider
import com.michaldrabik.ui_show.helpers.ShowLink
import com.michaldrabik.ui_show.helpers.ShowLink.IMDB
import com.michaldrabik.ui_show.helpers.ShowLink.JUST_WATCH
import com.michaldrabik.ui_show.helpers.ShowLink.TMDB
import com.michaldrabik.ui_show.helpers.ShowLink.TRAKT
import com.michaldrabik.ui_show.helpers.ShowLink.TVDB
import com.michaldrabik.ui_show.quickSetup.QuickSetupView
import com.michaldrabik.ui_show.related.RelatedListItem
import com.michaldrabik.ui_show.related.RelatedShowAdapter
import com.michaldrabik.ui_show.seasons.SeasonListItem
import com.michaldrabik.ui_show.seasons.SeasonsAdapter
import com.michaldrabik.ui_show.views.AddToShowsButton.State.ADD
import com.michaldrabik.ui_show.views.AddToShowsButton.State.IN_ARCHIVE
import com.michaldrabik.ui_show.views.AddToShowsButton.State.IN_MY_SHOWS
import com.michaldrabik.ui_show.views.AddToShowsButton.State.IN_WATCHLIST
import kotlinx.android.synthetic.main.fragment_show_details.*
import kotlinx.android.synthetic.main.fragment_show_details_actor_full_view.*
import kotlinx.android.synthetic.main.fragment_show_details_next_episode.*
import kotlinx.android.synthetic.main.view_links_menu.view.*
import org.threeten.bp.Duration
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
class ShowDetailsFragment : BaseFragment<ShowDetailsViewModel>(R.layout.fragment_show_details) {

  override val viewModel by viewModels<ShowDetailsViewModel> { viewModelFactory }

  private val showId by lazy { IdTrakt(requireArguments().getLong(ARG_SHOW_ID, -1)) }

  private val actorsAdapter by lazy { ActorsAdapter() }
  private val relatedAdapter by lazy { RelatedShowAdapter() }
  private val seasonsAdapter by lazy { SeasonsAdapter() }

  private val imageHeight by lazy {
    if (resources.configuration.orientation == ORIENTATION_PORTRAIT) screenHeight()
    else screenWidth()
  }
  private val imageRatio by lazy { resources.getString(R.string.detailsImageRatio).toFloat() }
  private val imagePadded by lazy { resources.getBoolean(R.bool.detailsImagePadded) }

  private val actorViewCorner by lazy { requireContext().dimenToPx(R.dimen.actorFullTileCorner) }
  private val animationEnterRight by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_in_from_right) }
  private val animationExitRight by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_out_from_right) }
  private val animationEnterLeft by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_in_from_left) }
  private val animationExitLeft by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_out_from_left) }

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiShowDetailsComponentProvider).provideShowDetailsComponent().inject(this)
    super.onCreate(savedInstanceState)
    setFragmentResultListener(REQUEST_CUSTOM_IMAGE) { _, bundle ->
      viewModel.loadBackgroundImage()
      if (!bundle.getBoolean(ARG_CUSTOM_IMAGE_CLEARED)) showCustomImages(showId.id, true)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
    setupView()
    setupStatusBar()
    setupActorsList()
    setupRelatedList()
    setupSeasonsList()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
      if (!isInitialized) {
        loadShowDetails(showId, requireAppContext())
        isInitialized = true
      }
      loadPremium()
    }
  }

  override fun onResume() {
    super.onResume()
    hideNavigation()
    handleBackPressed()
  }

  private fun setupView() {
    hideNavigation()
    showDetailsImageGuideline.setGuidelineBegin((imageHeight * imageRatio).toInt())
    showDetailsEpisodesView.itemClickListener = { show, episode, season, isWatched ->
      showEpisodeDetails(show, episode, season, isWatched, episode.hasAired(season))
    }
    listOf(showDetailsBackArrow, showDetailsBackArrow2).onClick { requireActivity().onBackPressed() }
    showDetailsImage.onClick {
      val bundle = bundleOf(
        ARG_SHOW_ID to showId.id,
        ARG_FAMILY to SHOW,
        ARG_TYPE to FANART
      )
      navigateTo(R.id.actionShowDetailsFragmentToArtGallery, bundle)
      Analytics.logShowGalleryClick(showId.id)
    }
    showDetailsCommentsButton.onClick {
      showDetailsCommentsView.clear()
      showCommentsView()
      viewModel.loadComments()
    }
    showDetailsTipGallery.onClick {
      it.gone()
      showTip(SHOW_DETAILS_GALLERY)
    }
    showDetailsTipQuickProgress.onClick {
      it.gone()
      showTip(SHOW_DETAILS_QUICK_PROGRESS)
    }
    showDetailsAddButton.run {
      isEnabled = false
      onAddMyShowsClickListener = {
        viewModel.addFollowedShow(requireAppContext())
        showDetailsTipQuickProgress.fadeIf(!isTipShown(SHOW_DETAILS_QUICK_PROGRESS))
      }
      onAddWatchLaterClickListener = { viewModel.addWatchlistShow(requireAppContext()) }
      onArchiveClickListener = { openArchiveConfirmationDialog() }
      onRemoveClickListener = { viewModel.removeFromFollowed(requireAppContext()) }
    }
    showDetailsRemoveTraktButton.onNoClickListener = {
      showDetailsAddButton.fadeIn()
      showDetailsRemoveTraktButton.fadeOut()
    }
  }

  private fun setupStatusBar() {
    showDetailsBackArrow.doOnApplyWindowInsets { view, insets, _, _ ->
      if (imagePadded) showDetailsMainLayout.updatePadding(top = insets.systemWindowInsetTop)
      arrayOf<View>(view, showDetailsBackArrow2, showDetailsEpisodesView, showDetailsCommentsView)
        .forEach { v ->
          (v.layoutParams as MarginLayoutParams).updateMargins(top = insets.systemWindowInsetTop)
        }
    }
  }

  private fun setupActorsList() {
    val context = requireContext()
    showDetailsActorsRecycler.apply {
      setHasFixedSize(true)
      adapter = actorsAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
    actorsAdapter.itemClickListener = { showFullActorView(it) }
  }

  private fun setupRelatedList() {
    val context = requireContext()
    showDetailsRelatedRecycler.apply {
      setHasFixedSize(true)
      adapter = relatedAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
    relatedAdapter.missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    relatedAdapter.itemClickListener = {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, it.show.traktId) }
      navigateTo(R.id.actionShowDetailsFragmentToSelf, bundle)
    }
  }

  private fun setupSeasonsList() {
    val context = requireContext()
    showDetailsSeasonsRecycler.apply {
      setHasFixedSize(true)
      adapter = seasonsAdapter
      layoutManager = LinearLayoutManager(context, VERTICAL, false)
      itemAnimator = null
    }
    seasonsAdapter.itemClickListener = { showEpisodesView(it) }
    seasonsAdapter.itemCheckedListener = { item: SeasonListItem, isChecked: Boolean ->
      viewModel.setWatchedSeason(requireAppContext(), item.season, isChecked)
    }
  }

  private fun showEpisodesView(item: SeasonListItem) {
    showDetailsEpisodesView.run {
      bind(item)
      fadeIn(265) {
        bindEpisodes(item.episodes)
        viewModel.loadSeasonTranslation(item)
      }
      startAnimation(animationEnterRight)
      itemCheckedListener = { episode, season, isChecked ->
        viewModel.setWatchedEpisode(requireAppContext(), episode, season, isChecked)
      }
      seasonCheckedListener = { season, isChecked ->
        viewModel.setWatchedSeason(requireAppContext(), season, isChecked)
      }
    }
    showDetailsMainLayout.run {
      fadeOut(200)
      startAnimation(animationExitRight)
    }
    showDetailsBackArrow.crossfadeTo(showDetailsBackArrow2)
  }

  private fun showCommentsView() {
    showDetailsCommentsView.run {
      fadeIn(275)
      startAnimation(animationEnterRight)
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
      fadeIn()
      startAnimation(animationEnterLeft)
    }
    showDetailsBackArrow2.crossfadeTo(showDetailsBackArrow)
  }

  private fun showEpisodeDetails(
    show: Show,
    episode: Episode,
    season: Season?,
    isWatched: Boolean,
    showButton: Boolean = true
  ) {
    val modal = EpisodeDetailsBottomSheet.create(show, episode, isWatched, showButton)
    if (season != null) {
      modal.onEpisodeWatchedClick = { viewModel.setWatchedEpisode(requireAppContext(), episode, season, it) }
      modal.onRatingChanged = { viewModel.refreshEpisodesRatings() }
    }
    modal.show(requireActivity().supportFragmentManager, "MODAL")
  }

  private fun showCustomImages(showId: Long, isPremium: Boolean?) {
    if (isPremium == false) {
      navigateTo(R.id.actionShowDetailsFragmentToPremium)
      return
    }
    val bundle = bundleOf(
      ARG_SHOW_ID to showId,
      ARG_FAMILY to SHOW
    )
    navigateTo(R.id.actionShowDetailsFragmentToCustomImages, bundle)
  }

  private fun showFullActorView(actor: Actor) {
    Glide.with(this)
      .load("$TMDB_IMAGE_BASE_ACTOR_FULL_URL${actor.image}")
      .transform(CenterCrop(), RoundedCorners(actorViewCorner))
      .into(showDetailsActorFullImage)

    val actorView = showDetailsActorsRecycler.findViewWithTag<View>(actor.tmdbId)
    val transform = MaterialContainerTransform().apply {
      startView = actorView
      endView = showDetailsActorFullContainer
      scrimColor = TRANSPARENT
      addTarget(showDetailsActorFullContainer)
    }
    TransitionManager.beginDelayedTransition(showDetailsRoot, transform)
    actorView.gone()
    showDetailsActorFullImdb.apply {
      val hasImdbId = actor.imdbId != null
      visibleIf(hasImdbId)
      if (hasImdbId) {
        onClick { openIMDbLink(IdImdb(actor.imdbId!!), "name") }
      }
    }
    showDetailsActorFullName.apply {
      text = getString(R.string.textActorRole, actor.name, actor.role)
      fadeIn()
    }
    showDetailsActorFullContainer.apply {
      tag = actor
      onClick { hideFullActorView(actor) }
      visible()
    }
    showDetailsActorFullMask.apply {
      onClick { hideFullActorView(actor) }
      fadeIn()
    }
  }

  private fun hideFullActorView(actor: Actor) {
    val actorView = showDetailsActorsRecycler.findViewWithTag<View>(actor.tmdbId)
    val transform = MaterialContainerTransform().apply {
      startView = showDetailsActorFullContainer
      endView = actorView
      scrimColor = TRANSPARENT
      addTarget(actorView)
    }
    TransitionManager.beginDelayedTransition(showDetailsRoot, transform)
    showDetailsActorFullContainer.gone()
    actorView.visible()
    showDetailsActorFullMask.fadeOut()
    showDetailsActorFullName.fadeOut()
  }

  private fun render(uiModel: ShowDetailsUiModel) {
    uiModel.run {
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
          country.toUpperCase(),
          show.runtime.toString(),
          getString(R.string.textMinutesShort),
          renderGenres(show.genres)
        )
        showDetailsRating.text = String.format(ENGLISH, getString(R.string.textVotes), show.rating, show.votes)
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
            openWebUrl(show.trailer)
            Analytics.logShowTrailerClick(show)
          }
        }
        showDetailsLinksButton.run {
          onClick {
            openLinksMenu(show, uiModel.country ?: UNITED_STATES)
            Analytics.logShowLinksClick(show)
          }
        }
        showDetailsSeparator4.visible()
        showDetailsCustomImagesLabel.visible()
        showDetailsCustomImagesLabel.onClick { showCustomImages(show.traktId, isPremium) }
        showDetailsAddButton.isEnabled = true
      }
      showLoading?.let {
        if (!showDetailsEpisodesView.isVisible && !showDetailsCommentsView.isVisible) {
          showDetailsMainLayout.fadeIf(!it)
          showDetailsMainProgress.visibleIf(it)
        }
      }
      followedState?.let {
        when {
          it.isMyShows -> showDetailsAddButton.setState(IN_MY_SHOWS, it.withAnimation)
          it.isWatchlist -> showDetailsAddButton.setState(IN_WATCHLIST, it.withAnimation)
          it.isArchived -> showDetailsAddButton.setState(IN_ARCHIVE, it.withAnimation)
          else -> showDetailsAddButton.setState(ADD, it.withAnimation)
        }
      }
      nextEpisode?.let { renderNextEpisode(it) }
      image?.let { renderImage(it) }
      actors?.let { renderActors(it) }
      seasons?.let {
        renderSeasons(it)
        renderRuntimeLeft(it)
        (requireAppContext() as WidgetsProvider).requestShowsWidgetsUpdate()
      }
      translation?.let { renderTranslation(it) }
      seasonTranslation?.let { item ->
        item.consume()?.let { showDetailsEpisodesView.bindEpisodes(it.episodes, animate = false) }
      }
      relatedShows?.let { renderRelatedShows(it) }
      comments?.let { showDetailsCommentsView.bind(it) }
      ratingState?.let { renderRating(it) }
      showFromTraktLoading?.let {
        showDetailsRemoveTraktButton.isLoading = it
        showDetailsAddButton.isEnabled = !it
      }
      removeFromTraktHistory?.let { event ->
        event.consume()?.let {
          showDetailsAddButton.fadeIf(!it)
          showDetailsRemoveTraktButton.run {
            fadeIf(it)
            onYesClickListener = { viewModel.removeFromTraktHistory() }
          }
        }
      }
      removeFromTraktWatchlist?.let { event ->
        event.consume()?.let {
          showDetailsAddButton.fadeIf(!it)
          showDetailsRemoveTraktButton.run {
            fadeIf(it)
            onYesClickListener = { viewModel.removeFromTraktWatchlist() }
          }
        }
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
        val rate = rating.userRating?.rating ?: INITIAL_RATING
        openRateDialog(rate, rate != 0)
      } else {
        showSnack(MessageEvent.info(R.string.textSignBeforeRate))
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

  private fun renderNextEpisode(episodeBundle: Pair<Show, Episode>) {
    episodeBundle.run {
      val (show, episode) = episodeBundle
      showDetailsEpisodeText.text =
        String.format(ENGLISH, getString(R.string.textEpisodeTitle), episode.season, episode.number, episode.title)
      showDetailsEpisodeCard.visible()
      showDetailsEpisodeCard.onClick {
        showEpisodeDetails(show, episode, null, isWatched = false, showButton = false)
      }

      episode.firstAired?.let {
        val displayDate = it.toLocalTimeZone().toDisplayString()
        showDetailsEpisodeAirtime.visible()
        showDetailsEpisodeAirtime.text = displayDate
      }
    }
  }

  private fun renderActors(actors: List<Actor>) {
    actorsAdapter.setItems(actors)
    showDetailsActorsRecycler.fadeIf(actors.isNotEmpty())
    showDetailsActorsEmptyView.fadeIf(actors.isEmpty())
    showDetailsActorsProgress.gone()
  }

  private fun renderSeasons(seasonsItems: List<SeasonListItem>) {
    seasonsAdapter.setItems(seasonsItems)
    showDetailsEpisodesView.updateEpisodes(seasonsItems)
    showDetailsSeasonsRecycler.fadeIf(seasonsItems.isNotEmpty())
    showDetailsSeasonsLabel.fadeIf(seasonsItems.isNotEmpty())
    showDetailsSeasonsEmptyView.fadeIf(seasonsItems.isEmpty())
    showDetailsSeasonsProgress.gone()
    showDetailsAddButton.onQuickSetupClickListener = {
      openQuickSetupDialog(seasonsItems.map { it.season })
    }
  }

  private fun renderRuntimeLeft(seasonsItems: List<SeasonListItem>) {
    val runtimeLeft = seasonsItems
      .filter { !it.season.isSpecial() }
      .flatMap { it.episodes }
      .filterNot { it.isWatched }
      .sumBy { it.episode.runtime }
      .toLong()

    val duration = Duration.ofMinutes(runtimeLeft)
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()

    val runtimeText = when {
      hours <= 0 -> getString(R.string.textRuntimeLeftMinutes, minutes.toString())
      else -> getString(R.string.textRuntimeLeftHours, hours.toString(), minutes.toString())
    }
    showDetailsRuntimeLeft.text = runtimeText
    showDetailsRuntimeLeft.fadeIf(seasonsItems.isNotEmpty() && runtimeLeft > 0)
  }

  private fun renderRelatedShows(items: List<RelatedListItem>) {
    relatedAdapter.setItems(items)
    showDetailsRelatedRecycler.fadeIf(items.isNotEmpty())
    showDetailsRelatedLabel.fadeIf(items.isNotEmpty())
    showDetailsRelatedProgress.gone()
  }

  private fun renderTranslation(translation: Translation?) {
    if (translation?.overview?.isNotBlank() == true) {
      showDetailsDescription.text = translation.overview
    }
    if (translation?.title?.isNotBlank() == true) {
      showDetailsTitle.text = translation.title.capitalizeWords()
    }
  }

  private fun openIMDbLink(id: IdImdb, type: String) {
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse("imdb:///$type/${id.id}")
    try {
      startActivity(i)
    } catch (e: ActivityNotFoundException) {
      // IMDb App not installed. Start in web browser
      openWebUrl("http://www.imdb.com/$type/${id.id}")
    }
  }

  private fun openShowLink(
    link: ShowLink,
    id: String,
    country: AppCountry = UNITED_STATES
  ) {
    if (link == IMDB) {
      openIMDbLink(IdImdb(id), "title")
    } else {
      openWebUrl(link.getUri(id, country))
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun openLinksMenu(show: Show, country: AppCountry) {
    val ids = show.ids
    showDetailsMainLayout.setOnTouchListener { _, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        showDetailsLinksMenu.fadeOut()
        showDetailsMainLayout.setOnTouchListener(null)
      }
      false
    }
    showDetailsLinksMenu.run {
      fadeIn(125)
      viewLinkTrakt.visibleIf(ids.trakt.id != -1L)
      viewLinkTrakt.onClick {
        openShowLink(TRAKT, ids.trakt.id.toString())
        fadeOut(125)
      }
      viewLinkTmdb.visibleIf(ids.tmdb.id != -1L)
      viewLinkTmdb.onClick {
        openShowLink(TMDB, ids.tmdb.id.toString())
        fadeOut(125)
      }
      viewLinkImdb.visibleIf(ids.imdb.id.isNotBlank())
      viewLinkImdb.onClick {
        openShowLink(IMDB, ids.imdb.id)
        fadeOut(125)
      }
      viewLinkTvdb.visibleIf(ids.tvdb.id != -1L)
      viewLinkTvdb.onClick {
        openShowLink(TVDB, ids.tvdb.id.toString())
        fadeOut(125)
      }
      viewLinkJustWatch.onClick {
        openShowLink(JUST_WATCH, show.title, country)
        fadeOut(125)
      }
    }
  }

  private fun openShareSheet(show: Show) {
    val intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, "Hey! Check out ${show.title}:\nhttp://www.imdb.com/title/${show.ids.imdb.id}")
      type = "text/plain"
    }

    val shareIntent = Intent.createChooser(intent, "Share ${show.title}")
    startActivity(shareIntent)

    Analytics.logShowShareClick(show)
  }

  private fun openRateDialog(rating: Int, showRemove: Boolean) {
    val context = requireContext()
    val rateView = RateView(context).apply {
      setPadding(context.dimenToPx(R.dimen.spaceNormal))
      setRating(rating)
    }
    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
      .setView(rateView)
      .setPositiveButton(R.string.textRate) { _, _ -> viewModel.addRating(rateView.getRating()) }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .apply {
        if (showRemove) {
          setNeutralButton(R.string.textRateDelete) { _, _ -> viewModel.deleteRating() }
        }
      }
      .show()
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
        viewModel.setQuickProgress(requireAppContext(), view.getSelectedItem())
      }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  private fun openArchiveConfirmationDialog() {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setTitle(R.string.textArchiveConfirmationTitle)
      .setMessage(R.string.textArchiveConfirmationMessage)
      .setPositiveButton(R.string.textYes) { _, _ -> viewModel.addArchiveShow() }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .setNeutralButton(R.string.textWhatIsArchive) { _, _ -> openArchiveDescriptionDialog() }
      .show()
  }

  private fun openArchiveDescriptionDialog() {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setTitle(R.string.textWhatIsArchiveFull)
      .setMessage(R.string.textArchiveDescription)
      .setPositiveButton(R.string.textOk) { _, _ -> openArchiveConfirmationDialog() }
      .show()
  }

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      when {
        showDetailsEpisodesView.isVisible -> {
          hideExtraView(showDetailsEpisodesView)
          return@addCallback
        }
        showDetailsCommentsView.isVisible -> {
          hideExtraView(showDetailsCommentsView)
          return@addCallback
        }
        showDetailsActorFullContainer.isVisible -> {
          hideFullActorView(showDetailsActorFullContainer.tag as Actor)
          return@addCallback
        }
        else -> {
          remove()
          findNavControl().popBackStack()
        }
      }
    }
  }
}
