package com.example.onlineMusic_2.fragment

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.adapter.MusicDetailListAdapter
import com.example.onlineMusic_2.bean.LrcRow
import com.example.onlineMusic_2.bean.Lyric
import com.example.onlineMusic_2.bean.SongsBean
import com.example.onlineMusic_2.db.MusicInfoDB
import com.example.onlineMusic_2.interfaces.MusicService
import com.example.onlineMusic_2.net.HttpUtil
import com.example.onlineMusic_2.net.ServiceCreator
import com.example.onlineMusic_2.receiver.FragmentBroadCastReceiver
import com.example.onlineMusic_2.receiver.MediaBroadCastReceiver
import com.example.onlineMusic_2.utils.DefaultLrcParser
import com.example.onlineMusic_2.utils.OnlinePlaying
import kotlinx.android.synthetic.main.fragment_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : BaseFragment() {
    private var adapter: MusicDetailListAdapter? =null
    var lrc_list:ArrayList<LrcRow>?=null
    var isEntered:Boolean=false
    lateinit var mAnimator:ObjectAnimator

    //网络服务相关初始化
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
    //监听打开侧滑抽屉并选择item
    var fragmentBroadCastReceiver:FragmentBroadCastReceiver? = null
    val fragmentReceiverListener = object: FragmentBroadCastReceiver.FragmentReceiverListener{
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
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d("DetailFragment","onActivityCreated")
        super.onActivityCreated(savedInstanceState)

        //设置音乐的recycleView适配器
        val layoutManager= LinearLayoutManager(context)
        detailListRecyclerView.layoutManager=layoutManager

        //设置音乐列表的适配器
        adapter= mActivity?.let { MusicDetailListAdapter(it, OnlinePlaying.musiclist) }
        detailListRecyclerView.adapter=adapter

        //开启动画
        initAnimation()

        //初始化页面和各类事件
        initViews()
    }

    //初始化页面控件
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun initViews(){
        //打开侧滑抽屉
        musicListImageView.setOnClickListener {
            if (drawerLayout1?.isDrawerOpen(GravityCompat.END)==false) {
                drawerLayout1?.openDrawer(GravityCompat.END)
            }
        }

        //设置音乐标题
        musicTitleTextView.text=OnlinePlaying.currentMusic
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
            Log.d("MusicdetailActivity","musicNext")
            OnlinePlaying.playNextMusic()
            musicPlayImageView.setImageResource(R.mipmap.pause)
        }
        //监听点击上一首
        musicLastImageView.setOnClickListener {
            OnlinePlaying.playPreMusic()
            musicPlayImageView.setImageResource(R.mipmap.pause)
        }
        //监听seekbar的点击拖动
        progressBar3.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //跳转到指定位置
                val progress =seekBar.progress
                OnlinePlaying.playBinder.seekTo(progress)
            }
        })
        //监听随机播放按钮
        musicRandomImageView.setOnClickListener {
            when(OnlinePlaying.isRandomPlay){
                true->{
                    OnlinePlaying.isRandomPlay=false
                    musicRandomImageView.setImageResource(R.mipmap.sequence1)
                }
                false->{
                    OnlinePlaying.isRandomPlay=true
                    musicRandomImageView.setImageResource(R.mipmap.random1)
                }
            }
        }

        //点击中间图片，滚动显示歌词
        musicMainImageView.setOnClickListener {
            OnlinePlaying.isScollLrc=true
            lrcView.visibility=View.VISIBLE
            musicMainImageView.visibility=View.GONE
            musicWordsTextView.visibility=View.GONE
            musicWordsTextView2.visibility=View.GONE

            setViewBackGround(anotherFragment)

            mAnimator.pause()
        }
        //点击滚动的空间，转换为两行显示歌词
        lrcView.setOnClickListener {
            OnlinePlaying.isScollLrc=false
            lrcView.visibility=View.INVISIBLE
            musicMainImageView.visibility=View.VISIBLE
            musicWordsTextView.visibility=View.VISIBLE
            musicWordsTextView2.visibility=View.VISIBLE
            anotherFragment.setBackgroundResource(R.mipmap.background4)
            if(OnlinePlaying.playBinder.getPlayingStatus()){
                //设置图片旋转
                mAnimator.start()
            }else {
                mAnimator.pause()
            }
        }
        //拖动歌词
        /*lrcView.setOnDragListener { v, event ->
            lrcView.onDrag()
        }*/
        //当前是否是上下滚动显示歌词
        if(OnlinePlaying.isScollLrc){
            lrcView.visibility=View.VISIBLE
            musicMainImageView.visibility=View.GONE
            musicWordsTextView.visibility=View.GONE
            musicWordsTextView2.visibility=View.GONE

            setViewBackGround(anotherFragment)

        }else{
            lrcView.visibility=View.INVISIBLE
            musicMainImageView.visibility=View.VISIBLE
            musicWordsTextView.visibility=View.VISIBLE
            musicWordsTextView2.visibility=View.VISIBLE
            anotherFragment.setBackgroundResource(R.mipmap.background4)
        }
        //点击我喜欢收藏歌曲
        myLikeImageView.setOnClickListener {
            val loginedUser = context?.getSharedPreferences("userData", Context.MODE_PRIVATE)
            val userId = loginedUser?.getInt("u_id", -1)
            if(userId != null && OnlinePlaying.playingType == 1){
                val songBean: SongsBean? = OnlinePlaying.onlineMusic
                val position:Int = OnlinePlaying.online_music_position
                val musicId:String = songBean?.id.toString()
                //首先判断这首歌是否已经在数据库中
                if(context?.let { MusicInfoDB.getMusicInfoDB(it)?.isExist(userId, musicId) }!!){
                    //获取歌曲的类型
                    val musicType = MusicInfoDB.getMusicInfoDB(context!!)?.getMusicType(userId, musicId)
                    if(musicType == 3){
                        //取消喜欢
                        MusicInfoDB.getMusicInfoDB(context!!)?.updateMusicType(userId, musicId, 3, 1)
                        OnlinePlaying.onlineMusic?.musicType = 1
                        OnlinePlaying.onlinemusiclist[position].musicType = 1
                        myLikeImageView.setImageResource(R.drawable.ic_favorite_border_red_24dp)

                    }else if(musicType == 1){
                        //修改歌曲类型为喜欢
                        OnlinePlaying.onlineMusic?.musicType = 3
                        OnlinePlaying.onlinemusiclist[position].musicType = 3
                        MusicInfoDB.getMusicInfoDB(context!!)?.updateMusicType(userId, musicId, 1, 3)
                        myLikeImageView.setImageResource(R.mipmap.heart)
                    }
                }else{
                    if (songBean != null) {
                        //增加喜欢
                        OnlinePlaying.onlineMusic?.musicType = 3
                        OnlinePlaying.onlinemusiclist[position].musicType = 3
                        myLikeImageView.setImageResource(R.mipmap.heart)
                        MusicInfoDB.getMusicInfoDB(context!!)?.add(songBean, userId)
                    }
                }
                adapter?.notifyDataSetChanged()
            }
        }

    }
    /**
     * 负责播放音乐和更新控件的内部类
     */
    inner class MusicProgressTask : AsyncTask<MediaPlayer, Int, Boolean>() {
        override fun onPreExecute() {}
        override fun doInBackground(vararg params: MediaPlayer?) = try {
            while(isEntered&&OnlinePlaying.playBinder.getPlayingStatus()){
                Thread.sleep(1000)
                //不能在这里更新控件，需要使用handleMessage
                publishProgress(OnlinePlaying.playBinder.getProgress())
            }
            true
        } catch (e: Exception) {
            false
        }

        //根据进度更新控件
        override fun onProgressUpdate(vararg values: Int?) {
            //设置进度条
            values[0]?.let { updateSeek(it) }
            values[0]?.let { updateTime(position = it) }
            if(OnlinePlaying.isScollLrc){
                if(values[0]!=null&&lrcView!=null)
                    lrcView.onProgress(values[0])
            }else{
                showLrc(values[0])
            }
        }
    }

    /**
     * 页面重新出现
     */
    override fun onResume() {
        super.onResume()
        isEntered=true
        Log.i("DetailFragment", "onResume")
        //注册监听事件
        initReceivers()


        //开始更新控件
        if(OnlinePlaying.playBinder.getPlayingStatus()){
            val intent= Intent("isPlaying")
            activity?.sendBroadcast(intent)
        }else{
            val intent= Intent("isPaused")
            activity?.sendBroadcast(intent)
        }
        /**
         * 判断当前是否随机播放
         */
        if(OnlinePlaying.isRandomPlay){
            musicRandomImageView.setImageResource(R.mipmap.random1)
        }else{
            musicRandomImageView.setImageResource(R.mipmap.sequence1)
        }
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onPause() {
        Log.i("DetailFragment", "onPause")
        super.onPause()
        //取消注册广播
        activity?.let { mediaBroadCastReceiver?.unRegisterReceiver(it) }
        activity?.let { fragmentBroadCastReceiver?.unRegisterReceiver(it) }

        isEntered=false
        MusicProgressTask().cancel(true)

        mAnimator.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MusicProgressTask().cancel(true)
    }

    /**
     * 更新进度条的进度
     */
    fun updateSeek(position:Int){
        progressBar3?.progress = position
        if(progressBar3==null)
            Log.i("DetailFragment", "updateSeek: ${position}")
    }

    /**
     * 更新当前时间
     */
    fun updateTime(position:Int){
        val minute=position/1000/60 as Int
        var minutes=minute.toString()
        if(minute<10)
            minutes="0"+minute
        val second=position/1000%60 as Int
        var seconds=second.toString()
        if(second<10)
            seconds="0"+second
        startTimeTextView?.text=minutes+":"+seconds
    }

    /**
     * 两行交替显示歌词
     */
    private fun showLrc(current: Int?) {
        for((index,row) in lrc_list!!.withIndex()){
            if(index+1< lrc_list!!.size){
                if(current!! >= row.time && current < lrc_list!![index+1].time){
                    //第二行的歌词字体颜色固定
                    musicWordsTextView2?.text= lrc_list!![index+1].content
                    musicWordsTextView2?.setTextColor(Color.parseColor("#009fdf"))
                    //第一行的歌词设置颜色渐变
                    val spannnableString: SpannableString = SpannableString(row.content)
                    val colorSpan: ForegroundColorSpan = ForegroundColorSpan(Color.parseColor("#ff0000"))
                    val colorBSpan: ForegroundColorSpan = ForegroundColorSpan(Color.parseColor("#009fdf"))
                    val end:Double=(current-row.time).toDouble()/row.getTotalTime().toDouble()* (row.content?.length!!)
                    if(end>0){
                        spannnableString.setSpan(colorSpan,0, Math.min((end+1).toInt(), row.content!!.length), Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        if((end+1).toInt()< row.content!!.length)
                            spannnableString.setSpan(colorBSpan,Math.min((end+1).toInt(), row.content!!.length-1), row.content!!.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                    }
                    musicWordsTextView?.text=spannnableString
                    break
                }
            }else{
                musicWordsTextView2?.text= ""
                //第一行的歌词设置颜色渐变
                val spannnableString: SpannableString = SpannableString(row.content)
                val colorSpan: ForegroundColorSpan = ForegroundColorSpan(Color.parseColor("#ff0000"))
                val colorBSpan: ForegroundColorSpan = ForegroundColorSpan(Color.parseColor("#009fdf"))
                var span= current?.minus(row.time)
                if(span==null)
                    span=0
                val end:Double=span.toDouble()/row.getTotalTime().toDouble()* (row.content?.length!!)
                if(end>0){
                    spannnableString.setSpan(colorSpan,0, Math.min((end+1).toInt(), row.content!!.length), Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                    if((end+1).toInt()< row.content!!.length)
                        spannnableString.setSpan(colorBSpan,Math.min((end+1).toInt(), row.content!!.length-1), row.content!!.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                }
                musicWordsTextView?.text=spannnableString
                break
            }
        }
    }
    /**
     * 获取整首歌的歌词
     */
    private fun getLrc(): ArrayList<LrcRow>? {
        val defaultLrcParser= DefaultLrcParser()
        if(OnlinePlaying.playingType == 0){
            //歌词文件名
            val lrc= OnlinePlaying.currentMusic+".lrc"
            val assetManager= activity?.assets
            val fd=assetManager?.open(lrc)
            val list = defaultLrcParser.getLrcRows(fd) as ArrayList<LrcRow>
            return list
        }else{
            var list:ArrayList<LrcRow>? = ArrayList()
            //获取在线歌曲歌词
            OnlinePlaying.onlineMusic?.id?.let { musicService.getMusicLyric(id = it)
                    .enqueue(object: Callback<Lyric> {
                        override fun onResponse(call: Call<Lyric>, response: Response<Lyric>) {
                            val res = response.body()
                            val lyricString: String? = res?.lrc?.lyric
                            if(lyricString != null){
//                                Log.d("DetailFragment", lyricString)
                                val lyricRowList:List<String> = lyricString.split("\n")
                                val parserList = defaultLrcParser.getLrcRows(lyricRowList)
                                if(parserList != null)
                                    list = defaultLrcParser.getLrcRows(lyricRowList) as ArrayList<LrcRow>
                                //设置歌词
                                lrc_list = list
                                if(lrcView != null)
                                    list?.let { lrcView.setLrc(it) }
                            }
                        }
                        override fun onFailure(call: Call<Lyric>, t: Throwable) {
                            t.printStackTrace()
                        }
                    })
            }
            return list
        }
    }

    /**
     * 初始化图片旋转
     */
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun initAnimation(){
        mAnimator = ObjectAnimator.ofFloat(musicMainImageView, "rotation", 0.0f, 360.0f)
        mAnimator.duration = 10000 //设定转一圈的时间
        mAnimator.repeatCount = Animation.INFINITE //设定无限循环
        mAnimator.repeatMode = ObjectAnimator.RESTART // 循环模式
        mAnimator.interpolator = LinearInterpolator() // 匀速
        mAnimator.start() //动画开始
        mAnimator.pause() //动画暂停
    }

    //初始化service和广播等事件
    private fun initReceivers(){
        //注册和监听音乐播放广播
        mediaBroadCastReceiver = activity?.let { MediaBroadCastReceiver(it) }
        mediaBroadCastReceiver?.addNotificationReceiverListener(mediaReceiverListener)
        activity?.let { mediaBroadCastReceiver?.registerReceiver(it) }

        //注册和监听侧滑抽屉打开广播
        fragmentBroadCastReceiver = activity?.let { FragmentBroadCastReceiver(it) }
        fragmentBroadCastReceiver?.addFragmentReceiverListener(fragmentReceiverListener)
        activity?.let { fragmentBroadCastReceiver?.registerReceiver(it) }

    }

    //接收歌曲播放广播
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun doReceiveMediaAction(intent: Intent) {
        val action: String? =intent.action
        if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_PLAYING)){
            Log.i("TAG", "当前音乐被播放了 ")
            musicTitleTextView.text= OnlinePlaying.currentMusic//设置标题
            musicPlayImageView.setImageResource(R.mipmap.pause)
            progressBar3.max= OnlinePlaying.playBinder.getDuration()
            //设置音乐图片
            setImageViewImage(musicMainImageView)

            //设置我喜欢图标
            setMyLikeImageView()

            //设置背景图片
            if(OnlinePlaying.isScollLrc) {
                setViewBackGround(anotherFragment)
                mAnimator.pause()
            }
            else {
                anotherFragment.setBackgroundResource(R.mipmap.background4)
                //设置图片旋转
                mAnimator.start()
            }
            val minute=OnlinePlaying.playBinder.getDuration()/1000/60 as Int
            var minutes=minute.toString()
            if(minute<10)
                minutes="0"+minute
            val second=OnlinePlaying.playBinder.getDuration()/1000%60 as Int
            var seconds=second.toString()
            if(second<10)
                seconds="0"+second
            endTimeTextView.text=minutes+":"+seconds
            //获取整首歌的歌词
            lrc_list=getLrc()
            Log.d("DetailFragment", lrc_list.toString())
            lrc_list?.let { lrcView.setLrc(it) }
            updateTime(OnlinePlaying.playBinder.getProgress())
            MusicProgressTask().execute()
        }else if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_PAUSED)){
            Log.i("TAG", "没有音乐在播放")
            //获取整首歌的歌词
            lrc_list=getLrc()
            musicPlayImageView.setImageResource(R.mipmap.play)
            //设置音乐图片
            setImageViewImage(musicMainImageView)

            //设置我喜欢图标
            setMyLikeImageView()

            musicTitleTextView.text= OnlinePlaying.currentMusic//设置标题
            progressBar3.max= OnlinePlaying.playBinder.getDuration()
            //设置背景图片
            if(OnlinePlaying.isScollLrc) {
                setViewBackGround(anotherFragment)
                mAnimator.pause()
            }
            else {
                anotherFragment.setBackgroundResource(R.mipmap.background4)
                mAnimator.pause()
            }
            updateTime(OnlinePlaying.playBinder.getProgress())
            updateSeek(OnlinePlaying.playBinder.getProgress())
            showLrc(OnlinePlaying.playBinder.getProgress())
            lrc_list?.let { lrcView.setLrc(it) }
            MusicProgressTask().cancel(true)
        }else if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_FINISHED)){
            Log.i("TAG", "完成播放准备播放下一首")
//                OnlinePlaying.playNextMusic()
            MusicProgressTask().cancel(true)
        }
    }

    private fun doReceiveFragmentAction(intent: Intent){
        val action: String? =intent.action
        if(action.equals(FragmentBroadCastReceiver.ACTION_FRAGMENT_PLAY_DETAILSONG)){
            //关闭侧滑抽屉
            if (drawerLayout1?.isDrawerOpen(GravityCompat.END)==true) {
                drawerLayout1?.closeDrawers()
            }
        }
    }

    /**
     * 设置我喜欢图标
     */
    fun setMyLikeImageView(){
        //获取已经登录的用户
        val loginedUser = context?.getSharedPreferences("userData", Context.MODE_PRIVATE)
        val userId = loginedUser?.getInt("u_id", -1)
        if(OnlinePlaying.playingType == 0){
            myLikeImageView.setImageResource(R.drawable.ic_favorite_border_red_24dp)
        }else if(OnlinePlaying.playingType == 1){//当前播放的是网络歌曲
            if(OnlinePlaying.onlineMusic?.musicType == 3){
                myLikeImageView.setImageResource(R.mipmap.heart)
            }else{
                myLikeImageView.setImageResource(R.drawable.ic_favorite_border_red_24dp)
            }
        }
    }
    /**
     * 设置背景图片的地址
     */
    fun setViewBackGround(view:View){
        if(OnlinePlaying.playingType == 0)
            view.setBackgroundResource(OnlinePlaying.music.filmImageId)
        else if(OnlinePlaying.playingType == 1){
            if(OnlinePlaying.onlineMusic?.musicUrl != null)
                Glide.with(this).asBitmap().load(OnlinePlaying.onlineMusic?.musicUrl ).into(object : SimpleTarget<Bitmap?>() {
                    //设置宽高
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        val drawable: Drawable = BitmapDrawable(resource)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            view.background = drawable //设置背景
                        }
                    }
                })
        }
    }

    /**
     * 设置旋转图片的地址
     */
    fun setImageViewImage(view:ImageView){
        if(OnlinePlaying.playingType == 0)
            view.setImageResource(OnlinePlaying.music.filmImageId)
        else if(OnlinePlaying.playingType == 1){
            Glide.with(this).load(OnlinePlaying.onlineMusic?.musicUrl).into(view)
        }
    }
}