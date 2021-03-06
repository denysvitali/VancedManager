package com.vanced.manager.ui.viewmodels

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.crowdin.platform.Crowdin
import com.vanced.manager.R
import com.vanced.manager.core.downloader.MicrogDownloader.downloadMicrog
import com.vanced.manager.core.downloader.MusicDownloader.downloadMusic
import com.vanced.manager.core.downloader.VancedDownloader.downloadVanced
import com.vanced.manager.model.DataModel
import com.vanced.manager.model.ProgressModel
import com.vanced.manager.ui.fragments.MainFragment
import com.vanced.manager.utils.AppUtils.installing
import com.vanced.manager.utils.InternetTools
import com.vanced.manager.utils.PackageHelper.uninstallApk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class HomeViewModel(application: Application): AndroidViewModel(application) {

    val app = application

    //val variant = getDefaultSharedPreferences(application).getString("vanced_variant", "nonroot")
    var variant = "nonroot"

    val vanced = ObservableField<DataModel>()
    val microg = ObservableField<DataModel>()
    val music = ObservableField<DataModel>()
    val manager = ObservableField<DataModel>()
    val fetching = ObservableBoolean()

    fun fetchData() {
        CoroutineScope(Dispatchers.IO).launch {
            fetching.set(true)
            Crowdin.forceUpdate(getApplication())
            vanced.set(DataModel("vanced", variant, app))
            microg.set(DataModel("microg", context = app))
            music.set(DataModel("music", context = app))
            manager.set(DataModel("manager", context = app))
            fetching.set(false)
        }
    }
    
    //private val microgSnackbar = Snackbar.make(, R.string.no_microg, Snackbar.LENGTH_LONG).setAction(R.string.install) { downloadMicrog(getApplication()) }
    private val microgToast = Toast.makeText(app, R.string.no_microg, Toast.LENGTH_LONG)
    
    private val vancedPkgName =
        if (variant == "root") 
            "com.google.android.youtube"
        else 
            "com.vanced.android.youtube"

    fun openMicrogSettings() {
        try {
            val intent = Intent()
            intent.component = ComponentName(
                "com.mgoogle.android.gms",
                "org.microg.gms.ui.SettingsActivity"
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(getApplication(), intent, null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(getApplication(), "Error", Toast.LENGTH_SHORT).show()
        }
    }

    fun openUrl(url: String) {
        val color: Int =
            when (url) {
                "https://discord.gg/TUVd7rd" -> R.color.Discord
                "https://t.me/joinchat/AAAAAEHf-pi4jH1SDlAL4w" -> R.color.Telegram
                "https://twitter.com/YTVanced" -> R.color.Twitter
                "https://reddit.com/r/vanced" -> R.color.Reddit
                "https://vanced.app" -> R.color.Vanced
                "https://brave.com/van874" -> R.color.Brave
                else -> R.color.Vanced
            }
            
        InternetTools.openUrl(url, color, getApplication())
    }
    
    fun installVanced() {
        if (!installing) {
            if (!fetching.get()) {
                if (variant == "nonroot" && !microg.get()?.isAppInstalled()!!) {
                    microgToast.show()
                } else {
                    if (app.getSharedPreferences("installPrefs", Context.MODE_PRIVATE).getBoolean("valuesModified", false)) {
                        downloadVanced(app)
                    } else {
                        findNavController(MainFragment()).navigate(R.id.toInstallThemeFragment)
                    }
                }
            }
        } else
            Toast.makeText(getApplication(), R.string.installation_wait, Toast.LENGTH_SHORT).show()
    }
    
    fun installMusic() {
        if (!installing) {
            if (!fetching.get()) {
                if (!microg.get()?.isAppInstalled()!!) {
                    microgToast.show()
                } else {
                    downloadMusic(getApplication())
                }
            }
        } else
            Toast.makeText(getApplication(), R.string.installation_wait, Toast.LENGTH_SHORT).show()
    }
    
    fun installMicrog() {
        if (!installing)
            downloadMicrog(getApplication())
        else
            Toast.makeText(getApplication(), R.string.installation_wait, Toast.LENGTH_SHORT).show()
    }
    
    fun uninstallVanced() = uninstallApk(vancedPkgName, app)
    fun uninstallMusic() = uninstallApk("com.vanced.android.apps.youtube.music", app)
    fun uninstallMicrog() = uninstallApk("com.mgoogle.android.gms", app)
    
    companion object {
        val vancedProgress = ObservableField<ProgressModel>()
        val musicProgress = ObservableField<ProgressModel>()
        val microgProgress = ObservableField<ProgressModel>()
    }

    init {
        fetching.set(false)
        fetchData()
    }

}
