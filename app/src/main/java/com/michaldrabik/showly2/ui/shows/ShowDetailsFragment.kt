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
import com.michaldrabik.showly2.utilities.fadeIn
import com.michaldrabik.showly2.utilities.gone
import com.michaldrabik.showly2.utilities.nowUtc
import com.michaldrabik.showly2.utilities.onClick
import com.michaldrabik.showly2.utilities.visible
import com.michaldrabik.showly2.utilities.visibleIf
import com.michaldrabik.showly2.utilities.withFailListener
import com.michaldrabik.showly2.utilities.withSuccessListener
import kotlinx.android.synthetic.main.fragment_show_details.*
import org.threeten.bp.Duration
import kotlin.math.absoluteValue

@SuppressLint("SetTextI18n", "DefaultLocale")
class ShowDetailsFragment : BaseFragment<ShowDetailsViewModel>() {

  private val showId by lazy { arguments?.getLong("id", -1) ?: -1 }

  override val layoutResId = R.layout.fragment_show_details

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
        maxLines = if (maxLines == 100) 3 else 100
        showDetailsMoreButton.setText(if (maxLines == 100) R.string.buttonShowLess else R.string.buttonShowMore)
      }
    }
  }

  private fun render(uiModel: ShowDetailsUiModel) {
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
      val daysToAir = timeToAir.toDays().toInt().absoluteValue
      if (daysToAir == 0) {
        val hoursToAir = timeToAir.toHours().toInt().absoluteValue
        showDetailsEpisodeAirtime.text = resources.getQuantityString(R.plurals.textHoursToAir, hoursToAir, hoursToAir)
        return
      }
      showDetailsEpisodeAirtime.text = resources.getQuantityString(R.plurals.textDaysToAir, daysToAir, daysToAir)
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
