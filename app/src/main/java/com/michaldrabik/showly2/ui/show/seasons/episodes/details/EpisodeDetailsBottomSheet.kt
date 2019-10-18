package com.michaldrabik.showly2.ui.show.seasons.episodes.details

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.ui.common.base.BaseBottomSheetFragment
import com.michaldrabik.showly2.utilities.extensions.*
import kotlinx.android.synthetic.main.view_episode_details.*
import kotlinx.android.synthetic.main.view_episode_details.view.*

class EpisodeDetailsBottomSheet : BaseBottomSheetFragment<EpisodeDetailsViewModel>() {

  companion object {
    private const val ARG_ID = "ARG_ID"
    private const val ARG_NUMBER = "ARG_NUMBER"
    private const val ARG_SEASON = "ARG_SEASON"
    private const val ARG_TITLE = "ARG_TITLE"
    private const val ARG_OVERVIEW = "ARG_OVERVIEW"
    private const val ARG_IS_WATCHED = "ARG_IS_WATCHED"
    private const val ARG_SHOW_BUTTON = "ARG_SHOW_BUTTON"

    fun create(
      episode: Episode,
      isWatched: Boolean,
      showButton: Boolean = true
    ): EpisodeDetailsBottomSheet {
      val bundle = Bundle().apply {
        putLong(ARG_ID, episode.ids.tvdb)
        putString(ARG_TITLE, episode.title)
        putString(ARG_OVERVIEW, episode.overview)
        putInt(ARG_SEASON, episode.season)
        putInt(ARG_NUMBER, episode.number)
        putBoolean(ARG_IS_WATCHED, isWatched)
        putBoolean(ARG_SHOW_BUTTON, showButton)
      }
      return EpisodeDetailsBottomSheet().apply { arguments = bundle }
    }
  }

  var onEpisodeWatchedClick: ((Boolean) -> Unit) = {}

  private val episodeTvdbId by lazy { arguments!!.getLong(ARG_ID) }
  private val episodeTitle by lazy { arguments!!.getString(ARG_TITLE, "") }
  private val episodeOverview by lazy { arguments!!.getString(ARG_OVERVIEW, "") }
  private val episodeNumber by lazy { arguments!!.getInt(ARG_NUMBER) }
  private val episodeSeason by lazy { arguments!!.getInt(ARG_SEASON) }
  private val isWatched by lazy { arguments!!.getBoolean(ARG_IS_WATCHED) }
  private val showButton by lazy { arguments!!.getBoolean(ARG_SHOW_BUTTON) }

  private val cornerRadius by lazy { requireContext().dimenToPx(R.dimen.episodeDetailsCorner) }

  override val layoutResId = R.layout.view_episode_details

  override fun getTheme(): Int = R.style.BottomSheetDialogTheme

  override fun onCreate(savedInstanceState: Bundle?) {
    appComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(EpisodeDetailsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
    viewModel.loadImage(episodeTvdbId)

    view.run {
      episodeDetailsName.text =
        context.getString(R.string.textSeasonEpisode, episodeSeason, episodeNumber)
      episodeDetailsTitle.text = episodeTitle
      episodeDetailsOverview.text =
        if (episodeOverview.isBlank()) getString(R.string.textNoDescription) else episodeOverview
      episodeDetailsButton.run {
        visibleIf(showButton)
        setImageResource(if (isWatched) R.drawable.ic_eye else R.drawable.ic_check)
        onClick {
          onEpisodeWatchedClick.invoke(!isWatched)
          dismiss()
        }
      }
    }
  }

  private fun render(uiModel: EpisodeDetailsUiModel) {
    uiModel.imageLoading?.let { episodeDetailsProgress.visibleIf(it) }
    uiModel.image?.let {
      Glide.with(this)
        .load("${Config.TVDB_IMAGE_BASE_URL}${it.fileUrl}")
        .transform(CenterCrop(), RoundedCorners(cornerRadius))
        .transition(DrawableTransitionOptions.withCrossFade(200))
        .withSuccessListener { episodeDetailsProgress.gone() }
        .withFailListener {
          episodeDetailsProgress.gone()
          episodeDetailsImagePlaceholder.visible()
        }
        .into(episodeDetailsImage)
    }
  }

  override fun onDismiss(dialog: DialogInterface) {
    onEpisodeWatchedClick = {}
    super.onDismiss(dialog)
  }
}