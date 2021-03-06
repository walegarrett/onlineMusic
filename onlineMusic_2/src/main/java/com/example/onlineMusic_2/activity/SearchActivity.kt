package com.example.onlineMusic_2.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.adapter.OnlineMusicAdapter
import com.example.onlineMusic_2.bean.AlbumBean
import com.example.onlineMusic_2.bean.SongInfo
import com.example.onlineMusic_2.bean.SongsBean
import com.example.onlineMusic_2.db.MusicInfoDB
import com.example.onlineMusic_2.interfaces.MusicService
import com.example.onlineMusic_2.net.*
import com.example.onlineMusic_2.receiver.FragmentBroadCastReceiver
import com.example.onlineMusic_2.receiver.MediaBroadCastReceiver
import com.example.onlineMusic_2.utils.OnlinePlaying
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search.content_layout
import kotlinx.android.synthetic.main.activity_search.load_layout
import kotlinx.android.synthetic.main.activity_search.loadingImg
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchActivity : BaseActivity() {
    private var adapter: OnlineMusicAdapter? =null
    private var onlinemusiclist: List<SongsBean> = listOf()

    val musicService = ServiceCreator.create(MusicService::class.java)

    private var mAsyncTaskHttpUtil: AsyncTaskHttpUtil? = null

    var mediaBroadCastReceiver: MediaBroadCastReceiver? = null
    val mediaReceiverListener = object: MediaBroadCastReceiver.MediaReceiverListener{
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

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //???toolbar?????????????????????actionbar?????????
        setSupportActionBar(toolbar)
        //????????????????????????
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            //??????????????????
            val attrs = intArrayOf(android.R.attr.homeAsUpIndicator)
            val ta = theme.obtainStyledAttributes(attrs)
            val indicator = ta.getDrawable(0)
            it.setHomeAsUpIndicator(indicator)
        }

        showContentView()
    }

    override fun onResume() {
        super.onResume()
        //??????????????????
        initReceivers()
    }


    override fun onDestroy() {
        super.onDestroy()
        //??????????????????
        mediaBroadCastReceiver?.unRegisterReceiver(this)
        this.let { fragmentBroadCastReceiver?.unRegisterReceiver(it) }
    }

    //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_search,menu)
        // ??????menu?????????????????????
        val item = menu!!.findItem(R.id.search)

        // ?????????????????????SearchView
        val searchView: SearchView = item.actionView as SearchView
        searchView.queryHint = "??????????????????????????????"
        searchView.isSubmitButtonEnabled = true
        // ???searchView????????????
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // ???????????????????????????
            override fun onQueryTextSubmit(query: String?): Boolean {
                //???????????????????????????
                showLodingView()
                val keywords = searchView.query.toString()
                //??????????????????
                musicService.getSearchMusicsBean(keywords = keywords,type = 1, limit = 30)
                    .enqueue(object: Callback<SongInfo> {
                        override fun onResponse(call: Call<SongInfo>, response: Response<SongInfo>) {
                            val songInfo = response.body()
                            if(songInfo != null){
                                val songList: List<SongsBean>? = songInfo.result?.songs
                                if (songList != null) {
                                    onlinemusiclist = songList
                                }
                                Log.d("songslist", songList.toString())
                                //????????????????????????????????????
                                val t1 = Thread{
                                    getSongsImage()
                                }
                                t1.start()
                                t1.join()
                                //?????????????????????
                                showContentView()
                                //???????????????
                                val layoutManager= LinearLayoutManager(applicationContext)
                                musicRecyclerViewSearch.layoutManager=layoutManager
                                //??????????????????????????????
                                adapter= onlinemusiclist.let { OnlineMusicAdapter(it) }
                                musicRecyclerViewSearch.adapter=adapter
                            }
                        }
                        override fun onFailure(call: Call<SongInfo>, t: Throwable) {
                            t.printStackTrace()
                        }
                    })
                return false
            }
            // ????????????????????????
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return true
    }
    //??????????????????
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->finish()
            R.id.search ->{}

        }
        return true
    }
    //??????????????????????????????????????????????????????
    fun getSongsImage(){
        var ids:String = ""
        for((index, song) in onlinemusiclist.withIndex()){
            ids += song.id
            ids += ","

            //?????????????????????????????????
            val songers = song.artists
            var songersName = ""
            if (songers != null) {
                for((cnt, songer) in songers.withIndex()){
                    songersName += songer.name
                    if(cnt < songers.size-1)
                        songersName += "/"
                }
            }
            onlinemusiclist.get(index).playerName = songersName
            onlinemusiclist.get(index).musicType = 1
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
                        Log.d("SearchActivity", songs?.size.toString() + " " + onlinemusiclist.size.toString())

                        for((index, song) in onlinemusiclist.withIndex()){
                            if(index >= songs?.size!!)
                                break
                            onlinemusiclist.get(index).al = AlbumBean()
                            val picUrl = songs.get(index).al?.picUrl
                            onlinemusiclist.get(index).al?.picUrl = picUrl
                            onlinemusiclist.get(index).musicUrl = picUrl

//                            onlinemusiclist?.get(index)?.al?.picUrl?.let { Log.d("SearchActivity", it) }
                        }

                    }
                }

            })
    }
    /**
     * ?????????????????????
     */
    fun showLodingView(){
        val rotateAnimation = AnimationUtils.loadAnimation(
            this,
            R.anim.anim_rotate
        )
        rotateAnimation.setInterpolator(LinearInterpolator()) // ??????
        content_layout.setVisibility(View.GONE)
        load_layout.setVisibility(View.VISIBLE)
        loadingImg.clearAnimation()
        loadingImg.startAnimation(rotateAnimation)
    }

    /**
     * ??????????????????
     */
    fun showContentView(){
        content_layout.setVisibility(View.VISIBLE)
        load_layout.setVisibility(View.GONE)
        loadingImg.clearAnimation()
    }

    //?????????service??????????????????
    private fun initReceivers(){
        mediaBroadCastReceiver = MediaBroadCastReceiver(this)
        mediaBroadCastReceiver?.addNotificationReceiverListener(mediaReceiverListener)
        mediaBroadCastReceiver?.registerReceiver(this)

        fragmentBroadCastReceiver= this.let { FragmentBroadCastReceiver(it) }
        fragmentBroadCastReceiver?.addFragmentReceiverListener(fragmentBroadCastReceiverListener)
        this.let { fragmentBroadCastReceiver?.registerReceiver(it) }
    }

    //????????????????????????
    private fun doReceiveMediaAction(intent: Intent) {
        val action: String? =intent.action
        if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_FINISHED)){
//            OnlinePlaying.playNextMusic()
        }else if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_PLAYING)){
        }else if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_PAUSED)){
        }
    }

    //??????????????????
    fun setImageResource(imageView: ImageView){
        if(OnlinePlaying.playingType == 0)
            imageView.setImageResource(OnlinePlaying.music.filmImageId)
        else if(OnlinePlaying.playingType == 1){
            Glide.with(this).load(OnlinePlaying.onlineMusic?.musicUrl).into(imageView)
        }
    }

    //????????????????????????
    fun doReceiveFragmentAction(intent: Intent){
        val action: String? =intent.action
        if(action.equals(FragmentBroadCastReceiver.ACTION_FRAGMENT_LIKE_CLICKED)){
            val loginedUser = this.getSharedPreferences("userData", Context.MODE_PRIVATE)
            val userId = loginedUser?.getInt("u_id", -1)

            val position:Int = intent.getIntExtra("position", -1)
            if(userId != null && position != -1){
                val songBean:SongsBean = onlinemusiclist.get(position)
                val musicId:String = songBean.id.toString()
                Log.d("NetmusiclistFragment", songBean.toString())

                //????????????????????????????????????????????????
                if(this.let { MusicInfoDB.getMusicInfoDB(it)?.isExist(userId, musicId) }!!){
                    //?????????????????????
                    val musicType = MusicInfoDB.getMusicInfoDB(this)?.getMusicType(userId, musicId)
                    if(musicType == 3){
                        //????????????
                        MusicInfoDB.getMusicInfoDB(this)?.updateMusicType(userId, musicId, 3, 1)
                        onlinemusiclist.get(position).musicType = 1
                    }else if(musicType == 1){
                        //????????????
                        MusicInfoDB.getMusicInfoDB(this)?.updateMusicType(userId, musicId, 1, 3)
                        onlinemusiclist.get(position).musicType = 3
                    }
                }else{
                    //????????????
                    onlinemusiclist.get(position).musicType = 3
                    MusicInfoDB.getMusicInfoDB(this)?.add(songBean, userId)
                }

                adapter?.notifyDataSetChanged()
            }

        }
    }
}