package com.example.onlineMusic_2.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.bean.Music
import com.example.onlineMusic_2.db.UserDB
import com.example.onlineMusic_2.fragment.*
import com.example.onlineMusic_2.receiver.MediaBroadCastReceiver
import com.example.onlineMusic_2.receiver.UserInfoBroadCastReceiver
import com.example.onlineMusic_2.service.MusicPlayService
import com.example.onlineMusic_2.utils.ActivityCollector
import com.example.onlineMusic_2.utils.ImageUtil
import com.example.onlineMusic_2.utils.OnlinePlaying
import com.example.onlineMusic_2.utils.OnlinePlaying.playBinder
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.properties.Delegates


class MainActivity : BaseActivity() {
    lateinit var nav_header:View
    lateinit var headImageView:ImageView
    lateinit var nickName:TextView
    //获取已经登录的用户
    lateinit var loginedUser:SharedPreferences
    lateinit var loginedUserName:String
    var loginedUserId by Delegates.notNull<Int>()
    companion object{
        //供别的activity传递参数到本activity提供参考
        fun actionStart(context: Context){
            val intent= Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
        val PERMISSON_REQUESTCODE = 1
    }
    var mediaBroadCastReceiver: MediaBroadCastReceiver? = null
    val mediaReceiverListener = object: MediaBroadCastReceiver.MediaReceiverListener{
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                doReceiveMediaAction(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "onCreate")
        //初始化导航栏
        initView()

        //初始化播放列表
        initMusics()


        //开启Service
        val intent=Intent(this,MusicPlayService::class.java)
        startService(intent)
        //绑定Service
        bindMusicService()

        //初始往frameLayout中添加一个新的fragment
        val manager: FragmentManager = getSupportFragmentManager()
        val fragmentTransaction = manager.beginTransaction()
        fragmentTransaction.add(
            R.id.replace_layout,
            MainFragment()
        )
        fragmentTransaction.commit()

    }


    //创建菜单选项
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toobar,menu)
        return true
    }
    //菜单项的选中
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->drawerLayout.openDrawer(GravityCompat.START)
            R.id.searchs ->{
                val intent:Intent = Intent(this,SearchActivity::class.java)
                startActivity(intent)
            }
            R.id.share->{}
            R.id.settings ->{SettingsActivity.actionStart(this)}
            R.id.exit -> {
                ActivityCollector.finishAll()
            }
        }
        return true
    }

    //绑定服务
    private fun bindMusicService(){
        val intent= Intent(this, MusicPlayService::class.java)
        intent.putExtra("musicTitle","${OnlinePlaying.currentMusic}.mp3")
        //使用这种方法绑定service,会自动创建service，但是不会执行onStartCommand
        bindService(intent,OnlinePlaying.connection, Context.BIND_AUTO_CREATE)
        OnlinePlaying.connected=true
    }


    //初始化音乐列表
    private fun initMusics(){
        //初始化OnlinePlaying中的音乐列表
        OnlinePlaying.playingType = 0
        OnlinePlaying.musiclist.clear()

        val filmNames: Array<out String> =resources.getStringArray(R.array.musicName)
        //使用以下的方法获取mipmap的资源，而不是这种：resources.getStringArray(R.array.filmImage)
        val filmImages: TypedArray =resources.obtainTypedArray(R.array.musicImage)
        //将信息添加到list
        for((index, filmName) in filmNames.withIndex()){
            OnlinePlaying.musiclist.add(
                Music(
                    filmNames[index],
                    filmImages.getResourceId(
                        index,
                        R.mipmap.pic1
                    ),
                    ""
                )
            )
        }
        OnlinePlaying.music_position=0
        OnlinePlaying.currentMusic="夏-太一"
        OnlinePlaying.music=OnlinePlaying.getMusic(0)
    }
    override fun onDestroy() {
        super.onDestroy()
        val intent=Intent(this,MusicPlayService::class.java)
        playBinder.setPlayingStatus(false)
        //关闭绑定，同时关闭服务
        unbindService(OnlinePlaying.connection)
        stopService(intent)
        Log.d("MainActivity","onDestroy")

    }

    override fun onResume() {
        super.onResume()
        //注册监听事件
        initReceivers()
        //主活动重新出现在栈顶时设置当前播放音乐的图标
        setImageResource(navigation_center_image)
        setHeadImageView()

        //设置头像图标
        setHeadImageView()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity","onPause")
        //取消注册广播
        mediaBroadCastReceiver?.unRegisterReceiver(this)
    }

    //碎片的替换
    fun replaceFragment(fragment:Fragment){
        //初始往frameLayout中添加一个新的fragment
        val manager: FragmentManager = getSupportFragmentManager()
        val fragmentTransaction = manager.beginTransaction()
        fragmentTransaction.replace(R.id.replace_layout, fragment)
        fragmentTransaction.commit()
    }

    //初始化
    fun initView(){
        //获取已经登录的用户
        loginedUser = getSharedPreferences("userData", Context.MODE_PRIVATE)
        loginedUserName = loginedUser?.getString("u_userName", "点击登录").toString()
        loginedUserId = loginedUser?.getInt("u_id", -1)

        //让toolbar的外观和功能与actionbar都相似
        setSupportActionBar(toolbar)

        //添加一个导航按钮（实现侧滑抽屉）
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.homemenu)
        }
        //设置侧滑抽屉里面菜单项的点击事件
        navView.setCheckedItem(R.id.navMember)//设置默认选中
        navView.itemIconTintList=null

        //侧滑抽屉的item点击事件
        navView.setNavigationItemSelectedListener {

            drawerLayout.closeDrawers()//关闭侧滑抽屉
            true
        }
        //侧滑抽屉中图像的点击事件
        nav_header = navView.inflateHeaderView(R.layout.nav_header)
        headImageView = nav_header.findViewById<ImageView>(R.id.iconImage)
        nickName = nav_header.findViewById(R.id.nickName)
        //设置头像图标
        setHeadImageView()

        //点击头像修改图片
        headImageView.setOnClickListener {
            //打开文件选择器
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            //指定只显示图片
            intent.type = "image/*"
            startActivityForResult(intent, 2)
        }
        //初始化底部导航栏
        bottomView.itemIconTintList=null//导航栏图标显示
        bottomView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.tab_main->{
                    replaceFragment(MainFragment())
                }
                R.id.tab_list->{
                    replaceFragment(ListFragment())
                }
                R.id.tab_music->{
                    replaceFragment(DetailFragment())
                }
                R.id.tab_toplist->{
                    replaceFragment(ToplistFragment())
                }
                R.id.tab_about->{
                    replaceFragment(AboutFragment())
                }
            }
            true
        }

        if(loginedUserName.equals("点击登录")){
            drawer_login.text = "点击登录"
        }else{
            drawer_login.text = "退出登录"
        }

        // 设置登录/退出登录
        drawer_login.setOnClickListener {
            if(drawer_login.text.toString() == "点击登录"){
                drawerLayout.closeDrawers()//关闭侧滑抽屉
                LoginActivity.actionStart(this)
            }else{
                //清空已登录记录
                val edit = loginedUser.edit()
                edit.clear()
                edit.apply()
                //数据库中设置已登录的用户为未登录
                if (loginedUserName != null && !loginedUserName.equals("点击登录")) {
                    UserDB.getUserDB(this)?.updateUserStatus(loginedUserName, 0)
                }
                drawer_login.text = "点击登录"
                //已登录广播
                val intent=Intent(UserInfoBroadCastReceiver.ACTION_USER_IMAGE_LOGINED)
                sendBroadcast(intent)
                //退出登录或者登录时都需要设置侧滑抽屉中的地址
                setHeadImageView()
                drawerLayout.closeDrawers()//关闭侧滑抽屉
            }
        }
        //点击设置
        drawer_settings.setOnClickListener {
            SettingsActivity.actionStart(this)
        }
    }
    fun setHeadImageView(){
        //设置头像地址
        if (loginedUserName != null && !loginedUserName.equals("点击登录")) {
            Log.d("MainActivity", loginedUserName)
            val imagePath = loginedUser?.getString("u_imagePath", "未自定义图片")

            if (imagePath != null) {
                Log.d("MainActivity", imagePath)
            }
            if(imagePath.equals("未自定义图片") || imagePath == ""){
                headImageView.setImageResource(R.mipmap.headpic1)
            }else{
                //图片显示
                val bitmap = ImageUtil.getBitmapFromUri(this, Uri.parse(imagePath))
                //侧滑抽屉中图像
                headImageView.setImageBitmap(bitmap)
            }
            val nickNames = loginedUser.getString("u_realName","您还未登录")
            nickName.text = nickNames

        }else{
            headImageView.setImageResource(R.mipmap.headpic1)
            nickName.text = "您还未登录"
        }
    }
    /**
     * 返回结果----选择图片
     */
    @SuppressLint("CommitPrefEdits")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            2 -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    data.data?.let{uri ->
//                        val imagePath = ImageUtil.getRealPathFromUri(this, uri)
                        val imagePath = uri.toString()
                        if (loginedUserName != null && !loginedUserName.equals("点击登录")) {
                            UserDB.getUserDB(this)?.updateImagePath(loginedUserId, imagePath)
                            val storeUser = getSharedPreferences("userData", Context.MODE_PRIVATE).edit()
                            storeUser.putString("u_imagePath", imagePath)
                            storeUser.apply()
                        }
                        Log.d("MainActivity", uri.toString())
                        //图片显示
                        val bitmap = ImageUtil.getBitmapFromUri(this, Uri.parse(imagePath))
                        //侧滑抽屉中图像
                        headImageView.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }

    /**
     * 从uri获取图像
     */
    fun getBitmapFromUri(uri:Uri) = contentResolver.openFileDescriptor(uri, "r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }
    //初始化service和广播等事件
    private fun initReceivers(){
        mediaBroadCastReceiver = MediaBroadCastReceiver(this)
        mediaBroadCastReceiver?.addNotificationReceiverListener(mediaReceiverListener)
        mediaBroadCastReceiver?.registerReceiver(this)
    }

    //接收歌曲播放广播
    private fun doReceiveMediaAction(intent: Intent) {
        val action: String? =intent.action
        if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_FINISHED)){
//            OnlinePlaying.playNextMusic()
        }else if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_PLAYING)){
            setImageResource(navigation_center_image)
        }else if(action.equals(MediaBroadCastReceiver.ACTION_MEDIA_PAUSED)){
            setImageResource(navigation_center_image)
        }
    }

    //设置图片资源
    fun setImageResource(imageView:ImageView){
        if(OnlinePlaying.playingType == 0)
            imageView.setImageResource(OnlinePlaying.music.filmImageId)
        else if(OnlinePlaying.playingType == 1){
            Glide.with(this).load(OnlinePlaying.onlineMusic?.musicUrl).into(imageView)
        }
    }

}
