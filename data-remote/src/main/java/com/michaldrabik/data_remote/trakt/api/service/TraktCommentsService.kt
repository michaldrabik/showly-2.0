package com.michaldrabik.data_remote.trakt.api.service

import com.michaldrabik.data_remote.trakt.model.Comment
import com.michaldrabik.data_remote.trakt.model.request.CommentRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TraktCommentsService {

  @GET("comments/{id}/replies")
  suspend fun fetchCommentReplies(
    @Path("id") commentId: Long,
    @Query("timestamp") timestamp: Long
  ): List<Comment>

  @POST("comments")
  suspend fun postComment(
    @Header("Authorization") authToken: String,
    @Body commentBody: CommentRequest
  ): Comment

  @POST("comments/{id}/replies")
  suspend fun postCommentReply(
    @Header("Authorization") authToken: String,
    @Path("id") commentId: Long,
    @Body commentBody: CommentRequest
  ): Comment

  @DELETE("comments/{id}")
  suspend fun deleteComment(
    @Header("Authorization") authToken: String,
    @Path("id") commentIt: Long
  ): Response<Any>
}
