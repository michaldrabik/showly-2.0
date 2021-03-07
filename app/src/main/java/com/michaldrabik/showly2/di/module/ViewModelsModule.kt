package com.michaldrabik.showly2.di.module

import androidx.lifecycle.ViewModel
import com.michaldrabik.showly2.di.ViewModelKey
import com.michaldrabik.showly2.ui.main.MainViewModel
import com.michaldrabik.ui_comments.post.PostCommentViewModel
import com.michaldrabik.ui_discover.DiscoverViewModel
import com.michaldrabik.ui_discover_movies.DiscoverMoviesViewModel
import com.michaldrabik.ui_episodes.details.EpisodeDetailsViewModel
import com.michaldrabik.ui_gallery.custom.CustomImagesViewModel
import com.michaldrabik.ui_gallery.fanart.ArtGalleryViewModel
import com.michaldrabik.ui_lists.create.CreateListViewModel
import com.michaldrabik.ui_lists.my_lists.MyListsViewModel
import com.michaldrabik.ui_movie.MovieDetailsViewModel
import com.michaldrabik.ui_my_movies.main.FollowedMoviesViewModel
import com.michaldrabik.ui_my_movies.mymovies.MyMoviesViewModel
import com.michaldrabik.ui_my_shows.archive.ArchiveViewModel
import com.michaldrabik.ui_my_shows.main.FollowedShowsViewModel
import com.michaldrabik.ui_my_shows.myshows.MyShowsViewModel
import com.michaldrabik.ui_my_shows.watchlist.WatchlistViewModel
import com.michaldrabik.ui_premium.PremiumViewModel
import com.michaldrabik.ui_progress.calendar.ProgressCalendarViewModel
import com.michaldrabik.ui_progress.main.ProgressViewModel
import com.michaldrabik.ui_progress.progress.ProgressMainViewModel
import com.michaldrabik.ui_progress_movies.calendar.ProgressMoviesCalendarViewModel
import com.michaldrabik.ui_progress_movies.main.ProgressMoviesViewModel
import com.michaldrabik.ui_progress_movies.progress.ProgressMoviesMainViewModel
import com.michaldrabik.ui_search.SearchViewModel
import com.michaldrabik.ui_settings.SettingsViewModel
import com.michaldrabik.ui_show.ShowDetailsViewModel
import com.michaldrabik.ui_statistics.StatisticsViewModel
import com.michaldrabik.ui_statistics_movies.StatisticsMoviesViewModel
import com.michaldrabik.ui_trakt_sync.TraktSyncViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import com.michaldrabik.ui_my_movies.watchlist.WatchlistViewModel as WatchlistViewModelMovies

@Suppress("unused")
@Module
abstract class ViewModelsModule {

  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel::class)
  abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(SettingsViewModel::class)
  abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(DiscoverViewModel::class)
  abstract fun bindDiscoverViewModel(viewModel: DiscoverViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(DiscoverMoviesViewModel::class)
  abstract fun bindDiscoverMoviesViewModel(viewModel: DiscoverMoviesViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(EpisodeDetailsViewModel::class)
  abstract fun bindEpisodeDetailsViewModel(viewModel: EpisodeDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(CustomImagesViewModel::class)
  abstract fun bindCustomImagesViewModel(viewModel: CustomImagesViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ArtGalleryViewModel::class)
  abstract fun bindFanartGalleryViewModel(viewModel: ArtGalleryViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(MyShowsViewModel::class)
  abstract fun bindMyShowsViewModel(viewModel: MyShowsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(MyMoviesViewModel::class)
  abstract fun bindMyMoviesViewModel(viewModel: MyMoviesViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(MyListsViewModel::class)
  abstract fun bindMyListsViewModel(viewModel: MyListsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(CreateListViewModel::class)
  abstract fun bindCreateListViewModel(viewModel: CreateListViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(SearchViewModel::class)
  abstract fun bindSearchViewModel(viewModel: SearchViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(WatchlistViewModel::class)
  abstract fun bindWatchlistViewModel(viewModel: WatchlistViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(WatchlistViewModelMovies::class)
  abstract fun bindWatchlistMoviesViewModel(viewModel: WatchlistViewModelMovies): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ArchiveViewModel::class)
  abstract fun bindArchiveViewModel(viewModel: ArchiveViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(MovieDetailsViewModel::class)
  abstract fun bindMovieDetailsViewModel(viewModel: MovieDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(PostCommentViewModel::class)
  abstract fun bindPostCommentViewModel(viewModel: PostCommentViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ShowDetailsViewModel::class)
  abstract fun bindShowDetailsViewModel(viewModel: ShowDetailsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ProgressViewModel::class)
  abstract fun bindProgressViewModel(viewModel: ProgressViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ProgressMainViewModel::class)
  abstract fun bindProgressMainViewModel(viewModel: ProgressMainViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ProgressCalendarViewModel::class)
  abstract fun bindProgressCalendarViewModel(viewModel: ProgressCalendarViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ProgressMoviesViewModel::class)
  abstract fun bindProgressMoviesViewModel(viewModel: ProgressMoviesViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ProgressMoviesMainViewModel::class)
  abstract fun bindProgressMoviesMainViewModel(viewModel: ProgressMoviesMainViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(ProgressMoviesCalendarViewModel::class)
  abstract fun bindProgressMoviesCalendarViewModel(viewModel: ProgressMoviesCalendarViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(FollowedShowsViewModel::class)
  abstract fun bindFollowedShowsViewModel(viewModel: FollowedShowsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(FollowedMoviesViewModel::class)
  abstract fun bindFollowedMoviesViewModel(viewModel: FollowedMoviesViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(TraktSyncViewModel::class)
  abstract fun bindTraktSyncViewModel(viewModel: TraktSyncViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(StatisticsViewModel::class)
  abstract fun bindStatisticsViewModel(viewModel: StatisticsViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(PremiumViewModel::class)
  abstract fun bindPremiumViewModel(viewModel: PremiumViewModel): ViewModel

  @Binds
  @IntoMap
  @ViewModelKey(StatisticsMoviesViewModel::class)
  abstract fun bindStatisticsMoviesViewModel(viewModel: StatisticsMoviesViewModel): ViewModel
}
