package com.example.onlineMusic_2.fragment

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment

open class BaseFragment: Fragment() {
    var mActivity: Activity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mActivity = context as Activity
    }
}