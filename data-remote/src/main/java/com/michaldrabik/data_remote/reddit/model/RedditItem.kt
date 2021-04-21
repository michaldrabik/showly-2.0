package com.michaldrabik.data_remote.reddit.model

data class RedditItem(
  val id: String,
  val is_self: Boolean,
  val title: String,
  val url: String,
  val score: Long,
  val preview: Preview?,
  val created_utc: Long,
) {

  data class Preview(
    val images: List<Image>?,
  )

  data class Image(
    val resolutions: List<Resolution>?,
  ) {
    data class Resolution(
      val url: String,
      val width: Int,
      val height: Int,
    )
  }

  fun findImageUrl(): String? {
    val resolutions = preview?.images?.firstOrNull()?.resolutions
    return resolutions?.firstOrNull { it.width > 600 }?.url
      ?: resolutions?.lastOrNull()?.url
  }
}
