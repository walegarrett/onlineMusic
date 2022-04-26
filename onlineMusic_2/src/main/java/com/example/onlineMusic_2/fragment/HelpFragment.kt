package com.example.onlineMusic_2.fragment

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.adapter.HelpAdapter
import com.example.onlineMusic_2.bean.Help
import com.example.onlineMusic_2.db.HelpDB
import kotlinx.android.synthetic.main.fragment_help.*
import java.util.*

class HelpFragment : BaseFragment() {

    private var recyclerView: RecyclerView? = null
    private var adapter: HelpAdapter? =null
    private var helpList: List<Help> = ArrayList<Help>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
        //加载数据
        loadData()
        initRecyclerView()
    }

    /**
     * 初始化布局页面
     */
    fun initViews(){
        recyclerView = helpRecyclerView
    }

    /**
     * 加载数据库的帮助信息
     */
    fun loadData(){
        if(mActivity != null){
            helpList = HelpDB.getHelpDB(mActivity!!)?.getAllHelpInfo()!!
        }
    }

    /**
     * 初始化recyclerView
     */
    fun initRecyclerView(){
        Log.d("HelpFragment", helpList.size.toString())
        //设置适配器
        val layoutManager= LinearLayoutManager(mActivity)
        recyclerView?.layoutManager=layoutManager
        //设置音乐列表的适配器
        if(context != null)
            adapter = mActivity?.let { HelpAdapter(it, helpList) }
        recyclerView?.adapter=adapter
    }
}