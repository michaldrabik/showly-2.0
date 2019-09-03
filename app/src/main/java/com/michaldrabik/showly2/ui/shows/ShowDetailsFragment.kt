package com.michaldrabik.showly2.ui.shows

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.michaldrabik.showly2.Config.TVDB_IMAGE_BASE_URL
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.Image
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.fragment_show_details.*
import kotlinx.android.synthetic.main.fragment_show_details_next_episode.*
import org.threeten.bp.Duration

@SuppressLint("SetTextI18n", "DefaultLocale")
class ShowDetailsFragment : BaseFragment<ShowDetailsViewModel>() {

  companion object {
    private const val OVERVIEW_MIN_LINES = 3
    private const val OVERVIEW_MAX_LINES = 100
  }

  override val layoutResId = R.layout.fragment_show_details

  private val showId by lazy { arguments?.getLong("id", -1) ?: -1 }

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
    viewModel.loadShowDetails(showId)
  }

  private fun setupView() {
    showDetailsMoreButton.onClick {
      showDetailsDescription.apply {
        maxLines = if (maxLines == OVERVIEW_MAX_LINES) OVERVIEW_MIN_LINES else OVERVIEW_MAX_LINES
        showDetailsMoreButton.setText(if (maxLines == OVERVIEW_MAX_LINES) R.string.buttonShowLess else R.string.buttonShowMore)
      }
    }
    showDetailsBackArrow.onClick { requireActivity().onBackPressed() }
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
  }

  private fun renderNextEpisode(nextEpisode: Episode) {
    nextEpisode.run {
      showDetailsEpisodeText.text = "${toDisplayString()} - '$title'"
      showDetailsEpisodeCard.fadeIn()

      val timeToAir = Duration.between(nowUtc(), firstAired)
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
