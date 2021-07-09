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
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.Config.INITIAL_RATING
import com.michaldrabik.common.Config.TMDB_IMAGE_BASE_ACTOR_FULL_URL
import com.michaldrabik.common.Mode
import com.michaldrabik.common.extensions.toLocalZone
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
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily.SHOW
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Ratings
import com.michaldrabik.ui_model.Season
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_model.Tip.SHOW_DETAILS_GALLERY
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_TAB_SELECTED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_WATCHED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_NEW_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_RATING_CHANGED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ACTION
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_CUSTOM_IMAGE_CLEARED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_FAMILY
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_REPLY_USER
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CUSTOM_IMAGE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_EPISODE_DETAILS
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_MANAGE_LISTS
import com.michaldrabik.ui_show.actors.ActorsAdapter
import com.michaldrabik.ui_show.helpers.NextEpisodeBundle
import com.michaldrabik.ui_show.helpers.ShowLink
import com.michaldrabik.ui_show.helpers.ShowLink.IMDB
import com.michaldrabik.ui_show.helpers.ShowLink.JUST_WATCH
import com.michaldrabik.ui_show.helpers.ShowLink.METACRITIC
import com.michaldrabik.ui_show.helpers.ShowLink.ROTTEN
import com.michaldrabik.ui_show.helpers.ShowLink.TMDB
import com.michaldrabik.ui_show.helpers.ShowLink.TRAKT
import com.michaldrabik.ui_show.helpers.ShowLink.TVDB
import com.michaldrabik.ui_show.helpers.StreamingsBundle
import com.michaldrabik.ui_show.quickSetup.QuickSetupView
import com.michaldrabik.ui_show.related.RelatedListItem
import com.michaldrabik.ui_show.related.RelatedShowAdapter
import com.michaldrabik.ui_show.seasons.SeasonListItem
import com.michaldrabik.ui_show.seasons.SeasonsAdapter
import com.michaldrabik.ui_show.views.AddToShowsButton.State.ADD
import com.michaldrabik.ui_show.views.AddToShowsButton.State.IN_ARCHIVE
import com.michaldrabik.ui_show.views.AddToShowsButton.State.IN_MY_SHOWS
import com.michaldrabik.ui_show.views.AddToShowsButton.State.IN_WATCHLIST
import com.michaldrabik.ui_streamings.recycler.StreamingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_show_details.*
import kotlinx.android.synthetic.main.fragment_show_details_actor_full_view.*
import kotlinx.android.synthetic.main.fragment_show_details_next_episode.*
import kotlinx.android.synthetic.main.view_links_menu.view.*
import java.time.Duration
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
@AndroidEntryPoint
class ShowDetailsFragment : BaseFragment<ShowDetailsViewModel>(R.layout.fragment_show_details) {

  override val viewModel by viewModels<ShowDetailsViewModel>()

  private val showId by lazy { IdTrakt(requireArguments().getLong(ARG_SHOW_ID, -1)) }

