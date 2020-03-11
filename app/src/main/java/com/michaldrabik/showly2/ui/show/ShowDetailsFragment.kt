package com.michaldrabik.showly2.ui.show

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.google.android.material.transition.MaterialContainerTransform
import com.michaldrabik.showly2.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.showly2.Config.TVDB_IMAGE_BASE_BANNERS_URL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.model.Actor
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.IdImdb
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.model.Image.Status.UNAVAILABLE
import com.michaldrabik.showly2.model.Season
import com.michaldrabik.showly2.model.Show
import com.michaldrabik.showly2.model.Tip.SHOW_DETAILS_GALLERY
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.common.views.AddToShowsButton.State.ADD
import com.michaldrabik.showly2.ui.common.views.AddToShowsButton.State.IN_MY_SHOWS
import com.michaldrabik.showly2.ui.common.views.AddToShowsButton.State.IN_WATCH_LATER
import com.michaldrabik.showly2.ui.show.actors.ActorsAdapter
import com.michaldrabik.showly2.ui.show.gallery.FanartGalleryFragment
import com.michaldrabik.showly2.ui.show.related.RelatedListItem
import com.michaldrabik.showly2.ui.show.related.RelatedShowAdapter
import com.michaldrabik.showly2.ui.show.seasons.SeasonListItem
import com.michaldrabik.showly2.ui.show.seasons.SeasonsAdapter
import com.michaldrabik.showly2.ui.show.seasons.episodes.details.EpisodeDetailsBottomSheet
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.fadeOut
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.screenHeight
import com.michaldrabik.showly2.utilities.extensions.screenWidth
import com.michaldrabik.showly2.utilities.extensions.toDisplayString
import com.michaldrabik.showly2.utilities.extensions.toLocalTimeZone
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import com.michaldrabik.showly2.utilities.extensions.withFailListener
import com.michaldrabik.showly2.utilities.extensions.withSuccessListener
import com.michaldrabik.showly2.widget.watchlist.WatchlistWidgetProvider
import kotlinx.android.synthetic.main.fragment_show_details.*
import kotlinx.android.synthetic.main.fragment_show_details_actor_full_view.*
import kotlinx.android.synthetic.main.fragment_show_details_next_episode.*
import org.threeten.bp.Duration

@SuppressLint("SetTextI18n", "DefaultLocale")
class ShowDetailsFragment : BaseFragment<ShowDetailsViewModel>(R.layout.fragment_show_details) {

  companion object {
    const val ARG_SHOW_ID = "ARG_SHOW_ID"
  }

  override val viewModel by viewModels<ShowDetailsViewModel> { viewModelFactory }

  private val showId by lazy { IdTrakt(arguments?.getLong(ARG_SHOW_ID, -1) ?: -1) }

  private val actorsAdapter by lazy { ActorsAdapter() }
  private val relatedAdapter by lazy { RelatedShowAdapter() }
  private val seasonsAdapter by lazy { SeasonsAdapter() }

