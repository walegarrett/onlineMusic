package com.example.onlineMusic_2.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.fragment.ThemeFragment

object ApplicationUtil {
    //设置主题
    fun setTheme(context: Context, position: Int) {
        val preSelect = getTheme(context)
        val sharedPreferences =
            context.getSharedPreferences("theme", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("theme_select", position).commit()
        if (preSelect != ThemeFragment.THEME_SIZE - 1) {
            sharedPreferences.edit().putInt("pre_theme_select", preSelect).commit()
        }
    }


    //得到主题
    fun getTheme(context: Context): Int {
        val sharedPreferences =
            context.getSharedPreferences("theme", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("theme_select", 0)
    }

    //得到上一次选择的主题，用于取消夜间模式时恢复用
    fun getPreTheme(context: Context): Int {
        val sharedPreferences =
            context.getSharedPreferences("theme", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("pre_theme_select", 0)
    }

    //设置夜间模式
    fun setNightMode(context: Context, mode: Boolean) {
        if (mode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        val sharedPreferences =
            context.getSharedPreferences("theme", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("night", mode).commit()
    }

    //得到是否夜间模式
    fun getNightMode(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences("theme", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("night", false)
    }

    //得到主题
    fun getMyThemeStyle(context: Context): Int {
        val themeId: Int = getTheme(context)
        return when (themeId) {
            0 -> R.style.BiLiPinkTheme
            1 -> R.style.ZhiHuBlueTheme
            2 -> R.style.KuAnGreenTheme
            3 -> R.style.CloudRedTheme
            4 -> R.style.TengLuoPurpleTheme
            5 -> R.style.SeaBlueTheme
            6 -> R.style.GrassGreenTheme
            7 -> R.style.CoffeeBrownTheme
            8 -> R.style.LemonOrangeTheme
            9 -> R.style.StartSkyGrayTheme
            10 -> R.style.NightModeTheme
            else -> R.style.BiLiPinkTheme
        }
    }
}