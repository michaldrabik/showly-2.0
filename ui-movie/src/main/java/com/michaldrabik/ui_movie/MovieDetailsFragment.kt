package com.michaldrabik.ui_movie

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateMargins
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.snackbar.Snackbar
import com.michaldrabik.common.Config
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.Analytics
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.common.WidgetsProvider
import com.michaldrabik.ui_base.common.sheets.links.LinksBottomSheet
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Operation
import com.michaldrabik.ui_base.common.sheets.ratings.RatingsBottomSheet.Options.Type
import com.michaldrabik.ui_base.common.sheets.remove_trakt.RemoveTraktBottomSheet
import com.michaldrabik.ui_base.utilities.SnackbarHost
import com.michaldrabik.ui_base.utilities.events.Event
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.copyToClipboard
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
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
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_base.utilities.extensions.withSuccessListener
import com.michaldrabik.ui_comments.fragment.CommentsFragment
import com.michaldrabik.ui_model.Genre
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Image
import com.michaldrabik.ui_model.ImageFamily.MOVIE
import com.michaldrabik.ui_model.ImageStatus.UNAVAILABLE
import com.michaldrabik.ui_model.ImageType.FANART
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Person
import com.michaldrabik.ui_model.PremiumFeature
import com.michaldrabik.ui_model.RatingState
import com.michaldrabik.ui_model.Translation
import com.michaldrabik.ui_movie.views.AddToMoviesButton.State.ADD
import com.michaldrabik.ui_movie.views.AddToMoviesButton.State.IN_HIDDEN
import com.michaldrabik.ui_movie.views.AddToMoviesButton.State.IN_MY_MOVIES
import com.michaldrabik.ui_movie.views.AddToMoviesButton.State.IN_WATCHLIST
import com.michaldrabik.ui_navigation.java.NavigationArgs
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_CUSTOM_IMAGE_CLEARED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_FAMILY
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_ITEM
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_PERSON
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_TYPE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_CUSTOM_IMAGE
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_MANAGE_LISTS
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_PERSON_DETAILS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsAddButton
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsBackArrow
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsCommentsButton
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsCustomImagesLabel
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsDescription
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsExtraInfo
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsHideLabel
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsImage
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsImageGuideline
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsImageProgress
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsLinksButton
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsMainContent
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsMainLayout
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsMainProgress
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsManageListsLabel
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsPlaceholder
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsPremiumAd
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsRateButton
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsRateProgress
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsSeparator5
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsShareButton
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsStatus
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsStreamingsFragment
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsTitle
import kotlinx.android.synthetic.main.fragment_movie_details.movieDetailsTrailerButton
import timber.log.Timber
import java.util.Locale.ENGLISH
import java.util.Locale.ROOT

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
@AndroidEntryPoint
class MovieDetailsFragment : BaseFragment<MovieDetailsViewModel>(R.layout.fragment_movie_details) {

  override val viewModel by viewModels<MovieDetailsViewModel>()
  override val navigationId = R.id.movieDetailsFragment

  private val movieId by lazy { IdTrakt(requireLong(ARG_MOVIE_ID)) }

