package com.example.myapplication.utils

import android.app.Activity
import android.content.Context

object SessionManger {

// false in theme means light and false means true
    fun  setThemeMode(activity:Activity?,isDarkMode:Boolean=true){
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean("Theme", isDarkMode)
            apply()
        }
    }


    fun getMyTheme(activity: Activity?):Boolean{
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?:return true
        val result = sharedPref.getBoolean("Theme", true)
        return result
    }
}