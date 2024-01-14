package com.michaldrabik.showly2.ui.main.cases.deeplink

import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdSlug
import com.michaldrabik.ui_model.IdTmdb
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class MainDeepLinksCase @Inject constructor(
  private val imdbDeepLinkCase: ImdbDeepLinkCase,
  private val tmdbDeepLinkCase: TmdbDeepLinkCase,
  private val traktDeepLinkCase: TraktDeepLinkCase,
) {

  suspend fun findById(imdbId: IdImdb) =
    imdbDeepLinkCase.findById(imdbId)

  suspend fun findById(tmdbId: IdTmdb, type: String) =
    tmdbDeepLinkCase.findById(tmdbId, type)

  suspend fun findById(traktSlug: IdSlug, type: String) =
    traktDeepLinkCase.findById(traktSlug, type)
}
