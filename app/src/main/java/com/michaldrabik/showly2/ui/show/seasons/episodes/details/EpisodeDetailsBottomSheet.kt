package com.michaldrabik.showly2.ui.show.seasons.episodes.details

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.showly2.Config
import com.michaldrabik.showly2.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.fragmentComponent
import com.michaldrabik.showly2.model.Episode
import com.michaldrabik.showly2.model.IdTrakt
import com.michaldrabik.showly2.ui.common.base.BaseBottomSheetFragment
import com.michaldrabik.showly2.ui.common.views.CommentView
import com.michaldrabik.showly2.ui.common.views.RateView
import com.michaldrabik.showly2.ui.common.views.RateView.Companion.INITIAL_RATING
import com.michaldrabik.showly2.utilities.extensions.dimenToPx
import com.michaldrabik.showly2.utilities.extensions.fadeIf
import com.michaldrabik.showly2.utilities.extensions.onClick
import com.michaldrabik.showly2.utilities.extensions.showShortInfoSnackbar
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
    private const val ARG_EPISODE = "ARG_EPISODE"
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
        putParcelable(ARG_EPISODE, episode)
        putBoolean(ARG_IS_WATCHED, isWatched)
        putBoolean(ARG_SHOW_BUTTON, showButton)
      }
      return EpisodeDetailsBottomSheet().apply { arguments = bundle }
    }
  }

  var onEpisodeWatchedClick: ((Boolean) -> Unit)? = null

  private val episodeTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_ID_TRAKT)) }
  private val episode by lazy { requireArguments().getParcelable<Episode>(ARG_EPISODE)!! }
  private val isWatched by lazy { requireArguments().getBoolean(ARG_IS_WATCHED) }
  private val showButton by lazy { requireArguments().getBoolean(ARG_SHOW_BUTTON) }

  private val cornerRadius by lazy { requireContext().dimenToPx(R.dimen.episodeDetailsCorner).toFloat() }

  override val layoutResId = R.layout.view_episode_details

  override fun getTheme(): Int = R.style.BottomSheetDialogTheme

  override fun onCreate(savedInstanceState: Bundle?) {
    fragmentComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(EpisodeDetailsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, Observer { render(it) })
      messageLiveData.observe(viewLifecycleOwner, Observer { renderSnackbar(it) })
      loadImage(episode.ids.tvdb)
      loadRatings(episode)
    }
    setupView(view)
  }

  private fun setupView(view: View) {
    view.run {
      val date = getDateString()
      episodeDetailsName.text =
        context.getString(R.string.textSeasonEpisodeDate, episode.season, episode.number, date)
      episodeDetailsTitle.text = episode.title
      episodeDetailsOverview.text =
        if (episode.overview.isBlank()) getString(R.string.textNoDescription) else episode.overview
      episodeDetailsButton.run {
        visibleIf(showButton)
        setImageResource(if (isWatched) R.drawable.ic_eye else R.drawable.ic_check)
        onClick {
          onEpisodeWatchedClick?.invoke(!isWatched)
          dismiss()
        }
      }
      episodeDetailsRatingLayout.visibleIf(episode.votes > 0)
      episodeDetailsRating.text = String.format("%.1f (%d votes)", episode.rating, episode.votes)
      episodeDetailsCommentsButton.onClick {
        viewModel.loadComments(episodeTraktId, episode.season, episode.number)
      }
    }
  }

  private fun getDateString(): String {
    val millis = episode.firstAired?.toInstant()?.toEpochMilli() ?: -1
    return if (millis == -1L) {
      "TBA"
    } else {
      ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("UTC"))
        .toLocalTimeZone()
        .toDisplayString()
    }
  }

  private fun openRateDialog(rating: Int) {
    val context = requireContext()
    val rateView = RateView(context).apply {
      setPadding(context.dimenToPx(R.dimen.spaceNormal))
      setRating(rating)
    }
    MaterialAlertDialogBuilder(context, R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
      .setView(rateView)
      .setPositiveButton(R.string.textRate) { _, _ -> viewModel.addRating(rateView.getRating(), episode) }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .show()
  }

  @SuppressLint("SetTextI18n")
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
        episodeDetailsButtons.visibleIf(!it && comments?.isEmpty() == true)
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
        episodeDetailsCommentsLabel.fadeIf(comments.isNotEmpty())
        episodeDetailsComments.fadeIf(comments.isNotEmpty())
        episodeDetailsCommentsEmpty.fadeIf(comments.isEmpty())
      }
      ratingState?.let { state ->
        episodeDetailsRateProgress.visibleIf(state.rateLoading == true)
        episodeDetailsRateButton.visibleIf(state.rateLoading == false)
        episodeDetailsRateButton.onClick {
          if (state.rateAllowed == true) openRateDialog(state.userRating?.rating ?: INITIAL_RATING)
          else renderSnackbar(R.string.textSignBeforeRate)
        }
        if (state.hasRating()) {
          episodeDetailsRateButton.text = "${state.userRating?.rating}/10"
          episodeDetailsRateButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        } else {
          episodeDetailsRateButton.setText(R.string.textRate)
          episodeDetailsRateButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextPrimary))
        }
      }
    }
  }

  private fun renderSnackbar(stringRes: Int) =
    episodeDetailsSnackbarHost.showShortInfoSnackbar(getString(stringRes))

  override fun onDismiss(dialog: DialogInterface) {
    onEpisodeWatchedClick = null
    super.onDismiss(dialog)
  }
}
