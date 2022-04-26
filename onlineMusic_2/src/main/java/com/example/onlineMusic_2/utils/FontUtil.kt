package com.example.onlineMusic_2.utils

import android.content.Context
import android.graphics.Typeface

class FontUtil(context: Context) {
    val typeFace: Typeface

    companion object {
        /**
         * 字体
         */
        private var _FontUtil: FontUtil? = null
        fun getInstance(context: Context): FontUtil? {
            if (_FontUtil == null) {
                _FontUtil = FontUtil(context)
            }
            return _FontUtil
        }
    }

    init {
        typeFace = Typeface.createFromAsset(
            context.assets,
            "fonts/iconfont.ttf"
        )
    }
}