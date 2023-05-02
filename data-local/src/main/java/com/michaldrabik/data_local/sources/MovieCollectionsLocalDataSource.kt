package com.michaldrabik.data_local.sources

import com.michaldrabik.data_local.database.model.MovieCollection

interface MovieCollectionsLocalDataSource {

  suspend fun getById(traktId: Long): MovieCollection?

  suspend fun getByMovieId(movieTraktId: Long): List<MovieCollection>

  suspend fun replace(movieTraktId: Long, entities: List<MovieCollection>)
}
