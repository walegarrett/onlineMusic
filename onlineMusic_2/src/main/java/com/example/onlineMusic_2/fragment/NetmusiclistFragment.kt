package com.example.onlineMusic_2.fragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.adapter.TopListItemAdapter
import com.example.onlineMusic_2.bean.*
import com.example.onlineMusic_2.db.MusicInfoDB
import com.example.onlineMusic_2.interfaces.MusicService
import com.example.onlineMusic_2.net.HttpUtil
import com.example.onlineMusic_2.net.ServiceCreator
import com.example.onlineMusic_2.receiver.FragmentBroadCastReceiver
import com.example.onlineMusic_2.receiver.MediaBroadCastReceiver
import com.example.onlineMusic_2.utils.OnlinePlaying
import kotlinx.android.synthetic.main.fragment_netmusiclist.*
import kotlinx.android.synthetic.main.fragment_netmusiclist.content_layout
import kotlinx.android.synthetic.main.fragment_netmusiclist.fab2
import kotlinx.android.synthetic.main.fragment_netmusiclist.load_layout
import kotlinx.android.synthetic.main.fragment_netmusiclist.loadingImg
import kotlinx.android.synthetic.main.fragment_netmusiclist.musicImageView
import kotlinx.android.synthetic.main.fragment_netmusiclist.musicLastImageView
import kotlinx.android.synthetic.main.fragment_netmusiclist.musicNextImageView
import kotlinx.android.synthetic.main.fragment_netmusiclist.musicPerson
import kotlinx.android.synthetic.main.fragment_netmusiclist.musicPlayImageView
import kotlinx.android.synthetic.main.fragment_netmusiclist.musicTitle
import kotlinx.android.synthetic.main.fragment_toplist.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetmusiclistFragment : BaseFragment() {
    private var adapter: TopListItemAdapter? =null
    private var itemList: List<SongsBean> = ArrayList<SongsBean>()
    private var likeImageList: List<Int> = ArrayList<Int>()

    val musicService = ServiceCreator.create(MusicService::class.java)



    //监听歌曲播放
    var mediaBroadCastReceiver: MediaBroadCastReceiver? = null
    val mediaReceiverListener = object: MediaBroadCastReceiver.MediaReceiverListener{
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                doReceiveMediaAction(intent)
            }
        }
    }

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_netmusiclist, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //获取传递的榜单id
        val toplistId = arguments?.getString("toplistId")
        if (toplistId != null) {
            initRecyclerView(toplistItemRecyclerView, toplistId)
        }

        //初始化页面
        initViews()


    }
    //获取某一个榜单的所有歌曲信息
    fun initRecyclerView(toplistItemRecyclerView: RecyclerView, toplistId:String){
        showLodingView()
        //获取已经登录的用户
        val loginedUser = context?.getSharedPreferences("userData", Context.MODE_PRIVATE)
        val userId = loginedUser?.getInt("u_id", -1)

        musicService.getOneTopList(toplistId)
            .enqueue(object: Callback<TopListItemBean> {
                override fun onFailure(call: Call<TopListItemBean>, t: Throwable) {
                    t.printStackTrace()
                }
                override fun onResponse(call: Call<TopListItemBean>, response: Response<TopListItemBean>) {
                    val res = response.body()
                    if(res != null){
                        val musics = res.playlist?.tracks
                        if (musics != null) {
                            for(item in musics){
                                val songsBean:SongsBean = SongsBean()
                                songsBean.id = item.id
                                songsBean.name = item.name
                                songsBean.ar = item.ar
                                //获取音乐类型
                                itemList = itemList.plus(songsBean)
                            }
                        }
                    }

                    val t1 = Thread{
                        getSongsInfo()
                        getMusicType()
                    }
                    t1.start()
                    t1.join()
                    showContentView()
                    //设置适配器
                    val layoutManager= LinearLayoutManager(mActivity)
                    toplistItemRecyclerView.layoutManager=layoutManager
                    //设置音乐列表的适配器
                    if(context != null)
                        adapter = TopListItemAdapter(context!!, itemList)
                    toplistItemRecyclerView.adapter=adapter
                }

            })
    }

    //获取音乐的歌手，图片等其他信息
    fun getSongsInfo(){
        var ids:String = ""
        for(song in itemList){
            ids += song.id
            ids += ","
        }

        ids = ids.substring(0, ids.length-1)
        musicService.getSongsDetail(ids)
            .enqueue(object: Callback<SongInfo>{
                override fun onFailure(call: Call<SongInfo>, t: Throwable) {
                    t.printStackTrace()
                }
                override fun onResponse(call: Call<SongInfo>, response: Response<SongInfo>) {
                    val res = response.body()
                    if(res != null){
                        val songs: List<SongsBean>? = res.songs
                        Log.d("NetmusiclistFragment", songs?.size.toString() + " " + itemList.size.toString())

                        for((index, song) in itemList.withIndex()){
                            if(index >= songs?.size!!)
                                break
                            itemList.get(index).al = AlbumBean()
                            val picUrl = songs.get(index).al?.picUrl
                            itemList.get(index).al?.picUrl = picUrl
                            val songers = songs.get(index).ar
                            var songersName = ""
                            if (songers != null) {
                                for((index, songer) in songers.withIndex()){
                                    songersName += songer.name
                                    if(index < songers.size-1)
                                        songersName += "/"
                                }
                            }
                            itemList.get(index).playerName = songersName
                            itemList.get(index).musicUrl = picUrl
                        }

                    }
                }

            })
    }

    //获取音乐类型
    fun getMusicType(){
        //获取已经登录的用户
        val loginedUser = context?.getSharedPreferences("userData", Context.MODE_PRIVATE)
        val userId = loginedUser?.getInt("u_id", -1)
        for((index, item) in itemList.withIndex()){
            val songsBean:SongsBean = itemList.get(index)
            //获取音乐类型
            if(context !=null && userId != null){
                val musicType: Int? =  MusicInfoDB.getMusicInfoDB(context!!)?.getMusicType(userId, songsBean.id.toString())
                Log.d("NetmusiclistFragment", musicType.toString())
                if(musicType != -1)
                    itemList.get(index).musicType = musicType
                else itemList.get(index).musicType = 1
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
    override fun onResume() {
        super.onResume()
        //初始化接收器，注册广播
        initReceivers()
    }

    override fun onPause() {
        super.onPause()
        //取消注册
        activity?.let { mediaBroadCastReceiver?.unRegisterReceiver(it) }
        activity?.let { fragmentBroadCastReceiver?.unRegisterReceiver(it) }
    }

    //初始化service和广播等事件
    fun initReceivers() {
        //注册和监听音乐播放广播
        mediaBroadCastReceiver = activity?.let { MediaBroadCastReceiver(it) }
        mediaBroadCastReceiver?.addNotificationReceiverListener(mediaReceiverListener)
        activity?.let { mediaBroadCastReceiver?.registerReceiver(it) }

        fragmentBroadCastReceiver= activity?.let { FragmentBroadCastReceiver(it) }
        fragmentBroadCastReceiver?.addFragmentReceiverListener(fragmentBroadCastReceiverListener)
        activity?.let { fragmentBroadCastReceiver?.registerReceiver(it) }
    }

    //初始化视图
    fun initViews(){
        //设置悬浮按钮的点击事件
        fab2.setOnClickListener {
            toplistItemRecyclerView.smoothScrollToPosition(0)
        }

        //监听开始播放等按钮
        musicPlayImageView.setOnClickListener {
            if(OnlinePlaying.playBinder.getPlayingStatus()){
                musicPlayImageView.setImageResource(R.mipmap.play)
            }else{
                musicPlayImageView.setImageResource(R.mipmap.pause)
            }
            OnlinePlaying.startPlayMusic()
        }

        //监听点击下一首
        musicNextImageView.setOnClickListener {
            OnlinePlaying.playNextMusic()
            musicPlayImageView.setImageResource(R.mipmap.pause)
        }

        //监听点击上一首
        musicLastImageView.setOnClickListener {
            OnlinePlaying.playPreMusic()
            musicPlayImageView.setImageResource(R.mipmap.pause)
        }
        //点击音乐图像
        musicImageView.setOnClickListener{
            //打开音乐播放界面
            val manager: FragmentManager?= activity?.getSupportFragmentManager()
            val fragmentTransaction = manager?.beginTransaction()
            fragmentTransaction?.replace(R.id.replace_layout, DetailFragment())
            fragmentTransaction?.commit()
        }
        //设置底部播放栏的内容
        setBottomMusicInfo()
        setImageViewImage(musicImageView)
    }

    /**
     * 设置旋转图片的地址
     */
    fun setImageViewImage(view: ImageView){
        if(OnlinePlaying.playingType == 0)
            view.setImageResource(OnlinePlaying.music.filmImageId)
        else if(OnlinePlaying.playingType == 1){
            Glide.with(this).load(OnlinePlaying.onlineMusic?.musicUrl).into(view)
        }
    }

    //设置底部播放栏的音乐标题和歌唱者
    fun setBottomMusicInfo(){
        if(OnlinePlaying.playingType == 0) {
            musicTitle.text = OnlinePlaying.currentMusic.substringBefore('-')//设置标题
            musicPerson.text = OnlinePlaying.currentMusic.substringAfter('-')
        } else if(OnlinePlaying.playingType == 1){
            musicTitle.text= OnlinePlaying.onlineMusic?.name
            musicPerson.text= OnlinePlaying.onlineMusic?.playerName
        }
    }

    //接收歌曲播放广播
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun doReceiveMediaAction(intent: Intent) {
        val action: String? =intent.action
        if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_PLAYING)){
            Log.i("TAG", "当前音乐被播放了 ")
            setBottomMusicInfo()

            musicPlayImageView.setImageResource(R.mipmap.pause)
            //设置音乐图片
            setImageViewImage(musicImageView)

        }else if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_PAUSED)){
            Log.i("TAG", "没有音乐在播放")
            musicPlayImageView.setImageResource(R.mipmap.play)
            //设置音乐图片
            setImageViewImage(musicImageView)
            setBottomMusicInfo()

        }else if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_FINISHED)){

        }
    }

    /**
     * 接收与碎片相关的广播，处理相关的控件（特别是点击列表的我喜欢图像）
     */
    fun doReceiveFragmentAction(intent: Intent){
        val action: String? =intent.action
        if(action.equals(FragmentBroadCastReceiver.ACTION_FRAGMENT_LIKE_CLICKED)){
            val loginedUser = context?.getSharedPreferences("userData", Context.MODE_PRIVATE)
            val userId = loginedUser?.getInt("u_id", -1)

            val position:Int = intent.getIntExtra("position", -1)
            if(userId != null && position != -1){
                val songBean:SongsBean = itemList.get(position)
                val musicId:String = songBean.id.toString()
                Log.d("NetmusiclistFragment", songBean.toString())

                //首先判断这首歌是否已经在数据库中
                if(context?.let { MusicInfoDB.getMusicInfoDB(it)?.isExist(userId, musicId) }!!){
                    //获取歌曲的类型
                    val musicType = MusicInfoDB.getMusicInfoDB(context!!)?.getMusicType(userId, musicId)
                    if(musicType == 3){
                        //取消喜欢
                        MusicInfoDB.getMusicInfoDB(context!!)?.updateMusicType(userId, musicId, 3, 1)
                        itemList.get(position).musicType = 1
                    }else if(musicType == 1){
                        //添加喜欢
                        MusicInfoDB.getMusicInfoDB(context!!)?.updateMusicType(userId, musicId, 1, 3)
                        itemList.get(position).musicType = 3
                    }
                }else{
                    //增加喜欢
                    itemList.get(position).musicType = 3
                    MusicInfoDB.getMusicInfoDB(context!!)?.add(songBean, userId)
                }

                adapter?.notifyDataSetChanged()
            }

        }
    }
}