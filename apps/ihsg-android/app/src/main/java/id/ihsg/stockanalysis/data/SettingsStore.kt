package id.ihsg.stockanalysis.data

import android.content.Context
import androidx.core.content.edit
import id.ihsg.stockanalysis.BuildConfig

class SettingsStore(context: Context) {
    private val preferences = context.getSharedPreferences("ihsg_settings", Context.MODE_PRIVATE)

    fun getBaseUrl(): String =
        preferences.getString(KEY_BASE_URL, BuildConfig.DEFAULT_API_BASE_URL) ?: BuildConfig.DEFAULT_API_BASE_URL

    fun setBaseUrl(value: String) {
        preferences.edit {
            putString(KEY_BASE_URL, value.trim().trimEnd('/'))
        }
    }

    companion object {
        private const val KEY_BASE_URL = "base_url"
    }
}
