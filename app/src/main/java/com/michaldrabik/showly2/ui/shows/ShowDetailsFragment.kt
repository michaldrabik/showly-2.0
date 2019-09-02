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
import com.michaldrabik.showly2.ui.common.base.BaseFragment
import com.michaldrabik.showly2.utilities.*
import kotlinx.android.synthetic.main.fragment_show_details.*
import org.threeten.bp.Duration

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
        maxLines = if (maxLines == 100) 5 else 100
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
    uiModel.nextEpisode?.let {
      val timeToAir = Duration.between(nowUtc(), it.firstAired)
      showDetailsEpisodeText.text = "${it.toDisplayString()} airs in ${timeToAir}."
      showDetailsEpisodeCard.fadeIn()
    }
    uiModel.imageLoading?.let { showDetailsImageProgress.visibleIf(it) }
    uiModel.image?.let {
      showDetailsImageProgress.visible()
      Glide.with(this)
        .load("$TVDB_IMAGE_BASE_URL${it.fileUrl}")
        .transform(CenterCrop())
        .transition(withCrossFade(200))
        .withFailListener { showDetailsImageProgress.gone() }
        .withSuccessListener { showDetailsImageProgress.gone() }
        .into(showDetailsImage)
    }
  }
}
