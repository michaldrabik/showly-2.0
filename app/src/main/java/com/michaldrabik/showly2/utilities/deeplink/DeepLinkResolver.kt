package com.michaldrabik.showly2.utilities.deeplink

import android.content.Intent
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.michaldrabik.showly2.R
import com.michaldrabik.showly2.utilities.deeplink.resolvers.ImdbSourceResolver
import com.michaldrabik.showly2.utilities.deeplink.resolvers.TmdbSourceResolver
import com.michaldrabik.showly2.utilities.deeplink.resolvers.TraktSourceResolver
import com.michaldrabik.ui_base.Logger
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.Show
import com.michaldrabik.ui_navigation.java.NavigationArgs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepLinkResolver @Inject constructor() {

  companion object {
    const val TMDB_TYPE_TV = "tv"
    const val TMDB_TYPE_MOVIE = "movie"

    const val TRAKT_TYPE_TV = "shows"
    const val TRAKT_TYPE_MOVIE = "movies"
  }

  private val sourceResolvers = setOf(
    TraktSourceResolver(),
    ImdbSourceResolver(),
    TmdbSourceResolver()
  )

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

  fun findSource(intent: Intent?): DeepLinkSource? {
    val path = intent?.data?.pathSegments ?: emptyList()
    return sourceResolvers.firstNotNullOfOrNull { it.resolve(path) }
  }

  fun resolveDestination(
    navController: NavController,
    navigationView: BottomNavigationView,
    show: Show,
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
      Logger.record(error, "DeepLinkResolver::resolveDestination(show:${show.traktId})")
    }
  }

  fun resolveDestination(
    navController: NavController,
    navigationView: BottomNavigationView,
    movie: Movie,
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
      Logger.record(error, "DeepLinkResolver::resolveDestination(movie:${movie.traktId})")
    }
  }

  private fun resetNavigation(navController: NavController, navigationView: BottomNavigationView) {
    while (navController.currentDestination?.id !in mainDestinations) {
      navController.popBackStack()
    }

    if (navController.currentDestination?.id !in progressDestinations) {
      navigationView.selectedItemId = R.id.menuProgress
    }
  }
}
