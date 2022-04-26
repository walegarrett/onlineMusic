package com.example.onlineMusic_2.fragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.activity.MainActivity
import com.example.onlineMusic_2.adapter.ThemeAdapter
import com.example.onlineMusic_2.bean.ThemeInfo
import com.example.onlineMusic_2.receiver.FragmentBroadCastReceiver
import com.example.onlineMusic_2.receiver.MediaBroadCastReceiver
import com.example.onlineMusic_2.utils.ApplicationUtil
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.fragment_theme.*
import java.util.*

class ThemeFragment : BaseFragment() {
    companion object{
        var THEME_SIZE = 11
    }
    private val themeType = arrayOf(
        "哔哩粉",
        "知乎蓝",
        "酷安绿",
        "网易红",
        "藤萝紫",
        "碧海蓝",
        "樱草绿",
        "咖啡棕",
        "柠檬橙",
        "星空灰",
        "夜间模式"
    )
    private val colors = intArrayOf(
        R.color.biliPink, R.color.zhihuBlue, R.color.kuanGreen, R.color.cloudRed,
        R.color.tengluoPurple, R.color.seaBlue, R.color.grassGreen, R.color.coffeeBrown,
        R.color.lemonOrange, R.color.startSkyGray, R.color.nightActionbar
    )

    private val TAG = "ThemeFragment"
    private var recyclerView: RecyclerView? = null
    private var adapter: ThemeAdapter? =null

    private var selectTheme = 0
    private var themeInfoList: List<ThemeInfo> = ArrayList<ThemeInfo>()

    var fragmentBroadCastReceiver: FragmentBroadCastReceiver? = null
    val fragmentBroadCastReceiverListener = object: FragmentBroadCastReceiver.FragmentReceiverListener{
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                doReceiveFragmentAction(intent)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_theme, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()

        recyclerView = theme_rv
        initRecyclerView(theme_rv)
    }

    fun initViews(){
        for (i in themeType.indices) {
            val info = ThemeInfo()
            info.name = themeType[i]
            info.color = colors[i]
            info.isSelect = (if (selectTheme == i) true else false)
            if (i == themeType.size - 1) {
                info.background = R.color.nightBg
            } else {
                info.background = R.color.colorWhite
            }
            themeInfoList = themeInfoList + info
        }
    }

    override fun onResume() {
        super.onResume()
        initReceivers()
    }

    override fun onDestroy() {
        super.onDestroy()
        //取消注册广播
        activity?.let { fragmentBroadCastReceiver?.unRegisterReceiver(it) }
    }

    //初始化service和广播等事件
    fun initReceivers() {
        //注册和监听广播
        fragmentBroadCastReceiver= activity?.let { FragmentBroadCastReceiver(it) }
        fragmentBroadCastReceiver?.addFragmentReceiverListener(fragmentBroadCastReceiverListener)
        activity?.let { fragmentBroadCastReceiver?.registerReceiver(it) }
    }
    fun initRecyclerView(recyclerView: RecyclerView){
        Log.d("ThemeFragment", themeInfoList.size.toString())
        //设置适配器
        val layoutManager= LinearLayoutManager(mActivity)
        recyclerView.layoutManager=layoutManager
        //设置音乐列表的适配器
        if(context != null)
            adapter = mActivity?.let { ThemeAdapter(it, themeInfoList, selectTheme) }
        recyclerView.adapter=adapter
    }

    private fun refreshTheme(themeInfo: ThemeInfo, position: Int) {
        if (position == THEME_SIZE - 1) {
            mActivity?.let { ApplicationUtil.setNightMode(it, true) }
        } else if (mActivity?.let { ApplicationUtil.getNightMode(it) }!!) {
            mActivity?.let { ApplicationUtil.setNightMode(it, false) }
        }
        selectTheme = position
        mActivity?.let { ApplicationUtil.setTheme(it, position) }
        activity?.toolbar?.setBackgroundColor(resources.getColor(themeInfo.color))

        recyclerView!!.setBackgroundColor(resources.getColor(themeInfo.background))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActivity?.getWindow()?.setStatusBarColor(resources.getColor(themeInfo.color))
        }
        for (info in themeInfoList) {
            info.isSelect = info.name.equals(themeInfo.name)
        }
        adapter!!.notifyDataSetChanged()
    }
    fun doReceiveFragmentAction(intent: Intent){
        val action: String? =intent.action
        if(action.equals(FragmentBroadCastReceiver.ACTION_FRAGMENT_THEME_CLICKED)){
            val position:Int = intent.getIntExtra("position", -1)
            if(position != -1){
                val themeInfo = themeInfoList[position]
                refreshTheme(themeInfo, position)
            }
        }
    }
}