  private val imageHeight by lazy {
    if (resources.configuration.orientation == ORIENTATION_PORTRAIT) screenHeight()
    else screenWidth()
  }
  private val actorViewCorner by lazy { requireContext().dimenToPx(R.dimen.actorFullTileCorner) }
  private val animationEnterRight by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_in_from_right) }
  private val animationExitRight by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_out_from_right) }

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT
    setupView()
    setupActorsList()
    setupRelatedList()
    setupSeasonsList()

    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it!!) })
      messageLiveData.observe(viewLifecycleOwner, Observer { showInfoSnackbar(it!!) })
      errorLiveData.observe(viewLifecycleOwner, Observer { showErrorSnackbar(it!!) })
      loadShowDetails(showId, requireContext().applicationContext)
    }
  }

  override fun onResume() {
    super.onResume()
    handleBackPressed()
  }

  private fun setupView() {
    showDetailsImageGuideline.setGuidelineBegin((imageHeight * 0.33).toInt())
    showDetailsEpisodesView.itemClickListener = { episode, season, isWatched ->
      showEpisodeDetails(episode, season, isWatched, episode.hasAired(season))
    }
    showDetailsBackArrow.onClick { requireActivity().onBackPressed() }
    showDetailsImage.onClick {
      val bundle = Bundle().apply { putLong(FanartGalleryFragment.ARG_SHOW_ID, showId.id) }
      navigateTo(R.id.actionShowDetailsFragmentToFanartGallery, bundle)
    }
    showDetailsCommentsButton.onClick {
      showDetailsCommentsView.clear()
      showCommentsView()
      viewModel.loadComments()
    }
    showDetailsTipGallery.onClick {
      it.gone()
      mainActivity().showTip(SHOW_DETAILS_GALLERY)
    }
  }

  private fun setupActorsList() {
    val context = requireContext()
    showDetailsActorsRecycler.apply {
      setHasFixedSize(true)
      adapter = actorsAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_actors)!!)
      })
    }
    actorsAdapter.itemClickListener = { showFullActorView(it) }
  }

  private fun setupRelatedList() {
    val context = requireContext()
    showDetailsRelatedRecycler.apply {
      setHasFixedSize(true)
      adapter = relatedAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_related_shows)!!)
      })
    }
    relatedAdapter.missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
    relatedAdapter.itemClickListener = {
      val bundle = Bundle().apply { putLong(ARG_SHOW_ID, it.show.ids.trakt.id) }
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
      viewModel.setWatchedSeason(item.season, isChecked)
    }
  }

  private fun showEpisodesView(item: SeasonListItem) {
    showDetailsEpisodesView.run {
      bind(item)
      fadeIn(275) {
        bindEpisodes(item.episodes)
      }
      startAnimation(animationEnterRight)
      itemCheckedListener = { episode, season, isChecked ->
        viewModel.setWatchedEpisode(episode, season, isChecked)
      }
      seasonCheckedListener = { season, isChecked ->
        viewModel.setWatchedSeason(season, isChecked)
      }
    }
    showDetailsMainLayout.run {
      fadeOut()
      startAnimation(animationExitRight)
    }
  }

  private fun showCommentsView() {
    showDetailsCommentsView.run {
      fadeIn(275)
      startAnimation(animationEnterRight)
    }
    showDetailsMainLayout.run {
      fadeOut()
      startAnimation(animationExitRight)
    }
  }

  private fun hideExtraView(view: View) {
    val animationEnter = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_in_from_left)
    val animationExit = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_slide_out_from_left)

    view.run {
      fadeOut()
      startAnimation(animationExit)
    }
    showDetailsMainLayout.run {
      fadeIn()
      startAnimation(animationEnter)
    }
  }

  private fun showEpisodeDetails(
    episode: Episode,
    season: Season?,
    isWatched: Boolean,
    showButton: Boolean = true
  ) {
    val modal = EpisodeDetailsBottomSheet.create(showId, episode, isWatched, showButton)
    if (season != null) {
      modal.onEpisodeWatchedClick = { viewModel.setWatchedEpisode(episode, season, it) }
    }
    modal.show(requireActivity().supportFragmentManager, "MODAL")
  }

  private fun showFullActorView(actor: Actor) {
    Glide.with(this)
      .load("$TVDB_IMAGE_BASE_BANNERS_URL${actor.image}")
      .onlyRetrieveFromCache(true)
      .transform(CenterCrop(), RoundedCorners(actorViewCorner))
      .into(showDetailsActorFullImage)

    val actorView = showDetailsActorsRecycler.findViewWithTag<View>(actor.id)
    val transform = MaterialContainerTransform(requireContext()).apply {
      startView = actorView
      endView = showDetailsActorFullContainer
      scrimColor = TRANSPARENT
    }
    TransitionManager.beginDelayedTransition(showDetailsRoot, transform)
    actorView.gone()
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
    val actorView = showDetailsActorsRecycler.findViewWithTag<View>(actor.id)
    val transform = MaterialContainerTransform(requireContext()).apply {
      startView = showDetailsActorFullContainer
      endView = actorView
      scrimColor = TRANSPARENT
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
        showDetailsDescription.text = show.overview
        showDetailsStatus.text = show.status.displayName
        val year = if (show.year > 0) show.year.toString() else ""
        showDetailsExtraInfo.text =
          "${show.network} $year | ${show.runtime} min | ${show.genres.take(2).joinToString(", ") { it.capitalize() }}"
        showDetailsRating.text = String.format("%.1f (%d votes)", show.rating, show.votes)
        showDetailsCommentsButton.visible()

        showDetailsShareIcon.run {
          visibleIf(show.ids.imdb.id.isNotBlank())
          onClick { openShareSheet(show) }
        }

        showDetailsTrailerButton.run {
          visibleIf(show.trailer.isNotBlank())
          onClick { openTrailerLink(show.trailer) }
        }

        showDetailsImdbButton.run {
          visibleIf(show.ids.imdb.id.isNotBlank())
          onClick { openIMDbLink(show.ids.imdb) }
        }

        showDetailsAddButton.run {
          onAddMyShowsClickListener = { viewModel.addFollowedShow(requireContext().applicationContext) }
          onAddWatchLaterClickListener = { viewModel.addWatchLaterShow() }
          onRemoveClickListener = { viewModel.removeFromFollowed(requireContext().applicationContext) }
        }
      }
      showLoading?.let {
        if (!showDetailsEpisodesView.isVisible && !showDetailsCommentsView.isVisible) {
          showDetailsMainLayout.fadeIf(!it)
          showDetailsMainProgress.visibleIf(it)
        }
      }
      isFollowed?.let {
        when {
          it.isMyShows -> showDetailsAddButton.setState(IN_MY_SHOWS, it.withAnimation)
          it.isWatchLater -> showDetailsAddButton.setState(IN_WATCH_LATER, it.withAnimation)
          else -> showDetailsAddButton.setState(ADD, it.withAnimation)
        }
      }
      nextEpisode?.let { renderNextEpisode(it) }
      image?.let { renderImage(it) }
      actors?.let { renderActors(it) }
      seasons?.let {
        renderSeasons(it)
        renderRuntimeLeft(it)
        WatchlistWidgetProvider.requestUpdate(requireContext())
      }
      relatedShows?.let { renderRelatedShows(it) }
      comments?.let { showDetailsCommentsView.bind(it) }
    }
  }

  private fun renderImage(image: Image) {
    if (image.status == UNAVAILABLE) {
      showDetailsImageProgress.gone()
      showDetailsImage.isClickable = false
      showDetailsImage.isEnabled = false
      return
    }
    Glide.with(this)
      .load("$TVDB_IMAGE_BASE_BANNERS_URL${image.fileUrl}")
      .transform(CenterCrop())
      .transition(withCrossFade(IMAGE_FADE_DURATION_MS))
      .withFailListener {
        showDetailsImageProgress.gone()
        showDetailsImage.isClickable = true
        showDetailsImage.isEnabled = true
      }
      .withSuccessListener {
        showDetailsImageProgress.gone()
        showDetailsTipGallery.fadeIf(!mainActivity().isTipShown(SHOW_DETAILS_GALLERY))
      }
      .into(showDetailsImage)
  }

  private fun renderNextEpisode(nextEpisode: Episode) {
    nextEpisode.run {
      showDetailsEpisodeText.text = toDisplayString()
      showDetailsEpisodeCard.visible()
      showDetailsEpisodeCard.onClick {
        showEpisodeDetails(nextEpisode, null, isWatched = false, showButton = false)
      }

      nextEpisode.firstAired?.let {
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
  }

  private fun renderRuntimeLeft(seasonsItems: List<SeasonListItem>) {
    val runtimeLeft = seasonsItems
      .flatMap { it.episodes }
      .filterNot { it.isWatched }
      .sumBy { it.episode.runtime }
      .toLong()

    val duration = Duration.ofMinutes(runtimeLeft)
    val hours = duration.toHours()
    val minutes = duration.minusHours(hours).toMinutes()

    val runtimeText = when {
      hours <= 0 -> getString(R.string.textRuntimeLeftMinutes, minutes)
      else -> getString(R.string.textRuntimeLeftHours, hours, minutes)
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

  private fun openTrailerLink(url: String) {
    Intent(Intent.ACTION_VIEW).apply {
      data = Uri.parse(url)
      startActivity(this)
    }
  }

  private fun openIMDbLink(id: IdImdb) {
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse("imdb:///title/${id.id}")
    try {
      startActivity(i)
    } catch (e: ActivityNotFoundException) {
      // IMDb App not installed. Start in web browser
      i.data = Uri.parse("http://www.imdb.com/title/${id.id}")
      startActivity(i)
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
          showNavigation()
          findNavController().popBackStack()
        }
      }
    }
  }

  override fun getSnackbarHost(): ViewGroup = showDetailsRoot
}
