package com.michaldrabik.showly2.utilities.deeplink

import android.content.Intent
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.michaldrabik.showly2.R
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_model.IdImdb
import com.michaldrabik.ui_model.IdTmdb
import com.michaldrabik.ui_model.IdTrakt
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepLinkResolver @Inject constructor() {

  companion object {
    const val SOURCE_IMDB = "imdb"
    const val SOURCE_TMDB = "tmdb"

    const val TMDB_TYPE_TV = "tv"
    const val TMDB_TYPE_MOVIE = "movie"
  }

  private val progressDestinations = arrayOf(
    R.id.progressMainFragment,
    R.id.progressMoviesMainFragment,
  )

  private val mainDestinations = arrayOf(
    *progressDestinations,
    R.id.discoverFragment,
    R.id.discoverMoviesFragment,
    R.id.followedShowsFragment,
    R.id.followedMoviesFragment,
    R.id.listsFragment,
    R.id.newsFragment,
  )

  fun resolveDestination(
    navController: NavController,
    navigationView: BottomNavigationView,
    show: Show
  ) {
    try {
      resetNavigation(navController, navigationView)

      val navBundle = bundleOf(NavigationArgs.ARG_SHOW_ID to show.traktId)
      val actionId = when (navController.currentDestination?.id) {
        R.id.progressMainFragment -> R.id.actionProgressFragmentToShowDetailsFragment
        R.id.progressMoviesMainFragment -> R.id.actionProgressMoviesFragmentToShowDetailsFragment
        else -> error("Unknown actionId. ActionId: ${navController.currentDestination?.id}")
      }
      navController.navigate(actionId, navBundle)
    } catch (error: Throwable) {
      Logger.record(error, "Source" to "DeepLinkResolver::resolveDestination(show:${show.traktId})")
    }
  }

  fun resolveDestination(
    navController: NavController,
    navigationView: BottomNavigationView,
    movie: Movie
  ) {
    try {
      resetNavigation(navController, navigationView)

      val navBundle = bundleOf(NavigationArgs.ARG_MOVIE_ID to movie.traktId)
      val actionId = when (navController.currentDestination?.id) {
        R.id.progressMainFragment -> R.id.actionProgressFragmentToMovieDetailsFragment
        R.id.progressMoviesMainFragment -> R.id.actionProgressMoviesFragmentToMovieDetailsFragment
        else -> error("Unknown actionId. ActionId: $navController.currentDestination?.id")
      }
      navController.navigate(actionId, navBundle)
    } catch (error: Throwable) {
      Logger.record(error, "Source" to "DeepLinkResolver::resolveDestination(movie:${movie.traktId})")
    }
  }

  fun findSource(intent: Intent?): Source? {
    val path = intent?.data?.pathSegments ?: emptyList()
    if (path.size >= 2 && path[1].startsWith("tt") && path[1].length > 2) {
      return ImdbSource(IdImdb(path[1]))
    }
    if (path.size >= 2 && (path[0] == TMDB_TYPE_TV || path[0] == TMDB_TYPE_MOVIE) && path[1].length > 1) {
      val id = path[1].substringBefore("-").trim()
      val type = path[0]
      return if (id.isDigitsOnly()) {
        TmdbSource(IdTmdb(id.toLong()), type)
      } else {
        null
      }
    }
    return null
  }

  private fun resetNavigation(navController: NavController, navigationView: BottomNavigationView) {
    while (navController.currentDestination?.id !in mainDestinations) {
      navController.popBackStack()
    }

    if (navController.currentDestination?.id !in progressDestinations) {
      navigationView.selectedItemId = R.id.menuProgress
    }
  }

  sealed class Source
  data class ImdbSource(val id: IdImdb) : Source()
  data class TmdbSource(val id: IdTmdb, val type: String) : Source()
  data class TraktSource(val id: IdTrakt) : Source()
}
