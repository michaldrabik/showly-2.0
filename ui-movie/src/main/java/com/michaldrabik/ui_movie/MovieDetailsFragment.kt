package com.michaldrabik.ui_movie

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
import android.view.ViewGroup
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.Config.INITIAL_RATING
import com.michaldrabik.common.Config.TMDB_IMAGE_BASE_ACTOR_FULL_URL
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
import com.michaldrabik.ui_model.Actor
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily.MOVIE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_movie.actors.ActorsAdapter
import com.michaldrabik.ui_movie.di.UiMovieDetailsComponentProvider
import com.michaldrabik.ui_movie.helpers.MovieLink
import com.michaldrabik.ui_movie.helpers.MovieLink.IMDB
import com.michaldrabik.ui_movie.helpers.MovieLink.JUST_WATCH
import com.michaldrabik.ui_movie.related.RelatedListItem
import com.michaldrabik.ui_movie.related.RelatedMovieAdapter
import com.michaldrabik.ui_movie.views.AddToMoviesButton.State.ADD
import com.michaldrabik.ui_movie.views.AddToMoviesButton.State.IN_MY_MOVIES
import com.michaldrabik.ui_movie.views.AddToMoviesButton.State.IN_WATCHLIST
import com.michaldrabik.ui_movie.views.AddToMoviesButton.State.UPCOMING
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_CUSTOM_IMAGE_CLEARED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_FAMILY
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CUSTOM_IMAGE
import kotlinx.android.synthetic.main.fragment_movie_details.*
import kotlinx.android.synthetic.main.fragment_movie_details_actor_full_view.*
import kotlinx.android.synthetic.main.view_links_movie_menu.view.*
import java.util.Locale.ENGLISH

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
class MovieDetailsFragment : BaseFragment<MovieDetailsViewModel>(R.layout.fragment_movie_details) {

  override val viewModel by viewModels<MovieDetailsViewModel> { viewModelFactory }

  private val movieId by lazy { IdTrakt(requireArguments().getLong(ARG_MOVIE_ID, -1)) }

  private val actorsAdapter by lazy { ActorsAdapter() }
  private val relatedAdapter by lazy { RelatedMovieAdapter() }

  private val imageHeight by lazy {
    if (resources.configuration.orientation == ORIENTATION_PORTRAIT) screenHeight()
    else screenWidth()
  }
  private val imageRatio by lazy { resources.getString(R.string.detailsImageRatio).toFloat() }
  private val imagePadded by lazy { resources.getBoolean(R.bool.detailsImagePadded) }