  private val imageHeight by lazy {
    if (resources.configuration.orientation == ORIENTATION_PORTRAIT) screenHeight()
    else screenWidth()
  }
  private val imageRatio by lazy { resources.getString(R.string.detailsImageRatio).toFloat() }
  private val imagePadded by lazy { resources.getBoolean(R.bool.detailsImagePadded) }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
    setupView()
    setupStatusBar()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { renderSnack(it) } },
      { viewModel.eventFlow.collect { handleEvent(it) } },
      doAfterLaunch = {
        if (!isInitialized) {
          viewModel.loadDetails(movieId)
          isInitialized = true
        }
        viewModel.loadPremium()
      }
    )

    setFragmentResultListener(REQUEST_PERSON_DETAILS) { _, bundle ->
      bundle.getParcelable<Person>(ARG_PERSON)?.let {
        viewModel.onPersonDetails(it)
      }
    }
  }

  private fun setupView() {
    hideNavigation()
    movieDetailsImageGuideline.setGuidelineBegin((imageHeight * imageRatio).toInt())
    movieDetailsBackArrow.onClick { requireActivity().onBackPressed() }
    movieDetailsImage.onClick {
      val bundle = bundleOf(
        ARG_MOVIE_ID to movieId.id,
        ARG_FAMILY to MOVIE,
        ARG_TYPE to FANART
      )
      navigateToSafe(R.id.actionMovieDetailsFragmentToArtGallery, bundle)
      Analytics.logMovieGalleryClick(movieId.id)
    }
    movieDetailsAddButton.run {
      isEnabled = false
      onAddMyMoviesClickListener = {
        viewModel.addFollowedMovie()
      }
      onAddWatchLaterClickListener = { viewModel.addWatchlistMovie() }
      onRemoveClickListener = { viewModel.removeFromFollowed() }
    }
    movieDetailsManageListsLabel.onClick { openListsDialog() }
    movieDetailsHideLabel.onClick { viewModel.addHiddenMovie() }
    movieDetailsTitle.onClick {
      requireContext().copyToClipboard(movieDetailsTitle.text.toString())
      showSnack(MessageEvent.Info(R.string.textCopiedToClipboard))
    }
    movieDetailsPremiumAd.onClick {
      navigateTo(R.id.actionMovieDetailsFragmentToPremium)
    }
  }

  private fun setupStatusBar() {
    movieDetailsBackArrow.doOnApplyWindowInsets { view, insets, _, _ ->
      val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
      if (imagePadded) {
        movieDetailsMainLayout
          .updatePadding(top = inset)
      } else {
        (movieDetailsShareButton.layoutParams as ViewGroup.MarginLayoutParams)
          .updateMargins(top = inset)
      }
      (view.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(top = inset)
    }
  }

  private fun render(uiState: MovieDetailsUiState) {
    uiState.run {
      movie?.let { movie ->
        movieDetailsTitle.text = movie.title
        movieDetailsDescription.setTextIfEmpty(movie.overview.ifBlank { getString(R.string.textNoDescription) })
        movieDetailsStatus.text = getString(movie.status.displayName)

        val releaseDate =
          when {
            movie.released != null -> String.format(ENGLISH, "%s", meta?.dateFormat?.format(movie.released)?.capitalizeWords())
            movie.year > 0 -> movie.year.toString()
            else -> ""
          }

        val country = if (movie.country.isNotBlank()) String.format(ENGLISH, "(%s)", movie.country) else ""
        movieDetailsExtraInfo.text = getString(
          R.string.textMovieExtraInfo,
          releaseDate,
          country.uppercase(ROOT),
          movie.runtime.toString(),
          getString(R.string.textMinutesShort),
          renderGenres(movie.genres)
        )
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
            openWebUrl(movie.trailer) ?: showSnack(MessageEvent.Info(R.string.errorCouldNotFindApp))
            Analytics.logMovieTrailerClick(movie)
          }
        }
        movieDetailsLinksButton.run {
          onClick {
            val args = LinksBottomSheet.createBundle(movie)
            navigateTo(R.id.actionMovieDetailsFragmentToLinks, args)
          }
        }
        movieDetailsSeparator5.visible()
        movieDetailsCustomImagesLabel.visibleIf(Config.SHOW_PREMIUM)
        movieDetailsCustomImagesLabel.onClick { openCustomImagesSheet(movie.traktId, meta?.isPremium) }
        movieDetailsCommentsButton.onClick {
          val bundle = CommentsFragment.createBundle(movie)
          navigateToSafe(R.id.actionMovieDetailsFragmentToComments, bundle)
        }
        movieDetailsAddButton.isEnabled = true
      }
      movieLoading?.let {
        movieDetailsMainLayout.fadeIf(!it, hardware = true)
        movieDetailsMainProgress.visibleIf(it)
      }
      followedState?.let {
        when {
          it.isMyMovie -> movieDetailsAddButton.setState(IN_MY_MOVIES, it.withAnimation)
          it.isWatchlist -> movieDetailsAddButton.setState(IN_WATCHLIST, it.withAnimation)
          it.isHidden -> movieDetailsAddButton.setState(IN_HIDDEN, it.withAnimation)
          else -> movieDetailsAddButton.setState(ADD, it.withAnimation)
        }
        movieDetailsHideLabel.visibleIf(!it.isHidden)
        (requireAppContext() as WidgetsProvider).requestMoviesWidgetsUpdate()
      }
      image?.let { renderImage(it) }
      translation?.let { renderTranslation(it) }
      listsCount?.let {
        val text =
          if (it > 0) getString(R.string.textMovieManageListsCount, it)
          else getString(R.string.textMovieManageLists)
        movieDetailsManageListsLabel.text = text
      }
      ratingState?.let { renderRating(it) }
      meta?.isPremium.let {
        movieDetailsPremiumAd.visibleIf(it != true)
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
        openRateDialog()
      } else {
        showSnack(MessageEvent.Info(R.string.textSignBeforeRateMovie))
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

  private fun renderTranslation(translation: Translation?) {
    if (translation?.overview?.isNotBlank() == true) {
      movieDetailsDescription.text = translation.overview
    }
    if (translation?.title?.isNotBlank() == true) {
      movieDetailsTitle.text = translation.title
    }
  }

  private fun renderSnack(event: MessageEvent) {
    if (event.textResId == R.string.errorMalformedMovie) {
      event.consume()?.let {
        val host = (requireActivity() as SnackbarHost).provideSnackbarLayout()
        val snack = host.showInfoSnackbar(getString(it), length = Snackbar.LENGTH_INDEFINITE) {
          viewModel.removeMalformedMovie(movieId)
        }
        snackbars.add(snack)
      }
      return
    }
    showSnack(event)
  }

  private fun handleEvent(event: Event<*>) {
    when (event) {
      is MovieDetailsEvent.Finish -> requireActivity().onBackPressed()
      is MovieDetailsEvent.RemoveFromTrakt -> openRemoveTraktSheet(event.navigationId)
    }
  }

  private fun openRemoveTraktSheet(@IdRes action: Int) {
    setFragmentResultListener(NavigationArgs.REQUEST_REMOVE_TRAKT) { _, bundle ->
      if (bundle.getBoolean(NavigationArgs.RESULT, false)) {
        val text = resources.getString(R.string.textTraktSyncMovieRemovedFromTrakt)
        (requireActivity() as SnackbarHost).provideSnackbarLayout().showInfoSnackbar(text)
      }
    }
    val args = RemoveTraktBottomSheet.createBundle(movieId, RemoveTraktBottomSheet.Mode.MOVIE)
    navigateTo(action, args)
  }

  private fun openShareSheet(movie: Movie) {
    val intent = Intent().apply {
      val text = "Hey! Check out ${movie.title}:\nhttps://trakt.tv/movies/${movie.ids.slug.id}\nhttps://www.imdb.com/title/${movie.ids.imdb.id}"
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, text)
      type = "text/plain"
    }

    val shareIntent = Intent.createChooser(intent, "Share ${movie.title}")
    startActivity(shareIntent)

    Analytics.logMovieShareClick(movie)
  }

  private fun openRateDialog() {
    setFragmentResultListener(NavigationArgs.REQUEST_RATING) { _, bundle ->
      when (bundle.getParcelable<Operation>(NavigationArgs.RESULT)) {
        Operation.SAVE -> renderSnack(MessageEvent.Info(R.string.textRateSaved))
        Operation.REMOVE -> renderSnack(MessageEvent.Info(R.string.textRateRemoved))
        else -> Timber.w("Unknown result.")
      }
      viewModel.loadUserRating()
    }
    val bundle = RatingsBottomSheet.createBundle(movieId, Type.MOVIE)
    navigateTo(R.id.actionMovieDetailsFragmentToRating, bundle)
  }

  private fun openListsDialog() {
    setFragmentResultListener(REQUEST_MANAGE_LISTS) { _, _ -> viewModel.loadListsCount() }
    val bundle = bundleOf(
      ARG_ID to movieId.id,
      ARG_TYPE to Mode.MOVIES.type
    )
    navigateToSafe(R.id.actionMovieDetailsFragmentToManageLists, bundle)
  }

  private fun openCustomImagesSheet(movieId: Long, isPremium: Boolean?) {
    if (isPremium == false) {
      val args = bundleOf(ARG_ITEM to PremiumFeature.CUSTOM_IMAGES)
      navigateTo(R.id.actionMovieDetailsFragmentToPremium, args)
      return
    }

    setFragmentResultListener(REQUEST_CUSTOM_IMAGE) { _, bundle ->
      viewModel.loadBackgroundImage()
      if (!bundle.getBoolean(ARG_CUSTOM_IMAGE_CLEARED)) openCustomImagesSheet(movieId, true)
    }

    val bundle = bundleOf(
      ARG_MOVIE_ID to movieId,
      ARG_FAMILY to MOVIE
    )
    navigateToSafe(R.id.actionMovieDetailsFragmentToCustomImages, bundle)
  }

  fun showStreamingsView(animate: Boolean) {
    if (!animate) {
      movieDetailsStreamingsFragment.visible()
      return
    }
    val animation = ConstraintSet().apply {
      clone(movieDetailsMainContent)
      setVisibility(movieDetailsStreamingsFragment.id, View.VISIBLE)
    }
    val transition = AutoTransition().apply {
      interpolator = DecelerateInterpolator(1.5F)
      duration = 200
    }
    TransitionManager.beginDelayedTransition(movieDetailsMainContent, transition)
    animation.applyTo(movieDetailsMainContent)
  }
}
