package com.michaldrabik.ui_comments.fragment.cases

import com.michaldrabik.common.extensions.nowUtcMillis
import com.michaldrabik.common.extensions.toMillis
import com.michaldrabik.repository.CommentsRepository
import com.michaldrabik.ui_model.Comment
import dagger.hilt.android.scopes.ViewModelScoped
import okhttp3.internal.EMPTY_RESPONSE
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ViewModelScoped
class DeleteCommentCase @Inject constructor(
  private val commentsRepository: CommentsRepository
) {

  suspend fun delete(comment: Comment) {
    val dateMillis = comment.createdAt?.toMillis()
    dateMillis?.let {
      if (nowUtcMillis() - it >= TimeUnit.DAYS.toMillis(13)) {
        throw HttpException(Response.error<Any>(409, EMPTY_RESPONSE))
      }
    }
    commentsRepository.deleteComment(comment.id)
  }
}
