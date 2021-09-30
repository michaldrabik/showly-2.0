package com.michaldrabik.showly2.utilities.deeplink

import android.content.Intent
import androidx.core.os.bundleOf
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

  private val tempDestinations = arrayOf(
    R.id.showDetailsFragment,
    R.id.movieDetailsFragment,
    R.id.settingsFragment,
    R.id.traktSyncFragment
  )

  private val mainDestinations = arrayOf(
    R.id.progressMainFragment,
    R.id.progressMoviesMainFragment
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
    return null
  }

  private fun resetNavigation(navController: NavController, navigationView: BottomNavigationView) {
    while (navController.currentDestination?.id in tempDestinations) {
      navController.popBackStack()
    }
    if (navController.currentDestination?.id !in mainDestinations) {
      navigationView.selectedItemId = R.id.menuProgress
    }
  }

  sealed class Source
  data class ImdbSource(val id: IdImdb) : Source()
  data class TmdbSource(val id: IdTmdb) : Source()
  data class TraktSource(val id: IdTrakt) : Source()
}
