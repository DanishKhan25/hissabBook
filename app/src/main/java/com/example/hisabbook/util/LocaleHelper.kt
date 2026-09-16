package com.example.hisabbook.util

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    fun applyLocale(context: Context, languageTag: String) {
        val tag = if (languageTag.lowercase().startsWith("hi")) "hi" else "en"
        val localeManager = context.getSystemService(LocaleManager::class.java)
        localeManager?.applicationLocales = LocaleList.forLanguageTags(tag)
    }

    fun getAppLanguage(context: Context): String {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val locales = localeManager?.applicationLocales
        return if (locales != null && !locales.isEmpty) {
            locales.get(0).language
        } else {
            Locale.getDefault().language
        }
    }
}
