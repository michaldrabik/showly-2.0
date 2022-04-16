package com.michaldrabik.ui_base.utilities

import com.michaldrabik.common.Mode

interface ModeHost {
  fun setMode(mode: Mode, force: Boolean = false)
  fun getMode(): Mode
}
