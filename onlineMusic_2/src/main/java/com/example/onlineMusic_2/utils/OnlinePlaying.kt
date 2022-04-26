package com.example.onlineMusic_2.utils

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.onlineMusic_2.bean.Music
import com.example.onlineMusic_2.bean.MusicUrl
import com.example.onlineMusic_2.bean.SongsBean
import com.example.onlineMusic_2.interfaces.MusicService
import com.example.onlineMusic_2.service.MusicPlayService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object OnlinePlaying {

    var musiclist=ArrayList<Music>()
    var onlinemusiclist = ArrayList<SongsBean>()

    //播放类型：0--表示android自带歌曲，1--表示网络歌曲，2--表示本地歌曲
    var playingType:Int = 0

    //当前歌曲在列表中的位置
    var music_position:Int=0
    //当前歌曲在列表中的位置
    var online_music_position:Int=0

    //当前播放的音乐
    var currentMusic:String=""

    var music:Music=Music("",0,"")
    var onlineMusic:SongsBean ?= null

    //当前是否绑定
    var connected:Boolean=false
    //是否随机播放
    var isRandomPlay:Boolean=false
    //是否滚动歌词
    var isScollLrc:Boolean=true

    val baseUrl1 = "http://123.57.176.198:3000"//http://10.101.58.109/    http://123.57.176.198:3000/     http://10.101.11.130:3000
    val retrofit = createRetrofit(baseUrl1)

    lateinit var playBinder: MusicPlayService.PlayBinder
    var connection=object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            playBinder=service as MusicPlayService.PlayBinder
        }
        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    //根据音乐列表的位置获取音乐实体
    fun getMusic(position:Int): Music {
        val music: Music
        if(position>=musiclist.size){
            music= musiclist[0]
        }else if(position<0){
            music= musiclist[musiclist.size-1]
        }else
            music= musiclist[position]
        return music
    }

    //根据音乐列表的位置获取音乐实体
    fun getOnlineMusic(position:Int): SongsBean {

        val music: SongsBean
        if(position>= onlinemusiclist.size){
            music= onlinemusiclist[0]
        }else if(position<0){
            music= onlinemusiclist[onlinemusiclist.size-1]
        }else
            music= onlinemusiclist[position]
        return music
    }

    /**
     * 开始播放音乐
     */
    fun startPlayMusic(){
        playBinder.startPlay()
    }

    /**
     * 播放上一首
     */
    fun playPreMusic(){
        if(playingType == 0){
            if(isRandomPlay){
                music_position=(0 until musiclist.size).random()
            }else{
                music_position -=1
            }
            val music: Music =getMusic(music_position)
            if(music_position<0)
                music_position=musiclist.size-1
            currentMusic=music.filmName
            this.music=music
            playBinder.setPlayingStatus(false)
            playBinder.changeMusic(currentMusic+".mp3")
        }else{
            if(isRandomPlay){
                online_music_position=(0 until onlinemusiclist.size).random()
            }else{
                online_music_position -=1
            }
            val music: SongsBean =getOnlineMusic(online_music_position)
            if(online_music_position<0)
                online_music_position= onlinemusiclist.size-1
            currentMusic= music.name.toString()
            this.onlineMusic = music
            playBinder.setPlayingStatus(false)


            val musicService = retrofit.create(MusicService::class.java)
            musicService.getMusic(id = music.id)
                .enqueue(object: Callback<MusicUrl> {
                    override fun onResponse(call: Call<MusicUrl>, response: Response<MusicUrl>) {
                        val musicurl = response.body()
                        if(musicurl != null){
                            val urllist: List<MusicUrl.DataBean>? = musicurl.data
                            if(urllist!=null && urllist.size>0) {
                                Log.d("获取歌曲url","playNextMusic")
                                urllist[0].url?.let { playBinder.changeMusic(it, 1) }
                            }
                        }
                    }
                    override fun onFailure(call: Call<MusicUrl>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
        }
//        playBinder.startPlay()
    }

    /**
     * 播放下一首
     */
    fun playNextMusic(){
        Log.d("OnlinePlaying", "playNextMusic")
        if(playingType == 0){
            if(isRandomPlay){
                music_position=(0 until musiclist.size).random()
            }else{
                music_position +=1
            }
            val music: Music =getMusic(music_position)
            if(music_position >= musiclist.size)
                music_position=0
            currentMusic=music.filmName
            this.music=music
            playBinder.setPlayingStatus(false)
            playBinder.changeMusic(currentMusic+".mp3")
        }else{
            if(isRandomPlay){
                online_music_position=(0 until onlinemusiclist.size).random()
            }else{
                online_music_position +=1
            }
            val music: SongsBean =getOnlineMusic(online_music_position)
            if(online_music_position >= onlinemusiclist.size)
                online_music_position = 0
            currentMusic= music.name.toString()
            this.onlineMusic = music
            playBinder.setPlayingStatus(false)


            val musicService = retrofit.create(MusicService::class.java)
            musicService.getMusic(id = music.id)
                .enqueue(object: Callback<MusicUrl> {
                    override fun onResponse(call: Call<MusicUrl>, response: Response<MusicUrl>) {
                        val musicurl = response.body()
                        if(musicurl != null){
                            val urllist: List<MusicUrl.DataBean>? = musicurl.data
                            if(urllist!=null && urllist.size>0) {
                                Log.d("获取歌曲url","playNextMusic")
                                urllist[0].url?.let { playBinder.changeMusic(it, 1) }
                            }
                        }
                    }
                    override fun onFailure(call: Call<MusicUrl>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
        }

//        playBinder.startPlay()
    }

    /**
     * 切换歌曲
     */
    fun changeMusic(position:Int){
        playingType = 0
        val music= musiclist[position]
        music_position=position
        currentMusic=music.filmName
        OnlinePlaying.music=music
        playBinder.setPlayingStatus(false)
        playBinder.changeMusic(currentMusic+".mp3")

//        playBinder.startPlay()
    }
    /**
     * 切换网络歌曲
     */
    fun changeMusic(pos:Int = 0, musicId:Int, musicUrl:String, type:Int){
        playingType = type
        val onlineMusic= onlinemusiclist[pos]
        online_music_position = pos

        currentMusic= onlineMusic.name.toString()
        OnlinePlaying.onlineMusic=onlineMusic

        playBinder.setPlayingStatus(false)
        playBinder.changeMusic(musicUrl,type = type)
    }

    /**
     * 根据指定的基IP地址获取retrofit
     */
    fun createRetrofit(baseURL: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseURL)//如果使用雷电模拟器，不能使用10.0.2.2地址，需要使用实际的ip地址
            .addConverterFactory(GsonConverterFactory.create())//当需要使用gson解析时调用该方法
            .build()
    }
}