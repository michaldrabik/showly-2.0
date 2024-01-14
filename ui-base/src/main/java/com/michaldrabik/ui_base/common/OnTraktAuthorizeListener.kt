package com.michaldrabik.ui_base.common

import android.net.Uri

interface OnTraktAuthorizeListener {
  fun onAuthorizationResult(authData: Uri?)
}
