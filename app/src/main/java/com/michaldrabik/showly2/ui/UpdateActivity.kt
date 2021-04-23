package com.michaldrabik.showly2.ui

import android.os.Bundle
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus.DOWNLOADED
import com.google.android.play.core.install.model.UpdateAvailability

abstract class UpdateActivity : BaseActivity() {

  companion object {
    private const val REQUEST_APP_UPDATE = 5278
    private const val DAYS_FOR_UPDATE = 5
  }

  private lateinit var updateListener: InstallStateUpdatedListener

  abstract fun onUpdateDownloaded(appUpdateManager: AppUpdateManager)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
    appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
      if (updateInfo.installStatus() == DOWNLOADED) {
        onUpdateDownloaded(appUpdateManager)
        return@addOnSuccessListener
      }

      if (updateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
        (updateInfo.clientVersionStalenessDays() ?: 0) >= DAYS_FOR_UPDATE &&
        updateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
      ) {
        startUpdate(appUpdateManager, updateInfo)
      }
    }
  }

  private fun startUpdate(
    appUpdateManager: AppUpdateManager,
    updateInfo: AppUpdateInfo,
  ) {
    updateListener = InstallStateUpdatedListener {
      if (it.installStatus() == DOWNLOADED) {
        onUpdateDownloaded(appUpdateManager)
        if (this@UpdateActivity::updateListener.isInitialized) {
          appUpdateManager.unregisterListener(updateListener)
        }
      }
    }

    appUpdateManager.registerListener(updateListener)
    appUpdateManager.startUpdateFlowForResult(updateInfo, AppUpdateType.FLEXIBLE, this, REQUEST_APP_UPDATE)
  }
}
