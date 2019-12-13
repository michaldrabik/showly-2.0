package com.michaldrabik.showly2.ui.show.seasons.episodes.details

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.appComponent
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.model.IdTvdb
import com.michaldrabik.showly2.ui.common.base.BaseBottomSheetFragment
import com.michaldrabik.showly2.ui.common.views.CommentView
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.gone
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.toDisplayString
import com.michaldrabik.showly2.utilities.extensions.toLocalTimeZone
import com.michaldrabik.showly2.utilities.extensions.visible
import com.michaldrabik.showly2.utilities.extensions.visibleIf
import com.michaldrabik.showly2.utilities.extensions.withFailListener
import kotlinx.android.synthetic.main.view_episode_details.*
import kotlinx.android.synthetic.main.view_episode_details.view.*
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class EpisodeDetailsBottomSheet : BaseBottomSheetFragment<EpisodeDetailsViewModel>() {

  companion object {
    private const val ARG_ID_TRAKT = "ARG_ID_TRAKT"
    private const val ARG_ID_TVDB = "ARG_ID_TVDB"
    private const val ARG_NUMBER = "ARG_NUMBER"
    private const val ARG_SEASON = "ARG_SEASON"
    private const val ARG_TITLE = "ARG_TITLE"
    private const val ARG_OVERVIEW = "ARG_OVERVIEW"
    private const val ARG_DATE = "ARG_DATE"
    private const val ARG_IS_WATCHED = "ARG_IS_WATCHED"
    private const val ARG_SHOW_BUTTON = "ARG_SHOW_BUTTON"

    fun create(
      showId: IdTrakt,
      episode: Episode,
      isWatched: Boolean,
      showButton: Boolean = true
    ): EpisodeDetailsBottomSheet {
      val bundle = Bundle().apply {
        putLong(ARG_ID_TRAKT, showId.id)
        putLong(ARG_ID_TVDB, episode.ids.tvdb.id)
        putString(ARG_TITLE, episode.title)
        putString(ARG_OVERVIEW, episode.overview)
        putInt(ARG_SEASON, episode.season)
        putInt(ARG_NUMBER, episode.number)
        putLong(ARG_DATE, episode.firstAired?.toInstant()?.toEpochMilli() ?: -1)
        putBoolean(ARG_IS_WATCHED, isWatched)
        putBoolean(ARG_SHOW_BUTTON, showButton)
      }
      return EpisodeDetailsBottomSheet().apply { arguments = bundle }
    }
  }

  var onEpisodeWatchedClick: ((Boolean) -> Unit)? = null

  private val episodeTraktId by lazy { IdTrakt(arguments!!.getLong(ARG_ID_TRAKT)) }
  private val episodeTvdbId by lazy { IdTvdb(arguments!!.getLong(ARG_ID_TVDB)) }
  private val episodeTitle by lazy { arguments!!.getString(ARG_TITLE, "") }
  private val episodeOverview by lazy { arguments!!.getString(ARG_OVERVIEW, "") }
  private val episodeNumber by lazy { arguments!!.getInt(ARG_NUMBER) }
  private val episodeSeason by lazy { arguments!!.getInt(ARG_SEASON) }
  private val episodeDate by lazy { arguments!!.getLong(ARG_DATE, -1) }
  private val isWatched by lazy { arguments!!.getBoolean(ARG_IS_WATCHED) }
  private val showButton by lazy { arguments!!.getBoolean(ARG_SHOW_BUTTON) }

  private val cornerRadius by lazy { requireContext().dimenToPx(R.dimen.episodeDetailsCorner).toFloat() }

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
    viewModel.run {
      uiStream.observe(viewLifecycleOwner, Observer { render(it!!) })
      loadImage(episodeTvdbId)
    }
    setupView(view)
  }

  private fun setupView(view: View) {
    view.run {
      val date = getDateString()
      episodeDetailsName.text =
        context.getString(R.string.textSeasonEpisodeDate, episodeSeason, episodeNumber, date)
      episodeDetailsTitle.text = episodeTitle
      episodeDetailsOverview.text =
        if (episodeOverview.isBlank()) getString(R.string.textNoDescription) else episodeOverview
      episodeDetailsButton.run {
        visibleIf(showButton)
        setImageResource(if (isWatched) R.drawable.ic_eye else R.drawable.ic_check)
        onClick {
          onEpisodeWatchedClick?.invoke(!isWatched)
          dismiss()
        }
      }
      episodeDetailsCommentsButton.onClick {
        episodeDetailsCommentsButton.gone()
        viewModel.loadComments(episodeTraktId, episodeSeason, episodeNumber)
      }
    }
  }

  private fun getDateString() =
    if (episodeDate == -1L) {
      "TBA"
    } else {
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(episodeDate), ZoneId.of("UTC"))
        .toLocalTimeZone()
        .toDisplayString()
    }

  private fun render(uiModel: EpisodeDetailsUiModel) {
    uiModel.run {
      imageLoading?.let { episodeDetailsProgress.visibleIf(it) }
      image?.let {
        Glide.with(this@EpisodeDetailsBottomSheet)
          .load("${Config.TVDB_IMAGE_BASE_BANNERS_URL}${it.fileUrl}")
          .transform(CenterCrop(), GranularRoundedCorners(cornerRadius, cornerRadius, 0F, 0F))
          .transition(DrawableTransitionOptions.withCrossFade(IMAGE_FADE_DURATION_MS))
          .withFailListener { episodeDetailsImagePlaceholder.visible() }
          .into(episodeDetailsImage)
      }
      commentsLoading?.let {
        episodeDetailsCommentsProgress.visibleIf(it)
      }
      comments?.let { comments ->
        episodeDetailsComments.removeAllViews()
        comments.forEach {
          val view = CommentView(requireContext()).apply {
            bind(it)
          }
          episodeDetailsComments.addView(view)
        }
        episodeDetailsCommentsLabel.visibleIf(comments.isNotEmpty())
        episodeDetailsComments.visibleIf(comments.isNotEmpty())
      }
    }
  }

  override fun onDismiss(dialog: DialogInterface) {
    onEpisodeWatchedClick = null
    super.onDismiss(dialog)
  }
}