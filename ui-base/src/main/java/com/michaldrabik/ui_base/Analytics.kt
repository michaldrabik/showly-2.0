package com.michaldrabik.ui_base

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.michaldrabik.ui_model.DiscoverFilters
import com.michaldrabik.ui_model.Episode
import com.michaldrabik.ui_model.Movie
import com.michaldrabik.ui_model.MyMoviesSection
import com.michaldrabik.ui_model.MyShowsSection
import com.michaldrabik.ui_model.Show
import java.util.Locale.ROOT

object Analytics {

  private val firebaseAnalytics by lazy { Firebase.analytics }

  fun logShowDetailsDisplay(show: Show) {
    firebaseAnalytics.logEvent("show_details_display") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logMovieDetailsDisplay(movie: Movie) {
    firebaseAnalytics.logEvent("movie_details_display") {
      param("movie_id_trakt", movie.traktId)
      param("movie_title", movie.title)
    }
  }

  fun logShowAddToMyShows(show: Show) {
    firebaseAnalytics.logEvent("show_add_to_my_shows") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logMovieAddToMyMovies(movie: Movie) {
    firebaseAnalytics.logEvent("movie_add_to_my_movies") {
      param("movie_id_trakt", movie.traktId)
      param("movie_title", movie.title)
    }
  }

  fun logShowAddToWatchlistShows(show: Show) {
    firebaseAnalytics.logEvent("show_add_to_see_later") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logMovieAddToWatchlistMovies(movie: Movie) {
    firebaseAnalytics.logEvent("movie_add_to_see_later") {
      param("movie_id_trakt", movie.traktId)
      param("movie_title", movie.title)
    }
  }

  fun logShowAddToArchive(show: Show) {
    firebaseAnalytics.logEvent("show_add_to_archive") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logMovieAddToArchive(movie: Movie) {
    firebaseAnalytics.logEvent("movie_add_to_archive") {
      param("movie_id_trakt", movie.traktId)
      param("movie_title", movie.title)
    }
  }

  fun logShowTrailerClick(show: Show) {
    firebaseAnalytics.logEvent("show_click_trailer") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logMovieTrailerClick(movie: Movie) {
    firebaseAnalytics.logEvent("movie_click_trailer") {
      param("movie_id_trakt", movie.traktId)
      param("movie_title", movie.title)
    }
  }

  fun logShowCommentsClick(show: Show) {
    firebaseAnalytics.logEvent("show_click_comments") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logMovieCommentsClick(movie: Movie) {
    firebaseAnalytics.logEvent("movie_click_comments") {
      param("movie_id_trakt", movie.traktId)
      param("movie_title", movie.title)
    }
  }

  fun logShowShareClick(show: Show) {
    firebaseAnalytics.logEvent("show_click_share") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logMovieShareClick(movie: Movie) {
    firebaseAnalytics.logEvent("movie_click_share") {
      param("movie_id_trakt", movie.traktId)
      param("movie_title", movie.title)
    }
  }

  fun logShowGalleryClick(idTrakt: Long) {
    firebaseAnalytics.logEvent("show_click_gallery") {
      param("show_id_trakt", idTrakt)
    }
  }

  fun logMovieGalleryClick(idTrakt: Long) {
    firebaseAnalytics.logEvent("movie_click_gallery") {
      param("movie_id_trakt", idTrakt)
    }
  }

  fun logShowQuickProgress(show: Show) {
    firebaseAnalytics.logEvent("show_quick_progress_set") {
      param("show_id_trakt", show.traktId)
      param("show_title", show.title)
    }
  }

  fun logMovieRated(movie: Movie, rating: Int) {
    firebaseAnalytics.logEvent("movie_rate") {
      param("movie_id_trakt", movie.traktId)
      param("movie_title", movie.title)
      param("movie_rating", rating.toLong())
    }
  }

  fun logEpisodeRated(idTrakt: Long, episode: Episode, rating: Int) {
    firebaseAnalytics.logEvent("episode_rate") {
      param("show_id_trakt", idTrakt)
      param("episode_id_trakt", episode.ids.trakt.id)
      param("episode_title", episode.title)
      param("episode_rating", rating.toLong())
    }
  }

  fun logDiscoverFiltersApply(filters: DiscoverFilters) {
    firebaseAnalytics.logEvent("discover_filters_set") {
      param("filters_feed_order", filters.feedOrder.name.lowercase(ROOT))
      param("filters_hide_anticipated", filters.hideAnticipated.toString())
      param("filters_genres", filters.genres.map { it.slug }.toTypedArray().contentToString())
    }
  }

  fun logDiscoverMoviesFiltersApply(filters: DiscoverFilters) {
    firebaseAnalytics.logEvent("discover_movies_filters_set") {
      param("filters_feed_order", filters.feedOrder.name.lowercase(ROOT))
      param("filters_hide_anticipated", filters.hideAnticipated.toString())
      param("filters_genres", filters.genres.map { it.slug }.toTypedArray().contentToString())
    }
  }

  // In App Rate

  fun logInAppRateDisplayed() =
    firebaseAnalytics.logEvent("in_app_rate_display", Bundle.EMPTY)

  fun logInAppRateDecision(isYes: Boolean) {
    val decision = if (isYes) "yes" else "no"
    firebaseAnalytics.logEvent("in_app_rate_decision") {
      param("decision", decision)
    }
  }

  // Trakt

  fun logTraktLogin() = firebaseAnalytics.logEvent("trakt_login", null)

  fun logTraktLogout() = firebaseAnalytics.logEvent("trakt_logout", null)

  fun logTraktFullSyncSuccess(import: Boolean, export: Boolean) {
    firebaseAnalytics.logEvent("trakt_full_sync_success") {
      param("sync_type_import", import.toString())
      param("sync_type_export", export.toString())
    }
  }

  fun logTraktQuickSyncSuccess(count: Int) {
    firebaseAnalytics.logEvent("trakt_quick_sync_success") {
      param("items_count", count.toLong())
    }
  }

  // Settings

  fun logSettingsTraktQuickSync(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_trakt_quick_sync") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsTraktQuickRemove(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_trakt_quick_remove") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsTraktQuickRate(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_trakt_quick_rate") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsRecentlyAddedAmount(amount: Long) {
    firebaseAnalytics.logEvent("settings_recently_added_amount") {
      param("amount", amount)
    }
  }

  fun logSettingsPushNotifications(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_push_notifications") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsAnnouncements(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_announcements") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsSpecialSeasons(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_special_seasons") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsProgressUpcoming(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_progress_upcoming") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsMoviesEnabled(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_movies") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsNewsEnabled(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_news") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsStreamingsEnabled(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_streamings") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsWidgetsTitlesEnabled(enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_widgets_titles") {
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsWhenToNotify(value: String) {
    firebaseAnalytics.logEvent("settings_when_to_notify") {
      param("value", value.lowercase(ROOT))
    }
  }

  fun logSettingsLanguage(value: String) {
    firebaseAnalytics.logEvent("settings_language") {
      param("value", value.lowercase(ROOT))
    }
  }

  fun logSettingsTheme(value: Int) {
    firebaseAnalytics.logEvent("settings_theme") {
      param("value", value.toString())
    }
  }

  fun logSettingsPremium(value: Boolean) {
    firebaseAnalytics.logEvent("settings_premium") {
      param("value", value.toString())
    }
  }

  fun logSettingsWidgetsTheme(value: Int) {
    firebaseAnalytics.logEvent("settings_widgets_theme") {
      param("value", value.toString())
    }
  }

  fun logSettingsCountry(value: String) {
    firebaseAnalytics.logEvent("settings_country") {
      param("value", value.lowercase(ROOT))
    }
  }

  fun logSettingsProgressType(value: String) {
    firebaseAnalytics.logEvent("settings_progress_next_type") {
      param("value", value.lowercase(ROOT))
    }
  }

  fun logSettingsDateFormat(value: String) {
    firebaseAnalytics.logEvent("settings_date_format") {
      param("value", value.lowercase(ROOT))
    }
  }

  fun logSettingsMyShowsSection(section: MyShowsSection, enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_my_shows_section") {
      param("section", section.name.lowercase(ROOT))
      param("enabled", enabled.toString())
    }
  }

  fun logSettingsMyMoviesSection(section: MyMoviesSection, enabled: Boolean) {
    firebaseAnalytics.logEvent("settings_my_movies_section") {
      param("section", section.name.lowercase(ROOT))
      param("enabled", enabled.toString())
    }
  }

  fun logInAppUpdate(versionName: String, versionCode: Long) {
    firebaseAnalytics.logEvent("in_app_update") {
      param("version_name", versionName)
      param("version_code", versionCode)
    }
  }

  fun logUnsupportedSubscriptions() {
    firebaseAnalytics.logEvent("unsupported_subscriptions", null)
  }

  fun logUnsupportedBilling() {
    firebaseAnalytics.logEvent("unsupported_billing", null)
  }
}
