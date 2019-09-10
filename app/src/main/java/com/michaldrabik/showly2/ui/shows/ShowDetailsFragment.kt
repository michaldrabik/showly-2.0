package com.michaldrabik.showly2.ui.shows

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.showly2.Config.TVDB_IMAGE_BASE_URL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.ui.shows.actors.ActorsAdapter
import com.michaldrabik.showly2.ui.shows.related.RelatedShowAdapter
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.fadeIn
import com.michaldrabik.showly2.utilities.extensions.getQuantityString
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.nowUtc
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.screenHeight
import com.michaldrabik.showly2.utilities.extensions.showInfoSnackbar
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import com.michaldrabik.showly2.utilities.extensions.withFailListener
import com.michaldrabik.showly2.utilities.extensions.withSuccessListener
import kotlinx.android.synthetic.main.fragment_show_details.*
import kotlinx.android.synthetic.main.fragment_show_details_next_episode.*
import org.threeten.bp.Duration

@SuppressLint("SetTextI18n", "DefaultLocale")
class ShowDetailsFragment : BaseFragment<ShowDetailsViewModel>() {

  companion object {
    const val ARG_SHOW_ID = "ARG_SHOW_ID"

    private const val OVERVIEW_MIN_LINES = 3
    private const val OVERVIEW_MAX_LINES = 100
  }

  override val layoutResId = R.layout.fragment_show_details

  private val showId by lazy { arguments?.getLong(ARG_SHOW_ID, -1) ?: -1 }
  private val actorsAdapter by lazy { ActorsAdapter() }
  private val relatedAdapter by lazy { RelatedShowAdapter() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    appComponent().inject(this)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(ShowDetailsViewModel::class.java)
      .apply {
        uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupActorsList()
    setupRelatedList()
    viewModel.loadShowDetails(showId)
  }

  private fun setupView() {
    showDetailsImageGuideline.setGuidelineBegin((screenHeight() * 0.33).toInt())
    showDetailsDescription.onClick { toggleDescription() }
    showDetailsMoreButton.onClick { toggleDescription() }
    showDetailsBackArrow.onClick { requireActivity().onBackPressed() }
  }

  private fun setupActorsList() {
    val context = requireContext()
    showDetailsActorsRecycler.apply {
      adapter = actorsAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_actors)!!)
      })
    }
    actorsAdapter.onItemClickListener = {
      showDetailsRoot.showInfoSnackbar(getString(R.string.textActorRole, it.name, it.role))
    }
  }

  private fun setupRelatedList() {
    val context = requireContext()
    showDetailsRelatedRecycler.apply {
      adapter = relatedAdapter
      layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
      addItemDecoration(DividerItemDecoration(context, HORIZONTAL).apply {
        setDrawable(ContextCompat.getDrawable(context, R.drawable.divider_related_shows)!!)
      })
    }
    relatedAdapter.missingImageListener = { ids, force -> viewModel.loadMissingImage(ids, force) }
  }

  private fun toggleDescription() {
    showDetailsDescription.apply {
      maxLines = if (maxLines == OVERVIEW_MAX_LINES) OVERVIEW_MIN_LINES else OVERVIEW_MAX_LINES
      showDetailsMoreButton.setText(if (maxLines == OVERVIEW_MAX_LINES) R.string.buttonShowLess else R.string.buttonShowMore)
    }
  }

  private fun render(uiModel: ShowDetailsUiModel) {
    uiModel.showLoading?.let {
      showDetailsMainLayout.visibleIf(!it)
      showDetailsMainProgress.visibleIf(it)
    }
    uiModel.show?.let { show ->
      showDetailsTitle.text = show.title
      showDetailsDescription.text = show.overview
      showDetailsStatus.text = show.status.split(" ").joinToString(" ") { it.capitalize() }
      showDetailsExtraInfo.text =
        "${show.network} | ${show.runtime} min | ${show.genres.take(2).joinToString(", ") { it.capitalize() }}"
      showDetailsRating.text = String.format("%.1f (%d votes)", show.rating, show.votes)
    }
    uiModel.nextEpisode?.let { renderNextEpisode(it) }
    uiModel.imageLoading?.let { showDetailsImageProgress.visibleIf(it) }
    uiModel.image?.let { renderImage(it) }
    uiModel.actors?.let {
      actorsAdapter.setItems(it)
      showDetailsActorsRecycler.fadeIf(it.isNotEmpty())
    }
    uiModel.relatedShows?.let {
      relatedAdapter.setItems(it)
      showDetailsRelatedRecycler.fadeIf(it.isNotEmpty())
    }
    uiModel.updateRelatedShow?.let { relatedAdapter.updateItem(it) }
  }

  private fun renderNextEpisode(nextEpisode: Episode) {
    nextEpisode.run {
      showDetailsEpisodeText.text = "${toDisplayString()} - '$title'"
      showDetailsEpisodeCard.fadeIn()

      val timeToAir = Duration.between(nowUtc(), firstAired)
      if (timeToAir.seconds < 0) {
        showDetailsEpisodeAirtime.text = getString(R.string.textAiredAlready)
        return
      }
      val days = timeToAir.toDays()
      if (days == 0L) {
        val hours = timeToAir.toHours()
        if (hours == 0L) {
          val minutes = timeToAir.toMinutes()
          showDetailsEpisodeAirtime.text = getQuantityString(R.plurals.textMinutesToAir, minutes)
          return
        }
        showDetailsEpisodeAirtime.text = getQuantityString(R.plurals.textHoursToAir, hours)
        return
      }
      showDetailsEpisodeAirtime.text = getQuantityString(R.plurals.textDaysToAir, days)
    }
  }

  private fun renderImage(image: Image) {
    showDetailsImageProgress.visible()
    Glide.with(this)
      .load("$TVDB_IMAGE_BASE_URL${image.fileUrl}")
      .transform(CenterCrop())
      .transition(withCrossFade(200))
      .withFailListener { showDetailsImageProgress.gone() }
      .withSuccessListener { showDetailsImageProgress.gone() }
      .into(showDetailsImage)
  }
}
