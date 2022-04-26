package com.example.onlineMusic_2.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.activity.LoginActivity
import com.example.onlineMusic_2.activity.RegisterActivity
import com.example.onlineMusic_2.db.MusicInfoDB
import com.example.onlineMusic_2.receiver.FragmentBroadCastReceiver
import com.example.onlineMusic_2.receiver.MediaBroadCastReceiver
import com.example.onlineMusic_2.receiver.UserInfoBroadCastReceiver
import com.example.onlineMusic_2.utils.ImageUtil
import com.example.onlineMusic_2.utils.OnlinePlaying
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_main.*


/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : BaseFragment() {
    //监听打开侧滑抽屉并选择item
    var userInfoBroadCastReceiver: UserInfoBroadCastReceiver? = null
    val userInfoReceiverListener = object: UserInfoBroadCastReceiver.UserInfoReceiverListener{
        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                doReceiveUserInfoAction(intent)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadData()
        //初始化视图
        initViews()

    }
    private fun doReceiveUserInfoAction(intent: Intent){
        val action: String? =intent.action
        val loginedUser = activity?.getSharedPreferences("userData", Context.MODE_PRIVATE)
        val loginedUserName = loginedUser?.getString("u_userName", "您还未登录哦！！！")
        val loginedNickName = loginedUser?.getString("u_realName", "未登录")
        if(action.equals(UserInfoBroadCastReceiver.ACTION_USER_IMAGE_LOGINED) || action.equals(UserInfoBroadCastReceiver.ACTION_USER_IMAGE_EXITED)){
            if(loginedUserName == null || loginedUserName.equals("您还未登录哦！！！")){
                toLogin.visibility = View.VISIBLE
            }else{
                toLogin.visibility = View.INVISIBLE
            }
            showUserName.text = loginedUserName
            showNickName.text = loginedNickName
            setUserImageView()
        }else if(action.equals(UserInfoBroadCastReceiver.ACTION_USER_IMAGE_CHANGED_CLICKED)){
            setUserImageView()
        }
    }

    /**
     * 加载数据---喜欢的歌曲以及本地歌曲的数量
     */
    fun loadData(){
        //获取已经登录的用户
        val loginedUser = context?.getSharedPreferences("userData", Context.MODE_PRIVATE)
        val userId = loginedUser?.getInt("u_id", -1)

        if (userId != null && userId != -1) {
            val count = mActivity?.let { MusicInfoDB.getMusicInfoDB(it)?.getLikeMusicCountByUserId(userId) }
            like_music_count.text = count.toString()
            toLogin.visibility = View.INVISIBLE
        }else{
            toLogin.visibility = View.VISIBLE
        }

    }
    //初始化控件和点击事件
    fun initViews(){
        //点击登录，跳转到LoginActivity
        toLogin.setOnClickListener {
            context?.let { it1 -> LoginActivity.actionStart(it1) }
        }

        my_like.setOnClickListener {
            replaceFragment(LikelistFragment())
        }
    }
    override fun onResume() {
        super.onResume()
        //获取已经登录的用户
        val loginedUser = activity?.getSharedPreferences("userData", Context.MODE_PRIVATE)
        val loginedUserName = loginedUser?.getString("u_userName", "未登录")
        val loginedNickName = loginedUser?.getString("u_realName", "未登录")
        showUserName.text = loginedUserName
        showNickName.text = loginedNickName
        setUserImageView()
        //注册广播
        initReceivers()
    }
    //设置侧滑栏的用户头像
    fun setUserImageView(){
        val loginedUser = activity?.getSharedPreferences("userData", Context.MODE_PRIVATE)
        val loginedUserName = loginedUser?.getString("u_userName", "您还未登录哦！！！")
        if (loginedUserName != null && !loginedUserName.equals("点击登录")) {
            Log.d("MainActivity", loginedUserName)
            val imagePath = loginedUser?.getString("u_imagePath", "未自定义图片")

            if (imagePath != null) {
                Log.d("MainActivity", imagePath)
            }
            if(imagePath.equals("未自定义图片") || imagePath == ""){
                userImageView.setImageResource(R.drawable.headphone)
            }else{
                //图片显示
                val bitmap = mActivity?.let { ImageUtil.getBitmapFromUri(it, Uri.parse(imagePath)) }
                //侧滑抽屉中图像
                userImageView.setImageBitmap(bitmap)
            }
        }else{
            userImageView.setImageResource(R.drawable.headphone)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.let { userInfoBroadCastReceiver?.unRegisterReceiver(it) }
    }
    //碎片的替换
    fun replaceFragment(fragment:Fragment){
        //初始往frameLayout中添加一个新的fragment
        val manager: FragmentManager? = activity?.supportFragmentManager
        val fragmentTransaction = manager?.beginTransaction()

        fragmentTransaction?.replace(R.id.replace_layout, fragment)
        fragmentTransaction?.addToBackStack("ToplistFragment")
        fragmentTransaction?.commit()
    }
    //初始化service和广播等事件
    private fun initReceivers(){
        //注册和监听音乐播放广播
        userInfoBroadCastReceiver = activity?.let { UserInfoBroadCastReceiver(it) }
        userInfoBroadCastReceiver?.addUserInfoReceiverListener(userInfoReceiverListener)
        activity?.let { userInfoBroadCastReceiver?.registerReceiver(it) }
    }
}