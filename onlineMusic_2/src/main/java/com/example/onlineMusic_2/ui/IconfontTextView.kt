package com.example.onlineMusic_2.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatTextView
import com.example.onlineMusic_2.utils.FontUtil

class IconfontTextView : AppCompatTextView {
    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context)
    }

    constructor(
        context: Context,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        // 设置字体图片
        val iconfont: Typeface? = FontUtil.getInstance(context)?.typeFace
        typeface = iconfont
    }
}