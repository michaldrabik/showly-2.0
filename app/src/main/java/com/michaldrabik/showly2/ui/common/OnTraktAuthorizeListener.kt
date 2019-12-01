package com.michaldrabik.showly2.ui.common

import android.net.Uri

interface OnTraktAuthorizeListener {
  fun onAuthorizationResult(authData: Uri?)
}