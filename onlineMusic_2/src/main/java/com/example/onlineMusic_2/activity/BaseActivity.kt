package com.example.onlineMusic_2.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.onlineMusic_2.R
import com.example.onlineMusic_2.receiver.NotificationBroadCastReceiver
import com.example.onlineMusic_2.utils.ActivityCollector
import com.example.onlineMusic_2.utils.OnlinePlaying

open class BaseActivity : AppCompatActivity() {

    var notificationBroadcastReceiver: NotificationBroadCastReceiver? = null
    val notificationReceiverListener = object: NotificationBroadCastReceiver.NotificationReceiverListener{
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                doReceiveNotificationAction(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        ActivityCollector.addActivity(this)
    }


    override fun onResume() {
        super.onResume()
        //注册监听事件和初始化服务
        initReceivers()
        Log.d("BaseActivity","onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("BaseActivity","onPause")
        //取消注册广播
        notificationBroadcastReceiver?.unRegisterReceiver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BaseActivity","onDestroy")
        ActivityCollector.removeActivity(this)
    }

    //根据通知栏的通知来更新控件
    private fun doReceiveNotificationAction(intent:Intent) {
        val action: String? =intent.action
        if(action.equals(NotificationBroadCastReceiver.ACTION_NOTIFICATION_PLAYBTN)){
            Log.i("TAG", "点击通知栏的播放按钮！！！ ")
            OnlinePlaying.startPlayMusic()
        }else if(action.equals(NotificationBroadCastReceiver.ACTION_NOTIFICATION_PREBTN)){
            Log.i("TAG", "点击通知栏的上一首按钮！！！ ")
            OnlinePlaying.playPreMusic()
        }else if (action.equals(NotificationBroadCastReceiver.ACTION_NOTIFICATION_NEXTBTN)){
            Log.i("TAG", "点击通知栏的下一首按钮！！！ ")
            OnlinePlaying.playNextMusic()
        }
    }

    //初始化service和广播等事件
    private fun initReceivers(){
        notificationBroadcastReceiver = NotificationBroadCastReceiver(this)
        notificationBroadcastReceiver?.addNotificationReceiverListener(notificationReceiverListener)
        notificationBroadcastReceiver?.registerReceiver(this)
    }

    override fun getResources(): Resources {
        val resources = super.getResources()
        val settingData = getSharedPreferences("settingData", Context.MODE_PRIVATE)
        val configuration: Configuration = resources.configuration
        if(settingData != null){
            val textSize = settingData.getString("textSize", "中号字体")
            if(textSize.equals("小号字体")){
                configuration.fontScale = 0.7F
            }else if(textSize.equals("小号字体")){
                configuration.fontScale = 1.0F
            }else{
                configuration.fontScale = 1.2F
            }
        }else{
            configuration.fontScale = 1.0f
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return resources
    }
}