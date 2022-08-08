package uz.gita.vocabulary.data.storage

import android.content.Context
import uz.gita.vocabulary.app.App

class SharedPref {
    companion object {
        private lateinit var instance: SharedPref

        fun getPref(): SharedPref {
            if (!::instance.isInitialized) {
                instance = SharedPref()
            }
            return instance
        }
    }

    private val pref = App.instance.getSharedPreferences("Word", Context.MODE_PRIVATE)

    var isFirst
        set(value) = pref.edit().putBoolean("isFirst", value).apply()
        get() = pref.getBoolean("isFirst", true)
}