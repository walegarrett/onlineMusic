package com.example.onlineMusic_2.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.receiver.MediaBroadCastReceiver
import com.example.onlineMusic_2.receiver.NotificationBroadCastReceiver
import com.example.onlineMusic_2.utils.OnlinePlaying
import java.net.URL


class MusicPlayService : Service() {
    var mediaPlayer= MediaPlayer()
    lateinit var remoteView:RemoteViews
    lateinit var manager:NotificationManager
    lateinit var notification: Notification

    var mediaBroadCastReceiver: MediaBroadCastReceiver? = null
    val mediaReceiverListener = object: MediaBroadCastReceiver.MediaReceiverListener{
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                doReceiveMediaAction(intent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.d("MusicPlayService","onCreate executed")

        //初始化通知栏
        initNotificationView()

        //初始化广播注册
        initReceivers()

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MusicPlayService","onStartCommand executed")
        return super.onStartCommand(intent, flags, startId)
    }

    //mBiner是提供给与本服务绑定了的activity访问的，方便了活动控制服务（即歌曲播放的控制）
    private val mBinder=PlayBinder()
    var currentTime:Int=0
    var running:Boolean=false//当前音乐是否在播放
    var duration:Int=0
    inner class PlayBinder: Binder(){
        fun startPlay(){
            startPlay_1()
        }
        //获取歌曲播放进度
        fun getProgress():Int{
           return  getProgress_1()
        }
        //歌曲的快进
        fun seekTo(seek:Int){
            seekTo_1(seek)
        }
        //切换歌曲
        fun changeMusic(musicTitle:String){
            changeMusic_1(musicTitle)
        }

        //切换在线歌曲
        fun changeMusic(musicUrl:String,type:Int){
            changeMusic_1(musicUrl,type)
        }
        //获取歌曲的播放时长
        fun getDuration():Int{
            return getDuration_1()
        }
        //获取当前歌曲的播放状态
        fun getPlayingStatus():Boolean{
            return getPlayingStatus_1()
        }
        fun setPlayingStatus(status:Boolean){
            setPlayingStatus_1(status)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("MusicService","onBind executed")
        val assetManager= applicationContext?.assets
        val musicTitle: String? = intent?.getStringExtra("musicTitle")
        val fd= musicTitle?.let { assetManager?.openFd(it) }
        if (fd != null) {
            mediaPlayer.setDataSource(fd.fileDescriptor,fd.startOffset,fd.length)
        }
        mediaPlayer.prepare()
        currentTime=0
        mediaPlayer.setOnPreparedListener {
            duration=mediaPlayer.duration
        }
        mediaPlayer.setOnCompletionListener {
            //一个广播，通知应用一首歌已经播放结束了
            val intent1=Intent(MediaBroadCastReceiver.ACTION_MEDIA_FINISHED)
            sendBroadcast(intent1)
            running=false
        }
        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        Log.d("MusicService","onDestroy executed")
        //取消注册广播
        mediaBroadCastReceiver?.unRegisterReceiver(this)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("MusicService","onUnbind executed")
        return super.onUnbind(intent)
    }

    //初始化service和广播等事件
    private fun initReceivers(){
        mediaBroadCastReceiver = MediaBroadCastReceiver(this)
        mediaBroadCastReceiver?.addNotificationReceiverListener(mediaReceiverListener)
        mediaBroadCastReceiver?.registerReceiver(this)
    }

    //初始化通知栏
    fun initNotificationView(){
        //开启前台service
        val CHANNEL_ONE_ID = "CHANNEL_ONE_ID"
        val CHANNEL_ONE_NAME = "CHANNEL_ONE_ID"
        val notificationChannel: NotificationChannel
        //进行8.0的判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                CHANNEL_ONE_ID,
                CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.setShowBadge(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(notificationChannel)
        }else{
            manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        // 当点击通知栏的按钮时需要使用的Intent
        val playIntent = Intent(NotificationBroadCastReceiver.ACTION_NOTIFICATION_PLAYBTN)
        val playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val nextIntent = Intent(NotificationBroadCastReceiver.ACTION_NOTIFICATION_NEXTBTN)
        val nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val preIntent = Intent(NotificationBroadCastReceiver.ACTION_NOTIFICATION_PREBTN)
        val prePendingIntent = PendingIntent.getBroadcast(this, 0, preIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //设置通知栏的布局文件
        remoteView = RemoteViews(packageName,R.layout.notification_main)
        //通知栏点击事件
        remoteView.setOnClickPendingIntent(R.id.playBtn,playPendingIntent)
        remoteView.setOnClickPendingIntent(R.id.nextBtn,nextPendingIntent)
        remoteView.setOnClickPendingIntent(R.id.preBtn,prePendingIntent)

        //更新通知栏的图标
        remoteView.setTextViewText(R.id.notificationTextView,OnlinePlaying.currentMusic)
        remoteView.setImageViewResource(R.id.playBtn,android.R.drawable.ic_media_play)
        remoteView.setImageViewResource(R.id.imageTitle,OnlinePlaying.music.filmImageId)

        notification = NotificationCompat.Builder(this, CHANNEL_ONE_ID).setChannelId(CHANNEL_ONE_ID)
            .setTicker("Nature")
            .setOnlyAlertOnce(true)//设置只响一次，首次开启通知时有响声
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContent(remoteView)
            .build()
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        startForeground(1, notification)
    }
    //开始播放
    fun startPlay_1(){
        //如果当前歌曲没有在播放，则播放音乐
        if(!mediaPlayer.isPlaying){
            Log.d("MusicService", "startPlay")
            mediaPlayer.start()
            running=true
            //发送音乐在播放的广播
            val intent=Intent(MediaBroadCastReceiver.ACTION_MEDIA_PLAYING)
            sendBroadcast(intent)
            currentTime = mediaPlayer.currentPosition
        }else{
            mediaPlayer.pause()
            running=false
            //发送音乐暂停播放
            val intent=Intent(MediaBroadCastReceiver.ACTION_MEDIA_PAUSED)
            sendBroadcast(intent)
        }
    }
    //获取歌曲播放进度
    fun getProgress_1():Int{
        currentTime = mediaPlayer.currentPosition
        return currentTime
    }
    //歌曲的快进
    fun seekTo_1(seek:Int){
        mediaPlayer.seekTo(seek)
        currentTime=seek
    }
    //切换歌曲
    fun changeMusic_1(musicTitle:String){
        if(mediaPlayer.isPlaying){
            running=false
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)
        }
        mediaPlayer.reset()
        val assetManager= applicationContext?.assets
        val fd= musicTitle.let { assetManager?.openFd(it) }
        if (fd != null) {
            mediaPlayer.setDataSource(fd.fileDescriptor,fd.startOffset,fd.length)
        }
        currentTime=0
        mediaPlayer.prepare()
        mediaPlayer.setOnPreparedListener {
            duration=mediaPlayer.duration
            //准备好就播放
            startPlay_1()
        }
        mediaPlayer.setOnCompletionListener {
            //一个广播，通知应用一首歌已经播放结束了
            val intent1=Intent(MediaBroadCastReceiver.ACTION_MEDIA_FINISHED)
            sendBroadcast(intent1)
            running=false
        }
        Log.d("MusicService", "changeMusic")
    }

    //切换在线歌曲
    fun changeMusic_1(musicUrl:String,type:Int){
        if(mediaPlayer.isPlaying){
            running=false
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)
        }
        mediaPlayer.reset()
        mediaPlayer.setDataSource(musicUrl)
//            mediaPlayer.setDataSource(applicationContext, Uri.parse(musicUrl))
        currentTime=0
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            duration=mediaPlayer.duration
            startPlay_1()
        }
        mediaPlayer.setOnCompletionListener {
            //一个广播，通知应用一首歌已经播放结束了
            val intent1=Intent(MediaBroadCastReceiver.ACTION_MEDIA_FINISHED)
            sendBroadcast(intent1)
            running=false
        }
        Log.d("MusicService", "changeOnlieMusic")
    }
    //获取歌曲的播放时长
    fun getDuration_1():Int{
        return duration
    }
    //获取当前歌曲的播放状态
    fun getPlayingStatus_1():Boolean{
        return running
    }
    fun setPlayingStatus_1(status:Boolean){
        running=status
    }

    //接收歌曲播放广播
    private fun doReceiveMediaAction(intent: Intent) {
        val action: String? =intent.action
        if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_FINISHED)){
            OnlinePlaying.playNextMusic()
        }else if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_PLAYING)){
            //更新通知栏的图标
            remoteView.setTextViewText(R.id.notificationTextView,OnlinePlaying.currentMusic)
            remoteView.setImageViewResource(R.id.playBtn,android.R.drawable.ic_media_pause)
            if(OnlinePlaying.playingType == 0)
                remoteView.setImageViewResource(R.id.imageTitle,OnlinePlaying.music.filmImageId)
            else {
                val t1 = Thread{
                    val opt = BitmapFactory.Options()
                    opt.inPreferredConfig = Bitmap.Config.RGB_565
                    opt.inPurgeable = true
                    opt.inInputShareable = true
                    opt.inSampleSize = 8
                    val picUrl:URL = URL(OnlinePlaying.onlineMusic?.musicUrl)
                    val pngBM = BitmapFactory.decodeStream(picUrl.openStream(),null, opt)
                    remoteView.setImageViewBitmap(R.id.imageTitle,pngBM)
                }
                t1.start()
                t1.join()
            }
            manager.notify(1,notification)
        }else if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_PAUSED)){
            //设置通知栏的图标为开始播放状态，以及设置歌曲名等
            remoteView.setTextViewText(R.id.notificationTextView,OnlinePlaying.currentMusic)
            remoteView.setImageViewResource(R.id.playBtn,android.R.drawable.ic_media_play)
            //设置图片
            if(OnlinePlaying.playingType == 0)
                remoteView.setImageViewResource(R.id.imageTitle,OnlinePlaying.music.filmImageId)
            else {
                val t1 = Thread{
                    val opt = BitmapFactory.Options()
                    opt.inPreferredConfig = Bitmap.Config.RGB_565
                    opt.inPurgeable = true
                    opt.inInputShareable = true
                    opt.inSampleSize = 8
                    val picUrl:URL = URL(OnlinePlaying.onlineMusic?.musicUrl)
                    val pngBM = BitmapFactory.decodeStream(picUrl.openStream(),null, opt)
                    remoteView.setImageViewBitmap(R.id.imageTitle,pngBM)
                }
                t1.start()
                t1.join()
            }

            manager.notify(1,notification)
        }
    }
}
