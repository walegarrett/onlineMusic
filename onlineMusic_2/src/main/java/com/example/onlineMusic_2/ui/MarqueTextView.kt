package com.example.onlineMusic_2.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView


/**
 * 自定义TextView 重写isFocused()函数，让他放回true也就是一直获取了
 * 焦点效果自然也就出来了，如果这都不能解决那肯定就不是焦点问题了。
 * 那就要找到问题，在想办法解决
 */
class MarqueTextView : AppCompatTextView {
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?) : super(context!!) {}

    override fun isFocused(): Boolean {
        return true
    }
}
