package com.michaldrabik.ui_episodes.details

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.Config.INITIAL_RATING
import com.michaldrabik.common.extensions.toDisplayString
import com.michaldrabik.common.extensions.toLocalTimeZone
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.common.views.RateView
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.MessageEvent.Companion.info
import com.michaldrabik.ui_base.utilities.MessageEvent.Type.ERROR
import com.michaldrabik.ui_base.utilities.MessageEvent.Type.INFO
import com.michaldrabik.ui_base.utilities.extensions.colorFromAttr
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.setTextFade
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_comments.CommentView
import com.michaldrabik.ui_episodes.R
import com.michaldrabik.ui_episodes.details.di.UiEpisodeDetailsComponentProvider
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTrakt
import kotlinx.android.synthetic.main.view_episode_details.*
import kotlinx.android.synthetic.main.view_episode_details.view.*
import java.util.Locale.ENGLISH

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

  private val showTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_ID_TRAKT)) }
  private val episode by lazy { requireArguments().getParcelable<Episode>(ARG_EPISODE)!! }
  private val isWatched by lazy { requireArguments().getBoolean(ARG_IS_WATCHED) }
  private val showButton by lazy { requireArguments().getBoolean(ARG_SHOW_BUTTON) }

  private val cornerRadius by lazy { requireContext().dimenToPx(R.dimen.bottomSheetCorner).toFloat() }

  override val layoutResId = R.layout.view_episode_details

  override fun getTheme(): Int = R.style.BottomSheetDialogTheme

  override fun onCreate(savedInstanceState: Bundle?) {
    (requireActivity() as UiEpisodeDetailsComponentProvider).provideEpisodeDetailsComponent().inject(this)
    super.onCreate(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.view_episode_details, container, false)
  }

  override fun createViewModel() =
    ViewModelProvider(this, viewModelFactory).get(EpisodeDetailsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.run {
      uiLiveData.observe(viewLifecycleOwner, { render(it) })
      messageLiveData.observe(viewLifecycleOwner, { renderSnackbar(it) })
      loadTranslation(showTraktId, episode)
      loadImage(episode.ids.tvdb)
      loadRatings(episode)
    }
    setupView(view)
  }

  private fun setupView(view: View) {
    view.run {
      val date = getDateString()
      episodeDetailsName.text =
        String.format(ENGLISH, context.getString(R.string.textSeasonEpisodeDate), episode.season, episode.number, date)
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
      episodeDetailsRating.text = String.format(ENGLISH, getString(R.string.textVotes), episode.rating, episode.votes)
      episodeDetailsCommentsButton.text = String.format(ENGLISH, getString(R.string.textLoadCommentsCount), episode.commentCount)
      episodeDetailsCommentsButton.onClick {
        viewModel.loadComments(showTraktId, episode.season, episode.number)
      }
    }
  }

  private fun getDateString(): String {
    val millis = episode.firstAired?.toInstant()?.toEpochMilli() ?: -1
    return if (millis == -1L) {
      getString(R.string.textTba)
    } else {
      com.michaldrabik.common.extensions.dateFromMillis(millis)
        .toLocalTimeZone()
        .toDisplayString()
    }
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
      .setPositiveButton(R.string.textRate) { _, _ -> viewModel.addRating(rateView.getRating(), episode, showTraktId) }
      .setNegativeButton(R.string.textCancel) { _, _ -> }
      .apply {
        if (showRemove) {
          setNeutralButton(R.string.textRateDelete) { _, _ -> viewModel.deleteRating(episode) }
        }
      }
      .show()
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiModel: EpisodeDetailsUiModel) {
    uiModel.run {
      imageLoading?.let { episodeDetailsProgress.visibleIf(it) }
      image?.let {
        Glide.with(this@EpisodeDetailsBottomSheet)
          .load(it.fullFileUrl)
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
          if (state.rateAllowed == true) {
            val rate = state.userRating?.rating ?: INITIAL_RATING
            openRateDialog(rate, rate != 0)
          } else {
            renderSnackbar(info(R.string.textSignBeforeRate))
          }
        }
        if (state.hasRating()) {
          episodeDetailsRateButton.text = "${state.userRating?.rating}/10"
          episodeDetailsRateButton.setTextColor(requireContext().colorFromAttr(android.R.attr.colorAccent))
        } else {
          episodeDetailsRateButton.setText(R.string.textRate)
          episodeDetailsRateButton.setTextColor(requireContext().colorFromAttr(android.R.attr.textColorPrimary))
        }
      }
      translation?.let { t ->
        t.consume()?.let {
          if (it.overview.isNotBlank()) {
            episodeDetailsOverview.setTextFade(it.overview, 0)
            if (it.title.isNotBlank()) {
              episodeDetailsTitle.setTextFade(it.title, 0)
            }
          }
        }
      }
    }
  }

  private fun renderSnackbar(message: MessageEvent) {
    message.consume()?.let {
      when (message.type) {
        INFO -> episodeDetailsSnackbarHost.showInfoSnackbar(getString(it))
        ERROR -> episodeDetailsSnackbarHost.showErrorSnackbar(getString(it))
      }
    }
  }

  override fun onDismiss(dialog: DialogInterface) {
    onEpisodeWatchedClick = null
    super.onDismiss(dialog)
  }
}
