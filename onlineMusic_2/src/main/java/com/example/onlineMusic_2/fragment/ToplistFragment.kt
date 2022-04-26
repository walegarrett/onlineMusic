package com.example.onlineMusic_2.fragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.adapter.TopListAdapter
import com.example.onlineMusic_2.bean.TopListBean
import com.example.onlineMusic_2.bean.TopListItemBean
import com.example.onlineMusic_2.interfaces.MusicService
import com.example.onlineMusic_2.net.HttpUtil
import com.example.onlineMusic_2.receiver.FragmentBroadCastReceiver
import kotlinx.android.synthetic.main.fragment_toplist.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ToplistFragment : BaseFragment() {
    private var adapter: TopListAdapter? =null
    private var toplists: List<TopListBean.ListBean>?=null

    val retrofit = HttpUtil.createRetrofit()
    val musicService = retrofit.create(MusicService::class.java)

    //监听歌曲播放
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
        Log.d("ListFragment","onCreate")
    }

    //创建视图，一定需要，否则无法完成页面的初始化
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_toplist, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //初始化排行榜
        initTopList(toplistRecycleView)
    }

    //初始化榜单
    fun initTopList(toplistRecycleView:RecyclerView){
        //加载搜索中图片表示正在发起网络请求但是还没有获取到结果
        showLodingView()
        //获取搜索结果
        musicService.getTopList()
            .enqueue(object: Callback<TopListBean> {
                override fun onResponse(call: Call<TopListBean>, response: Response<TopListBean>) {
                    val resToplist = response.body()
                    if(resToplist != null){
                        val toplist: List<TopListBean.ListBean>? = resToplist.list
                        toplists = toplist
                        val t1 = Thread{
                            getOneTopListItem()
                        }
                        t1.start()
                        t1.join()
                        //加载主页面
                        showContentView()
                        //设置适配器
                        val layoutManager= LinearLayoutManager(mActivity)
                        toplistRecycleView.layoutManager=layoutManager
                        //设置音乐列表的适配器
                        if(context != null && toplist != null)
                            adapter = toplists?.let { TopListAdapter(context!!, it) }
                        toplistRecycleView.adapter=adapter
                    }
                }
                override fun onFailure(call: Call<TopListBean>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    //获取单一的排行榜内所有歌曲
    fun getOneTopListItem(){
        if (toplists != null) {
            for((index, item) in toplists!!.withIndex()){
                val itemId: String? = item.id
                if (itemId != null) {
                    musicService.getOneTopList(itemId)
                        .enqueue(object: Callback<TopListItemBean>{
                            override fun onResponse(call: Call<TopListItemBean>, response: Response<TopListItemBean>) {
                                val resItem = response.body()
                                if(resItem != null){
                                    val songlist = resItem.playlist?.tracks
                                    if (songlist != null && songlist.size >= 3) {
                                        toplists!!.get(index).top3MusicList = songlist
//                                        Log.d("ToplistFragment", songlist.toString())
                                    }
                                }
                            }
                            override fun onFailure(call: Call<TopListItemBean>, t: Throwable) {
                                t.printStackTrace()
                            }

                        })
                }

            }
        }
    }

    /**
     * 显示加载中页面
     */
    fun showLodingView(){
        val rotateAnimation = AnimationUtils.loadAnimation(
            context,
            R.anim.anim_rotate
        )
        rotateAnimation.setInterpolator(LinearInterpolator()) // 匀速
        content_layout.setVisibility(View.GONE)
        load_layout.setVisibility(View.VISIBLE)
        loadingImg.clearAnimation()
        loadingImg.startAnimation(rotateAnimation)
    }

    /**
     * 显示内容界面
     */
    fun showContentView(){
        content_layout.setVisibility(View.VISIBLE)
        load_layout.setVisibility(View.GONE)
        loadingImg.clearAnimation()
    }
    //打开某一个item
    fun doReceiveFragmentAction(intent: Intent){
        val action: String? =intent.action
        if(action.equals(FragmentBroadCastReceiver.ACTION_FRAGMENT_OPEN_RANKSONG)){
            val rankId: String? = intent.getStringExtra("rankId")
            if (rankId != null) {
                Log.d("ToplistFragment", rankId)
                replaceFragment(NetmusiclistFragment(), rankId)
            }

        }
    }

    //碎片的替换
    fun replaceFragment(fragment:Fragment, rankId:String){
        //初始往frameLayout中添加一个新的fragment
        val manager: FragmentManager? = activity?.supportFragmentManager
        val fragmentTransaction = manager?.beginTransaction()

        val bundle:Bundle = Bundle()
        //给fragment传递参数：榜单id
        bundle.putString("toplistId", rankId)
        fragment.arguments = bundle

        fragmentTransaction?.replace(R.id.replace_layout, fragment)
        fragmentTransaction?.addToBackStack("ToplistFragment")
        fragmentTransaction?.commit()
    }

    //初始化service和广播等事件
    private fun initReceivers(){
        //注册和监听音乐播放广播
        fragmentBroadCastReceiver= activity?.let { FragmentBroadCastReceiver(it) }
        fragmentBroadCastReceiver?.addFragmentReceiverListener(fragmentBroadCastReceiverListener)
        activity?.let { fragmentBroadCastReceiver?.registerReceiver(it) }
    }

    override fun onResume() {
        super.onResume()
        initReceivers()
    }

    override fun onPause() {
        super.onPause()
        activity?.let { fragmentBroadCastReceiver?.unRegisterReceiver(it) }
    }
}