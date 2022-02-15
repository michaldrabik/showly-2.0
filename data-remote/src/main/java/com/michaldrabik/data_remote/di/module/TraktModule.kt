package com.michaldrabik.data_remote.di.module

import com.michaldrabik.data_remote.trakt.TraktInterceptor
import com.michaldrabik.data_remote.trakt.api.TraktApi
import com.michaldrabik.data_remote.trakt.api.service.TraktAuthService
import com.michaldrabik.data_remote.trakt.api.service.TraktCommentsService
import com.michaldrabik.data_remote.trakt.api.service.TraktMoviesService
import com.michaldrabik.data_remote.trakt.api.service.TraktPeopleService
import com.michaldrabik.data_remote.trakt.api.service.TraktSearchService
import com.michaldrabik.data_remote.trakt.api.service.TraktShowsService
import com.michaldrabik.data_remote.trakt.api.service.TraktSyncService
import com.michaldrabik.data_remote.trakt.api.service.TraktUsersService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TraktModule {

  @Provides
  @Singleton
  fun providesTraktApi(@Named("retrofitTrakt") retrofit: Retrofit): TraktApi =
    TraktApi(
      showsService = retrofit.create(TraktShowsService::class.java),
      moviesService = retrofit.create(TraktMoviesService::class.java),
      usersService = retrofit.create(TraktUsersService::class.java),
      authService = retrofit.create(TraktAuthService::class.java),
      commentsService = retrofit.create(TraktCommentsService::class.java),
      searchService = retrofit.create(TraktSearchService::class.java),
      peopleService = retrofit.create(TraktPeopleService::class.java),
      syncService = retrofit.create(TraktSyncService::class.java)
    )

  @Provides
  @Singleton
  fun providesTraktInterceptor() = TraktInterceptor()
}