  private var actorsAdapter: ActorsAdapter? = null
  private var relatedAdapter: RelatedShowAdapter? = null
  private var seasonsAdapter: SeasonsAdapter? = null
  private var streamingAdapter: StreamingAdapter? = null

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

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
    setupView()
    setupStatusBar()
    setupActorsList()
    setupRelatedList()
    setupSeasonsList()
    setupStreamingsList()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      seasonsLiveData.observe(
        viewLifecycleOwner,
        {
          renderSeasons(it)
          renderRuntimeLeft(it)
          (requireAppContext() as WidgetsProvider).requestShowsWidgetsUpdate()
        }
      )
      nextEpisodeLiveData.observe(viewLifecycleOwner, { renderNextEpisode(it) })
      actorsLiveData.observe(viewLifecycleOwner, { renderActors(it) })
      streamingsLiveData.observe(viewLifecycleOwner, { renderStreamings(it) })
      relatedLiveData.observe(viewLifecycleOwner, { renderRelatedShows(it) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
      if (!isInitialized) {
        loadShowDetails(showId, requireAppContext())
        isInitialized = true
      }
      loadPremium()
    }
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
      if (checkNavigation(R.id.showDetailsFragment)) {
        navigateTo(R.id.actionShowDetailsFragmentToArtGallery, bundle)
      }
      Analytics.logShowGalleryClick(showId.id)
    }
    showDetailsCommentsButton.onClick {
      showDetailsCommentsView.clear()
      showCommentsView()
      viewModel.loadComments()
    }
    showDetailsCommentsView.run {
      onRepliesClickListener = { viewModel.loadCommentReplies(it) }
      onReplyCommentClickListener = { showPostCommentSheet(comment = it) }
      onDeleteCommentClickListener = { openDeleteCommentDialog(it) }
      onPostCommentClickListener = { showPostCommentSheet() }
    }
    showDetailsTipGallery.onClick {
      it.gone()
      showTip(SHOW_DETAILS_GALLERY)
    }
    showDetailsAddButton.run {
      isEnabled = false
      onAddMyShowsClickListener = {
        viewModel.addFollowedShow()
      }
      onAddWatchLaterClickListener = { viewModel.addWatchlistShow(requireAppContext()) }
      onArchiveClickListener = { openArchiveConfirmationDialog() }
      onRemoveClickListener = { viewModel.removeFromFollowed() }
    }
    showDetailsRemoveTraktButton.onNoClickListener = {
      showDetailsAddButton.fadeIn()
      showDetailsRemoveTraktButton.fadeOut()
    }
    showDetailsManageListsLabel.onClick { openListsDialog() }
  }

  private fun setupStatusBar() {
    showDetailsBackArrow.doOnApplyWindowInsets { view, insets, _, _ ->
      if (imagePadded) {
        showDetailsMainLayout.updatePadding(top = insets.systemWindowInsetTop)
      } else {
        (showDetailsShareButton.layoutParams as MarginLayoutParams)
          .updateMargins(top = insets.systemWindowInsetTop)
      }
      arrayOf<View>(view, showDetailsBackArrow2, showDetailsEpisodesView, showDetailsCommentsView)
        .forEach { v ->
          (v.layoutParams as MarginLayoutParams).updateMargins(top = insets.systemWindowInsetTop)
        }
    }
  }

  private fun setupActorsList() {
    actorsAdapter = ActorsAdapter().apply {
      itemClickListener = { showFullActorView(it) }
    }
    showDetailsActorsRecycler.apply {
      setHasFixedSize(true)
      adapter = actorsAdapter
      layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
  }

  private fun setupRelatedList() {
    relatedAdapter = RelatedShowAdapter().apply {
      missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
      itemClickListener = {
        if (findNavControl()?.currentDestination?.id == R.id.showDetailsFragment) {
          val bundle = Bundle().apply { putLong(ARG_SHOW_ID, it.show.traktId) }
          navigateTo(R.id.actionShowDetailsFragmentToSelf, bundle)
        }
      }
    }
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
        viewModel.setWatchedSeason(requireAppContext(), item.season, isChecked)
      }
    }
    showDetailsSeasonsRecycler.apply {
      adapter = seasonsAdapter
      layoutManager = LinearLayoutManager(requireContext(), VERTICAL, false)
      itemAnimator = null
    }
  }

  private fun setupStreamingsList() {
    streamingAdapter = StreamingAdapter()
    showDetailsStreamingsRecycler.apply {
      setHasFixedSize(true)
      adapter = streamingAdapter
      layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
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
      fadeIn(275, withHardware = true)
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
      fadeIn(withHardware = true)
      startAnimation(animationEnterLeft)
    }
    showDetailsBackArrow2.crossfadeTo(showDetailsBackArrow)

    viewModel.refreshAnnouncements()
  }

  private fun showEpisodeDetails(
    show: Show,
    episode: Episode,
    season: Season?,
    isWatched: Boolean,
    showButton: Boolean = true,
    showTabs: Boolean = true,
  ) {
    if (!checkNavigation(R.id.showDetailsFragment)) return
    if (season !== null) {
      setFragmentResultListener(REQUEST_EPISODE_DETAILS) { _, bundle ->
        when {
          bundle.containsKey(ACTION_RATING_CHANGED) -> viewModel.refreshEpisodesRatings()
          bundle.containsKey(ACTION_EPISODE_WATCHED) -> {
            val watched = bundle.getBoolean(ACTION_EPISODE_WATCHED)
            viewModel.setWatchedEpisode(requireAppContext(), episode, season, watched)
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
    navigateTo(R.id.actionShowDetailsFragmentEpisodeDetails, bundle)
  }

  private fun showCustomImagesSheet(showId: Long, isPremium: Boolean?) {
    if (isPremium == false) {
      navigateTo(R.id.actionShowDetailsFragmentToPremium)
      return
    }

    setFragmentResultListener(REQUEST_CUSTOM_IMAGE) { _, bundle ->
      viewModel.loadBackgroundImage()
      if (!bundle.getBoolean(ARG_CUSTOM_IMAGE_CLEARED)) showCustomImagesSheet(showId, true)
    }

    val bundle = bundleOf(
      ARG_SHOW_ID to showId,
      ARG_FAMILY to SHOW
    )
    navigateTo(R.id.actionShowDetailsFragmentToCustomImages, bundle)
  }

  private fun showPostCommentSheet(comment: Comment? = null) {
    setFragmentResultListener(REQUEST_COMMENT) { _, bundle ->
      showSnack(MessageEvent.info(R.string.textCommentPosted))
      when (bundle.getString(ARG_COMMENT_ACTION)) {
        ACTION_NEW_COMMENT -> {
          val newComment = bundle.getParcelable<Comment>(ARG_COMMENT)!!
          viewModel.addNewComment(newComment)
          if (comment == null) showDetailsCommentsView.resetScroll()
        }
      }
    }

    val bundle = when {
      comment != null -> bundleOf(
        ARG_COMMENT_ID to comment.getReplyId(),
        ARG_REPLY_USER to comment.user.username
      )
      else -> bundleOf(ARG_SHOW_ID to showId.id)
    }
    navigateTo(R.id.actionShowDetailsFragmentToPostComment, bundle)
  }

  private fun showFullActorView(actor: Actor) {
    if (showDetailsActorFullContainer.isVisible) {
      return
    }

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
      fadeIn(withHardware = true)
    }
    showDetailsActorFullContainer.apply {
      tag = actor
      onClick { hideFullActorView(actor) }
      visible()
    }
    showDetailsActorFullMask.apply {
      onClick { hideFullActorView(actor) }
      fadeIn(withHardware = true)
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
            openWebUrl(show.trailer) ?: showSnack(MessageEvent.info(R.string.errorCouldNotFindApp))
            Analytics.logShowTrailerClick(show)
          }
        }
        showDetailsLinksButton.run {
          onClick {
            openLinksMenu(show, uiModel.country ?: UNITED_STATES)
            Analytics.logShowLinksClick(show)
          }
        }
        separator4.visible()
        showDetailsCustomImagesLabel.visibleIf(Config.SHOW_PREMIUM)
        showDetailsCustomImagesLabel.onClick { showCustomImagesSheet(show.traktId, isPremium) }
        showDetailsAddButton.isEnabled = true
      }
      showLoading?.let {
        if (!showDetailsEpisodesView.isVisible && !showDetailsCommentsView.isVisible) {
          showDetailsMainLayout.fadeIf(!it, hardware = true)
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
      listsCount?.let {
        val text =
          if (it > 0) getString(R.string.textShowManageListsCount, it)
          else getString(R.string.textShowManageLists)
        showDetailsManageListsLabel.text = text
      }
      image?.let { renderImage(it) }
      ratings?.let { renderRatings(it, show) }
      translation?.let { renderTranslation(it) }
      seasonTranslation?.let { item ->
        item.consume()?.let { showDetailsEpisodesView.bindEpisodes(it.episodes, animate = false) }
      }
      comments?.let {
        showDetailsCommentsView.bind(it, commentsDateFormat)
        if (isSignedIn == true) {
          showDetailsCommentsView.showCommentButton()
        }
      }
      ratingState?.let { renderRating(it) }
      showFromTraktLoading?.let {
        showDetailsRemoveTraktButton.isLoading = it
        showDetailsAddButton.isEnabled = !it
      }
      removeFromTraktHistory?.let { event ->
        event.consume()?.let {
          showDetailsAddButton.fadeIf(!it, hardware = true)
          showDetailsRemoveTraktButton.run {
            fadeIf(it, hardware = true)
            onYesClickListener = { viewModel.removeFromTraktHistory() }
          }
        }
      }
      removeFromTraktWatchlist?.let { event ->
        event.consume()?.let {
          showDetailsAddButton.fadeIf(!it, hardware = true)
          showDetailsRemoveTraktButton.run {
            fadeIf(it, hardware = true)
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

  private fun renderRatings(ratings: Ratings, show: Show?) {
    if (showDetailsRatings.isBound()) return
    showDetailsRatings.bind(ratings)
    show?.let {
      showDetailsRatings.onTraktClick = { openShowLink(TRAKT, show.traktId.toString()) }
      showDetailsRatings.onImdbClick = { openShowLink(IMDB, show.ids.imdb.id) }
      showDetailsRatings.onMetaClick = { openShowLink(METACRITIC, show.title) }
      showDetailsRatings.onRottenClick = {
        val url = it.rottenTomatoesUrl
        if (!url.isNullOrBlank()) {
          openWebUrl(url) ?: openShowLink(ROTTEN, "${show.title} ${show.year}")
        } else {
          openShowLink(ROTTEN, "${show.title} ${show.year}")
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
          showEpisodeDetails(show, episode, null, isWatched = false, showButton = false, showTabs = false)
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

  private fun renderActors(actors: List<Actor>) {
    if (actorsAdapter?.itemCount != 0) return
    actorsAdapter?.setItems(actors)
    showDetailsActorsRecycler.visibleIf(actors.isNotEmpty())
    showDetailsActorsEmptyView.visibleIf(actors.isEmpty())
    showDetailsActorsProgress.gone()
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
        showSnack(MessageEvent.info(R.string.textSeasonsEmpty))
      }
    }
  }

  private fun renderStreamings(streamings: StreamingsBundle) {
    if (streamingAdapter?.itemCount != 0) return
    val (items, isLocal) = streamings
    streamingAdapter?.setItems(items)
    if (items.isNotEmpty()) {
      if (isLocal) {
        showDetailsStreamingsRecycler.visible()
      } else {
        showDetailsStreamingsRecycler.fadeIn(withHardware = true)
      }
    } else if (!isLocal) {
      showDetailsStreamingsRecycler.gone()
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

  private fun openIMDbLink(id: IdImdb, type: String) {
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse("imdb:///$type/${id.id}")
    try {
      startActivity(i)
    } catch (e: ActivityNotFoundException) {
      // IMDb App not installed. Start in web browser
      openWebUrl("http://www.imdb.com/$type/${id.id}") ?: showSnack(MessageEvent.info(R.string.errorCouldNotFindApp))
    }
  }

  private fun openShowLink(
    link: ShowLink,
    id: String,
    country: AppCountry = UNITED_STATES,
  ) {
    if (link == IMDB) {
      openIMDbLink(IdImdb(id), "title")
    } else {
      openWebUrl(link.getUri(id, country)) ?: showSnack(MessageEvent.info(R.string.errorCouldNotFindApp))
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

  private fun openDeleteCommentDialog(comment: Comment) {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setTitle(R.string.textCommentConfirmDeleteTitle)
      .setMessage(R.string.textCommentConfirmDelete)
      .setPositiveButton(R.string.textYes) { _, _ -> viewModel.deleteComment(comment) }
      .setNegativeButton(R.string.textNo) { _, _ -> }
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
    navigateTo(R.id.actionShowDetailsFragmentToManageLists, bundle)
  }

  override fun setupBackPressed() {
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
