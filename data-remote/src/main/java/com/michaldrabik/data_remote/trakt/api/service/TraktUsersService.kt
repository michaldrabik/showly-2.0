package com.michaldrabik.data_remote.trakt.api.service

import com.michaldrabik.data_remote.trakt.model.CustomList
import com.michaldrabik.data_remote.trakt.model.HiddenItem
import com.michaldrabik.data_remote.trakt.model.SyncExportRequest
import com.michaldrabik.data_remote.trakt.model.SyncExportResult
import com.michaldrabik.data_remote.trakt.model.SyncItem
import com.michaldrabik.data_remote.trakt.model.User
import com.michaldrabik.data_remote.trakt.model.request.CreateListRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TraktUsersService {

  @GET("users/me")
  suspend fun fetchMyProfile(): User

  @GET("users/hidden/progress_watched?type=show&extended=full")
  suspend fun fetchHiddenShows(
    @Query("limit") pageLimit: Int
  ): List<HiddenItem>

  @POST("users/hidden/progress_watched")
  suspend fun postHiddenShows(
    @Body request: SyncExportRequest
  ): SyncExportResult

  @POST("users/hidden/calendar")
  suspend fun postHiddenMovies(
    @Body request: SyncExportRequest
  ): SyncExportResult

  @GET("users/hidden/calendar?type=movie&extended=full")
  suspend fun fetchHiddenMovies(
    @Query("limit") pageLimit: Int
  ): List<HiddenItem>

  @GET("users/me/lists")
  suspend fun fetchSyncLists(): List<CustomList>

  @GET("users/me/lists/{id}")
  suspend fun fetchSyncList(
    @Path("id") listId: Long
  ): CustomList

  @GET("users/me/lists/{id}/items/{types}?extended=full")
  suspend fun fetchSyncListItems(
    @Path("id") listId: Long,
    @Path("types") types: String,
    @Query("page") page: Int? = null,
    @Query("limit") limit: Int? = null
  ): List<SyncItem>

  @POST("users/me/lists")
  suspend fun postCreateList(
    @Body request: CreateListRequest
  ): CustomList

  @PUT("users/me/lists/{id}")
  suspend fun postUpdateList(
    @Path("id") listId: Long,
    @Body request: CreateListRequest
  ): CustomList

  @DELETE("users/me/lists/{id}")
  suspend fun deleteList(
    @Path("id") listId: Long
  ): Response<Any>

  @POST("users/me/lists/{id}/items")
  suspend fun postAddListItems(
    @Path("id") listId: Long,
    @Body request: SyncExportRequest
  ): SyncExportResult

  @POST("users/me/lists/{id}/items/remove")
  suspend fun postRemoveListItems(
    @Path("id") listId: Long,
    @Body request: SyncExportRequest
  ): SyncExportResult

  @POST("users/hidden/{section}/remove")
  suspend fun deleteHidden(
    @Path("section") section: String,
    @Body request: SyncExportRequest
  ): SyncExportResult
}
