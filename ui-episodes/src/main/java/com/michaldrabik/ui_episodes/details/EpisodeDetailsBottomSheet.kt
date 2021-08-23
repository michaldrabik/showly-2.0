package com.michaldrabik.ui_episodes.details

import android.annotation.SuppressLint
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.NORMAL
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.setPadding
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.michaldrabik.common.Config.IMAGE_FADE_DURATION_MS
import com.michaldrabik.common.Config.INITIAL_RATING
import com.michaldrabik.common.extensions.dateFromMillis
import com.michaldrabik.common.extensions.toLocalZone
import com.michaldrabik.ui_base.BaseBottomSheetFragment
import com.michaldrabik.ui_base.common.views.RateView
import com.michaldrabik.ui_base.utilities.MessageEvent
import com.michaldrabik.ui_base.utilities.MessageEvent.Companion.info
import com.michaldrabik.ui_base.utilities.MessageEvent.Type.ERROR
import com.michaldrabik.ui_base.utilities.MessageEvent.Type.INFO
import com.michaldrabik.ui_base.utilities.extensions.capitalizeWords
import com.michaldrabik.ui_base.utilities.extensions.dimenToPx
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.setTextFade
import com.michaldrabik.ui_base.utilities.extensions.showErrorSnackbar
import com.michaldrabik.ui_base.utilities.extensions.showInfoSnackbar
import com.michaldrabik.ui_base.utilities.extensions.visible
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.extensions.withFailListener
import com.michaldrabik.ui_comments.CommentView
import com.michaldrabik.ui_episodes.R
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_TAB_SELECTED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_EPISODE_WATCHED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_NEW_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_RATING_CHANGED
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ACTION
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_EPISODE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_REPLY_USER
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_EPISODE_DETAILS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.view_episode_details.*
import kotlinx.android.synthetic.main.view_episode_details.view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Locale.ENGLISH

@AndroidEntryPoint
class EpisodeDetailsBottomSheet : BaseBottomSheetFragment<EpisodeDetailsViewModel>() {

  companion object {
    const val ARG_ID_TRAKT = "ARG_ID_TRAKT"
    const val ARG_ID_TMDB = "ARG_ID_TMDB"
    const val ARG_EPISODE = "ARG_EPISODE"
    const val ARG_SEASON_EPISODES = "ARG_SEASON_EPISODES"
    const val ARG_IS_WATCHED = "ARG_IS_WATCHED"
    const val ARG_SHOW_BUTTON = "ARG_SHOW_BUTTON"
    const val ARG_SHOW_TABS = "ARG_SHOW_TABS"
  }

  private val showTraktId by lazy { IdTrakt(requireArguments().getLong(ARG_ID_TRAKT)) }
  private val showTmdbId by lazy { IdTmdb(requireArguments().getLong(ARG_ID_TMDB)) }
  private val episode by lazy { requireArguments().getParcelable<Episode>(ARG_EPISODE)!! }
  private val seasonEpisodes by lazy { requireArguments().getIntArray(ARG_SEASON_EPISODES) }
  private val isWatched by lazy { requireArguments().getBoolean(ARG_IS_WATCHED) }
  private val showButton by lazy { requireArguments().getBoolean(ARG_SHOW_BUTTON) }
  private val showTabs by lazy { requireArguments().getBoolean(ARG_SHOW_TABS) }

  private val cornerRadius by lazy { requireContext().dimenToPx(R.dimen.bottomSheetCorner).toFloat() }

  override val layoutResId = R.layout.view_episode_details

