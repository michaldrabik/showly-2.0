import com.michaldrabik.ui_discover.recycler.DiscoverListItem
import com.michaldrabik.ui_model.*

object TestData {

  val DISCOVER_LIST_ITEM = DiscoverListItem(
    show = Show(
      ids = Ids(
        trakt = IdTrakt(id = 0),
        slug = IdSlug(id = ""),
        tvdb = IdTvdb(id = 0),
        imdb = IdImdb(id = ""),
        tmdb = IdTmdb(id = 0),
        tvrage = IdTvRage(id = 0)
      ),
      title = "",
      year = 0,
      overview = "",
      firstAired = "",
      runtime = 0,
      airTime = AirTime(day = "", time = "", timezone = ""),
      certification = "",
      network = "",
      country = "",
      trailer = "",
      homepage = "",
      status = ShowStatus.UNKNOWN,
      rating = 0.0f,
      votes = 0,
      commentCount = 0,
      genres = listOf(),
      airedEpisodes = 0,
      updatedAt = 0
    ), image = Image(
      id = 0,
      idTvdb = IdTvdb(id = 0),
      type = ImageType.POSTER,
      family = ImageFamily.SHOW,
      fileUrl = "",
      thumbnailUrl = "",
      status = Image.Status.UNKNOWN
    ), isLoading = false, isFollowed = false, isSeeLater = false
  )
}
