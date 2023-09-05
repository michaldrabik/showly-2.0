package com.michaldrabik.showly2.ui.main.delegates

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import timber.log.Timber

interface UpdateDelegate {

  fun registerUpdate(
    activity: Activity,
    onUpdateDownloaded: (AppUpdateManager) -> Unit
  )
}

class MainUpdateDelegate : UpdateDelegate, DefaultLifecycleObserver {

  companion object {
    private const val REQUEST_APP_UPDATE = 5278
    private const val DAYS_FOR_UPDATE = 0
  }

  private lateinit var activity: Activity
  private lateinit var appUpdateManager: AppUpdateManager
  private lateinit var updateListener: InstallStateUpdatedListener

  private var onUpdateDownloaded: ((AppUpdateManager) -> Unit)? = null

  override fun registerUpdate(
    activity: Activity,
    onUpdateDownloaded: (AppUpdateManager) -> Unit
  ) {
    this.activity = activity
    (this.activity as LifecycleOwner).lifecycle.addObserver(this)
    this.onUpdateDownloaded = onUpdateDownloaded
    this.appUpdateManager = AppUpdateManagerFactory.create(activity.applicationContext)
  }

  override fun onCreate(owner: LifecycleOwner) {
    super.onCreate(owner)

    updateListener = InstallStateUpdatedListener {
      if (it.installStatus() == InstallStatus.DOWNLOADED) {
        onUpdateDownloaded?.invoke(appUpdateManager)
        if (this@MainUpdateDelegate::updateListener.isInitialized) {
          appUpdateManager.unregisterListener(updateListener)
        }
      }
    }
    appUpdateManager.registerListener(updateListener)

    startUpdate()
  }

  override fun onDestroy(owner: LifecycleOwner) {
    appUpdateManager.unregisterListener(updateListener)
    onUpdateDownloaded = null
    super.onDestroy(owner)
  }

  private fun startUpdate() {
    appUpdateManager.appUpdateInfo
      .addOnCompleteListener {
        if (it.isSuccessful) {
          val updateInfo = it.result
          Timber.d("Update info success: $updateInfo")

          if (updateInfo.installStatus() == InstallStatus.DOWNLOADED) {
            onUpdateDownloaded?.invoke(appUpdateManager)
            return@addOnCompleteListener
          }

          if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            (updateInfo.clientVersionStalenessDays() ?: 0) >= DAYS_FOR_UPDATE &&
            updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
          ) {
            Timber.d("Starting update flow...")
            appUpdateManager.startUpdateFlowForResult(
              updateInfo,
              activity,
              AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
              REQUEST_APP_UPDATE
            )
          }
        }
      }
  }
}