  override fun getTheme(): Int = R.style.CustomBottomSheetDialog

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val contextThemeWrapper = ContextThemeWrapper(activity, R.style.AppTheme)
    return inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.view_episode_details, container, false)
  }

  override fun createViewModel() =
    ViewModelProvider(this).get(EpisodeDetailsViewModel::class.java)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView(view)
    viewLifecycleOwner.lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        with(viewModel) {
          launch { uiState.collect { render(it) } }
          launch { messageState.collect { renderSnackbar(it) } }
          loadSeason(showTraktId, episode, seasonEpisodes)
          loadTranslation(showTraktId, episode)
          loadImage(showTmdbId, episode)
          loadRatings(episode)
        }
      }
    }
  }

  private fun setupView(view: View) {
    view.run {
      episodeDetailsTitle.text = episode.title
      episodeDetailsOverview.text =
        if (episode.overview.isBlank()) getString(R.string.textNoDescription) else episode.overview
      episodeDetailsButton.run {
        visibleIf(showButton)
        setImageResource(if (isWatched) R.drawable.ic_eye else R.drawable.ic_check)
        onClick {
          setFragmentResult(REQUEST_EPISODE_DETAILS, bundleOf(ACTION_EPISODE_WATCHED to !isWatched))
          closeSheet()
        }
      }
      episodeDetailsRatingLayout.visibleIf(episode.votes > 0)
      if (!showTabs) episodeDetailsTabs.gone()
      episodeDetailsRating.text = String.format(ENGLISH, getString(R.string.textVotes), episode.rating, episode.votes)
      episodeDetailsCommentsButton.text = String.format(ENGLISH, getString(R.string.textLoadCommentsCount), episode.commentCount)
      episodeDetailsCommentsButton.onClick {
        viewModel.loadComments(showTraktId, episode.season, episode.number)
      }
      episodeDetailsPostCommentButton.onClick { openPostCommentSheet() }
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

  private fun openPostCommentSheet(comment: Comment? = null) {
    setFragmentResultListener(REQUEST_COMMENT) { _, bundle ->
      renderSnackbar(info(R.string.textCommentPosted))
      when (bundle.getString(ARG_COMMENT_ACTION)) {
        ACTION_NEW_COMMENT -> {
          val newComment = bundle.getParcelable<Comment>(ARG_COMMENT)!!
          viewModel.addNewComment(newComment)
        }
      }
    }
    val bundle = when {
      comment != null -> bundleOf(
        ARG_COMMENT_ID to comment.getReplyId(),
        ARG_REPLY_USER to comment.user.username
      )
      else -> bundleOf(ARG_EPISODE_ID to episode.ids.trakt.id)
    }
    navigateTo(R.id.actionEpisodeDetailsDialogToPostComment, bundle)
  }

  @SuppressLint("SetTextI18n")
  private fun render(uiState: EpisodeDetailsUiState) {
    uiState.run {
      dateFormat?.let {
        val millis = episode.firstAired?.toInstant()?.toEpochMilli() ?: -1
        val date = if (millis == -1L) {
          getString(R.string.textTba)
        } else {
          it.format(dateFromMillis(millis).toLocalZone()).capitalizeWords()
        }
        val name = String.format(ENGLISH, requireContext().getString(R.string.textSeasonEpisodeDate), episode.season, episode.number, date)
        episodeDetailsName.text = name
      }
      isImageLoading.let { episodeDetailsProgress.visibleIf(it) }
      image?.let {
        Glide.with(this@EpisodeDetailsBottomSheet)
          .load(it.fullFileUrlEpisode)
          .transform(CenterCrop(), GranularRoundedCorners(cornerRadius, cornerRadius, 0F, 0F))
          .transition(DrawableTransitionOptions.withCrossFade(IMAGE_FADE_DURATION_MS))
          .withFailListener { episodeDetailsImagePlaceholder.visible() }
          .into(episodeDetailsImage)
      }
      isCommentsLoading.let {
        episodeDetailsButtons.visibleIf(!it)
        episodeDetailsCommentsProgress.visibleIf(it)
      }
      episodes?.let { renderEpisodes(it) }
      comments?.let { comments ->
        episodeDetailsComments.removeAllViews()
        comments.forEach {
          val view = CommentView(requireContext()).apply {
            bind(it, commentsDateFormat)
            if (it.replies > 0) {
              onRepliesClickListener = { comment -> viewModel.loadCommentReplies(comment) }
            }
            if (it.isSignedIn) {
              onReplyClickListener = { comment -> openPostCommentSheet(comment) }
            }
            if (it.replies == 0L && it.isMe && it.isSignedIn) {
              onDeleteClickListener = { comment -> openDeleteCommentDialog(comment) }
            }
          }
          episodeDetailsComments.addView(view)
        }
        episodeDetailsCommentsLabel.fadeIf(comments.isNotEmpty())
        episodeDetailsComments.fadeIf(comments.isNotEmpty())
        episodeDetailsCommentsEmpty.fadeIf(comments.isEmpty())
        episodeDetailsPostCommentButton.fadeIf(isSignedIn)
        episodeDetailsCommentsButton.isEnabled = false
        episodeDetailsCommentsButton.text = String.format(ENGLISH, getString(R.string.textLoadCommentsCount), comments.size)
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
          episodeDetailsRateButton.setTypeface(null, BOLD)
          episodeDetailsRateButton.text = "${state.userRating?.rating}/10"
        } else {
          episodeDetailsRateButton.setTypeface(null, NORMAL)
          episodeDetailsRateButton.setText(R.string.textRate)
        }
      }
      ratingChanged?.let {
        it.consume()?.let {
          setFragmentResult(REQUEST_EPISODE_DETAILS, bundleOf(ACTION_RATING_CHANGED to true))
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

  private fun renderEpisodes(episodes: List<Episode>) {
    with(episodeDetailsTabs) {
      removeAllTabs()
      removeOnTabSelectedListener(tabSelectedListener)
      episodes.forEach {
        addTab(
          newTab()
            .setText("${episode.season}x${it.number.toString().padStart(2, '0')}")
            .setTag(it)
        )
      }
      val index = episodes.indexOfFirst { it.number == episode.number }
      // Small trick to avoid UI tab change flick
      getTabAt(index)?.select()
      post {
        getTabAt(index)?.select()
        addOnTabSelectedListener(tabSelectedListener)
      }
      if (showTabs && episodes.isNotEmpty()) {
        fadeIn(duration = 200, startDelay = 100, withHardware = true)
      } else {
        gone()
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

  private fun openDeleteCommentDialog(comment: Comment) {
    MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog)
      .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
      .setTitle(R.string.textCommentConfirmDeleteTitle)
      .setMessage(R.string.textCommentConfirmDelete)
      .setPositiveButton(R.string.textYes) { _, _ -> viewModel.deleteComment(comment) }
      .setNegativeButton(R.string.textNo) { _, _ -> }
      .show()
  }

  private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab?) {
      episodeDetailsTabs?.removeOnTabSelectedListener(this)
      closeSheet()
      setFragmentResult(REQUEST_EPISODE_DETAILS, bundleOf(ACTION_EPISODE_TAB_SELECTED to tab?.tag))
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
    override fun onTabReselected(tab: TabLayout.Tab?) = Unit
  }
}
