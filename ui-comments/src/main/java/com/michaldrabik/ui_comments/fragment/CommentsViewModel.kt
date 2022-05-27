package com.michaldrabik.ui_comments.fragment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaldrabik.common.Mode
import com.michaldrabik.common.errors.ErrorHelper
import com.michaldrabik.common.errors.ShowlyError
import com.michaldrabik.repository.UserTraktManager
import com.michaldrabik.ui_base.dates.DateFormatProvider
import com.michaldrabik.ui_base.utilities.events.MessageEvent
import com.michaldrabik.ui_base.utilities.extensions.SUBSCRIBE_STOP_TIMEOUT
import com.michaldrabik.ui_base.utilities.extensions.findReplace
import com.michaldrabik.ui_base.utilities.extensions.rethrowCancellation
import com.michaldrabik.ui_base.viewmodel.ChannelsDelegate
import com.michaldrabik.ui_base.viewmodel.DefaultChannelsDelegate
import com.michaldrabik.ui_comments.R
import com.michaldrabik.ui_comments.fragment.CommentsFragment.Options
import com.michaldrabik.ui_comments.fragment.cases.DeleteCommentCase
import com.michaldrabik.ui_comments.fragment.cases.LoadCommentsCase
import com.michaldrabik.ui_comments.fragment.cases.LoadRepliesCase
import com.michaldrabik.ui_model.Comment
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_navigation.java.NavigationArgs.ARG_OPTIONS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val commentsCase: LoadCommentsCase,
  private val repliesCase: LoadRepliesCase,
  private val deleteCase: DeleteCommentCase,
  private val userManager: UserTraktManager,
  private val dateFormatProvider: DateFormatProvider
) : ViewModel(), ChannelsDelegate by DefaultChannelsDelegate() {

  private val commentsState = MutableStateFlow<List<Comment>?>(null)
  private val loadingState = MutableStateFlow(false)
  private val signedInState = MutableStateFlow(false)
  private val dateFormatState = MutableStateFlow<DateTimeFormatter?>(null)

  init {
    loadInitialState()
    savedStateHandle.get<Options>(ARG_OPTIONS)?.let {
      loadComments(it.id, it.mode)
    }
  }

  private fun loadInitialState() {
    viewModelScope.launch {
      signedInState.update { userManager.isAuthorized() }
      dateFormatState.update { dateFormatProvider.loadFullHourFormat() }
    }
  }

  private fun loadComments(id: IdTrakt, mode: Mode) {
    viewModelScope.launch {
      try {
        val comments = commentsCase.loadComments(id, mode)
        commentsState.update { comments }
      } catch (error: Throwable) {
        commentsState.update { emptyList() }
        Timber.e(error)
      }
    }
  }

  fun loadCommentReplies(comment: Comment) {
    var currentComments = uiState.value.comments?.toMutableList() ?: mutableListOf()
    if (currentComments.any { it.parentId == comment.id }) {
      return
    }
    viewModelScope.launch {
      try {
        val parent = currentComments.find { it.id == comment.id }
        parent?.let { p ->
          val copy = p.copy(isLoading = true)
          currentComments.findReplace(copy) { it.id == p.id }
          commentsState.value = currentComments
        }

        val replies = repliesCase.loadReplies(comment)

        currentComments = uiState.value.comments?.toMutableList() ?: mutableListOf()
        val parentIndex = currentComments.indexOfFirst { it.id == comment.id }
        if (parentIndex > -1) currentComments.addAll(parentIndex + 1, replies)
        parent?.let {
          currentComments.findReplace(parent.copy(isLoading = false, hasRepliesLoaded = true)) { it.id == comment.id }
        }

        commentsState.value = currentComments
      } catch (t: Throwable) {
        commentsState.value = currentComments
        messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
      }
    }
  }

  fun addNewComment(comment: Comment) {
    val currentComments = uiState.value.comments?.toMutableList() ?: mutableListOf()
    if (!comment.isReply()) {
      currentComments.add(0, comment)
    } else {
      val parentIndex = currentComments.indexOfLast { it.id == comment.parentId }
      if (parentIndex > -1) {
        val parent = currentComments[parentIndex]
        currentComments.add(parentIndex + 1, comment)
        val repliesCount = currentComments.count { it.parentId == parent.id }.toLong()
        currentComments.findReplace(parent.copy(replies = repliesCount)) {
          it.id == comment.parentId
        }
      }
    }
    commentsState.update { currentComments }
  }

  fun deleteComment(comment: Comment) {
    var currentComments = uiState.value.comments?.toMutableList() ?: mutableListOf()
    val target = currentComments.find { it.id == comment.id } ?: return

    viewModelScope.launch {
      try {
        val copy = target.copy(isLoading = true)
        currentComments.findReplace(copy) { it.id == target.id }
        commentsState.value = currentComments

        deleteCase.delete(target)

        currentComments = uiState.value.comments?.toMutableList() ?: mutableListOf()
        val targetIndex = currentComments.indexOfFirst { it.id == target.id }
        if (targetIndex > -1) {
          currentComments.removeAt(targetIndex)
          if (target.isReply()) {
            val parent = currentComments.first { it.id == target.parentId }
            val repliesCount = currentComments.count { it.parentId == parent.id }.toLong()
            currentComments.findReplace(parent.copy(replies = repliesCount)) { it.id == target.parentId }
          }
        }

        commentsState.value = currentComments
        messageChannel.send(MessageEvent.Info(R.string.textCommentDeleted))
      } catch (t: Throwable) {
        when (ErrorHelper.parse(t)) {
          is ShowlyError.CoroutineCancellation -> rethrowCancellation(t)
          is ShowlyError.ResourceConflictError -> messageChannel.send(MessageEvent.Error(R.string.errorCommentDelete))
          else -> messageChannel.send(MessageEvent.Error(R.string.errorGeneral))
        }
      }
    }
  }

  val uiState = combine(
    commentsState,
    loadingState,
    signedInState,
    dateFormatState
  ) { s1, s2, s3, s4 ->
    CommentsUiState(
      comments = s1,
      isLoading = s2,
      isSignedIn = s3,
      dateFormat = s4
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(SUBSCRIBE_STOP_TIMEOUT),
    initialValue = CommentsUiState()
  )
}