  private val actorViewCorner by lazy { requireContext().dimenToPx(R.dimen.actorMovieFullTileCorner) }
  private val animationEnterRight by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_in_from_right) }
  private val animationExitRight by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_out_from_right) }
  private val animationEnterLeft by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_in_from_left) }
  private val animationExitLeft by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_out_from_left) }

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiMovieDetailsComponentProvider).provideMovieDetailsComponent().inject(this)
    super.onCreate(savedInstanceState)
    setFragmentResultListener(REQUEST_CUSTOM_IMAGE) { _, bundle ->
      viewModel.loadBackgroundImage()
      if (!bundle.getBoolean(ARG_CUSTOM_IMAGE_CLEARED)) showCustomImages(movieId.id, true)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
    setupView()
    setupStatusBar()
    setupActorsList()
    setupRelatedList()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, { showSnack(it) })
      if (!isInitialized) {
        loadDetails(movieId, requireAppContext())
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
    movieDetailsImageGuideline.setGuidelineBegin((imageHeight * imageRatio).toInt())
    listOf(movieDetailsBackArrow, movieDetailsBackArrow2).onClick { requireActivity().onBackPressed() }
    movieDetailsImage.onClick {
      val bundle = bundleOf(
        ARG_MOVIE_ID to movieId.id,
        ARG_FAMILY to MOVIE,
        ARG_TYPE to FANART
      )
      navigateTo(R.id.actionMovieDetailsFragmentToArtGallery, bundle)
      Analytics.logMovieGalleryClick(movieId.id)
    }
    movieDetailsCommentsButton.onClick {
      movieDetailsCommentsView.clear()
      showCommentsView()
      viewModel.loadComments()
    }
    movieDetailsAddButton.run {
      isEnabled = false
      onAddMyMoviesClickListener = {
        viewModel.addFollowedMovie(requireAppContext())
      }
      onAddWatchLaterClickListener = { viewModel.addWatchlistMovie(requireAppContext()) }
      onRemoveClickListener = { viewModel.removeFromFollowed(requireAppContext()) }
    }
    movieDetailsRemoveTraktButton.onNoClickListener = {
      movieDetailsAddButton.fadeIn()
      movieDetailsRemoveTraktButton.fadeOut()
    }
  }

  private fun setupStatusBar() {
    movieDetailsBackArrow.doOnApplyWindowInsets { view, insets, _, _ ->
      if (imagePadded) movieDetailsMainLayout.updatePadding(top = insets.systemWindowInsetTop)
      arrayOf<View>(view, movieDetailsBackArrow2, movieDetailsCommentsView)
        .forEach { v ->
          (v.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(top = insets.systemWindowInsetTop)
        }
    }
  }

  private fun setupActorsList() {
    val context = requireContext()
    movieDetailsActorsRecycler.apply {
      setHasFixedSize(true)
      adapter = actorsAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
    actorsAdapter.itemClickListener = {
      showFullActorView(it)
    }
  }

  private fun setupRelatedList() {
    val context = requireContext()
    movieDetailsRelatedRecycler.apply {
      setHasFixedSize(true)
      adapter = relatedAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addDivider(R.drawable.divider_horizontal_list, HORIZONTAL)
    }
    relatedAdapter.missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    relatedAdapter.itemClickListener = {
      val bundle = Bundle().apply { putLong(ARG_MOVIE_ID, it.movie.ids.trakt.id) }
      navigateTo(R.id.actionMovieDetailsFragmentToSelf, bundle)
    }
  }

  private fun showCommentsView() {
    movieDetailsCommentsView.run {
      fadeIn(275)
      startAnimation(animationEnterRight)
    }
    movieDetailsMainLayout.run {
      fadeOut(200)
      startAnimation(animationExitRight)
    }
    movieDetailsBackArrow.crossfadeTo(movieDetailsBackArrow2)
  }

  private fun hideExtraView(view: View) {
    if (view.animation != null) return

    view.run {
      fadeOut(300)
      startAnimation(animationExitLeft)
    }
    movieDetailsMainLayout.run {
      fadeIn()
      startAnimation(animationEnterLeft)
    }
    movieDetailsBackArrow2.crossfadeTo(movieDetailsBackArrow)
  }

  private fun showFullActorView(actor: Actor) {
    Glide.with(this)
      .load("$TMDB_IMAGE_BASE_ACTOR_FULL_URL${actor.image}")
      .transform(CenterCrop(), RoundedCorners(actorViewCorner))
      .into(movieDetailsActorFullImage)

    val actorView = movieDetailsActorsRecycler.findViewWithTag<View>(actor.tmdbId)
    val transform = MaterialContainerTransform().apply {
      startView = actorView
      endView = movieDetailsActorFullContainer
      scrimColor = TRANSPARENT
      addTarget(movieDetailsActorFullContainer)
    }
    TransitionManager.beginDelayedTransition(movieDetailsRoot, transform)
    actorView.gone()
    movieDetailsActorFullImdb.apply {
      val imdbId = actor.imdbId != null
      visibleIf(imdbId)
      if (imdbId) onClick { openIMDbLink(IdImdb(actor.imdbId!!), "name") }
    }
    movieDetailsActorFullName.apply {
      text = getString(R.string.textActorRole, actor.name, actor.role)
      fadeIn()
    }
    movieDetailsActorFullContainer.apply {
      tag = actor
      onClick { hideFullActorView(actor) }
      visible()
    }
    movieDetailsActorFullMask.apply {
      onClick { hideFullActorView(actor) }
      fadeIn()
    }
  }

  private fun hideFullActorView(actor: Actor) {
    val actorView = movieDetailsActorsRecycler.findViewWithTag<View>(actor.tmdbId)
    val transform = MaterialContainerTransform().apply {
      startView = movieDetailsActorFullContainer
      endView = actorView
      scrimColor = TRANSPARENT
      addTarget(actorView)
    }
    TransitionManager.beginDelayedTransition(movieDetailsRoot, transform)
    movieDetailsActorFullContainer.gone()
    actorView.visible()
    movieDetailsActorFullMask.fadeOut()
    movieDetailsActorFullName.fadeOut()
  }

  private fun showCustomImages(movieId: Long, isPremium: Boolean?) {
    if (isPremium == false) {
      navigateTo(R.id.actionMovieDetailsFragmentToPremium)
      return
    }
    val bundle = bundleOf(
      ARG_MOVIE_ID to movieId,
      ARG_FAMILY to MOVIE
    )
    navigateTo(R.id.actionMovieDetailsFragmentToCustomImages, bundle)
  }

  private fun render(uiModel: MovieDetailsUiModel) {
    uiModel.run {
      movie?.let { movie ->
        movieDetailsTitle.text = movie.title
        movieDetailsDescription.setTextIfEmpty(if (movie.overview.isNotBlank()) movie.overview else getString(R.string.textNoDescription))
        movieDetailsStatus.text = getString(movie.status.displayName)

        val releaseDate =
          if (movie.released != null) String.format(ENGLISH, "%s", dateFormat?.format(movie.released)?.capitalizeWords())
          else movie.year.toString()

        val country = if (movie.country.isNotBlank()) String.format(ENGLISH, "(%s)", movie.country) else ""
        movieDetailsExtraInfo.text = getString(
          R.string.textMovieExtraInfo,
          releaseDate,
          country.toUpperCase(),
          movie.runtime.toString(),
          getString(R.string.textMinutesShort),
          renderGenres(movie.genres)
        )
        movieDetailsRating.text = String.format(ENGLISH, getString(R.string.textMovieVotes), movie.rating, movie.votes)
        movieDetailsCommentsButton.visible()
        movieDetailsShareButton.run {
          isEnabled = movie.ids.imdb.id.isNotBlank()
          alpha = if (isEnabled) 1.0F else 0.35F
          onClick { openShareSheet(movie) }
        }
        movieDetailsTrailerButton.run {
          isEnabled = movie.trailer.isNotBlank()
          alpha = if (isEnabled) 1.0F else 0.35F
          onClick {
            openWebUrl(movie.trailer)
            Analytics.logMovieTrailerClick(movie)
          }
        }
        movieDetailsLinksButton.run {
          onClick {
            openLinksMenu(movie, uiModel.country ?: UNITED_STATES)
            Analytics.logMovieLinksClick(movie)
          }
        }
        movieDetailsSeparator4.visible()
        movieDetailsCustomImagesLabel.visible()
        movieDetailsCustomImagesLabel.onClick { showCustomImages(movie.traktId, isPremium) }
        movieDetailsAddButton.isEnabled = true
      }
      movieLoading?.let {
        if (!movieDetailsCommentsView.isVisible) {
          movieDetailsMainLayout.fadeIf(!it)
          movieDetailsMainProgress.visibleIf(it)
        }
      }
      followedState?.let {
        when {
          it.isMyMovie -> movieDetailsAddButton.setState(IN_MY_MOVIES, it.withAnimation)
          it.isWatchlist -> movieDetailsAddButton.setState(IN_WATCHLIST, it.withAnimation)
          it.isUpcoming -> movieDetailsAddButton.setState(UPCOMING, it.withAnimation)
          else -> movieDetailsAddButton.setState(ADD, it.withAnimation)
        }
        (requireAppContext() as WidgetsProvider).requestMoviesWidgetsUpdate()
      }
      image?.let { renderImage(it) }
      actors?.let { renderActors(it) }
      translation?.let { renderTranslation(it) }
      relatedMovies?.let { renderRelatedMovies(it) }
      comments?.let {
        movieDetailsCommentsView.bind(it, dateFormat)
      }
      ratingState?.let { renderRating(it) }
      showFromTraktLoading?.let {
        movieDetailsRemoveTraktButton.isLoading = it
        movieDetailsAddButton.isEnabled = !it
      }
      removeFromTraktHistory?.let { event ->
        event.consume()?.let {
          movieDetailsAddButton.fadeIf(!it)
          movieDetailsRemoveTraktButton.run {
            fadeIf(it)
            onYesClickListener = { viewModel.removeFromTraktHistory() }
          }
        }
      }
      removeFromTraktWatchlist?.let { event ->
        event.consume()?.let {
          movieDetailsAddButton.fadeIf(!it)
          movieDetailsRemoveTraktButton.run {
            fadeIf(it)
            onYesClickListener = { viewModel.removeFromTraktWatchlist() }
          }
        }
      }
    }
  }

  private fun renderGenres(genres: List<String>) =
    genres
      .take(3)
      .mapNotNull { Genre.fromSlug(it) }
      .joinToString(", ") { getString(it.displayName) }

  private fun renderRating(rating: RatingState) {
    movieDetailsRateButton.visibleIf(rating.rateLoading == false, gone = false)
    movieDetailsRateProgress.visibleIf(rating.rateLoading == true)

    movieDetailsRateButton.text =
      if (rating.hasRating()) "${rating.userRating?.rating}/10"
      else getString(R.string.textMovieRate)

    val typeFace = if (rating.hasRating()) BOLD else NORMAL
    movieDetailsRateButton.setTypeface(null, typeFace)

    movieDetailsRateButton.onClick {
      if (rating.rateAllowed == true) {
        val rate = rating.userRating?.rating ?: INITIAL_RATING
        openRateDialog(rate, rate != 0)
      } else {
        showSnack(MessageEvent.info(R.string.textSignBeforeRateMovie))
      }
    }
  }

  private fun renderImage(image: Image) {
    if (image.status == UNAVAILABLE) {
      movieDetailsImageProgress.gone()
      movieDetailsPlaceholder.visible()
      movieDetailsImage.isClickable = false
      movieDetailsImage.isEnabled = false
      return
    }
    Glide.with(this)
      .load(image.fullFileUrl)
      .transform(CenterCrop())
      .transition(withCrossFade(IMAGE_FADE_DURATION_MS))
      .withFailListener {
        movieDetailsImageProgress.gone()
        movieDetailsPlaceholder.visible()
        movieDetailsImage.isClickable = true
        movieDetailsImage.isEnabled = true
      }
      .withSuccessListener {
        movieDetailsImageProgress.gone()
        movieDetailsPlaceholder.gone()
      }
      .into(movieDetailsImage)
  }

  private fun renderActors(actors: List<Actor>) {
    actorsAdapter.setItems(actors)
    movieDetailsActorsRecycler.fadeIf(actors.isNotEmpty())
    movieDetailsActorsEmptyView.fadeIf(actors.isEmpty())
    movieDetailsActorsProgress.gone()
  }

  private fun renderRelatedMovies(items: List<RelatedListItem>) {
    relatedAdapter.setItems(items)
    movieDetailsRelatedRecycler.fadeIf(items.isNotEmpty())
    movieDetailsRelatedLabel.fadeIf(items.isNotEmpty())
    movieDetailsRelatedProgress.gone()
  }

  private fun renderTranslation(translation: Translation?) {
    if (translation?.overview?.isNotBlank() == true) {
      movieDetailsDescription.text = translation.overview
    }
    if (translation?.title?.isNotBlank() == true) {
      movieDetailsTitle.text = translation.title.capitalizeWords()
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

  private fun openMovieLink(link: MovieLink, id: String, country: AppCountry = UNITED_STATES) {
    if (link == IMDB) {
      openIMDbLink(IdImdb(id), "title")
    } else {
      openWebUrl(link.getUri(id, country))
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun openLinksMenu(movie: Movie, country: AppCountry) {
    val ids = movie.ids
    movieDetailsMainLayout.setOnTouchListener { _, event ->
      if (event.action == MotionEvent.ACTION_DOWN) {
        movieDetailsLinksMenu.fadeOut()
        movieDetailsMainLayout.setOnTouchListener(null)
      }
      false
    }
    movieDetailsLinksMenu.run {
      fadeIn(125)
      viewLinkTrakt.visibleIf(ids.trakt.id != -1L)
      viewLinkTrakt.onClick {
        openMovieLink(MovieLink.TRAKT, ids.trakt.id.toString())
        fadeOut(125)
      }
      viewLinkTmdb.visibleIf(ids.tmdb.id != -1L)
      viewLinkTmdb.onClick {
        openMovieLink(MovieLink.TMDB, ids.tmdb.id.toString())
        fadeOut(125)
      }
      viewLinkImdb.visibleIf(ids.imdb.id.isNotBlank())
      viewLinkImdb.onClick {
        openMovieLink(IMDB, ids.imdb.id)
        fadeOut(125)
      }
      viewLinkJustWatch.onClick {
        openMovieLink(JUST_WATCH, movie.title, country)
        fadeOut(125)
      }
    }
  }

  private fun openShareSheet(movie: Movie) {
    val intent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, "Hey! Check out ${movie.title}:\nhttp://www.imdb.com/title/${movie.ids.imdb.id}")
      type = "text/plain"
    }

    val shareIntent = Intent.createChooser(intent, "Share ${movie.title}")
    startActivity(shareIntent)

    Analytics.logMovieShareClick(movie)
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

  private fun handleBackPressed() {
    val dispatcher = requireActivity().onBackPressedDispatcher
    dispatcher.addCallback(viewLifecycleOwner) {
      when {
        movieDetailsCommentsView.isVisible -> {
          hideExtraView(movieDetailsCommentsView)
          return@addCallback
        }
        movieDetailsActorFullContainer.isVisible -> {
          hideFullActorView(movieDetailsActorFullContainer.tag as Actor)
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
