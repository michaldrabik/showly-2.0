package com.michaldrabik.ui_comments.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.michaldrabik.common.Mode
import com.michaldrabik.ui_base.BaseFragment
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.addDivider
import com.michaldrabik.ui_base.utilities.extensions.doOnApplyWindowInsets
import com.michaldrabik.ui_base.utilities.extensions.fadeIf
import com.michaldrabik.ui_base.utilities.extensions.fadeIn
import com.michaldrabik.ui_base.utilities.extensions.fadeOut
import com.michaldrabik.ui_base.utilities.extensions.gone
import com.michaldrabik.ui_base.utilities.extensions.launchAndRepeatStarted
import com.michaldrabik.ui_base.utilities.extensions.navigateToSafe
import com.michaldrabik.ui_base.utilities.extensions.onClick
import com.michaldrabik.ui_base.utilities.extensions.requireParcelable
import com.michaldrabik.ui_base.utilities.extensions.updateTopMargin
import com.michaldrabik.ui_base.utilities.extensions.visibleIf
import com.michaldrabik.ui_base.utilities.viewBinding
import com.michaldrabik.ui_comments.CommentsAdapter
import com.michaldrabik.ui_comments.R
import com.michaldrabik.ui_comments.databinding.FragmentCommentsBinding
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs.ACTION_NEW_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ACTION
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_COMMENT_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_MOVIE_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_OPTIONS
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_REPLY_USER
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_SHOW_ID
import com.michaldrabik.ui_navigation.java.NavigationArgs.REQUEST_COMMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize

@SuppressLint("SetTextI18n", "DefaultLocale", "SourceLockedOrientationActivity")
@AndroidEntryPoint
class CommentsFragment : BaseFragment<CommentsViewModel>(R.layout.fragment_comments) {

  companion object {
    const val BACK_UP_BUTTON_THRESHOLD = 25

    fun createBundle(movie: Movie): Bundle =
      bundleOf(ARG_OPTIONS to Options(movie.ids.trakt, Mode.MOVIES))

    fun createBundle(show: Show): Bundle =
      bundleOf(ARG_OPTIONS to Options(show.ids.trakt, Mode.SHOWS))
  }

  override val navigationId = R.id.commentsFragment
  override val viewModel by viewModels<CommentsViewModel>()
  private val binding by viewBinding(FragmentCommentsBinding::bind)

  private var commentsAdapter: CommentsAdapter? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupView()
    setupRecycler()
    setupStatusBar()

    launchAndRepeatStarted(
      { viewModel.uiState.collect { render(it) } },
      { viewModel.messageFlow.collect { showSnack(it) } }
    )
  }

  private fun setupView() {
    hideNavigation()
    with(binding) {
      commentsBackArrow.onClick { requireActivity().onBackPressed() }
      commentsPostButton.onClick { openPostCommentSheet() }
      commentsUpButton.onClick {
        commentsUpButton.fadeOut(150)
        resetScroll()
      }
    }
  }

  private fun setupStatusBar() {
    with(binding) {
      commentsRecycler.doOnApplyWindowInsets { _, insets, padding, _ ->
        val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
        commentsRecycler.updatePadding(top = padding.top + inset)
        commentsTitle.updateTopMargin(inset)
        commentsBackArrow.updateTopMargin(inset)
      }
    }
  }

  private fun setupRecycler() {
    commentsAdapter = CommentsAdapter(
      onDeleteClickListener = { openDeleteCommentDialog(it) },
      onReplyClickListener = { openPostCommentSheet(it) },
      onRepliesClickListener = { viewModel.loadCommentReplies(it) }
    )
    binding.commentsRecycler.apply {
      setHasFixedSize(true)
      adapter = commentsAdapter
      layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
      itemAnimator = null
      addDivider(R.drawable.divider_comments_list)
      addOnScrollListener(recyclerScrollListener)
    }
  }

  private fun openPostCommentSheet(comment: Comment? = null) {
    setFragmentResultListener(REQUEST_COMMENT) { _, bundle ->
      showSnack(MessageEvent.Info(R.string.textCommentPosted))
      when (bundle.getString(ARG_COMMENT_ACTION)) {
        ACTION_NEW_COMMENT -> {
          val newComment = bundle.getParcelable<Comment>(ARG_COMMENT)
          newComment?.let { viewModel.addNewComment(newComment) }
          if (comment == null) {
            binding.commentsRecycler.smoothScrollToPosition(0)
          }
        }
      }
    }

    val bundle = when {
      comment != null -> bundleOf(
        ARG_COMMENT_ID to comment.getReplyId(),
        ARG_REPLY_USER to comment.user.username
      )
      else -> {
        val (id, mode) = requireParcelable<Options>(ARG_OPTIONS)
        when (mode) {
          Mode.SHOWS -> bundleOf(ARG_SHOW_ID to id.id)
          Mode.MOVIES -> bundleOf(ARG_MOVIE_ID to id.id)
        }
      }
    }

    navigateToSafe(R.id.actionCommentsFragmentToPostComment, bundle)
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

  private fun resetScroll() {
    with(binding) {
      commentsRecycler.smoothScrollToPosition(0)
      commentsBackArrow.animate().translationY(0F).start()
      commentsTitle.animate().translationY(0F).start()
    }
  }

  private fun render(uiState: CommentsUiState) {
    with(uiState) {
      comments?.let {
        commentsAdapter?.setItems(comments, dateFormat)
        with(binding) {
          commentsProgress.gone()
          commentsEmpty.visibleIf(comments.isEmpty())
          commentsPostButton.fadeIf(isSignedIn, duration = 200, startDelay = 150)
        }
      }
    }
  }

  override fun onDestroyView() {
    commentsAdapter = null
    super.onDestroyView()
  }

  private val recyclerScrollListener = object : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
      if (newState != RecyclerView.SCROLL_STATE_IDLE) {
        return
      }
      val layoutManager = (binding.commentsRecycler.layoutManager as? LinearLayoutManager)
      if ((layoutManager?.findFirstVisibleItemPosition() ?: 0) >= BACK_UP_BUTTON_THRESHOLD) {
        binding.commentsUpButton.fadeIn(150)
      } else {
        binding.commentsUpButton.fadeOut(150)
      }
    }
  }

  @Parcelize
  data class Options(val id: IdTrakt, val mode: Mode) : Parcelable
}
