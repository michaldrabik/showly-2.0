package com.michaldrabik.ui_model

/**
 * AVAILABLE - image's web url is known to be valid when used the last time.
 * UNKNOWN - image's web url has not been yet checked with remote images service (TVDB).
 * UNAVAILABLE - remote images service does not contain any valid image url.
 */
enum class ImageStatus {
  AVAILABLE,
  UNKNOWN,
  UNAVAILABLE
}
